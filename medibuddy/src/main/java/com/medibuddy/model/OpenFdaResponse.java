package com.medibuddy.model;


import java.util.List;

public class OpenFdaResponse {
    private OpenFdaMeta meta;
    private List<DrugLabelResult> results;

    public OpenFdaMeta getMeta() {
        return meta;
    }

    public void setMeta(OpenFdaMeta meta) {
        this.meta = meta;
    }

    public List<DrugLabelResult> getResults() {
        return results;
    }

    public void setResults(List<DrugLabelResult> results) {
        this.results = results;
    }
}