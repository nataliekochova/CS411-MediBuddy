package com.medibuddy.model;


import java.util.List;

public class OpenFdaResponse {
    private OpenFdaMetadata meta;
    private List<DrugLabelResult> results;

    public OpenFdaMetadata getMeta() {
        return meta;
    }

    public void setMeta(OpenFdaMetadata meta) {
        this.meta = meta;
    }

    public List<DrugLabelResult> getResults() {
        return results;
    }

    public void setResults(List<DrugLabelResult> results) {
        this.results = results;
    }
}