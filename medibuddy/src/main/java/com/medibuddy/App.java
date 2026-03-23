package com.medibuddy;

import com.medibuddy.client.OpenFdaClient;

public class App {
    public static void main(String[] args) {
        OpenFdaClient client = new OpenFdaClient();

        String medicationName = "ibuprofen";

        try {
            String json = client.searchDrugLabelRaw(medicationName);
            System.out.println("Raw JSON response:");
            System.out.println(json);
        } catch (Exception e) {
            System.err.println("Error calling openFDA:");
            e.printStackTrace();
        }
    }
}
