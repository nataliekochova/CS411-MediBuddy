package com.medibuddy.ui;

import com.medibuddy.client.OpenFdaClient;
import com.medibuddy.service.MedicationStore;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
<<<<<<< HEAD
import com.medibuddy.App;
=======
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.util.Duration;


>>>>>>> 3d40dec (fixed UI, added top bar, added bottom navigation bar glider (waste of a lot of time but it works now))

public class AppShell {

    private final OpenFdaClient client;
    private final MedicationStore store;
    private final App app;

    private final BorderPane root;
    private final StackPane contentArea;

    private HBox navBar;
    private Rectangle indicator;
    private Button todayBtn, medsBtn, alertsBtn, settingsBtn;

    public AppShell(App app, OpenFdaClient client, MedicationStore store) {
        this.app = app;
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
        root.setTop(createHeader());
        root.setBottom(buildNavBar());
        
        showMedicationsPage();
        Platform.runLater(() -> animateIndicator(medsBtn));

        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
    if (newScene != null) {
        newScene.windowProperty().addListener((obs2, oldWin, newWin) -> {
            if (newWin != null) {
                newWin.showingProperty().addListener((obs3, wasShowing, isShowing) -> {
                    if (isShowing) {
                        animateIndicator(medsBtn);
                    }
                });
            }
        });
    }
});
    }

    private ImageView createIcon(String path) {
        Image img = new Image(getClass().getResourceAsStream(path));
        ImageView view = new ImageView(img);

        view.setFitWidth(24);
        view.setFitHeight(24);
        view.setPreserveRatio(true);

        return view;
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

    public void logout() {
        app.showLoginPage();
    }

   // public void showSchedulePage() {
        //contentArea.getChildren().setAll(new SchedulePage(store).getView());
        public void showSchedulePage() {
        SchedulePage page = new SchedulePage(store);  // new page
        //page.refresh();                               // 
        contentArea.getChildren().setAll(page.getView());
    }

    public void showInteractionsPage() {
        contentArea.getChildren().setAll(
            new InteractionsPage(client, store).getView()
        );
    }

    public void showSettingsPage() {
        contentArea.getChildren().setAll(new SettingsPage(this).getView());
    }

    private HBox createHeader() {
    HBox header = new HBox();
    header.getStyleClass().add("header-bar");
    header.setPadding(new Insets(8,20 ,8,20));
    header.setSpacing(12);

    // Logo
    ImageView logo = new ImageView(
        new Image(getClass().getResourceAsStream("/icons/logo.png"))
    );
    logo.setFitWidth(60);
    logo.setFitHeight(60);
    logo.setPreserveRatio(true);

    // App name + version
    VBox titleBox = new VBox(-2);
    titleBox.setPadding(new Insets(-4, 0, 0, 0));
    Label appName = new Label("MediBuddy");
    appName.getStyleClass().add("header-title");

    Label version = new Label("Free version");
    version.getStyleClass().add("header-subtitle");

    titleBox.getChildren().addAll(appName, version);

    // Spacer
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    // Premium button
    Button premiumBtn = new Button("Premium");
    premiumBtn.getStyleClass().add("premium-button");

    header.getChildren().addAll(logo, titleBox, spacer, premiumBtn);

    return header;
}

    private Parent buildNavBar() {
    navBar = new HBox(40);
    navBar.setAlignment(Pos.CENTER);
    navBar.getStyleClass().add("bottom-nav");

    todayBtn = makeNavButton("/icons/schedule.png");
medsBtn = makeNavButton("/icons/meds.png");
alertsBtn = makeNavButton("/icons/interactions.png");
settingsBtn = makeNavButton("/icons/settings.png");

    navBar.getChildren().addAll(medsBtn, todayBtn, alertsBtn, settingsBtn);

    indicator = new Rectangle(60, 36);   // wider + taller
    indicator.setArcWidth(18);
    indicator.setArcHeight(18);
    indicator.getStyleClass().add("nav-indicator");
    indicator.setMouseTransparent(true); // so clicks go through

    indicator.getStyleClass().add("nav-indicator");

    StackPane wrapper = new StackPane();
    wrapper.getChildren().addAll(indicator, navBar);
    wrapper.setPadding(new Insets(12));

    todayBtn.setOnAction(e -> {
    animateIndicator(todayBtn);
    showSchedulePage();   // or showTodayPage() if you have one
});

medsBtn.setOnAction(e -> {
    animateIndicator(medsBtn);
    showMedicationsPage();
});

alertsBtn.setOnAction(e -> {
    animateIndicator(alertsBtn);
    showInteractionsPage();
});

settingsBtn.setOnAction(e -> {
    animateIndicator(settingsBtn);
    showSettingsPage();
});


    return wrapper;
}

private Button makeNavButton(String iconPath) {
    ImageView iv = new ImageView(
        new Image(getClass().getResourceAsStream(iconPath))
    );
    iv.setFitWidth(24);
    iv.setFitHeight(24);

    Button b = new Button();
    b.setGraphic(iv);
    b.getStyleClass().add("nav-icon");
    return b;
}

private void animateIndicator(Button target) {
    Bounds targetBounds = target.localToScene(target.getBoundsInLocal());
    Bounds navBounds = navBar.localToScene(navBar.getBoundsInLocal());

    if (targetBounds == null || navBounds == null) return;

    double targetCenterX = targetBounds.getMinX() + targetBounds.getWidth() / 2;
    double navCenterX = navBounds.getMinX() + navBounds.getWidth() / 2;

    // Center the pill under the icon
    double offset = targetCenterX - navCenterX;

    TranslateTransition tt = new TranslateTransition(Duration.millis(300), indicator);
    tt.setToX(offset);
    tt.setInterpolator(Interpolator.EASE_BOTH);
    tt.play();
}
} 