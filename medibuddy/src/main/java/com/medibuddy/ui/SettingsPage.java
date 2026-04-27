package com.medibuddy.ui;

import com.medibuddy.service.EmailService;
import com.medibuddy.service.PdfExportService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import com.medibuddy.model.EmergencyContact;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;

public class SettingsPage {
    private final VBox root;
    private final PdfExportService pdfExportService = new PdfExportService();

    public SettingsPage(AppShell shell) {
        root = new VBox(12);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("page-root");

        Label title = new Label("Settings");
        title.getStyleClass().add("title");

        Label body = new Label("Account settings");
        body.getStyleClass().add("results-text");

        VBox appearanceCard = new VBox(8);
        appearanceCard.getStyleClass().addAll("card", "settings-section");

        Label appearanceTitle = new Label("Appearance");
        appearanceTitle.getStyleClass().add("section-title");

        Label appearanceDescription = new Label("Switch the app between light and dark mode.");
        appearanceDescription.getStyleClass().addAll("results-text", "settings-description");

        CheckBox darkModeToggle = new CheckBox("Enable dark mode");
        darkModeToggle.setSelected(shell.isDarkModeEnabled());
        darkModeToggle.selectedProperty().addListener((obs, wasSelected, isSelected) ->
                shell.setDarkModeEnabled(isSelected));

        HBox toggleRow = new HBox(darkModeToggle);
        toggleRow.setAlignment(Pos.CENTER_LEFT);
        toggleRow.getStyleClass().add("settings-toggle-row");
        HBox.setHgrow(darkModeToggle, Priority.ALWAYS);

        appearanceCard.getChildren().addAll(appearanceTitle, appearanceDescription, toggleRow);

        VBox emergencyCard = new VBox(8);
        emergencyCard.getStyleClass().addAll("card", "settings-section");

        Label emergencyTitle = new Label("Emergency Contact");
        emergencyTitle.getStyleClass().add("section-title");

        TextField contactNameField = new TextField();
        contactNameField.setPromptText("Contact name");

        TextField contactEmailField = new TextField();
        contactEmailField.setPromptText("Contact email");

        ListView<EmergencyContact> contactListView = new ListView<>();
        contactListView.setPrefHeight(140);
        contactListView.getItems().setAll(shell.getStore().getEmergencyContacts());

        Button saveContactButton = new Button("Save Emergency Contact");
        saveContactButton.getStyleClass().add("button");
        saveContactButton.setMaxWidth(Double.MAX_VALUE);

        saveContactButton.setOnAction(e -> {
            String name = contactNameField.getText() == null ? "" : contactNameField.getText().trim();
            String email = contactEmailField.getText() == null ? "" : contactEmailField.getText().trim();

            if (name.isBlank()) {
                showAlert(Alert.AlertType.ERROR, "Missing Name", "Please enter the contact name.");
                return;
            }

            if (email.isBlank()) {
                showAlert(Alert.AlertType.ERROR, "Missing Email", "Please enter the contact email.");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                showAlert(Alert.AlertType.ERROR, "Invalid Email", "Please enter a valid email.");
                return;
            }

            shell.getStore().addEmergencyContact(name, email);
            contactNameField.clear();
            contactEmailField.clear();
            contactListView.getItems().setAll(shell.getStore().getEmergencyContacts());

            showAlert(Alert.AlertType.INFORMATION, "Saved", "Emergency contact saved.");
        });

        Button deleteContactButton = new Button("Delete Selected Contact");
        contactListView.getSelectionModel().selectedItemProperty().addListener((obs, oldContact, selectedContact) -> {
            if (selectedContact == null) {
                deleteContactButton.setText("Delete Selected Contact");
            } else {
                deleteContactButton.setText("Delete " + selectedContact.getName());
            }
        });
        deleteContactButton.getStyleClass().add("danger-button");
        deleteContactButton.setMaxWidth(Double.MAX_VALUE);

        deleteContactButton.setOnAction(e -> {
            EmergencyContact selectedContact = contactListView.getSelectionModel().getSelectedItem();

            if (selectedContact == null) {
                showAlert(Alert.AlertType.ERROR,
                        "No Contact Selected",
                        "Please select a contact to delete.");
                return;
            }

            shell.getStore().deleteEmergencyContact(selectedContact.getId());
            contactListView.getItems().setAll(shell.getStore().getEmergencyContacts());

            showAlert(Alert.AlertType.INFORMATION,
                    "Contact Deleted",
                    "Emergency contact deleted.");
        });

        emergencyCard.getChildren().addAll(
                emergencyTitle,
                contactNameField,
                contactEmailField,
                saveContactButton,
                contactListView,
                deleteContactButton
        );

        Button exportButton = new Button("Export My Data as PDF");
        exportButton.getStyleClass().add("button");
        exportButton.setMaxWidth(Double.MAX_VALUE);
        exportButton.setOnAction(e -> exportPdf(shell));


        Button testEmailButton = new Button("Send Test Email");
        testEmailButton.getStyleClass().add("button");
        testEmailButton.setMaxWidth(Double.MAX_VALUE);

        testEmailButton.setOnAction(e -> {
            try {
                EmailService emailService = new EmailService();
                emailService.sendTestEmail("modyadnan05@gmail.com");

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Email Sent");
                alert.setHeaderText(null);
                alert.setContentText("Test email sent successfully.");
                alert.showAndWait();

            } catch (Exception ex) {
                ex.printStackTrace();

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Email Failed");
                alert.setHeaderText(null);
                alert.setContentText("Email failed: " + ex.getMessage());
                alert.showAndWait();
            }
        });
        Button logoutButton = new Button("Log Out");
        logoutButton.getStyleClass().add("danger-button");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setOnAction(e -> shell.logout());

        root.getChildren().addAll(title, body, appearanceCard, emergencyCard, exportButton, logoutButton);
    }

    public Parent getView() {
        return root;
    }

    private void exportPdf(AppShell shell) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export MediBuddy PDF");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        chooser.setInitialFileName(buildDefaultFileName(shell.getUsername()));

        File outputFile = chooser.showSaveDialog(root.getScene().getWindow());
        if (outputFile == null) {
            return;
        }

        if (!outputFile.getName().toLowerCase().endsWith(".pdf")) {
            outputFile = new File(outputFile.getParentFile(), outputFile.getName() + ".pdf");
        }

        try {
            pdfExportService.exportUserMedicationReport(
                    shell.getUsername(),
                    shell.getStore().getMedications(),
                    outputFile.toPath()
            );
            showAlert(Alert.AlertType.INFORMATION, "Export Complete",
                    "Your medication report was saved to:\n" + outputFile.getAbsolutePath());
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Export Failed",
                    "MediBuddy could not create the PDF.\n" + ex.getMessage());
        }
    }

    private String buildDefaultFileName(String username) {
        String safeUsername = (username == null || username.isBlank())
                ? "medibuddy-user"
                : username.trim().replaceAll("[^a-zA-Z0-9-_]", "_");
        return safeUsername + "-medibuddy-report.pdf";
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
