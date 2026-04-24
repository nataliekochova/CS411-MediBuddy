package com.medibuddy;

import com.medibuddy.client.OpenFdaClient;
import com.medibuddy.service.MedicationStore;
import com.medibuddy.ui.AppShell;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        OpenFdaClient client = new OpenFdaClient();
        MedicationStore store = new MedicationStore();

        AppShell shell = new AppShell(client, store);

         //
         //   getClass().getResourceAsStream("/fonts/Inter.ttf"),
         //   14

         ;

        Scene scene = new Scene(shell.getView(), 390, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("MediBuddy");
        stage.setScene(scene);
        stage.setMinWidth(360);
        stage.setMinHeight(620);
        stage.show();
        System.out.println(Font.getFamilies());
    }

    public static void main(String[] args) {
        launch();
    }
}