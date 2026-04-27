package com.medibuddy.ui;

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

        Button exportButton = new Button("Export My Data as PDF");
        exportButton.getStyleClass().add("button");
        exportButton.setMaxWidth(Double.MAX_VALUE);
        exportButton.setOnAction(e -> exportPdf(shell));

        Button logoutButton = new Button("Log Out");
        logoutButton.getStyleClass().add("danger-button");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setOnAction(e -> shell.logout());

        root.getChildren().addAll(title, body, appearanceCard, exportButton, logoutButton);
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
