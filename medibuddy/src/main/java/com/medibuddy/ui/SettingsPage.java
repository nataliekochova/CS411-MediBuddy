package com.medibuddy.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SettingsPage {
    private final VBox root;

    public SettingsPage(AppShell shell) {
        root = new VBox(12);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("page-root");

        Label title = new Label("Settings");
        title.getStyleClass().add("title");

        Label body = new Label("Account settings");
        body.getStyleClass().add("results-text");

        Button logoutButton = new Button("Log Out");
        logoutButton.getStyleClass().add("danger-button");
        logoutButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setOnAction(e -> shell.logout());

        root.getChildren().addAll(title, body, logoutButton);
    }

    public Parent getView() {
        return root;
    }
}