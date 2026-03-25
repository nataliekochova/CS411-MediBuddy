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

    private VBox createInfoCard(DrugLabelResult result) {
        VBox card = createCard("Basic Information");

        card.getChildren().add(createField("Brand", getOpenFdaValue(result.getOpenfda(), "brand_name")));
        card.getChildren().add(createField("Generic", getOpenFdaValue(result.getOpenfda(), "generic_name")));
        card.getChildren().add(createField("Manufacturer", getOpenFdaValue(result.getOpenfda(), "manufacturer_name")));

        return card;
    }

    private VBox createTextCard(String title, String content) {
        VBox card = createCard(title);

        Label body = new Label(content == null || content.isBlank() ? "N/A" : content);
        body.setWrapText(true);
        body.getStyleClass().add("card-body");

        card.getChildren().add(body);
        return card;
    }

    private VBox createCard(String titleText) {
        Label title = new Label(titleText);
        title.getStyleClass().add("card-title");

        VBox card = new VBox(8, title);
        card.getStyleClass().add("info-card");

        return card;
    }

    private VBox createField(String labelText, String valueText) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");

        Label value = new Label((valueText == null || valueText.isBlank()) ? "N/A" : valueText);
        value.setWrapText(true);
        value.getStyleClass().add("field-value");

        VBox fieldBox = new VBox(2, label, value);
        return fieldBox;
    }

    private String getOpenFdaValue(Map<String, List<String>> openfda, String key) {
        if (openfda != null && openfda.containsKey(key) && !openfda.get(key).isEmpty()) {
            return openfda.get(key).get(0);
        }
        return "N/A";
    }

    private String getFirstText(List<String> values) {
        if (values == null || values.isEmpty() || values.get(0) == null || values.get(0).isBlank()) {
            return "N/A";
        }
        return values.get(0);
    }

    private void showDrugResult(VBox resultsContainer, DrugLabelResult result) {
        resultsContainer.getChildren().clear();

        resultsContainer.getChildren().add(createInfoCard(result));
        resultsContainer.getChildren().add(createTextCard("Purpose", getFirstText(result.getPurpose())));
        resultsContainer.getChildren().add(createTextCard("Indications", getFirstText(result.getIndications_and_usage())));
        resultsContainer.getChildren().add(createTextCard("Warnings", getFirstText(result.getWarnings())));
        resultsContainer.getChildren().add(createTextCard("Dosage", getFirstText(result.getDosage_and_administration())));
    }

    @Override
    public void start(Stage stage) {
        Label title = new Label("MediBuddy");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Search for medication label information");
        subtitle.getStyleClass().add("subtitle");

        TextField input = new TextField();
        input.setPromptText("Enter a medication name");
        input.setMaxWidth(Double.MAX_VALUE);
        input.getStyleClass().add("text-field");

        Button searchButton = new Button("Search");
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.getStyleClass().add("button");

        Label resultsTitle = new Label("Results");
        resultsTitle.getStyleClass().add("section-title");

        VBox resultsContainer = new VBox(12);
        resultsContainer.getStyleClass().add("results-container");

        Label placeholder = new Label("Search for a medication to see details.");
        placeholder.getStyleClass().add("results-text");
        placeholder.setWrapText(true);

        resultsContainer.getChildren().add(placeholder);

        ScrollPane resultsScroll = new ScrollPane(resultsContainer);
        resultsScroll.setFitToWidth(true);
        resultsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        resultsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        resultsScroll.setPrefHeight(360);
        resultsScroll.getStyleClass().add("results-scroll");


        VBox resultsCard = new VBox(10, resultsTitle, resultsScroll);
        resultsCard.setPadding(new Insets(14));
        resultsCard.getStyleClass().add("card");
        VBox.setVgrow(resultsScroll, Priority.ALWAYS);

        VBox content = new VBox(12, title, subtitle, input, searchButton, resultsCard);
        content.setPadding(new Insets(18));
        content.getStyleClass().add("root");

        input.setOnAction(e -> searchButton.fire());

        searchButton.setOnAction(e -> {
            String medName = input.getText().trim();

            if (medName.isEmpty()) {
                resultsContainer.getChildren().clear();

                Label prompt = new Label("Please enter a medication name.");
                prompt.getStyleClass().add("results-text");
                prompt.setWrapText(true);

                resultsContainer.getChildren().add(prompt);
                return;
            }

            resultsContainer.getChildren().clear();

            Label loading = new Label("Searching...");
            loading.getStyleClass().add("results-text");
            loading.setWrapText(true);

            resultsContainer.getChildren().add(loading);

            new Thread(() -> {
                try {
                    OpenFdaResponse response = client.searchDrugLabel(medName);

                    Platform.runLater(() -> {
                        resultsContainer.getChildren().clear();

                        if (response.getResults() == null || response.getResults().isEmpty()) {
                            Label none = new Label("No results found.");
                            none.getStyleClass().add("results-text");
                            none.setWrapText(true);
                            resultsContainer.getChildren().add(none);
                        } else {
                            DrugLabelResult first = response.getResults().get(0);
                            showDrugResult(resultsContainer, first);
                        }
                    });

                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        resultsContainer.getChildren().clear();

                        Label error = new Label("Error:\n" + ex.getMessage());
                        error.getStyleClass().add("results-text");
                        error.setWrapText(true);
                        resultsContainer.getChildren().add(error);
                    });
                }
            }).start();
        });

        Scene scene = new Scene(content, 380, 620);

        scene.getStylesheets().add(
            getClass().getResource("/style.css").toExternalForm()
        );

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