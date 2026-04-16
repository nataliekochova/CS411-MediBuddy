package com.medibuddy.ui;

import com.medibuddy.client.OpenFdaClient;
import com.medibuddy.model.OpenFdaResponse;
import com.medibuddy.model.SavedMedication;
import com.medibuddy.service.MedicationStore;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.*;

public class InteractionsPage {

    private final VBox root;
    private final OpenFdaClient client;
    private final MedicationStore store;

    private final VBox resultsBox;

    public InteractionsPage(OpenFdaClient client, MedicationStore store) {
        this.client = client;
        this.store = store;

        root = new VBox(16);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("page-root");

        Label title = new Label("Interactions");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Check your saved medications for potential interactions");
        subtitle.getStyleClass().add("subtitle");

        Button scanButton = new Button("Scan My Medications");
        scanButton.setOnAction(e -> runInteractionScan());

        resultsBox = new VBox(12);
        resultsBox.getStyleClass().add("results-container");

        root.getChildren().addAll(title, subtitle, scanButton, resultsBox);
    }

    public Parent getView() {
        return root;
    }

    private void runInteractionScan() {
        resultsBox.getChildren().clear();

        List<SavedMedication> meds = store.getMedications();

        if (meds.size() < 2) {
            resultsBox.getChildren().add(makeCard("Add at least two medications to check interactions."));
            return;
        }

        new Thread(() -> {
            try {
                Map<SavedMedication, String> medTextMap = new HashMap<>();

                // Step 1: fetch label text for each med
                for (SavedMedication med : meds) {
                    String text = fetchRelevantText(med);
                    medTextMap.put(med, text.toLowerCase());
                }

                List<String> interactions = new ArrayList<>();

                // Step 2: compare pairs
                for (int i = 0; i < meds.size(); i++) {
                    for (int j = i + 1; j < meds.size(); j++) {
                        SavedMedication a = meds.get(i);
                        SavedMedication b = meds.get(j);

                        String textA = medTextMap.get(a);
                        String textB = medTextMap.get(b);

                        if (textA.contains(b.getGenericName().toLowerCase())
                                || textB.contains(a.getGenericName().toLowerCase())) {

                            interactions.add(
                                    a.getDisplayName() + " may interact with " + b.getDisplayName()
                            );
                        }
                    }
                }

                Platform.runLater(() -> displayResults(interactions, meds, medTextMap));

            } catch (Exception e) {
                Platform.runLater(() ->
                        resultsBox.getChildren().add(makeCard("Error: " + e.getMessage()))
                );
            }
        }).start();
    }

    private String fetchRelevantText(SavedMedication med) {
        try {
            OpenFdaResponse res = client.searchDrugLabel(med.getGenericName());

            if (res.getResults() == null || res.getResults().isEmpty()) {
                return "";
            }

            var result = res.getResults().get(0);

            StringBuilder sb = new StringBuilder();

            if (result.getDrug_interactions() != null)
                sb.append(String.join(" ", result.getDrug_interactions()));

            if (result.getWarnings() != null)
                sb.append(" ").append(String.join(" ", result.getWarnings()));

            if (result.getWarnings_and_cautions() != null)
                sb.append(" ").append(String.join(" ", result.getWarnings_and_cautions()));

            return sb.toString();

        } catch (Exception e) {
            return "";
        }
    }

    private void displayResults(List<String> interactions,
                                List<SavedMedication> meds,
                                Map<SavedMedication, String> medTextMap) {

        resultsBox.getChildren().clear();

        Label section1 = new Label("Potential interactions in your list");
        section1.getStyleClass().add("section-title");

        resultsBox.getChildren().add(section1);

        if (interactions.isEmpty()) {
            resultsBox.getChildren().add(makeCard("No direct interactions detected."));
        } else {
            for (String s : interactions) {
                resultsBox.getChildren().add(makeCard(s));
            }
        }

        Label section2 = new Label("General warnings");
        section2.getStyleClass().add("section-title");

        resultsBox.getChildren().add(section2);

        for (SavedMedication med : meds) {
            String text = medTextMap.get(med);

            if (text.contains("alcohol") || text.contains("food")) {
                resultsBox.getChildren().add(
                        makeCard(med.getDisplayName() + ": may have food/alcohol interactions")
                );
            }
        }
    }

    private VBox makeCard(String text) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");

        Label body = new Label(text);
        body.getStyleClass().add("card-body");

        card.getChildren().add(body);
        return card;
    }
}