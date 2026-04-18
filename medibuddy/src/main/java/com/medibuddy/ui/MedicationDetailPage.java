package com.medibuddy.ui;

import com.medibuddy.model.MedicationSchedule;
import com.medibuddy.model.SavedMedication;
import com.medibuddy.service.MedicationStore;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MedicationDetailPage {

    private final AppShell shell;
    private final SavedMedication medication;
    private final VBox root;
    private final MedicationStore store;
    private TextField timeField;
    private TextField frequencyField;
    private ChoiceBox<String> frequencyTypeBox;
    private MedicationSchedule scheduleBeingEdited = null;

    public MedicationDetailPage(AppShell shell, MedicationStore store, SavedMedication medication) {
        this.shell = shell;
        this.store = store;
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

        Button removeButton = new Button("Remove from My List");
        removeButton.getStyleClass().add("danger-button");
        removeButton.setMaxWidth(Double.MAX_VALUE);
        removeButton.setOnAction(e -> {
            store.removeMedication(medication);
            shell.showMedicationsPage();
        });

        VBox detailsContainer = new VBox(12);
        detailsContainer.getStyleClass().add("results-container");

        detailsContainer.getChildren().add(createInfoCard());
        detailsContainer.getChildren().add(createTextCard("My Dose", medication.getUserDose()));
        detailsContainer.getChildren().add(createTextCard("My Form", medication.getUserForm()));
        detailsContainer.getChildren().add(createTextCard("Purpose", medication.getPurpose()));
        detailsContainer.getChildren().add(createTextCard("Indications", medication.getIndications()));
        detailsContainer.getChildren().add(createTextCard("Warnings", medication.getWarnings()));
        detailsContainer.getChildren().add(createTextCard("Label Dosage Guidance", medication.getLabelDosage()));

        VBox schedulesCard = createCard("Saved Schedules");
        updateSchedulesList(schedulesCard);
        detailsContainer.getChildren().add(schedulesCard);

        VBox scheduleCard = createCard("Add Schedule");

        timeField = new TextField();
        timeField.setPromptText("Time (e.g. 08:00)");

        frequencyField = new TextField();
        frequencyField.setPromptText("Frequency (e.g. 1)");

        frequencyTypeBox = new ChoiceBox<>();
        frequencyTypeBox.getItems().addAll("Daily", "Weekly", "Monthly");
        frequencyTypeBox.setValue("Daily");

        Label scheduleErrorLabel = new Label();
        scheduleErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        scheduleErrorLabel.setVisible(false);

        Button saveScheduleButton = new Button("Save Schedule");
        saveScheduleButton.setMaxWidth(Double.MAX_VALUE);

        saveScheduleButton.setOnAction(e -> {
            String time = timeField.getText();
            String type = frequencyTypeBox.getValue();
            String freqText = frequencyField.getText();

            if (time.isBlank() || freqText.isBlank() || type == null || type.isBlank()) {
                scheduleErrorLabel.setText("Define all parameters of the schedule");
                scheduleErrorLabel.setVisible(true);
                return;
            }

            scheduleErrorLabel.setVisible(false);
            try {
                int freq = Integer.parseInt(frequencyField.getText());
                MedicationSchedule newSchedule = new MedicationSchedule(time, freq, type);
                if (scheduleBeingEdited != null) {
                    store.removeScheduleFromMedication(medication, scheduleBeingEdited);
                    store.addScheduleToMedication(medication, newSchedule);
                    scheduleBeingEdited = null;
                } else {
                    store.addScheduleToMedication(medication, newSchedule);
                }

                updateSchedulesList(schedulesCard);

                timeField.clear();
                frequencyField.clear();
                frequencyTypeBox.setValue("Daily");
            } catch (Exception ex) {
                System.out.println("Invalid schedule input.");
            }
        });

        scheduleCard.getChildren().addAll(
                timeField,
                frequencyField,
                frequencyTypeBox,
                scheduleErrorLabel,
                saveScheduleButton
        );
        detailsContainer.getChildren().add(scheduleCard);

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

        VBox content = new VBox(12, title, subtitle, backButton, removeButton, detailsCard);
        content.setPadding(new Insets(18));
        content.getStyleClass().add("root");

        return content;
    }

    private VBox createInfoCard() {
        VBox card = createCard("Basic Information");

        card.getChildren().add(createField("Brand", medication.getBrandName()));
        card.getChildren().add(createField("Generic", medication.getGenericName()));
        card.getChildren().add(createField("Manufacturer", medication.getManufacturer()));
        card.getChildren().add(createField("Dose / Form", medication.getDoseAndFormDisplay()));

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

    private void updateSchedulesList(VBox schedulesCard) {
        schedulesCard.getChildren().clear();

        for (MedicationSchedule sched : medication.getSchedules()) {
            Label label = new Label(
                    sched.getFrequencyType() + " - " +
                    sched.getTime() + " (" + sched.getFrequencyPerDay() + "x)"
            );

            Button editBtn = new Button("Edit");
            editBtn.getStyleClass().add("button");
            editBtn.setOnAction(e -> {
                timeField.setText(sched.getTime());
                frequencyField.setText(String.valueOf(sched.getFrequencyPerDay()));
                frequencyTypeBox.setValue(sched.getFrequencyType());
                scheduleBeingEdited = sched;
                updateSchedulesList(schedulesCard);
            });

            Button deleteBtn = new Button("Delete");
            deleteBtn.getStyleClass().add("danger-button");
            deleteBtn.setOnAction(e -> {
                store.removeScheduleFromMedication(medication, sched);
                updateSchedulesList(schedulesCard);
            });

            HBox buttonRow = new HBox(8, editBtn, deleteBtn);

            VBox row = new VBox(4, label, buttonRow);
            row.getStyleClass().add("schedule-row");

            schedulesCard.getChildren().add(row);
        }

        if (medication.getSchedules().isEmpty()) {
            schedulesCard.getChildren().add(new Label("No schedules saved."));
        }
    }
}
