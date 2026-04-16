package com.medibuddy.client;

//import com.medibuddy.config.OpenFdaConfig;
import com.medibuddy.model.OpenFdaResponse;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class OpenFdaClient {
    private static final String BASE_URL = "https://api.fda.gov/drug/label.json";

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenFdaClient() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public OpenFdaResponse searchDrugLabel(String medicationName) throws IOException, InterruptedException {
        String encodedName = URLEncoder.encode(medicationName, StandardCharsets.UTF_8);

        String url = BASE_URL
                + "?search=openfda.generic_name:"
                + encodedName
                + "&limit=8";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("openFDA request failed with status " + response.statusCode()
                    + "\nResponse body:\n" + response.body());
        }

        return objectMapper.readValue(response.body(), OpenFdaResponse.class);
    }
}