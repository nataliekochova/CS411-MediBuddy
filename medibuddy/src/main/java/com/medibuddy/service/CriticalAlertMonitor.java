package com.medibuddy.service;

import com.medibuddy.model.EmergencyContact;
import com.medibuddy.model.MedicationSchedule;
import com.medibuddy.model.SavedMedication;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class CriticalAlertMonitor {

    private final MedicationStore store;
    private final EmailService emailService;
    private final Timeline timeline;

    private final Set<String> currentlySending = ConcurrentHashMap.newKeySet();

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    public CriticalAlertMonitor(MedicationStore store) {
        this.store = store;
        this.emailService = new EmailService();

        this.timeline = new Timeline(
                new KeyFrame(Duration.seconds(60), e -> checkNow())
        );

        this.timeline.setCycleCount(Timeline.INDEFINITE);
    }

    public void start() {
        checkNow();
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }

    private void checkNow() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        String todayShort = today.getDayOfWeek()
                .toString()
                .substring(0, 3);

        for (SavedMedication medication : store.getAllMedications()) {
            if (!isMedicationActiveToday(medication, today)) {
                continue;
            }

            for (MedicationSchedule schedule : medication.getSchedules()) {
                if (!schedule.getDay().equalsIgnoreCase(todayShort)) {
                    continue;
                }

                checkSchedule(today, now, medication, schedule);
            }
        }
    }

    private void checkSchedule(
            LocalDate today,
            LocalTime now,
            SavedMedication medication,
            MedicationSchedule schedule
    ) {
        if (!schedule.isCriticalAlertEnabled()) {
            return;
        }

        Boolean doseStatus = store.getDoseStatus(today, schedule);

        // If user already clicked taken or missed, do not auto-send.
        if (doseStatus != null) {
            return;
        }

        if (store.hasCriticalAlertBeenSent(schedule, today)) {
            return;
        }

        LocalTime scheduledTime;

        try {
            scheduledTime = parseScheduleTime(schedule.getTime());
        } catch (Exception e) {
            System.err.println("Could not parse schedule time: " + schedule.getTime());
            return;
        }

        LocalTime alertTime = scheduledTime.plusMinutes(schedule.getMissedWindowMinutes());

        if (now.isBefore(alertTime)) {
            return;
        }

        EmergencyContact contact = store.getEmergencyContactById(schedule.getEmergencyContactId());

        if (contact == null) {
            System.err.println("Critical Alert enabled but no emergency contact found.");
            return;
        }

        String sendingKey = today + "|" + store.getScheduleId(schedule);

        if (!currentlySending.add(sendingKey)) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendCriticalAlert(
                        contact.getEmail(),
                        medication.getDisplayName(),
                        schedule.getTime()
                );

                store.markCriticalAlertSent(schedule, today);

                System.out.println("Automatic Critical Alert sent to " + contact.getEmail());

            } catch (Exception e) {
                System.err.println("Automatic Critical Alert failed: " + e.getMessage());
                e.printStackTrace();

            } finally {
                currentlySending.remove(sendingKey);
            }
        });
    }

    private boolean isMedicationActiveToday(SavedMedication medication, LocalDate today) {
        if (medication.getStartDate() != null && today.isBefore(medication.getStartDate())) {
            return false;
        }

        if (medication.getEndDate() != null && today.isAfter(medication.getEndDate())) {
            return false;
        }

        return true;
    }

    private LocalTime parseScheduleTime(String time) {
        String cleaned = time == null ? "" : time.trim().toUpperCase();

        cleaned = cleaned.replaceAll("\\s+", " ");

        // Converts 08:00AM into 08:00 AM
        cleaned = cleaned.replaceAll("([0-9])([AP]M)$", "$1 $2");

        return LocalTime.parse(cleaned, TIME_FORMAT);
    }
}