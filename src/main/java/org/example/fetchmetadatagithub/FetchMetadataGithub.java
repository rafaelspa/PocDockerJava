package org.example.fetchmetadatagithub;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FetchMetadataGithub {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        List<String> uri_repositories = List.of(
                "https://api.github.com/repos/rafaelspa/PocDockerJava",
                "https://api.github.com/repos/ifspcodelab/event-platform-frontend",
                "https://api.github.com/repos/kubernetes-client/python",
                "https://api.github.com/repos/BillRun/system",
                "https://api.github.com/repos/PostgREST/postgrest",
                "https://api.github.com/repos/leehach/census-postgres"
        );
        HttpRequest request;
        HttpResponse<String> response;
        String responseBody;
        String size;
        String lang;
        Pattern sizePattern = Pattern.compile("\"size\":\\d+");
        Pattern langPattern = Pattern.compile("\"language\":\"(.*?)\"");
        Matcher sizeMatcher;
        Matcher langMatcher;

        request = HttpRequest.newBuilder()
                .uri(new URI("https://github.com/git"))
                .build();

        response = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            for (String uri : uri_repositories) {
                request = HttpRequest.newBuilder()
                        .uri(new URI(uri))
                        .build();

                response = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.ALWAYS)
                        .build()
                        .send(request, HttpResponse.BodyHandlers.ofString());

                responseBody = response.body();

                sizeMatcher = sizePattern.matcher(responseBody);

                langMatcher = langPattern.matcher(responseBody);

                System.out.println("Repository: " + uri.substring(28));
                while (sizeMatcher.find()) {
                    size = responseBody.substring(sizeMatcher.start(),sizeMatcher.end());
                    System.out.println("Size: " + size.substring(7));
                }

                while (langMatcher.find()) {
                    lang = responseBody.substring(langMatcher.start(),langMatcher.end());
                    System.out.println("Language: " + lang.substring(12, lang.length() - 1));
                }
                System.out.println();
            }
        } else {
            System.out.println("The api is down");
        }
    }
}
