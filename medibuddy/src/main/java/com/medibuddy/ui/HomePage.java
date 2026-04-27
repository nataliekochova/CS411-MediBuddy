package com.medibuddy.ui;

import com.medibuddy.model.SavedMedication;
import com.medibuddy.service.MedicationStore;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


public class HomePage {

    private final AppShell shell;
    private final MedicationStore store;
    private final VBox root;

    public HomePage(AppShell shell, MedicationStore store) {
        this.shell = shell;
        this.store = store;
        this.root = build();
    }

    public Parent getView() {
        return root;
    }

    private VBox build() {

        // Title
        Label subtitle = new Label("My Medications");
        subtitle.getStyleClass().add("subtitle");

        // Count label
        int medCount = store.getMedications().size();
        Label countLabel = new Label(medCount + " medications tracked");
        countLabel.getStyleClass().add("med-count-label");

        // Medication list container
        VBox medList = new VBox(14);

        if (store.getMedications().isEmpty()) {
            Label empty = new Label("No medications added yet.");
            empty.getStyleClass().add("results-text");
            medList.getChildren().add(empty);
        } else {
            for (SavedMedication med : store.getMedications()) {
                medList.getChildren().add(buildMedicationCard(med));
            }
        }

        // Scrollable list
        ScrollPane scroll = new ScrollPane(medList);
        medList.getStyleClass().clear(); 
        scroll.getStyleClass().clear();   
        scroll.setStyle("-fx-background-color: transparent;");
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Search button at bottom
        Button searchButton = new Button("Search Medications");
        searchButton.getStyleClass().add("button");
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.setOnAction(e -> shell.showSearchPage());

        // Page layout
        VBox content = new VBox(20, subtitle, countLabel, scroll, searchButton);
        content.setPadding(new Insets(18));

        return content;
    }

    // -----------------------------
    // Medication Card Builder
    // -----------------------------
    private VBox buildMedicationCard(SavedMedication med) {

    VBox card = new VBox(6);
    card.getStyleClass().add("med-card");

    Label name = new Label(med.getDisplayName());
    name.getStyleClass().add("med-card-title");

    Label dose = new Label(med.getDoseAndFormDisplay());
    dose.getStyleClass().add("med-card-sub");

    Label schedule = new Label(formatSchedule(med));
    schedule.getStyleClass().add("med-card-sub");

    Label nextDose = new Label("Next dose: " + computeNextDose(med));
    nextDose.getStyleClass().add("med-card-nextdose");

    card.getChildren().addAll(name, dose, schedule, nextDose);

    // ⭐ MAKE THE CARD NAVIGATE TO DETAIL PAGE
    card.setOnMouseClicked(e -> shell.showMedicationDetailPage(med));
    card.setStyle("-fx-cursor: hand;");

    return card;
}



    // -----------------------------
    // Schedule Formatting
    // -----------------------------
    private String formatSchedule(SavedMedication med) {
        if (med.getSchedules().isEmpty()) return "No schedule";

        return med.getSchedules().stream()
                .map(s -> s.getDay() + " — " + s.getTime())
                .collect(Collectors.joining(", "));
    }

    // -----------------------------
    // Next Dose Calculation
    // -----------------------------
    private String computeNextDose(SavedMedication med) {

    LocalDateTime now = LocalDateTime.now();
    List<LocalDateTime> upcoming = new ArrayList<>();

    for (var s : med.getSchedules()) {

        DayOfWeek dow = convertDay(s.getDay());
        LocalDate next = now.toLocalDate().with(TemporalAdjusters.nextOrSame(dow));

        LocalTime time = LocalTime.parse(s.getTime(), TIME_12H);

        LocalDateTime dt = LocalDateTime.of(next, time);

        if (dt.isAfter(now)) {
            upcoming.add(dt);
        }
    }

    if (upcoming.isEmpty()) return "No upcoming doses";

    LocalDateTime nextDose = Collections.min(upcoming);

    return nextDose.format(DateTimeFormatter.ofPattern("EEE MMM d 'at' hh:mm a"));
}

       

    private DayOfWeek convertDay(String shortDay) {
    return switch (shortDay.toLowerCase()) {
        case "mon" -> DayOfWeek.MONDAY;
        case "tue" -> DayOfWeek.TUESDAY;
        case "wed" -> DayOfWeek.WEDNESDAY;
        case "thu" -> DayOfWeek.THURSDAY;
        case "fri" -> DayOfWeek.FRIDAY;
        case "sat" -> DayOfWeek.SATURDAY;
        case "sun" -> DayOfWeek.SUNDAY;
        default -> throw new IllegalArgumentException("Invalid day: " + shortDay);
    };
}

private static final DateTimeFormatter TIME_12H =
        DateTimeFormatter.ofPattern("hh:mm a");
}