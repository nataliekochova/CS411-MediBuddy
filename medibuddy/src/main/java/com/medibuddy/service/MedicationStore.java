package com.medibuddy.service;

import com.medibuddy.model.SavedMedication;
import com.medibuddy.model.MedicationSchedule;
import java.util.List;
import java.util.ArrayList;
import com.medibuddy.model.MedicationSchedule;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedicationStore {
    
    private final List<SavedMedication> medications = new ArrayList<>();

    // The medication currently selected in the UI
    private SavedMedication selectedMedication;

    // Return an unmodifiable list (for UI display)
    public List<SavedMedication> getMedications() {
        return Collections.unmodifiableList(medications);
    }

    // Return the actual list (for CalendarPage)
    public List<SavedMedication> getAllMedications() {
        return medications;
    }

    public void addMedication(SavedMedication medication) {
        medications.add(medication);
    }

    public void removeMedication(SavedMedication medication) {
        medications.remove(medication);
    }

    private final List<MedicationSchedule> schedules = new ArrayList<>();

    public void setSelectedMedication(SavedMedication med) {
        this.selectedMedication = med;
    }

    public SavedMedication getSelectedMedication() {
        return this.selectedMedication;
    }

public void addScheduleToMedication(SavedMedication med, MedicationSchedule schedule) {
        if (med != null) {
            med.getSchedules().add(schedule);
        }
    }

    public void removeScheduleFromMedication(SavedMedication med, MedicationSchedule schedule) {
        if (med != null) {
            med.getSchedules().remove(schedule);
        }
    }
}



