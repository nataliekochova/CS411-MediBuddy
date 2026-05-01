package com.medibuddy.ui;

import com.medibuddy.model.MedicationSchedule;
import com.medibuddy.model.SavedMedication;
import com.medibuddy.service.MedicationStore;
import com.medibuddy.model.EmergencyContact;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MedicationDetailPage {

    private final AppShell shell;
    private final SavedMedication medication;
    private final MedicationStore store;
    private final VBox root;


    private VBox timesContainer;
    private HBox daySelector;
    private DatePicker startDatePicker;
    private DatePicker endDatePicker;
    private CheckBox criticalAlertCheckBox;
    private ComboBox<EmergencyContact> emergencyContactComboBox;
    private TextField missedWindowField;

    private MedicationSchedule scheduleBeingEdited = null;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("hh:mm a");

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

        // -------------------------
        // TIMES SECTION
        // -------------------------
        Label timesLabel = new Label("Times to Take:");
        timesLabel.getStyleClass().add("field-label");

        timesContainer = new VBox(6);
        addTimeField(); // default time field

        Button addTimeButton = new Button("+ Add Time");
        addTimeButton.setOnAction(e -> addTimeField());

        // -------------------------
        // DAY SELECTOR
        // -------------------------
        Label daysLabel = new Label("Days:");
        daysLabel.getStyleClass().add("field-label");

        daySelector = buildDaySelector();

        // -------------------------
        // DATE PICKERS
        // -------------------------
        startDatePicker = new DatePicker();
        startDatePicker.setPromptText("Start Date");

        endDatePicker = new DatePicker();
        endDatePicker.setPromptText("End Date");
        
        startDatePicker.getStyleClass().add("date-picker");
        endDatePicker.getStyleClass().add("date-picker");

        if (medication.getStartDate() != null) {
            startDatePicker.setValue(medication.getStartDate());
        }
        if (medication.getEndDate() != null) {
            endDatePicker.setValue(medication.getEndDate());
        }

        criticalAlertCheckBox = new CheckBox("Enable Critical Alert");

        Label emergencyContactLabel = new Label("Emergency Contact:");

        emergencyContactComboBox = new ComboBox<>();
        emergencyContactComboBox.setPromptText("Select emergency contact");
        emergencyContactComboBox.setMaxWidth(Double.MAX_VALUE);
        emergencyContactComboBox.getItems().setAll(store.getEmergencyContacts());
        emergencyContactComboBox.setOnShowing(e ->
                emergencyContactComboBox.getItems().setAll(store.getEmergencyContacts())
        );

        Label missedWindowLabel = new Label("Missed Window Minutes:");

        missedWindowField = new TextField();
        missedWindowField.setPromptText("Time in Minutes(Example: 30)");
        missedWindowField.setText("30");
        missedWindowField.setMaxWidth(Double.MAX_VALUE);

        // Hide these by default
        emergencyContactLabel.setVisible(false);
        emergencyContactLabel.setManaged(false);

        emergencyContactComboBox.setVisible(false);
        emergencyContactComboBox.setManaged(false);

        missedWindowLabel.setVisible(false);
        missedWindowLabel.setManaged(false);

        missedWindowField.setVisible(false);
        missedWindowField.setManaged(false);

        // Show only when Critical Alert is selected
        criticalAlertCheckBox.selectedProperty().addListener((obs, oldVal, selected) -> {
            emergencyContactLabel.setVisible(selected);
            emergencyContactLabel.setManaged(selected);

            emergencyContactComboBox.setVisible(selected);
            emergencyContactComboBox.setManaged(selected);

            missedWindowLabel.setVisible(selected);
            missedWindowLabel.setManaged(selected);

            missedWindowField.setVisible(selected);
            missedWindowField.setManaged(selected);
        });

        Label scheduleErrorLabel = new Label();
        scheduleErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        scheduleErrorLabel.setVisible(false);

        Button saveScheduleButton = new Button("Save Schedule");
        saveScheduleButton.setMaxWidth(Double.MAX_VALUE);

        saveScheduleButton.setOnAction(e -> {

            // -------------------------
            // VALIDATION
            // -------------------------
            List<String> times = collectTimes();
            if (times.isEmpty()) {
                scheduleErrorLabel.setText("Enter at least one valid time (e.g. 08:00 AM)");
                scheduleErrorLabel.setVisible(true);
                return;
            }

            List<String> selectedDays = collectSelectedDays();
            if (selectedDays.isEmpty()) {
                scheduleErrorLabel.setText("Select at least one day.");
                scheduleErrorLabel.setVisible(true);
                return;
            }

            LocalDate start = startDatePicker.getValue();
            LocalDate end = endDatePicker.getValue();

            if (start == null || end == null) {
                scheduleErrorLabel.setText("Select both start and end dates.");
                scheduleErrorLabel.setVisible(true);
                return;
            }

            if (end.isBefore(start)) {
                scheduleErrorLabel.setText("End date cannot be before start date.");
                scheduleErrorLabel.setVisible(true);
                return;
            }

            scheduleErrorLabel.setVisible(false);

            boolean criticalAlertEnabled = criticalAlertCheckBox.isSelected();
            EmergencyContact selectedContact = emergencyContactComboBox.getValue();

            int missedWindowMinutes = 30;

            if (criticalAlertEnabled) {
                String missedWindowText = missedWindowField.getText() == null
                        ? ""
                        : missedWindowField.getText().trim();

                try {
                    missedWindowMinutes = Integer.parseInt(missedWindowText);
                } catch (NumberFormatException ex) {
                    scheduleErrorLabel.setText("Missed window must be a number of minutes.");
                    scheduleErrorLabel.setVisible(true);
                    return;
                }

                if (missedWindowMinutes <= 0) {
                    scheduleErrorLabel.setText("Missed window must be greater than 0.");
                    scheduleErrorLabel.setVisible(true);
                    return;
                }
            }

            if (criticalAlertEnabled && selectedContact == null) {
                scheduleErrorLabel.setText("Select an emergency contact for Critical Alert.");
                scheduleErrorLabel.setVisible(true);
                return;
            }

            // Save date range to medication
            medication.setStartDate(start);
            medication.setEndDate(end);
            store.updateMedicationDateRange(medication);

            // If editing, remove old schedule
            if (scheduleBeingEdited != null) {
                store.removeScheduleFromMedication(medication, scheduleBeingEdited);
                scheduleBeingEdited = null;
            }

            // Create new schedules
            for (String day : selectedDays) {
                for (String time : times) {

                    Integer selectedContactId;

                    if (selectedContact == null) {
                        selectedContactId = null;
                    } else {
                        selectedContactId = selectedContact.getId();
                    }

                    MedicationSchedule sched = new MedicationSchedule(
                            day,
                            time,
                            criticalAlertEnabled,
                            selectedContactId,
                            missedWindowMinutes
                    );

                    store.addScheduleToMedication(medication, sched);
                }
            }

            updateSchedulesList(schedulesCard);
            clearInputs();

        });

        scheduleCard.getChildren().addAll(
                timesLabel,
                timesContainer,
                addTimeButton,
                daysLabel,
                daySelector,
                new Label("Start Date:"),
                startDatePicker,
                new Label("End Date:"),
                endDatePicker,
                criticalAlertCheckBox,
                emergencyContactLabel,
                emergencyContactComboBox,
                missedWindowLabel,
                missedWindowField,
                scheduleErrorLabel,
                saveScheduleButton
        );

        detailsContainer.getChildren().add(scheduleCard);

        ScrollPane detailsScroll = new ScrollPane(detailsContainer);
        detailsScroll.setFitToWidth(true);
        detailsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        detailsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        detailsScroll.setPrefHeight(420);
        //detailsScroll.getStyleClass().add("results-scroll");

        VBox detailsCard = new VBox(10, detailsScroll);
        detailsCard.setPadding(new Insets(14));
        detailsCard.getStyleClass().add("card");
        VBox.setVgrow(detailsScroll, Priority.ALWAYS);

        VBox content = new VBox(12, title, subtitle, backButton, removeButton, detailsCard);
        content.setPadding(new Insets(18));
        content.getStyleClass().add("root");

        return content;
    }

    // -------------------------
    // TIME FIELD HANDLING
    // -------------------------
    private void addTimeField() {
        TextField tf = new TextField();
        tf.setPromptText("08:00 AM");
        timesContainer.getChildren().add(tf);
    }

    private List<String> collectTimes() {
        List<String> times = new ArrayList<>();

        for (Node n : timesContainer.getChildren()) {
            if (n instanceof TextField tf) {
                String t = tf.getText().trim();
                if (!t.isEmpty() && t.matches("^(0[1-9]|1[0-2]):[0-5][0-9] (AM|PM)$")) {
                    times.add(t);
                }
            }
        }
        return times;
    }

    // -------------------------
    // DAY SELECTOR
    // -------------------------
    private HBox buildDaySelector() {
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        HBox box = new HBox(8);

        for (String d : days) {
            ToggleButton btn = new ToggleButton(d);
            btn.getStyleClass().add("day-button");
            btn.setMinWidth(40);
            box.getChildren().add(btn);
        }
        return box;
    }

    private List<String> collectSelectedDays() {
        List<String> days = new ArrayList<>();
        for (Node n : daySelector.getChildren()) {
            ToggleButton btn = (ToggleButton) n;
            if (btn.isSelected()) {
                days.add(btn.getText());
            }
        }
        return days;
    }

    // -------------------------
    // CLEAR INPUTS
    // -------------------------
    private void clearInputs() {
        timesContainer.getChildren().clear();
        addTimeField();

        for (Node n : daySelector.getChildren()) {
            ((ToggleButton) n).setSelected(false);
        }

        criticalAlertCheckBox.setSelected(false);
        emergencyContactComboBox.setValue(null);
        missedWindowField.setText("30");

    }

    // -------------------------
    // SCHEDULE LIST
    // -------------------------
    private void updateSchedulesList(VBox schedulesCard) {
        schedulesCard.getChildren().clear();

        for (MedicationSchedule sched : medication.getSchedules()) {

            String criticalText = sched.isCriticalAlertEnabled()
                    ? " | Critical Alert: ON (" + sched.getMissedWindowMinutes() + " min)"
                    : " | Critical Alert: OFF";

            Label label = new Label(
                    sched.getDay() + " - " + sched.getTime() + criticalText
            );

            Button editBtn = new Button("Edit");
            editBtn.getStyleClass().add("button");
            editBtn.setOnAction(e -> {
                scheduleBeingEdited = sched;

                // Load time
                timesContainer.getChildren().clear();
                TextField tf = new TextField(sched.getTime());
                timesContainer.getChildren().add(tf);

                // Load day
                for (Node n : daySelector.getChildren()) {
                    ToggleButton btn = (ToggleButton) n;
                    btn.setSelected(btn.getText().equals(sched.getDay()));
                }

                // Load dates
                startDatePicker.setValue(medication.getStartDate());
                endDatePicker.setValue(medication.getEndDate());

                criticalAlertCheckBox.setSelected(sched.isCriticalAlertEnabled());

                EmergencyContact contact = store.getEmergencyContactById(sched.getEmergencyContactId());
                emergencyContactComboBox.setValue(contact);

                missedWindowField.setText(String.valueOf(sched.getMissedWindowMinutes()));

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

    // -------------------------
    // INFO CARDS
    // -------------------------
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
}
