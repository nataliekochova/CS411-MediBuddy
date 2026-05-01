package com.medibuddy.ui;

import com.medibuddy.model.MedicationSchedule;
import com.medibuddy.model.SavedMedication;
import com.medibuddy.service.MedicationStore;
import com.medibuddy.model.EmergencyContact;
import com.medibuddy.service.EmailService;
import com.medibuddy.service.GoogleCalendarService;



import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.concurrent.Task;
import javafx.scene.control.Tooltip;

import java.time.LocalDate;
import java.util.*;

public class SchedulePage {

    private final MedicationStore store;
    private final BorderPane root;

    private final Label streakLabel;
    private final Label streakMessageLabel;

    private final HBox dayStrip;
    private final ListView<DoseRow> doseListView;
    

    private LocalDate centerDate = LocalDate.now();
    private LocalDate selectedDate = LocalDate.now();

    // adherence[date][schedule] = true (taken), false (missed)
    private final Map<LocalDate, Map<MedicationSchedule, Boolean>> adherence = new HashMap<>();

    public SchedulePage(MedicationStore store) {
        this.store = store;
        this.root = new BorderPane();

        // Initialize streak labels (Option A)
        this.streakLabel = new Label("Current streak: 0 days");
        this.streakMessageLabel = new Label("Keep it going!");

        // -------------------------
        // TOP SECTION
        // -------------------------
        VBox topBox = new VBox(10);
        topBox.setPadding(new Insets(18));
        topBox.getStyleClass().add("page-root");

        // -------------------------
        // STREAK BANNER
        // -------------------------
        HBox streakBanner = new HBox(12);
        streakBanner.getStyleClass().add("streak-banner");
        streakBanner.setPadding(new Insets(10));
        streakBanner.setAlignment(Pos.CENTER_LEFT);
        streakBanner.setMaxWidth(Double.MAX_VALUE);

        Label fireIcon = new Label("🔥");
        fireIcon.getStyleClass().add("streak-fire");

        VBox streakTextBox = new VBox(2);
        streakLabel.getStyleClass().add("streak-title");
        streakMessageLabel.getStyleClass().add("streak-message");

        streakTextBox.getChildren().addAll(streakLabel, streakMessageLabel);
        streakTextBox.setAlignment(Pos.CENTER_LEFT);
        streakBanner.getChildren().addAll(fireIcon, streakTextBox);

        topBox.getChildren().add(streakBanner);
        Button syncGoogleCalendarButton = new Button("Sync next 7 days to Google Calendar");
        syncGoogleCalendarButton.setMaxWidth(Double.MAX_VALUE);
        syncGoogleCalendarButton.setOnAction(e -> syncToGoogleCalendar(syncGoogleCalendarButton));
        topBox.getChildren().add(syncGoogleCalendarButton);

        // -------------------------
        // 7-DAY MOVABLE CALENDAR STRIP
        // -------------------------
        HBox calendarRow = new HBox(10);
        calendarRow.setAlignment(Pos.CENTER);

        Button prevButton = new Button("<");
        Button nextButton = new Button(">");

        dayStrip = new HBox(8);
        dayStrip.setAlignment(Pos.CENTER);

        prevButton.setOnAction(e -> {
            centerDate = centerDate.minusDays(1);
            rebuildDayStrip();
        });

        nextButton.setOnAction(e -> {
            centerDate = centerDate.plusDays(1);
            rebuildDayStrip();
        });

        calendarRow.getChildren().addAll(prevButton, dayStrip, nextButton);
        topBox.getChildren().add(calendarRow);

        // -------------------------
        // DOSE LIST
        // -------------------------
        doseListView = new ListView<>();
        doseListView.setCellFactory(list -> new DoseRowCell());
        VBox.setVgrow(doseListView, Priority.ALWAYS);

        VBox content = new VBox(10, topBox, doseListView);
        content.getStyleClass().add("schedule-page");
        content.setPadding(new Insets(0, 18, 18, 18));

        root.setCenter(content);

        // Initial build
        rebuildDayStrip();
        refreshDoseListForDate(selectedDate);
        updateStreak();
    }

    public Parent getView() {
        return root;
    }

