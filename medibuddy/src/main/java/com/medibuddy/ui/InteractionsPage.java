package com.medibuddy.ui;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class InteractionsPage {
    private final VBox root;

    public InteractionsPage() {
        root = new VBox(12);
        root.setPadding(new Insets(18));
        root.getStyleClass().add("page-root");

        Label title = new Label("Interactions");
        title.getStyleClass().add("title");

        Label body = new Label("Interaction warnings page placeholder");
        body.getStyleClass().add("results-text");

        root.getChildren().addAll(title, body);
    }

    public Parent getView() {
        return root;
    }
}