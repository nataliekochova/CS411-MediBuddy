package com.medibuddy.ui;

import com.medibuddy.App;
import com.medibuddy.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;

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

    // Title
    Label title = new Label("Create a new account");
    title.getStyleClass().add("title-large");

    // Username label + field
    Label usernameLabel = new Label("Username");
    usernameLabel.getStyleClass().add("field-label");

    TextField usernameInput = new TextField();
    usernameInput.getStyleClass().add("blue-input");

    // Password label + field
    Label passwordLabel = new Label("Password");
    passwordLabel.getStyleClass().add("field-label");

    PasswordField passwordInput = new PasswordField();
    passwordInput.getStyleClass().add("blue-input");

    // Repeat password label + field
    Label repeatLabel = new Label("Repeat password");
    repeatLabel.getStyleClass().add("field-label");

    PasswordField repeatInput = new PasswordField();
    repeatInput.getStyleClass().add("blue-input");

    // Remember me checkbox
    CheckBox rememberMe = new CheckBox("Remember me on this device");
    rememberMe.getStyleClass().add("remember-checkbox");

    // Back button (small)
    Button backButton = new Button("Back");
    backButton.getStyleClass().add("secondary-small-button");
    backButton.setMaxWidth(140);

    backButton.setOnAction(e -> app.showLoginPage());

    // Main button
    Button createButton = new Button("Take me to the app →");
    createButton.getStyleClass().add("primary-wide-button");
    createButton.setMaxWidth(Double.MAX_VALUE);

    // Message label
    Label message = new Label();
    message.getStyleClass().add("results-text");

    // Bottom logo + text
    ImageView logoIcon = new ImageView(
            new Image(getClass().getResourceAsStream("/icons/logo.png"))
    );
    logoIcon.setFitWidth(35);
    logoIcon.setPreserveRatio(true);

    Label logoText = new Label("MediBuddy");
    logoText.getStyleClass().add("logo-text");

    HBox logoRow = new HBox(8, logoIcon, logoText);
    logoRow.setAlignment(Pos.CENTER);

    // Logic
    createButton.setOnAction(e -> {
        String username = usernameInput.getText().trim();
        String password = passwordInput.getText();
        String repeat = repeatInput.getText();

        if (username.isEmpty() || password.isEmpty() || repeat.isEmpty()) {
            message.setText("Please fill in all fields.");
            return;
        }

        if (!password.equals(repeat)) {
            message.setText("Passwords do not match.");
            return;
        }

        boolean success = authService.createAccount(username, password);

        if (success) {
            app.showMainApp(0, username);
        } else {
            message.setText("Username already exists.");
        }
    });

    VBox layout = new VBox(14,
            title,
            usernameLabel, usernameInput,
            passwordLabel, passwordInput,
            repeatLabel, repeatInput,
            rememberMe,
            backButton,          // ← INSERTED HERE
            createButton,
            message,
            logoRow
    );

    layout.setPadding(new Insets(24));
    layout.setAlignment(Pos.CENTER);

    return layout;
}
}