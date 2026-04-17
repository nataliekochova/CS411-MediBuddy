package com.medibuddy.model;

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
            return dose + " • " + form;
        }
        if (!dose.isBlank()) {
            return dose;
        }
        if (!form.isBlank()) {
            return form;
        }
        return "No dose/form entered";
    }
}