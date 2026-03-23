package com.medibuddy.client;

import com.medibuddy.config.OpenFdaConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class OpenFdaClient {
    private final HttpClient httpClient;

    public OpenFdaClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    public String searchDrugLabelRaw(String medicationName) throws IOException, InterruptedException {
        String encodedName = URLEncoder.encode(medicationName, StandardCharsets.UTF_8);

        String url = OpenFdaConfig.BASE_URL
                + "?search=openfda.generic_name:"
                + encodedName
                + "&limit="
                + OpenFdaConfig.DEFAULT_LIMIT;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        int statusCode = response.statusCode();
        if (statusCode != 200) {
            throw new IOException("openFDA request failed with status " + statusCode
                    + "\nResponse body:\n" + response.body());
        }

        return response.body();
    }
}