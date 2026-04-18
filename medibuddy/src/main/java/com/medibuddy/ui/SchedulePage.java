package com.medibuddy.ui;

import com.medibuddy.model.MedicationSchedule;
import com.medibuddy.model.SavedMedication;
import com.medibuddy.service.MedicationStore;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class SchedulePage {

    private final MedicationStore store;
    private final VBox root;

    public SchedulePage(MedicationStore store) {
        this.store = store;

        root = new VBox(12);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("page-root");

        Label title = new Label("Today's Schedule");
        title.getStyleClass().add("title");

        ListView<String> doseList = new ListView<>();

        // Load today's doses
        LocalDate today = LocalDate.now();
        updateDoseListForDate(today, doseList);

        root.getChildren().addAll(title, doseList);
    }

    public Parent getView() {
        return root;
    }

    private void updateDoseListForDate(LocalDate date, ListView<String> list) {
        list.getItems().clear();

        for (SavedMedication med : store.getAllMedications()) {
            for (MedicationSchedule sched : med.getSchedules()) {

                // Only daily for now — weekly/monthly later
                if ("daily".equals(sched.getFrequencyType())) {
                    list.getItems().add(
                        med.getDisplayName() + " at " + sched.getTime() + " (" + sched.getFrequencyPerDay() + "x)"
                    );
                }
            }
        }
    }
}
