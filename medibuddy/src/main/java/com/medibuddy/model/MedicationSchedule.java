package com.medibuddy.model;

public class MedicationSchedule {

    private final String day;   // "Mon", "Tue", ...
    private final String time;  // "08:00 AM"

    public MedicationSchedule(String day, String time) {
        this.day = day;
        this.time = time;
    }

    public String getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }
}