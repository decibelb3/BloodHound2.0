package com.bloodhound2;

import com.bloodhound2.app.AppContext;
import com.bloodhound2.app.AppNavigator;
import com.bloodhound2.controller.DashboardController;
import com.bloodhound2.controller.LoginController;
import com.bloodhound2.controller.RegisterController;
import com.bloodhound2.service.SessionContext;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class BloodHoundApp extends Application implements AppNavigator {

    private static final double DEFAULT_WIDTH = 1080;
    private static final double DEFAULT_HEIGHT = 820;
    private static final double MIN_WIDTH = 980;
    private static final double MIN_HEIGHT = 740;

    private Stage primaryStage;
    private final AppContext context = new AppContext();

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("BloodHound 2.0");
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        showLogin();
        stage.show();
    }

    @Override
    public void showLogin() {
        SessionContext.clear();
        loadScene("/fxml/login.fxml", new LoginController(context, this));
    }

    @Override
    public void showRegister() {
        loadScene("/fxml/register.fxml", new RegisterController(context, this));
    }

    @Override
    public void showDashboard() {
        loadScene("/fxml/dashboard.fxml", new DashboardController(context, this));
    }

    private void loadScene(String resource, Object controller) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(Objects.requireNonNull(BloodHoundApp.class.getResource(resource)));
            loader.setController(controller);
            Parent root = loader.load();
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            scene.getStylesheets().add(
                    Objects.requireNonNull(BloodHoundApp.class.getResource("/styles/app.css"))
                            .toExternalForm());
            primaryStage.setScene(scene);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load UI: " + resource, e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
