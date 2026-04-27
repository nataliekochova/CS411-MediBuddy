package com.medibuddy.ui;

import com.medibuddy.App;
import com.medibuddy.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

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
        VBox layout = new VBox(20);
    layout.setPadding(new Insets(40, 30, 40, 30));
    layout.setAlignment(Pos.TOP_CENTER);
    layout.getStyleClass().add("login-root");
    

    // Logo image
    ImageView logo = new ImageView(
            new Image(getClass().getResourceAsStream("/icons/logo.png"))
    );
    logo.setFitWidth(250);
    logo.setPreserveRatio(true);

    // Friendly welcome text
    Label hello = new Label("Hello there,");
    hello.getStyleClass().add("login-hello");

    Label welcome = new Label("Welcome to MediBuddy, your personal prescription manager.");
    welcome.getStyleClass().add("login-welcome");
    welcome.setWrapText(true);

    // Form fields

    
    Label usernameLabel = new Label("Username");
usernameLabel.getStyleClass().add("login-field-label");

TextField usernameInput = new TextField();
usernameInput.getStyleClass().add("login-text-field");

Label passwordLabel = new Label("Password");
passwordLabel.getStyleClass().add("login-field-label");

PasswordField passwordInput = new PasswordField();
passwordInput.getStyleClass().add("login-text-field");

    

    Label message = new Label();
    message.getStyleClass().add("results-text");

    // Buttons
    Button loginButton = new Button("Login");
    loginButton.getStyleClass().add("login-primary-button");
    loginButton.setMaxWidth(Double.MAX_VALUE);

    Button createAccountButton = new Button("Create Account");
    createAccountButton.getStyleClass().add("login-secondary-button");
    createAccountButton.setMaxWidth(Double.MAX_VALUE);

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

    createAccountButton.setOnAction(e -> app.showCreateAccountPage());

    layout.getChildren().addAll(
            logo,
            hello,
            welcome,
            usernameLabel,
            usernameInput,
            passwordLabel,
            passwordInput,
            loginButton,
            createAccountButton,
            message
    );

    return layout;
}}