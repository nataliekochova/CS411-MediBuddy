package com.medibuddy.ui;

import com.medibuddy.model.MedicationSchedule;
import com.medibuddy.model.SavedMedication;
import com.medibuddy.service.MedicationStore;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

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
        dayStrip.getChildren().clear();

        LocalDate start = centerDate.minusDays(3);
        for (int i = 0; i < 7; i++) {
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

            if (date.equals(selectedDate)) {
                btn.setSelected(true);
            }

            btn.setOnAction(e -> {
                selectedDate = date;
                dayStrip.getChildren().forEach(n -> {
                    if (n instanceof ToggleButton tb && tb != btn) {
                        tb.setSelected(false);
                    }
                });
                refreshDoseListForDate(selectedDate);
            });

            dayStrip.getChildren().add(btn);
            VBox dayContainer = new VBox(2);
dayContainer.setAlignment(Pos.CENTER);

Label triangle = new Label("▲");
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
        Map<MedicationSchedule, Boolean> dayMap = adherence.get(date);
        if (dayMap == null) return null;
        return dayMap.get(sched);
    }

    private void setStatusFor(LocalDate date, MedicationSchedule sched, Boolean status) {
        Map<MedicationSchedule, Boolean> dayMap = adherence.computeIfAbsent(date, d -> new HashMap<>());
        dayMap.put(sched, status);
        updateStreak();
    }

    // -----------------------------
    // STREAK LOGIC
    // -----------------------------
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
        LocalDate today = LocalDate.now();
        int streak = 0;

        LocalDate cursor = today;

        while (true) {
            LocalDate dateForLambda = cursor;

            List<DoseRow> rows = buildDoseRowsForDate(cursor);
            if (rows.isEmpty()) break;

            boolean allTaken = rows.stream().allMatch(r -> {
                Boolean status = getStatusFor(dateForLambda, r.schedule());
                return Boolean.TRUE.equals(status);
            });

            if (!allTaken) break;

            streak++;
            cursor = cursor.minusDays(1);
        }

        return streak;
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

            String scheduleInfo = row.schedule().getDay() + " at " + row.schedule().getTime();
            Label subtitle = new Label(scheduleInfo);
            subtitle.getStyleClass().add("dose-subtitle");

            HBox statusRow = new HBox(8);
            statusRow.setAlignment(Pos.CENTER_LEFT);

            ToggleButton takenBtn = new ToggleButton("Taken");
            ToggleButton missedBtn = new ToggleButton("Missed");

            takenBtn.getStyleClass().addAll("dose-status-button", "dose-taken");
            missedBtn.getStyleClass().addAll("dose-status-button", "dose-missed");

            Boolean status = row.status();
            if (Boolean.TRUE.equals(status)) {
                takenBtn.setSelected(true);
            } else if (Boolean.FALSE.equals(status)) {
                missedBtn.setSelected(true);
            }

            takenBtn.setOnAction(e -> {
                missedBtn.setSelected(false);
                setStatusFor(row.date(), row.schedule(), true);
                updateRowBackground(true);
            });

            missedBtn.setOnAction(e -> {
                takenBtn.setSelected(false);
                setStatusFor(row.date(), row.schedule(), false);
                updateRowBackground(false);
            });

            statusRow.getChildren().addAll(takenBtn, missedBtn);

            VBox box = new VBox(4, title, subtitle, statusRow);
            box.setPadding(new Insets(10));
            box.getStyleClass().add("dose-card");

            updateRowBackground(status);

            setGraphic(box);
        }

        private void updateRowBackground(Boolean status) {
            if (status == null) {
                setStyle("");
            } else if (status) {
                setStyle("-fx-background-color: #dcfce7;");
            } else {
                setStyle("-fx-background-color: #fee2e2;");
            }
        }
    }
}