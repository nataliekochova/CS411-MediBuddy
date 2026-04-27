package com.medibuddy.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SavedMedication {

    private final String brandName;
    private final String genericName;
    private final String manufacturer;
    private final String purpose;
    private final String indications;
    private final String warnings;
    private final String labelDosage;
    private final String userDose;
    private final String userForm;

    private LocalDate startDate;   
    private LocalDate endDate;     

    private final List<MedicationSchedule> schedules = new ArrayList<>();

    public SavedMedication(
            String brandName,
            String genericName,
            String manufacturer,
            String purpose,
            String indications,
            String warnings,
            String labelDosage,
            String userDose,
            String userForm
    ) {
        this.brandName = brandName;
        this.genericName = genericName;
        this.manufacturer = manufacturer;
        this.purpose = purpose;
        this.indications = indications;
        this.warnings = warnings;
        this.labelDosage = labelDosage;
        this.userDose = userDose;
        this.userForm = userForm;
    }

    // ---------------------------
    // NEW GETTERS + SETTERS
    // ---------------------------

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // ---------------------------
    // EXISTING GETTERS
    // ---------------------------

    public String getBrandName() {
        return brandName;
    }

    public String getGenericName() {
        return genericName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getIndications() {
        return indications;
    }

    public String getWarnings() {
        return warnings;
    }

    public String getLabelDosage() {
        return labelDosage;
    }

    public String getUserDose() {
        return userDose;
    }

    public String getUserForm() {
        return userForm;
    }

    public String getDisplayName() {
        if (brandName != null && !brandName.isBlank() && !"N/A".equals(brandName)) {
            return brandName;
        }
        if (genericName != null && !genericName.isBlank()) {
            return genericName;
        }
        return "Unknown Medication";
    }

    public String getDoseAndFormDisplay() {
        String dose = userDose == null ? "" : userDose.trim();
        String form = userForm == null ? "" : userForm.trim();

        if (!dose.isBlank() && !form.isBlank()) {
            return dose + " / " + form;
        }
        if (!dose.isBlank()) {
            return dose;
        }
        if (!form.isBlank()) {
            return form;
        }
        return "No dose/form entered";
    }

    // ---------------------------
    // SCHEDULE MANAGEMENT
    // ---------------------------

    public void addSchedule(MedicationSchedule schedule) {
        schedules.add(schedule);
    }

    public List<MedicationSchedule> getSchedules() {
        return schedules;
    }

    public String getName() {
        return getDisplayName();
    }
}