    // -----------------------------
    // DAY STRIP
    // -----------------------------
    private void rebuildDayStrip() {
       
        ToggleGroup dayGroup = new ToggleGroup();
         dayStrip.getChildren().clear();

        LocalDate start = centerDate.minusDays(2);
        for (int i = 0; i < 5; i++) {
            LocalDate date = start.plusDays(i);
            String dayShort = date.getDayOfWeek().toString().substring(0, 3);
            String labelText = dayShort + "\n" + date.getDayOfMonth();

            ToggleButton btn = new ToggleButton(labelText);
            btn.setUserData(date);
            btn.setMinWidth(50);
            btn.setMaxWidth(60);
            btn.setContentDisplay(ContentDisplay.TOP);
            btn.setAlignment(Pos.CENTER);
            btn.setAlignment(Pos.CENTER);
            btn.getStyleClass().add("day-button");
            btn.setToggleGroup(dayGroup);

            if (date.equals(selectedDate)) {
                btn.setSelected(true);
            }

            btn.setOnAction(e -> {
                selectedDate = date;
               // dayStrip.getChildren().forEach(n -> {
                //    if (n instanceof ToggleButton tb && tb != btn) {
                //        tb.setSelected(false);
                //    }
                //});
                refreshDoseListForDate(selectedDate);
            });

            dayStrip.getChildren().add(btn);
            VBox dayContainer = new VBox(2);
dayContainer.setAlignment(Pos.CENTER);

Label triangle = new Label("▼");
triangle.getStyleClass().add("day-triangle");
triangle.setVisible(date.equals(LocalDate.now()));

dayContainer.getChildren().addAll(triangle, btn);
dayStrip.getChildren().add(dayContainer);
        }

        
    }

    // -----------------------------
    // DOSE LIST
    // -----------------------------
    private void refreshDoseListForDate(LocalDate date) {
        List<DoseRow> rows = buildDoseRowsForDate(date);
        doseListView.getItems().setAll(rows);
    }

    private List<DoseRow> buildDoseRowsForDate(LocalDate date) {
        String dayShort = date.getDayOfWeek().toString().substring(0, 3);
        List<DoseRow> rows = new ArrayList<>();

        for (SavedMedication med : store.getAllMedications()) {
            LocalDate start = med.getStartDate();
            LocalDate end = med.getEndDate();

            if (start != null && end != null) {
                if (date.isBefore(start) || date.isAfter(end)) continue;
            }

            for (MedicationSchedule sched : med.getSchedules()) {
                if (!sched.getDay().equalsIgnoreCase(dayShort)) continue;

                Boolean status = getStatusFor(date, sched);
                rows.add(new DoseRow(date, med, sched, status));
            }
        }

        return rows;
    }

    private Boolean getStatusFor(LocalDate date, MedicationSchedule sched) {
        return store.getDoseStatus(date, sched);
    }

    private void setStatusFor(LocalDate date, MedicationSchedule sched, Boolean status) {
        store.setDoseStatus(date, sched, status);
        updateStreak();
    }

    // -----------------------------
    // STREAK LOGIC
    // -----------------------------

