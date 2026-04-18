package com.medibuddy.model;

import java.util.ArrayList;
import java.util.List;

public class SavedMedication {
    private final String brandName;
    private final String genericName;
    private final String manufacturer;
    private final String purpose;
    private final String indications;
    private final String warnings;
    private final String dosage;
    private List<MedicationSchedule> schedules = new ArrayList<>();

    public SavedMedication(
            String brandName,
            String genericName,
            String manufacturer,
            String purpose,
            String indications,
            String warnings,
            String dosage
    ) {
        this.brandName = brandName;
        this.genericName = genericName;
        this.manufacturer = manufacturer;
        this.purpose = purpose;
        this.indications = indications;
        this.warnings = warnings;
        this.dosage = dosage;
    }

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

    public String getDosage() {
        return dosage;
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
