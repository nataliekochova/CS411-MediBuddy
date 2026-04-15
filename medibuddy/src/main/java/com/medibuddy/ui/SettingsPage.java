package com.medibuddy.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class SettingsPage {
    private final VBox root;

    public SettingsPage() {
        root = new VBox(12);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("page-root");

        Label title = new Label("Settings");
        title.getStyleClass().add("title");

        Label body = new Label("Settings page placeholder");
        body.getStyleClass().add("results-text");

        root.getChildren().addAll(title, body);
    }

    public Parent getView() {
        return root;
    }
}