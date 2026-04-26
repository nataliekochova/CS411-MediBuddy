package com.medibuddy.service;

import com.medibuddy.model.MedicationSchedule;
import com.medibuddy.model.SavedMedication;

import java.util.*;

public class MedicationStore {

    private final int userId;
    private final List<SavedMedication> medications = new ArrayList<>();

    private SavedMedication selectedMedication;

    public MedicationStore(int userId) {
        this.userId = userId;
        loadMedications(); // now does nothing (no DB)
    }

    public int getUserId() {
        return userId;
    }

    public List<SavedMedication> getMedications() {
        return Collections.unmodifiableList(medications);
    }

    public List<SavedMedication> getAllMedications() {
        return medications;
    }

    // -----------------------------
    // MEDICATION MANAGEMENT
    // -----------------------------

    public void addMedication(SavedMedication medication) {
        medications.add(medication);
    }

    public void removeMedication(SavedMedication medication) {
        medications.remove(medication);
    }

    private void loadMedications() {
        // No database — nothing to load
    }

    public void setSelectedMedication(SavedMedication med) {
        this.selectedMedication = med;
    }

    public SavedMedication getSelectedMedication() {
        return selectedMedication;
    }

    // -----------------------------
    // SCHEDULE MANAGEMENT
    // -----------------------------

    public void addScheduleToMedication(SavedMedication med, MedicationSchedule schedule) {
        if (med == null) return;
        med.getSchedules().add(schedule);
    }

    public void removeScheduleFromMedication(SavedMedication med, MedicationSchedule schedule) {
        if (med == null) return;
        med.getSchedules().remove(schedule);
    }
}