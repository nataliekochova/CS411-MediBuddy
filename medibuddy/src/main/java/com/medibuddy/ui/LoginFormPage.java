package com.medibuddy.ui;

import com.medibuddy.App;
import com.medibuddy.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginFormPage {

    private final App app;
    private final AuthService authService = new AuthService();
    private final VBox root;

    public LoginFormPage(App app) {
        this.app = app;
        this.root = build();
    }

    public Parent getView() {
        return root;
    }

    private VBox build() {

        VBox layout = new VBox(16);
        layout.setPadding(new Insets(40, 30, 40, 30));
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("Login");
        title.getStyleClass().add("form-title");

        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Username");
        usernameInput.getStyleClass().add("text-field");

        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Password");
        passwordInput.getStyleClass().add("text-field");

        Label message = new Label();
        message.getStyleClass().add("results-text");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        Button backButton = new Button("Back");
        backButton.getStyleClass().add("button-secondary");
        backButton.setMaxWidth(Double.MAX_VALUE);

        loginButton.setOnAction(e -> {
            String username = usernameInput.getText().trim();
            String password = passwordInput.getText();

            Integer userId = authService.login(username, password);

            if (userId != null) {
                app.showMainApp(userId);
            } else {
                message.setText("Invalid username or password.");
            }
        });

        backButton.setOnAction(e -> app.showLoginPage());

        layout.getChildren().addAll(
                title,
                usernameInput,
                passwordInput,
                loginButton,
                backButton,
                message
        );

        return layout;
    }
}

