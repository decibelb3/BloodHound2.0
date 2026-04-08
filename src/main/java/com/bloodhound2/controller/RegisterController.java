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

public class RegisterController {

    private final AuthService authService;
    private final AppNavigator navigator;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    public RegisterController(AppContext context, AppNavigator navigator) {
        this.authService = context.authService;
        this.navigator = navigator;
    }

    @FXML
    private void initialize() {
        errorLabel.setText("");
    }

    @FXML
    private void onRegister() {
        errorLabel.setText("");
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errorLabel.setText("Passwords do not match.");
            return;
        }
        try {
            var user =
                    authService.register(
                            usernameField.getText(), emailField.getText(), passwordField.getText());
            SessionContext.setCurrentUser(user);
            navigator.showDashboard();
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (SQLException e) {
            errorLabel.setText("Could not save account. Check the database and try again.");
        }
    }

    @FXML
    private void onBackToLogin() {
        navigator.showLogin();
    }
}
