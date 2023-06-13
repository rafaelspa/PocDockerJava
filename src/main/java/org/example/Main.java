package org.example;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient.Request;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

// https://github.com/docker-java/docker-java

public class Main {
    public static void main(String[] args) {

       // Instantiating a DockerClientConfig

        DockerClientConfig standard = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

        // Use with configurations in 'docker-java.properties' file like this:

//        DockerClientConfig custom = DefaultDockerClientConfig.createDefaultConfigBuilder()
//                .withDockerHost("tcp://docker.somewhere.tld:2376")
//                .withDockerTlsVerify(true)
//                .withDockerCertPath("/home/user/.docker")
//                .withRegistryUsername(registryUser)
//                .withRegistryPassword(registryPass)
//                .withRegistryEmail(registryMail)
//                .withRegistryUrl(registryUrl)
//                .build();

        // Instantiating a DockerHttpClient

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(standard.getDockerHost())
                .sslConfig(standard.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        // Make requests using one of the docker-java-transport-* libraries
        Request request = Request.builder()
                .method(Request.Method.GET)
                .path("/_ping")
                .build();

        //Response response = httpClient.execute(request);

//        try (Response response = httpClient.execute(request)) {
////            assertThat(response.getStatusCode(), equalTo(200));
////            assertThat(IOUtils.toString(response.getBody()), equalTo("OK"));
//            if (response.getStatusCode() == 200) {
//                System.out.println("ok");
//            } else {
//                System.out.println("not ok");
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }


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

        CreateContainerResponse containerResponse = dockerClient.createContainerCmd("docker/whalesay")
                .withCmd("cowsay", "hello there")
                .withName("whalesay-docker-java").exec();

        dockerClient.startContainerCmd(containerResponse.getId()).exec();
    }
}