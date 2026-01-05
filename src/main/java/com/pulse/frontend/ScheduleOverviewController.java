package com.pulse.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ScheduleOverviewController {
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private TextField serverUrlField;
    
    private static final String SERVER_URL = "http://localhost:8080/health";
    
    @FXML
    public void initialize() {
        // Check server status
        checkServerStatus();
        
        // Setup URL field
        if (serverUrlField != null) {
            serverUrlField.setText(SERVER_URL);
            serverUrlField.setEditable(false);
        }
    }
    
    private void checkServerStatus() {
        new Thread(() -> {
            try {
                URL url = new URI(SERVER_URL).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                try {
                    conn.setConnectTimeout(2000);
                    conn.setReadTimeout(2000);
                    conn.setRequestMethod("GET");
                    
                    int responseCode = conn.getResponseCode();
                    boolean isOnline = (responseCode >= 200 && responseCode < 300);
                    
                    javafx.application.Platform.runLater(() -> {
                        if (statusLabel != null) {
                            if (isOnline) {
                                statusLabel.setText("REST Backend: Online");
                                statusLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #28a745; -fx-font-weight: bold;");
                            } else {
                                statusLabel.setText("REST Backend: Offline");
                                statusLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #dc3545; -fx-font-weight: bold;");
                            }
                        }
                    });
                } finally {
                    conn.disconnect();
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    if (statusLabel != null) {
                        statusLabel.setText("REST Backend: Offline");
                        statusLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    }
                });
            }
        }).start();
    }
}
