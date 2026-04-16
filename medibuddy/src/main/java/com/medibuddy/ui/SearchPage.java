package com.medibuddy.ui;

import com.medibuddy.client.OpenFdaClient;
import com.medibuddy.model.DrugLabelResult;
import com.medibuddy.model.OpenFdaResponse;
import com.medibuddy.model.SavedMedication;
import com.medibuddy.service.MedicationStore;
import com.medibuddy.model.OpenFdaMetadata;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;


public class SearchPage {

    private final AppShell shell;
    private final OpenFdaClient client;
    private final MedicationStore store;
    private final VBox root;

    private DrugLabelResult currentResult;
    private Button addButton;

    public SearchPage(AppShell shell, OpenFdaClient client, MedicationStore store) {
        this.shell = shell;
        this.client = client;
        this.store = store;
        this.root = build();
    }

    public Parent getView() {
        return root;
    }

    private VBox build() {
        Label title = new Label("MediBuddy");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Search for medication label information");
        subtitle.getStyleClass().add("subtitle");

        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.getStyleClass().add("button");
        backButton.setOnAction(e -> shell.showMedicationsPage());

        TextField input = new TextField();
        input.setPromptText("Enter a medication name");
        input.setMaxWidth(Double.MAX_VALUE);
        input.getStyleClass().add("text-field");

        Button searchButton = new Button("Search");
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.getStyleClass().add("button");

        addButton = new Button("Add to My List");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.getStyleClass().add("button");
        addButton.setDisable(true);

        addButton.setOnAction(e -> {
            if (currentResult != null) {
                store.addMedication(toSavedMedication(currentResult));
                shell.showMedicationsPage();
            }
        });

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

        VBox content = new VBox(12, title, subtitle, backButton, input, searchButton, addButton, resultsCard);
        content.setPadding(new Insets(18));
        content.getStyleClass().add("root");

        input.setOnAction(e -> searchButton.fire());

        searchButton.setOnAction(e -> {
            String medName = input.getText().trim();

            currentResult = null;
            addButton.setDisable(true);

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
                            currentResult = null;
                            addButton.setDisable(true);
                            showSearchResults(resultsContainer, response.getResults());
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

        

        return content;
    }

    private SavedMedication toSavedMedication(DrugLabelResult result) {
        return new SavedMedication(
                getOpenFdaValue(result.getOpenfda(), "brand_name"),
                getOpenFdaValue(result.getOpenfda(), "generic_name"),
                getOpenFdaValue(result.getOpenfda(), "manufacturer_name"),
                getFirstText(result.getPurpose()),
                getFirstText(result.getIndications_and_usage()),
                getFirstText(result.getWarnings()),
                getFirstText(result.getDosage_and_administration())
        );
    }

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

        return new VBox(2, label, value);
    }

    private void showDrugResult(VBox resultsContainer, DrugLabelResult result) {
        resultsContainer.getChildren().clear();

        resultsContainer.getChildren().add(createInfoCard(result));
        resultsContainer.getChildren().add(createTextCard("Purpose", getFirstText(result.getPurpose())));
        resultsContainer.getChildren().add(createTextCard("Indications", getFirstText(result.getIndications_and_usage())));
        resultsContainer.getChildren().add(createTextCard("Warnings", getFirstText(result.getWarnings())));
        resultsContainer.getChildren().add(createTextCard("Dosage", getFirstText(result.getDosage_and_administration())));
    }

    private void showSearchResults(VBox resultsContainer, List<DrugLabelResult> results) {
        resultsContainer.getChildren().clear();

        for (DrugLabelResult result : results) {
            resultsContainer.getChildren().add(createSearchResultCard(result));
        }
    }

    private VBox createSearchResultCard(DrugLabelResult result) {
        Label title = new Label(getDisplayTitle(result));
        title.getStyleClass().add("card-title");

        VBox card = new VBox(8);
        card.getStyleClass().add("info-card");

        card.getChildren().add(title);
        card.getChildren().add(createField("Brand", getOpenFdaValue(result.getOpenfda(), "brand_name")));
        card.getChildren().add(createField("Generic", getOpenFdaValue(result.getOpenfda(), "generic_name")));
        card.getChildren().add(createField("Manufacturer", getOpenFdaValue(result.getOpenfda(), "manufacturer_name")));

        Button selectButton = new Button("Select");
        selectButton.getStyleClass().add("button");
        selectButton.setMaxWidth(Double.MAX_VALUE);

        selectButton.setOnAction(e -> {
            currentResult = result;
            addButton.setDisable(false);
        });

        card.getChildren().add(selectButton);

        return card;
    }

    private String getDisplayTitle(DrugLabelResult result) {
        String brand = getOpenFdaValue(result.getOpenfda(), "brand_name");
        String generic = getOpenFdaValue(result.getOpenfda(), "generic_name");

        if (!"N/A".equals(brand)) {
            return brand;
        }
        if (!"N/A".equals(generic)) {
            return generic;
        }
        return "Unnamed Medication";
    }

    private String getOpenFdaValue(OpenFdaMetadata openfda, String key) {
    if (openfda == null) {
        return "N/A";
    }

    return switch (key) {
        case "brand_name" ->
                getFirstText(openfda.getBrand_name());
        case "generic_name" ->
                getFirstText(openfda.getGeneric_name());
        case "manufacturer_name" ->
                getFirstText(openfda.getManufacturer_name());
        default -> "N/A";
    };
}

    private String getFirstText(List<String> values) {
        if (values == null || values.isEmpty() || values.get(0) == null || values.get(0).isBlank()) {
            return "N/A";
        }
        return values.get(0);
    }
}