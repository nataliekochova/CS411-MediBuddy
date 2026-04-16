package com.medibuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenFdaMetadata {
    private List<String> brand_name;
    private List<String> generic_name;
    private List<String> manufacturer_name;

    public List<String> getBrand_name() { return brand_name; }
    public void setBrand_name(List<String> brand_name) { this.brand_name = brand_name; }

    public List<String> getGeneric_name() { return generic_name; }
    public void setGeneric_name(List<String> generic_name) { this.generic_name = generic_name; }

    public List<String> getManufacturer_name() { return manufacturer_name; }
    public void setManufacturer_name(List<String> manufacturer_name) { this.manufacturer_name = manufacturer_name; }
}