package com.medibuddy;

import com.medibuddy.client.OpenFdaClient;
import com.medibuddy.service.MedicationStore;
import com.medibuddy.ui.AppShell;
import com.medibuddy.ui.LoginPage;
import com.medibuddy.ui.CreateAccountPage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        showLoginPage();

        stage.setTitle("MediBuddy");
        stage.setMinWidth(360);
        stage.setMinHeight(620);
        stage.show();
    }

    public void showLoginPage() {
        LoginPage loginPage = new LoginPage(this);

        Scene scene = new Scene(loginPage.getView(), 390, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setScene(scene);
    }

    public void showMainApp(int userId) {
        OpenFdaClient client = new OpenFdaClient();
        MedicationStore store = new MedicationStore(userId);

        AppShell shell = new AppShell(this, client, store);

        Scene scene = new Scene(shell.getView(), 390, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setScene(scene);
    }

    public void showCreateAccountPage() {
        CreateAccountPage page = new CreateAccountPage(this);

        Scene scene = new Scene(page.getView(), 390, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}