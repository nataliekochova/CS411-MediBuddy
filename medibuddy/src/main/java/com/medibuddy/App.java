package com.medibuddy;

import com.medibuddy.client.OpenFdaClient;
import com.medibuddy.model.SavedMedication;
import com.medibuddy.service.MedicationStore;
import com.medibuddy.ui.HomePage;
import com.medibuddy.ui.MedicationDetailPage;
import com.medibuddy.ui.SearchPage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private Stage stage;

    private final OpenFdaClient client = new OpenFdaClient();
    
    private final MedicationStore store = createStore();

    private MedicationStore createStore() {
        MedicationStore store = new MedicationStore();
        
        return store;
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("MediBuddy");
        showHomePage();
        stage.show();
    }

    public void showHomePage() {
        Scene scene = new Scene(new HomePage(this, store).getView(), 380, 620);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }

    public void showSearchPage() {
        Scene scene = new Scene(new SearchPage(this, client, store).getView(), 380, 620);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }

    public void showMedicationDetailPage(SavedMedication medication) {
        Scene scene = new Scene(new MedicationDetailPage(this, medication).getView(), 380, 620);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
    }

    public static void main(String[] args) {
        launch();
    }
}