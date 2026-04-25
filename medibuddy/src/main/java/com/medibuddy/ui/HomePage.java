package com.medibuddy.ui;

import com.medibuddy.model.SavedMedication;
import com.medibuddy.service.MedicationStore;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class HomePage {
    private final AppShell shell;
    private final MedicationStore store;
    private final VBox root;

    public HomePage(AppShell shell, MedicationStore store) {
        this.shell = shell;
        this.store = store;
        this.root = build();
    }

    public Parent getView() {
        return root;
    }

    private VBox build() {
        //Label title = new Label("MediBuddy");
        //title.getStyleClass().add("title");

        Label subtitle = new Label("My Medications");
        subtitle.getStyleClass().add("subtitle");

        Button searchButton = new Button("Search Medications");
        searchButton.getStyleClass().add("button");
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.setOnAction(e -> shell.showSearchPage());

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
                Label nameLabel = new Label(med.getDisplayName());
                nameLabel.getStyleClass().add("card-title");

                Label doseFormLabel = new Label(med.getDoseAndFormDisplay());
                doseFormLabel.getStyleClass().add("subtitle");
                doseFormLabel.setWrapText(true);

                VBox medButtonContent = new VBox(4, nameLabel, doseFormLabel);

                Button medButton = new Button();
                medButton.setGraphic(medButtonContent);
                medButton.getStyleClass().add("med-list-button");
                medButton.setMaxWidth(Double.MAX_VALUE);
                medButton.setWrapText(true);
                medButton.setOnAction(e -> shell.showMedicationDetailPage(med));

                medList.getChildren().add(medButton);
            }
        }

        ScrollPane medScroll = new ScrollPane(medList);
        medScroll.setFitToWidth(true);
        medScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        medScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        medScroll.setPrefHeight(420);
        medScroll.getStyleClass().add("results-scroll");

        Rectangle clip = new Rectangle();
clip.widthProperty().bind(medScroll.widthProperty());
clip.heightProperty().bind(medScroll.heightProperty());
clip.setArcWidth(16);
clip.setArcHeight(16);
medScroll.setClip(clip);

        VBox card = new VBox(10, savedTitle, medScroll);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(14));
        VBox.setVgrow(medScroll, Priority.ALWAYS);

        //VBox content = new VBox(12, title, subtitle, searchButton, card);
        VBox content = new VBox(12, subtitle, searchButton, card);
        content.setPadding(new Insets(18));

        return content;
    }
}