    private boolean hasAnySchedules() {
        for (SavedMedication med : store.getAllMedications()) {
            if (!med.getSchedules().isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private void updateStreak() {
        int streak = computeStreak();
        streakLabel.setText("Current streak: " + streak + " day" + (streak == 1 ? "" : "s"));

        if (streak == 0) {
            streakMessageLabel.setText("Start your streak today!");
        } else if (streak < 5) {
            streakMessageLabel.setText("Nice work—keep it going!");
        } else {
            streakMessageLabel.setText("Amazing consistency. You're on fire!");
        }
    }

    private int computeStreak() {
        if (!hasAnySchedules()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        int streak = 0;

        LocalDate cursor = today;
        int calendarDaysScanned = 0;
        int maxCalendarDaysToScan = 365;
        int scheduledDaysChecked = 0;
        int maxScheduledDaysToCheck = 365;

        while (scheduledDaysChecked < maxScheduledDaysToCheck
                && calendarDaysScanned < maxCalendarDaysToScan) {
            List<DoseRow> rows = buildDoseRowsForDate(cursor);

            if (rows.isEmpty()) {
                cursor = cursor.minusDays(1);
                calendarDaysScanned++;
                continue;
            }

            LocalDate dateForLambda = cursor;

            boolean allTaken = rows.stream().allMatch(r -> {
                Boolean status = getStatusFor(dateForLambda, r.schedule());
                return Boolean.TRUE.equals(status);
            });

            if (!allTaken) {
                break;
            }

            streak++;
            scheduledDaysChecked++;
            cursor = cursor.minusDays(1);
            calendarDaysScanned++;
        }

        return streak;
    }

    private void sendCriticalAlertIfNeeded(DoseRow row) {
        MedicationSchedule schedule = row.schedule();

        if (!schedule.isCriticalAlertEnabled()) {
            return;
        }

        EmergencyContact contact = store.getEmergencyContactById(schedule.getEmergencyContactId());

    

        if (contact == null) {
            showAlert(Alert.AlertType.WARNING,
                    "No Emergency Contact",
                    "This medication has Critical Alert enabled, but no contact was found.");
            return;
        }

        try {
            EmailService emailService = new EmailService();

            emailService.sendCriticalAlert(
                    contact.getEmail(),
                    row.medication().getDisplayName(),
                    schedule.getTime()
            );

            showAlert(Alert.AlertType.INFORMATION,
                    "Critical Alert Sent",
                    "Emergency email sent to " + contact.getName() + " at " + contact.getEmail());

        } catch (Exception ex) {
            ex.printStackTrace();

            showAlert(Alert.AlertType.ERROR,
                    "Critical Alert Failed",
                    "Could not send emergency email:\n" + ex.getMessage());
        }
    }

    private void syncToGoogleCalendar(Button syncButton) {
        syncButton.setDisable(true);
        syncButton.setText("Syncing...");

        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() throws Exception {
                GoogleCalendarService calendarService = new GoogleCalendarService(store);
                return calendarService.syncNextSevenDays();
            }
        };

        task.setOnSucceeded(e -> {
            syncButton.setDisable(false);
            syncButton.setText("Sync next 7 days to Google Calendar");

            int createdCount = task.getValue();

            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Google Calendar Synced",
                    "Created " + createdCount + " medication reminder event" +
                            (createdCount == 1 ? "" : "s") + " in Google Calendar."
            );
        });

        task.setOnFailed(e -> {
            syncButton.setDisable(false);
            syncButton.setText("Sync next 7 days to Google Calendar");

            Throwable ex = task.getException();
            ex.printStackTrace();

            showAlert(
                    Alert.AlertType.ERROR,
                    "Google Calendar Sync Failed",
                    ex.getMessage() == null ? "Unknown error" : ex.getMessage()
            );
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }



    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    // -----------------------------
    // DOSE ROW MODEL
    // -----------------------------
    private record DoseRow(LocalDate date, SavedMedication medication,
                           MedicationSchedule schedule, Boolean status) {}

    // -----------------------------
    // LIST CELL
    // -----------------------------
    private class DoseRowCell extends javafx.scene.control.ListCell<DoseRow> {
        @Override
        protected void updateItem(DoseRow row, boolean empty) {
            super.updateItem(row, empty);

            if (empty || row == null) {
                setGraphic(null);
                return;
            }

            Label title = new Label(row.medication().getDisplayName());
            title.getStyleClass().add("dose-title");
            title.setWrapText(true);
            title.prefWidthProperty().bind(doseListView.widthProperty().subtract(150));
            title.setMinWidth(0);

            String scheduleInfo = row.schedule().getDay() + " at " + row.schedule().getTime();
            Label subtitle = new Label(scheduleInfo);
            subtitle.getStyleClass().add("dose-subtitle");

            HBox statusRow = new HBox(8);
            statusRow.setAlignment(Pos.CENTER_LEFT);

            ToggleButton takenBtn = new ToggleButton("✓");
            takenBtn.setPrefWidth(32);
            takenBtn.setPrefHeight(32);
            ToggleButton missedBtn = new ToggleButton("✘");
            missedBtn.setPrefWidth(32);
            missedBtn.setPrefHeight(32);

            takenBtn.getStyleClass().addAll("dose-status-button", "dose-taken");
            missedBtn.getStyleClass().addAll("dose-status-button", "dose-missed");

            Boolean status = row.status();
            if (Boolean.TRUE.equals(status)) {
                takenBtn.setSelected(true);
            } else if (Boolean.FALSE.equals(status)) {
                missedBtn.setSelected(true);
            }

            boolean isFutureDose = row.date().isAfter(LocalDate.now());

            takenBtn.setDisable(isFutureDose);
            missedBtn.setDisable(isFutureDose);

            if (isFutureDose) {
                takenBtn.setTooltip(new Tooltip("You cannot mark future medications yet."));
                missedBtn.setTooltip(new Tooltip("You cannot mark future medications yet."));
            }

            takenBtn.setOnAction(e -> {
                if (takenBtn.isSelected()) {
                    missedBtn.setSelected(false);
                    setStatusFor(row.date(), row.schedule(), true);
                    updateRowBackground(true);
                } else {
                    setStatusFor(row.date(), row.schedule(), null);
                    updateRowBackground(null);
                }
            });

            missedBtn.setOnAction(e -> {
                if (missedBtn.isSelected()) {
                    takenBtn.setSelected(false);
                    setStatusFor(row.date(), row.schedule(), false);
                    updateRowBackground(false);
                } else {
                    setStatusFor(row.date(), row.schedule(), null);
                    updateRowBackground(null);
                }
            });

            statusRow.getChildren().addAll(takenBtn, missedBtn);

            VBox textBox = new VBox(2, title, subtitle);
            textBox.setMaxWidth(Double.MAX_VALUE);

            HBox contentRow = new HBox(10);
            contentRow.setAlignment(Pos.CENTER_LEFT);
            contentRow.getChildren().addAll(textBox, statusRow);

            HBox.setHgrow(textBox, Priority.ALWAYS);
            statusRow.setMinWidth(72);
            statusRow.setMaxWidth(72);

            VBox box = new VBox(contentRow);
            box.getStyleClass().add("dose-card");
            box.setMaxWidth(Double.MAX_VALUE);

            updateRowBackground(status);

            setGraphic(box);
        }

        private void updateRowBackground(Boolean status) {
    setStyle("");  
}
    }
}
