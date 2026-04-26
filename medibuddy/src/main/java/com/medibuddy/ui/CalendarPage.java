package com.medibuddy.ui;

import com.medibuddy.model.Dose;
import com.medibuddy.model.MedicationSchedule;
import com.medibuddy.model.SavedMedication;
import com.medibuddy.service.MedicationStore;

import javafx.scene.Parent;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class CalendarPage {

    private final MedicationStore store;
    private final VBox root;

    public CalendarPage(MedicationStore store) {
        this.store = store;
        this.root = new VBox(10);

        DatePicker picker = new DatePicker(LocalDate.now());
        ListView<String> doseList = new ListView<>();

        picker.setOnAction(e -> {
            LocalDate date = picker.getValue();
            doseList.getItems().setAll(getDosesFor(date));
        });

        root.getChildren().addAll(picker, doseList);
    }

   private List<String> getDosesFor(LocalDate date) {
    List<String> result = new ArrayList<>();

    // Convert LocalDate → "Mon", "Tue", etc.
    String dayShort = date.getDayOfWeek().toString().substring(0, 3);

    for (SavedMedication med : store.getAllMedications()) {
        for (MedicationSchedule sched : med.getSchedules()) {

            // Only show schedules that match the selected day
            if (sched.getDay().equalsIgnoreCase(dayShort)) {
                result.add(med.getDisplayName() + " at " + sched.getTime());
            }
        }
    }

    return result;
}

    public Parent getView() {
        return root;
    }
}
