package com.medibuddy.ui;

import com.medibuddy.App;
import com.medibuddy.model.SavedMedication;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MedicationDetailPage {

    private final AppShell shell;
    private final SavedMedication medication;
    private final VBox root;

    public MedicationDetailPage(AppShell shell, SavedMedication medication) {
        this.shell = shell;
        this.medication = medication;
        this.root = build();
    }

    public Parent getView() {
        return root;
    }

    private VBox build() {
        Label title = new Label("MediBuddy");
        title.getStyleClass().add("title");

        Label subtitle = new Label(medication.getDisplayName());
        subtitle.getStyleClass().add("subtitle");

        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.getStyleClass().add("button");
        backButton.setOnAction(e -> shell.showMedicationsPage());

        VBox detailsContainer = new VBox(12);
        detailsContainer.getStyleClass().add("results-container");

        detailsContainer.getChildren().add(createInfoCard());
        detailsContainer.getChildren().add(createTextCard("Purpose", medication.getPurpose()));
        detailsContainer.getChildren().add(createTextCard("Indications", medication.getIndications()));
        detailsContainer.getChildren().add(createTextCard("Warnings", medication.getWarnings()));
        detailsContainer.getChildren().add(createTextCard("Dosage", medication.getDosage()));

        ScrollPane detailsScroll = new ScrollPane(detailsContainer);
        detailsScroll.setFitToWidth(true);
        detailsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        detailsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        detailsScroll.setPrefHeight(420);
        detailsScroll.getStyleClass().add("results-scroll");

        VBox detailsCard = new VBox(10, detailsScroll);
        detailsCard.setPadding(new Insets(14));
        detailsCard.getStyleClass().add("card");
        VBox.setVgrow(detailsScroll, Priority.ALWAYS);

        VBox content = new VBox(12, title, subtitle, backButton, detailsCard);
        content.setPadding(new Insets(18));
        content.getStyleClass().add("root");

        return content;
    }

    private VBox createInfoCard() {
        VBox card = createCard("Basic Information");

        card.getChildren().add(createField("Brand", medication.getBrandName()));
        card.getChildren().add(createField("Generic", medication.getGenericName()));
        card.getChildren().add(createField("Manufacturer", medication.getManufacturer()));

        return card;
    }

    private VBox createTextCard(String title, String content) {
        VBox card = createCard(title);

        Label body = new Label(safeText(content));
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

        Label value = new Label(safeText(valueText));
        value.setWrapText(true);
        value.getStyleClass().add("field-value");

        return new VBox(2, label, value);
    }

    private String safeText(String text) {
        if (text == null || text.isBlank()) {
            return "N/A";
        }
        return text;
    }
}