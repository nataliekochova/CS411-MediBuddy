package com.medibuddy.ui;

import com.medibuddy.App;
import com.medibuddy.model.SavedMedication;
import com.medibuddy.service.MedicationStore;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class HomePage {
    private final App app;
    private final MedicationStore store;
    private final VBox root;

    public HomePage(App app, MedicationStore store) {
        this.app = app;
        this.store = store;
        this.root = build();
    }

    public Parent getView() {
        return root;
    }

    private VBox build() {
        Label title = new Label("MediBuddy");
        title.getStyleClass().add("title");

        Label subtitle = new Label("My Medications");
        subtitle.getStyleClass().add("subtitle");

        Button searchButton = new Button("Search Medications");
        searchButton.getStyleClass().add("button");
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.setOnAction(e -> app.showSearchPage());

        Label savedTitle = new Label("Saved Medications");
        savedTitle.getStyleClass().add("section-title");

        VBox medList = new VBox(12);
        medList.getStyleClass().add("results-container");

        if (store.getMedications().isEmpty()) {
            Label empty = new Label("No medications added yet.");
            empty.getStyleClass().add("results-text");
            empty.setWrapText(true);
            medList.getChildren().add(empty);
        } else {
            for (SavedMedication med : store.getMedications()) {
                Button medButton = new Button(med.getDisplayName());
                medButton.getStyleClass().add("med-list-button");
                medButton.setMaxWidth(Double.MAX_VALUE);
                medButton.setWrapText(true);
                medButton.setOnAction(e -> app.showMedicationDetailPage(med));
                medList.getChildren().add(medButton);
            }
        }

        ScrollPane medScroll = new ScrollPane(medList);
        medScroll.setFitToWidth(true);
        medScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        medScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        medScroll.setPrefHeight(420);
        medScroll.getStyleClass().add("results-scroll");

        VBox card = new VBox(10, savedTitle, medScroll);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(14));
        VBox.setVgrow(medScroll, Priority.ALWAYS);

        VBox content = new VBox(12, title, subtitle, searchButton, card);
        content.setPadding(new Insets(18));

        return content;
    }
}