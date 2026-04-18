package com.medibuddy.model;


public class MedicationSchedule {
    private final String time;        // "08:00"
    private final int frequencyPerDay;
    private final String frequencyType; // "daily", "weekly", "monthly"

    public MedicationSchedule(String time, int frequencyPerDay, String frequencyType) {
        this.time = time;
        this.frequencyPerDay = frequencyPerDay;
        this.frequencyType = frequencyType;
    }

    public String getTime() { return time; }
    public int getFrequencyPerDay() { return frequencyPerDay; }
    public String getFrequencyType() { return frequencyType; }
}
