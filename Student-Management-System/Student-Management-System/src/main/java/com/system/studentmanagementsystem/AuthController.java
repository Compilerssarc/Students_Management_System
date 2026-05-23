package com.system.studentmanagementsystem;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    private Connection conn;

    @FXML
    public void initialize() {
        conn = DBConnection.connect();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Please enter both username and password.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            String query = "SELECT password FROM users WHERE username = ?";
            PreparedStatement pst = conn.prepareStatement(query);
            pst.setString(1, username);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, storedHashedPassword)) {
                    lblMessage.setText("Login successful!");
                    lblMessage.setTextFill(javafx.scene.paint.Color.GREEN);
                    loadMainApplication(event);
                } else {
                    lblMessage.setText("Invalid username or password.");
                    lblMessage.setTextFill(javafx.scene.paint.Color.RED);
                }
            } else {
                lblMessage.setText("Invalid username or password.");
                lblMessage.setTextFill(javafx.scene.paint.Color.RED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Database error occurred.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    @FXML
    private void handleSignup(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblMessage.setText("Please enter both username and password.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        try {
            String checkQuery = "SELECT id FROM users WHERE username = ?";
            PreparedStatement checkPst = conn.prepareStatement(checkQuery);
            checkPst.setString(1, username);
            ResultSet rs = checkPst.executeQuery();

            if (rs.next()) {
                lblMessage.setText("Username already exists.");
                lblMessage.setTextFill(javafx.scene.paint.Color.RED);
                return;
            }

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement insertPst = conn.prepareStatement(insertQuery);
            insertPst.setString(1, username);
            insertPst.setString(2, hashedPassword);
            insertPst.executeUpdate();

            lblMessage.setText("Sign up successful! You can now login.");
            lblMessage.setTextFill(javafx.scene.paint.Color.GREEN);
            txtUsername.clear();
            txtPassword.clear();

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Database error occurred.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    private void loadMainApplication(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/system/studentmanagementsystem/main.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Student Record Management System");
            stage.hide();
            stage.show();

            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);

        } catch (Exception e) {
            e.printStackTrace();
            lblMessage.setText("Error loading main application.");
            lblMessage.setTextFill(javafx.scene.paint.Color.RED);
        }
    }
}