package com.medibuddy.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.medibuddy.model.MedicationSchedule;
import com.medibuddy.model.SavedMedication;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "MediBuddy";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(CalendarScopes.CALENDAR_EVENTS);
    private static final String CREDENTIALS_FILE = "/credentials.json";

    private final MedicationStore store;
    private final ZoneId zoneId;

    public GoogleCalendarService(MedicationStore store) {
        this.store = store;
        this.zoneId = ZoneId.systemDefault();
    }

    public int syncNextSevenDays() throws Exception {
        Calendar service = getCalendarService();
        int createdCount = 0;

        LocalDate today = LocalDate.now();
        LocalDate lastDay = today.plusDays(6);

        for (SavedMedication med : store.getAllMedications()) {
            for (MedicationSchedule schedule : med.getSchedules()) {
                for (LocalDate date = today; !date.isAfter(lastDay); date = date.plusDays(1)) {
                    if (!scheduleMatchesDate(med, schedule, date)) {
                        continue;
                    }

                    Event event = buildMedicationEvent(med, schedule, date);
                    service.events().insert("primary", event).execute();
                    createdCount++;
                }
            }
        }

        return createdCount;
    }

    private Calendar getCalendarService() throws Exception {
        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = getCredentials(httpTransport);

        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(NetHttpTransport httpTransport) throws Exception {
        InputStream inputStream = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE);

        if (inputStream == null) {
            throw new IllegalStateException(
                    "Missing credentials.json. Add your Google OAuth Desktop credentials file to src/main/resources/credentials.json"
            );
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                JSON_FACTORY,
                new InputStreamReader(inputStream)
        );

        Path tokenDirectory = Path.of("tokens", "google-calendar");
        Files.createDirectories(tokenDirectory);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        )
                .setDataStoreFactory(new FileDataStoreFactory(tokenDirectory.toFile()))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private boolean scheduleMatchesDate(SavedMedication med, MedicationSchedule schedule, LocalDate date) {
        LocalDate start = med.getStartDate();
        LocalDate end = med.getEndDate();

        if (start != null && date.isBefore(start)) {
            return false;
        }

        if (end != null && date.isAfter(end)) {
            return false;
        }

        String dayShort = date.getDayOfWeek().toString().substring(0, 3);
        return schedule.getDay().equalsIgnoreCase(dayShort);
    }

    private Event buildMedicationEvent(SavedMedication med, MedicationSchedule schedule, LocalDate date) {
        LocalTime time = parseScheduleTime(schedule.getTime());

        LocalDateTime startDateTime = LocalDateTime.of(date, time);
        LocalDateTime endDateTime = startDateTime.plusMinutes(15);

        ZonedDateTime zonedStart = startDateTime.atZone(zoneId);
        ZonedDateTime zonedEnd = endDateTime.atZone(zoneId);

        Event event = new Event()
                .setSummary("Take " + med.getDisplayName())
                .setDescription("MediBuddy reminder: " + med.getDoseAndFormDisplay());

        EventDateTime start = new EventDateTime()
                .setDateTime(new DateTime(zonedStart.toInstant().toEpochMilli()))
                .setTimeZone(zoneId.toString());

        EventDateTime end = new EventDateTime()
                .setDateTime(new DateTime(zonedEnd.toInstant().toEpochMilli()))
                .setTimeZone(zoneId.toString());

        event.setStart(start);
        event.setEnd(end);

        return event;
    }

    private LocalTime parseScheduleTime(String timeText) {
        String cleaned = timeText == null ? "" : timeText.trim().toUpperCase();

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("h:mm a"),
                DateTimeFormatter.ofPattern("hh:mm a"),
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("HH:mm")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(cleaned, formatter);
            } catch (Exception ignored) {
            }
        }

        throw new IllegalArgumentException("Invalid schedule time: " + timeText + ". Use a format like 08:00 AM.");
    }
}