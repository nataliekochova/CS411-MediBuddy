package com.medibuddy;

import com.medibuddy.client.OpenFdaClient;
import com.medibuddy.model.DrugLabelResult;
import com.medibuddy.model.OpenFdaResponse;

import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) {
        OpenFdaClient client = new OpenFdaClient();

        try {
            OpenFdaResponse response = client.searchDrugLabel("acetaminophen");

            if (response.getResults() == null || response.getResults().isEmpty()) {
                System.out.println("No results found.");
                return;
            }

            DrugLabelResult first = response.getResults().get(0);

            System.out.println("First result:");
            printOpenFdaField(first.getOpenfda(), "brand_name");
            printOpenFdaField(first.getOpenfda(), "generic_name");
            printOpenFdaField(first.getOpenfda(), "manufacturer_name");

            printSection("Purpose", first.getPurpose());
            printSection("Indications and Usage", first.getIndications_and_usage());
            printSection("Warnings", first.getWarnings());
            printSection("Dosage and Administration", first.getDosage_and_administration());

        } catch (Exception e) {
            System.err.println("Error:");
            e.printStackTrace();
        }
    }

    private static void printOpenFdaField(Map<String, List<String>> openfda, String fieldName) {
        if (openfda != null && openfda.containsKey(fieldName) && !openfda.get(fieldName).isEmpty()) {
            System.out.println(fieldName + ": " + openfda.get(fieldName).get(0));
        } else {
            System.out.println(fieldName + ": not available");
        }
    }

    private static void printSection(String label, List<String> values) {
        System.out.println("\n" + label + ":");
        if (values == null || values.isEmpty()) {
            System.out.println("not available");
        } else {
            System.out.println(values.get(0));
        }
    }
}
