package com.medibuddy.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Dose {
    private SavedMedication medication;
    private LocalDate date;
    private LocalTime time;

    public Dose(SavedMedication medication, LocalDate date, LocalTime time) {
        this.medication = medication;
        this.date = date;
        this.time = time;
    }

    public SavedMedication getMedication() { return medication; }
    public LocalDate getDate() { return date; }
    public LocalTime getTime() { return time; }
}