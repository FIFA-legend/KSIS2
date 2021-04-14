package by.bsuir;

import by.bsuir.client.Client;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class MainWindowController {

    @FXML
    private TextField portField;

    @FXML
    private TextField ipField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button loginButton;

    @FXML
    private TextArea logField;

    @FXML
    private TextField loginField;

    @FXML
    private TextArea messageArea;

    @FXML
    private Button sendButton;

    @FXML
    private Button exitButton;

    private Client client;

    @FXML
    void initialize() {
        logField.setText("Connected...\n");

        loginButton.setOnAction(actionEvent -> {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            String login = loginField.getText();
            String password = passwordField.getText();
            client = new Client(port, ip, login, password);
            client.addUserOnlineListener(l -> logField.setText(logField.getText() + "Online " + l + "\n"));
            client.addUserOfflineListener(l -> logField.setText(logField.getText() + "Offline " + l + "\n"));
            client.addMessageListener((from, text) -> logField.setText(logField.getText() + from + ": " + text + "\n"));
            if (client.connect()) {
                if (client.login()) {
                    afterLogin(true);
                } else {
                    printInvalidLoginError();
                }
            } else {
                printInvalidServerError();
            }
        });

        sendButton.setOnAction(actionEvent -> {
            String message = messageArea.getText();
            if (client.message(message)) {
                logField.setText(logField.getText() + "You: " + message + "\n");
            }
            messageArea.clear();
        });

        exitButton.setOnAction(actionEvent -> {
            if (client.logoff()) {
                afterLogin(false);
                client = null;
                messageArea.clear();
                logField.clear();
                logField.setText("Connected...\n");
            }
        });

        App.getStage().setOnCloseRequest(windowEvent -> {
            if (client != null) {
                client.logoff();
            }
        });
    }

    private void afterLogin(boolean isLoggedIn) {
        ipField.setVisible(!isLoggedIn);
        portField.setVisible(!isLoggedIn);
        loginField.setVisible(!isLoggedIn);
        loginButton.setVisible(!isLoggedIn);
        passwordField.setVisible(!isLoggedIn);
        messageArea.setVisible(isLoggedIn);
        exitButton.setVisible(isLoggedIn);
        sendButton.setVisible(isLoggedIn);
        logField.setVisible(isLoggedIn);
    }

    private void printInvalidServerError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("There is no such server.");
        alert.show();
    }

    private void printInvalidLoginError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("This login is used or password is incorrect");
        alert.show();
    }
}
