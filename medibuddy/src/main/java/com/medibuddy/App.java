package com.medibuddy;

import com.medibuddy.client.OpenFdaClient;
import com.medibuddy.service.MedicationStore;
import com.medibuddy.ui.AppShell;
import com.medibuddy.ui.LoginPage;
import com.medibuddy.ui.CreateAccountPage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.medibuddy.db.Database;

public class App extends Application {

    private Stage primaryStage;
    private double width = 450;
    private double height = 800;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        Database.initialize();

        showLoginPage();

        stage.setTitle("MediBuddy");
        stage.setMinWidth(width);
        stage.setMinHeight(height);
        stage.show();
    }

    public void showLoginPage() {
        LoginPage loginPage = new LoginPage(this);

        Scene scene = new Scene(loginPage.getView(), width, height);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setScene(scene);
    }

    public void showMainApp(int userId) {
        OpenFdaClient client = new OpenFdaClient();
        MedicationStore store = new MedicationStore(userId);

        AppShell shell = new AppShell(this, client, store);

        Scene scene = new Scene(shell.getView(), width, height);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setScene(scene);
    }

    public void showCreateAccountPage() {
        CreateAccountPage page = new CreateAccountPage(this);

        Scene scene = new Scene(page.getView(), width, height);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}