package com.bloodhound2.controller;

import com.bloodhound2.app.AppContext;
import com.bloodhound2.app.AppNavigator;
import com.bloodhound2.service.AuthService;
import com.bloodhound2.service.SessionContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class LoginController {

    private final AuthService authService;
    private final AppNavigator navigator;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    public LoginController(AppContext context, AppNavigator navigator) {
        this.authService = context.authService;
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        errorLabel.setText("");
    }

    @FXML
    private void onLogin() {
        errorLabel.setText("");
        try {
            var user = authService.login(usernameField.getText(), passwordField.getText());
            if (user.isEmpty()) {
                errorLabel.setText("Invalid username or password.");
                return;
            }
            SessionContext.setCurrentUser(user.get());
            navigator.showDashboard();
        } catch (SQLException e) {
            errorLabel.setText("Could not connect to the database. Is MySQL running?");
        }
    }

    @FXML
    private void onGoToRegister() {
        navigator.showRegister();
    }
}
