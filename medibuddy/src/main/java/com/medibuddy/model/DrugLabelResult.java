package com.medibuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DrugLabelResult {
    private List<String> drug_interactions;
    private List<String> warnings;
    private List<String> warnings_and_cautions;
    private List<String> contraindications;
    private List<String> ask_doctor;
    private List<String> ask_doctor_or_pharmacist;
    private OpenFdaMetadata openfda;

    public List<String> getDrug_interactions() {
        return drug_interactions;
    }

    public void setDrug_interactions(List<String> drug_interactions) {
        this.drug_interactions = drug_interactions;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<String> getWarnings_and_cautions() {
        return warnings_and_cautions;
    }

    public void setWarnings_and_cautions(List<String> warnings_and_cautions) {
        this.warnings_and_cautions = warnings_and_cautions;
    }

    public List<String> getContraindications() {
        return contraindications;
    }

    public void setContraindications(List<String> contraindications) {
        this.contraindications = contraindications;
    }

    public List<String> getAsk_doctor() {
        return ask_doctor;
    }

    public void setAsk_doctor(List<String> ask_doctor) {
        this.ask_doctor = ask_doctor;
    }

    public List<String> getAsk_doctor_or_pharmacist() {
        return ask_doctor_or_pharmacist;
    }

    public void setAsk_doctor_or_pharmacist(List<String> ask_doctor_or_pharmacist) {
        this.ask_doctor_or_pharmacist = ask_doctor_or_pharmacist;
    }

    public OpenFdaMetadata getOpenfda() {
        return openfda;
    }

    public void setOpenfda(OpenFdaMetadata openfda) {
        this.openfda = openfda;
    }
}