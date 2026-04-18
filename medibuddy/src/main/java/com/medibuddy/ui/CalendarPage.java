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

        for (SavedMedication med : store.getAllMedications()) {
            for (MedicationSchedule sched : med.getSchedules()) {

                // Generate doses based on frequency
                for (int i = 0; i < sched.getFrequencyPerDay(); i++) {
                    LocalTime t = LocalTime.parse(sched.getTime()); // simple version
                    result.add(med.getName() + " at " + t.toString());
                }
            }
        }

        return result;
    }

    public Parent getView() {
        return root;
    }
}
