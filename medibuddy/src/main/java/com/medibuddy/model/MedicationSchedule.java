package com.medibuddy.model;

public class MedicationSchedule {

    private final String day;   // "Mon", "Tue", ...
    private final String time;  // "08:00 AM"

    private final boolean criticalAlertEnabled;
    private final Integer emergencyContactId;
    private final int missedWindowMinutes;

    public MedicationSchedule(String day, String time) {
        this(day, time, false, null, 30);
    }

    public MedicationSchedule(
            String day,
            String time,
            boolean criticalAlertEnabled,
            Integer emergencyContactId,
            int missedWindowMinutes
    ) {
        this.day = day;
        this.time = time;
        this.criticalAlertEnabled = criticalAlertEnabled;
        this.emergencyContactId = emergencyContactId;
        this.missedWindowMinutes = missedWindowMinutes;
    }

    public String getDay() {
        return day;
    }

    public String getTime() {
        return time;
    }

    public boolean isCriticalAlertEnabled() {
        return criticalAlertEnabled;
    }

    public Integer getEmergencyContactId() {
        return emergencyContactId;
    }

    public int getMissedWindowMinutes() {
        return missedWindowMinutes;
    }
}