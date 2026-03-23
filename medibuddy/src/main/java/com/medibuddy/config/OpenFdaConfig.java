package com.medibuddy.config;

public class OpenFdaConfig {
    public static final String BASE_URL = "https://api.fda.gov/drug/label.json";
    public static final int DEFAULT_LIMIT = 3;

    private OpenFdaConfig() {
        // Prevent instantiation
    }
}
