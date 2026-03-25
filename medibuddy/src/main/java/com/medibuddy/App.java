package com.medibuddy;

import com.medibuddy.client.OpenFdaClient;
import com.medibuddy.model.DrugLabelResult;
import com.medibuddy.model.OpenFdaResponse;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class App extends Application {

    private final OpenFdaClient client = new OpenFdaClient();

    @Override
    public void start(Stage stage) {
        Label title = new Label("MediBuddy");
        title.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: #1f2937;
                """);

        Label subtitle = new Label("Search for medication label information");
        subtitle.setStyle("""
                -fx-font-size: 13px;
                -fx-text-fill: #6b7280;
                """);

        TextField input = new TextField();
        input.setPromptText("Enter a medication name");
        input.setMaxWidth(Double.MAX_VALUE);
        input.setStyle("""
                -fx-font-size: 14px;
                -fx-background-radius: 10px;
                -fx-border-radius: 10px;
                -fx-padding: 10px;
                -fx-border-color: #d1d5db;
                -fx-background-color: white;
                """);

        Button searchButton = new Button("Search");
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.setStyle("""
                -fx-background-color: #2563eb;
                -fx-text-fill: white;
                -fx-font-size: 14px;
                -fx-font-weight: bold;
                -fx-background-radius: 10px;
                -fx-padding: 10px 14px 10px 14px;
                """);

        Label resultsTitle = new Label("Results");
        resultsTitle.setStyle("""
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-text-fill: #1f2937;
                """);

        Label results = new Label("Search for a medication to see details.");
        results.setWrapText(true);
        results.setMaxWidth(Double.MAX_VALUE);
        results.setStyle("""
                -fx-font-size: 13px;
                -fx-text-fill: #111827;
                """);

        ScrollPane resultsScroll = new ScrollPane(results);
        resultsScroll.setFitToWidth(true);
        resultsScroll.setStyle("""
                -fx-background: white;
                -fx-background-color: white;
                -fx-border-color: #d1d5db;
                -fx-border-radius: 10px;
                -fx-background-radius: 10px;
                """);
        resultsScroll.setPrefHeight(360);
        results.setStyle("""
                -fx-font-size: 13px;
                -fx-background-radius: 10px;
                -fx-border-radius: 10px;
                -fx-border-color: #d1d5db;
                -fx-control-inner-background: white;
                """);

        VBox resultsCard = new VBox(10, resultsTitle, resultsScroll);
        resultsCard.setPadding(new Insets(14));
        resultsCard.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 16px;
                -fx-border-radius: 16px;
                -fx-border-color: #e5e7eb;
                """);
        VBox.setVgrow(results, Priority.ALWAYS);

        VBox content = new VBox(12, title, subtitle, input, searchButton, resultsCard);
        content.setPadding(new Insets(18));
        content.setStyle("-fx-background-color: #f3f4f6;");

        input.setOnAction(e -> searchButton.fire());

        searchButton.setOnAction(e -> {
            String medName = input.getText().trim();

            if (medName.isEmpty()) {
                results.setText("Please enter a medication name.");
                return;
            }

            results.setText("Searching...");

            new Thread(() -> {
                try {
                    OpenFdaResponse response = client.searchDrugLabel(medName);

                    String text;
                    if (response.getResults() == null || response.getResults().isEmpty()) {
                        text = "No results found.";
                    } else {
                        DrugLabelResult first = response.getResults().get(0);
                        text = buildResultText(first);
                    }

                    Platform.runLater(() -> results.setText(text));
                } catch (Exception ex) {
                    Platform.runLater(() -> results.setText("Error:\n" + ex.getMessage()));
                }
            }).start();
        });

        Scene scene = new Scene(content, 380, 620);

        stage.setTitle("MediBuddy");
        stage.setMinWidth(360);
        stage.setMinHeight(520);
        stage.setScene(scene);
        stage.show();
    }

    private String buildResultText(DrugLabelResult first) {
        StringBuilder sb = new StringBuilder();

        sb.append("Medication Information\n\n");

        appendOpenFdaField(sb, first.getOpenfda(), "brand_name", "Brand");
        appendOpenFdaField(sb, first.getOpenfda(), "generic_name", "Generic");
        appendOpenFdaField(sb, first.getOpenfda(), "manufacturer_name", "Manufacturer");

        appendSection(sb, "Purpose", first.getPurpose());
        appendSection(sb, "Indications", first.getIndications_and_usage());
        appendSection(sb, "Warnings", first.getWarnings());
        appendSection(sb, "Dosage", first.getDosage_and_administration());

        return sb.toString();
    }

    private void appendOpenFdaField(StringBuilder sb, Map<String, List<String>> openfda,
                                    String key, String label) {
        if (openfda != null && openfda.containsKey(key) && !openfda.get(key).isEmpty()) {
            sb.append(label).append(": ").append(openfda.get(key).get(0)).append("\n");
        } else {
            sb.append(label).append(": N/A\n");
        }
    }

    private void appendSection(StringBuilder sb, String label, List<String> values) {
        sb.append("\n").append(label).append(":\n");

        if (values == null || values.isEmpty()) {
            sb.append("N/A\n");
        } else {
            sb.append(values.get(0)).append("\n");
        }
    }

    public static void main(String[] args) {
        launch();
    }
}