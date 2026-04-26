package com.medibuddy.ui;

import com.medibuddy.App;
import com.medibuddy.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class LoginPage {

    private final App app;
    private final AuthService authService = new AuthService();
    private final VBox root;

    public LoginPage(App app) {
        this.app = app;
        this.root = build();
    }

    public Parent getView() {
        return root;
    }

    private VBox build() {
        Label title = new Label("MediBuddy");
        title.getStyleClass().add("title");

        Label subtitle = new Label("Login");
        subtitle.getStyleClass().add("subtitle");

        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Username");
        usernameInput.getStyleClass().add("text-field");

        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Password");
        passwordInput.getStyleClass().add("text-field");

        Label message = new Label();
        message.getStyleClass().add("results-text");

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("button");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        Button createAccountButton = new Button("Create Account");
        createAccountButton.getStyleClass().add("button");
        createAccountButton.setMaxWidth(Double.MAX_VALUE);

        loginButton.setOnAction(e -> {
            String username = usernameInput.getText().trim();
            String password = passwordInput.getText();

            Integer userId = authService.login(username, password);

            if (userId != null) {
                app.showMainApp(userId, username);
            } else {
                message.setText("Invalid username or password.");
            }
        });

        createAccountButton.setOnAction(e -> {
            app.showLoginPage(); // temporary, replaced below
            app.showCreateAccountPage();
        });

        VBox layout = new VBox(12,
                title,
                subtitle,
                usernameInput,
                passwordInput,
                loginButton,
                createAccountButton,
                message
        );

        layout.setPadding(new Insets(18));

        return layout;
    }
}
