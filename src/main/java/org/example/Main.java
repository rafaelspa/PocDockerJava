package org.example;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.CreateVolumeResponse;
import com.github.dockerjava.api.command.InspectVolumeResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

// https://github.com/docker-java/docker-java

public class Main {
    public static void main(String[] args) {

       // Instantiating a DockerClientConfig
        DockerClientConfig standard = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        // Instantiating a DockerHttpClient
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(standard.getDockerHost())
                .sslConfig(standard.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        // Instantiating a DockerClient
        DockerClient dockerClient = DockerClientImpl.getInstance(standard, httpClient);

        // execute Docker commands
        dockerClient.pingCmd().exec();


        System.out.println("Image list");
        List<Image> images = dockerClient.listImagesCmd().exec();
        for (Image image : images) {
            if (image.getRepoTags().length > 0) {
                System.out.println(image.getId() + " - " + image.getRepoTags()[0]);
            }
        }

        System.out.println("\nContainer list");
        List<Container> containers = dockerClient.listContainersCmd().withShowAll(true).exec();
        for (Container container : containers) {
            System.out.println("ID: " + container.getId());
            System.out.println("Name: " + Arrays.toString(container.getNames()));
            System.out.println("Image: " + container.getImage());
            System.out.println("Command: " + container.getCommand());
            //System.out.println("Port: " + Arrays.toString(container.getPorts()));
            System.out.println("Status: " + container.getStatus());
            System.out.println();
        }

        System.out.println("\nVolume list");
        List<InspectVolumeResponse> volumes = dockerClient.listVolumesCmd().withFilter("name", List.of("vol-whalesay")).exec().getVolumes();
        if (Objects.nonNull(volumes) && volumes.size() > 0) {
            for (var volume : volumes) {
                System.out.println("Name: " + volume.getName());
                System.out.println("Mountpoint: " + volume.getMountpoint());
                System.out.println("Driver: " + volume.getDriver());
                System.out.println("Mountpoint: " + volume.getMountpoint());
                System.out.println("Labels: " + volume.getLabels());
                System.out.println("Options: " + volume.getOptions());
            }
        } else {
            CreateVolumeResponse volumeResponse = dockerClient.createVolumeCmd().withName("vol-whalesay").exec();
            System.out.println("Name: " + volumeResponse.getName());
            System.out.println("Mountpoint: " + volumeResponse.getMountpoint());
            System.out.println("Driver: " + volumeResponse.getDriver());
            System.out.println("Mountpoint: " + volumeResponse.getMountpoint());
            System.out.println("Labels: " + volumeResponse.getLabels());
        }

        try {
            boolean whalesayExists = images.stream().anyMatch(image -> Arrays.asList(image.getRepoTags()).contains("docker/whalesay:latest"));
            if (!whalesayExists) {
                System.out.println("whalesay img does not exist");
                dockerClient.pullImageCmd("docker/whalesay")
                        .exec(new PullImageResultCallback())
                        .awaitCompletion(30, TimeUnit.SECONDS);
            }
            boolean containerExists = containers.stream()
                .anyMatch(container -> Arrays.asList(container.getNames()).contains("/whalesay-docker-java"));
            if (!containerExists) {
                //container creation
                CreateContainerResponse containerResponse = dockerClient.createContainerCmd("docker/whalesay")
                        .withCmd("cowsay", "hello there")
                        .withName("whalesay-docker-java").exec();

                dockerClient.startContainerCmd(containerResponse.getId()).exec();

                PipedOutputStream pipedOutputStream = new PipedOutputStream();
                InputStream inputStream = new PipedInputStream(pipedOutputStream);

                var response = dockerClient.attachContainerCmd(containerResponse.getId())
                        .withLogs(true)
                        .withStdErr(true)
                        .withStdOut(true)
                        .withFollowStream(true)
                        .exec(new ResultCallback.Adapter<>() {
                            public void onNext(Frame object) {
                                System.out.println(object);
                                try {
                                    pipedOutputStream.write(object.getPayload());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });

                try {
                    response.awaitCompletion();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}