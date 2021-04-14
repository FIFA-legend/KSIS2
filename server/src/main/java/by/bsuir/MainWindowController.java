package by.bsuir;

import by.bsuir.configuration.Server;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;

public class MainWindowController {

    @FXML
    private TextField portField;

    @FXML
    private TextField channelsField;

    @FXML
    private TextField ipField;

    @FXML
    private TextField passwordField;

    @FXML
    private Button connectButton;

    @FXML
    private Button disconnectButton;

    @FXML
    private TextArea logField;

    Server server;

    @FXML
    void initialize() {
        connectButton.setOnAction(actionEvent -> {
            int port = Integer.parseInt(portField.getText());
            int channels = Integer.parseInt(channelsField.getText());
            String ip = ipField.getText();
            String password = passwordField.getText();
            server = new Server(port, channels, ip, password);
            try {
                server.startServer();
                disconnectButton.setDisable(false);
                connectButton.setDisable(true);
            } catch (IOException e) {
                e.printStackTrace();
                printError();
            }
        });

        disconnectButton.setOnAction(actionEvent -> {
            server.stopServer();
            server = null;
            disconnectButton.setDisable(true);
            connectButton.setDisable(false);
        });

        App.getStage().setOnCloseRequest(windowEvent -> {
            if (server != null) {
                server.stopServer();
            }
        });
    }

    private void printError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("This address is used.");
        alert.show();
    }
}
