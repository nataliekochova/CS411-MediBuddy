package com.medibuddy.ui;

import com.medibuddy.App;
import com.medibuddy.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class CreateAccountPage {

    private final App app;
    private final AuthService authService = new AuthService();
    private final VBox root;

    public CreateAccountPage(App app) {
        this.app = app;
        this.root = build();
    }

    public Parent getView() {
        return root;
    }

    private VBox build() {
        Label title = new Label("Create Account");
        title.getStyleClass().add("title");

        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Username");
        usernameInput.getStyleClass().add("text-field");

        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Password");
        passwordInput.getStyleClass().add("text-field");

        Label message = new Label();
        message.getStyleClass().add("results-text");

        Button createButton = new Button("Create Account");
        createButton.getStyleClass().add("button");
        createButton.setMaxWidth(Double.MAX_VALUE);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button");
        backButton.setMaxWidth(Double.MAX_VALUE);

        createButton.setOnAction(e -> {
            String username = usernameInput.getText().trim();
            String password = passwordInput.getText();

            if (username.isEmpty() || password.isEmpty()) {
                message.setText("Please fill in all fields.");
                return;
            }

            boolean success = authService.createAccount(username, password);

            if (success) {
                message.setText("Account created! You can now log in.");
            } else {
                message.setText("Username already exists.");
            }
        });

        backButton.setOnAction(e -> app.showLoginPage());

        VBox layout = new VBox(12,
                title,
                usernameInput,
                passwordInput,
                createButton,
                backButton,
                message
        );

        layout.setPadding(new Insets(18));

        return layout;
    }
}