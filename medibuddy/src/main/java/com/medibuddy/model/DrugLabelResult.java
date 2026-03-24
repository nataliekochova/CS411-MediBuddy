package com.medibuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DrugLabelResult {
    private Map<String, List<String>> openfda;
    private List<String> indications_and_usage;
    private List<String> warnings;
    private List<String> purpose;
    private List<String> dosage_and_administration;
    private List<String> description;

    public Map<String, List<String>> getOpenfda() {
        return openfda;
    }

    public void setOpenfda(Map<String, List<String>> openfda) {
        this.openfda = openfda;
    }

    public List<String> getIndications_and_usage() {
        return indications_and_usage;
    }

    public void setIndications_and_usage(List<String> indications_and_usage) {
        this.indications_and_usage = indications_and_usage;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<String> getPurpose() {
        return purpose;
    }

    public void setPurpose(List<String> purpose) {
        this.purpose = purpose;
    }

    public List<String> getDosage_and_administration() {
        return dosage_and_administration;
    }

    public void setDosage_and_administration(List<String> dosage_and_administration) {
        this.dosage_and_administration = dosage_and_administration;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description;
    }
}
