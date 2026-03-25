package com.medibuddy.service;

public class MedicationStore {
    private final List<SavedMedication> medications = new ArrayList<>();

    public List<SavedMedication> getMedications() {
        return medications;
    }

    public void addMedication(SavedMedication medication) {
        medications.add(medication);
    }
}
