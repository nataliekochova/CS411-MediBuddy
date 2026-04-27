package com.medibuddy.service;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream input = AppConfig.class.getResourceAsStream("/config.properties")) {
            if (input == null) {
                throw new RuntimeException("Missing config.properties");
            }
            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    public static String getResendApiKey() {
        return props.getProperty("RESEND_API_KEY");
    }
}