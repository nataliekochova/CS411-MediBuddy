package com.medibuddy.ui;

import com.medibuddy.client.OpenFdaClient;
import com.medibuddy.service.MedicationStore;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class AppShell {

    private final OpenFdaClient client;
    private final MedicationStore store;

    private final BorderPane root;
    private final StackPane contentArea;

    private Button medsButton;
    private Button scheduleButton;
    private Button interactionsButton;
    private Button settingsButton;

    private ImageView medsIcon;
    private ImageView scheduleIcon;
    private ImageView interactionsIcon;
    private ImageView settingsIcon;

    public AppShell(OpenFdaClient client, MedicationStore store) {
        this.client = client;
        this.store = store;
        this.root = new BorderPane();
        this.contentArea = new StackPane();

        build();
    }

    public Parent getView() {
        return root;
    }

    private void build() {
        root.getStyleClass().add("app-shell");
        contentArea.getStyleClass().add("app-content");

        root.setCenter(contentArea);
        root.setBottom(createBottomNav());
        
        setActivePage("meds");
        showMedicationsPage();
    }

    private ImageView createIcon(String path) {
        Image img = new Image(getClass().getResourceAsStream(path));
        ImageView view = new ImageView(img);

        view.setFitWidth(24);
        view.setFitHeight(24);
        view.setPreserveRatio(true);

        return view;
    }

    private void setActivePage(String page) {
        medsButton.getStyleClass().remove("nav-active");
        scheduleButton.getStyleClass().remove("nav-active");
        interactionsButton.getStyleClass().remove("nav-active");
        settingsButton.getStyleClass().remove("nav-active");

        medsIcon.setImage(new Image(getClass().getResourceAsStream("/icons/meds.png")));
        scheduleIcon.setImage(new Image(getClass().getResourceAsStream("/icons/schedule.png")));
        interactionsIcon.setImage(new Image(getClass().getResourceAsStream("/icons/interactions.png")));
        settingsIcon.setImage(new Image(getClass().getResourceAsStream("/icons/settings.png")));

        switch (page) {
            case "meds" -> {
                medsButton.getStyleClass().add("nav-active");
                medsIcon.setImage(new Image(getClass().getResourceAsStream("/icons/meds_active.png")));
            }
            case "schedule" -> {
                scheduleButton.getStyleClass().add("nav-active");
                scheduleIcon.setImage(new Image(getClass().getResourceAsStream("/icons/schedule_active.png")));
            }
            case "interactions" -> {
                interactionsButton.getStyleClass().add("nav-active");
                interactionsIcon.setImage(new Image(getClass().getResourceAsStream("/icons/interactions_active.png")));
            }
            case "settings" -> {
                settingsButton.getStyleClass().add("nav-active");
                settingsIcon.setImage(new Image(getClass().getResourceAsStream("/icons/settings_active.png")));
            }
        }
    }

    private static class NavItem {
        Button button;
        ImageView icon;
        String normalPath;
        String activePath;

        NavItem(Button button, ImageView icon, String normalPath, String activePath) {
            this.button = button;
            this.icon = icon;
            this.normalPath = normalPath;
            this.activePath = activePath;
        }
    }

    private HBox createBottomNav() {
        medsIcon = createIcon("/icons/meds.png");
        scheduleIcon = createIcon("/icons/schedule.png");
        interactionsIcon = createIcon("/icons/interactions.png");
        settingsIcon = createIcon("/icons/settings.png");

        medsButton = new Button();
        medsButton.setGraphic(medsIcon);

        scheduleButton = new Button();
        scheduleButton.setGraphic(scheduleIcon);

        interactionsButton = new Button();
        interactionsButton.setGraphic(interactionsIcon);

        settingsButton = new Button();
        settingsButton.setGraphic(settingsIcon);

        medsButton.getStyleClass().add("nav-button");
        scheduleButton.getStyleClass().add("nav-button");
        interactionsButton.getStyleClass().add("nav-button");
        settingsButton.getStyleClass().add("nav-button");

        medsButton.setMaxWidth(Double.MAX_VALUE);
        scheduleButton.setMaxWidth(Double.MAX_VALUE);
        interactionsButton.setMaxWidth(Double.MAX_VALUE);
        settingsButton.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(medsButton, Priority.ALWAYS);
        HBox.setHgrow(scheduleButton, Priority.ALWAYS);
        HBox.setHgrow(interactionsButton, Priority.ALWAYS);
        HBox.setHgrow(settingsButton, Priority.ALWAYS);

        medsButton.setOnAction(e -> {
            setActivePage("meds");
            showMedicationsPage();
        });

        scheduleButton.setOnAction(e -> {
            setActivePage("schedule");
            showSchedulePage();
            contentArea.getChildren().setAll(new SchedulePage(store).getView());
        });

        interactionsButton.setOnAction(e -> {
            setActivePage("interactions");
            showInteractionsPage();
        });

        settingsButton.setOnAction(e -> {
            setActivePage("settings");
            showSettingsPage();
        });

        HBox navBar = new HBox(8, medsButton, scheduleButton, interactionsButton, settingsButton);
        navBar.setPadding(new Insets(10, 12, 12, 12));
        navBar.getStyleClass().add("bottom-nav");

        return navBar;
    }

    public void showMedicationsPage() {
        contentArea.getChildren().setAll(new HomePage(this, store).getView());
    }

    public void showSearchPage() {
        contentArea.getChildren().setAll(new SearchPage(this, client, store).getView());
    }

    public void showMedicationDetailPage(com.medibuddy.model.SavedMedication medication) {
        contentArea.getChildren().setAll(new MedicationDetailPage(this, store, medication).getView());
    }

    public void showSchedulePage() {
        contentArea.getChildren().setAll(new SchedulePage(store).getView());
    }

    public void showInteractionsPage() {
        contentArea.getChildren().setAll(
            new InteractionsPage(client, store).getView()
        );
    }

    public void showSettingsPage() {
        contentArea.getChildren().setAll(new SettingsPage().getView());
    }
}
