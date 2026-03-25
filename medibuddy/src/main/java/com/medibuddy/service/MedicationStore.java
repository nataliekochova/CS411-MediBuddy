package com.medibuddy.service;

import com.medibuddy.model.SavedMedication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MedicationStore {
    private final List<SavedMedication> medications = new ArrayList<>();

    public List<SavedMedication> getMedications() {
        return Collections.unmodifiableList(medications);
    }

    public void addMedication(SavedMedication medication) {
        medications.add(medication);
    }
}