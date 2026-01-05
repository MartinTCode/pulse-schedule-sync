package com.pulse.frontend;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class TestDashboardController {
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private TextField healthUrlField;
    
    @FXML
    private TextField canvasTestUrlField;
    
    @FXML
    private Label canvasTestStatusLabel;
    
    private static final String HEALTH_URL = "http://localhost:8080/health";
    private static final String CANVAS_TEST_URL = "http://localhost:8080/health/canvas-test";
    
    @FXML
    public void initialize() {
        // Setup URL fields
        if (healthUrlField != null) {
            healthUrlField.setText(HEALTH_URL);
            healthUrlField.setEditable(false);
        }
        
        if (canvasTestUrlField != null) {
            canvasTestUrlField.setText(CANVAS_TEST_URL);
            canvasTestUrlField.setEditable(false);
        }
        
        // Check server status
        checkServerStatus();
        testCanvasConnection();
    }
    
    private void checkServerStatus() {
        new Thread(() -> {
            try {
                URL url = new URI(HEALTH_URL).toURL();
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
                                statusLabel.setText("REST Backend: Online ✓");
                                statusLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #28a745; -fx-font-weight: bold;");
                            } else {
                                statusLabel.setText("REST Backend: Offline ✗");
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
                        statusLabel.setText("REST Backend: Offline ✗");
                        statusLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #dc3545; -fx-font-weight: bold;");
                    }
                });
            }
        }).start();
    }
    
    private void testCanvasConnection() {
        new Thread(() -> {
            try {
                URL url = new URI(CANVAS_TEST_URL).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                try {
                    conn.setConnectTimeout(2000);
                    conn.setReadTimeout(2000);
                    conn.setRequestMethod("GET");
                    
                    int responseCode = conn.getResponseCode();
                    String responseBody = readResponse(conn);
                    
                    javafx.application.Platform.runLater(() -> {
                        if (canvasTestStatusLabel != null) {
                            if (responseCode == 200) {
                                canvasTestStatusLabel.setText("Status: ✓ Connected (check response in browser)");
                                canvasTestStatusLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #28a745;");
                            } else if (responseCode == 503) {
                                // Try to extract error message from response
                                String statusMsg = "Status: ✗ Connection Failed";
                                if (responseBody.contains("CANVAS_TOKEN")) {
                                    statusMsg = "Status: ✗ Missing CANVAS_TOKEN in .env";
                                } else if (responseBody.contains("CANVAS_BASE_URL")) {
                                    statusMsg = "Status: ✗ Missing CANVAS_BASE_URL in .env";
                                } else if (responseBody.contains("authentication failed")) {
                                    statusMsg = "Status: ✗ Authentication Failed (invalid token)";
                                }
                                canvasTestStatusLabel.setText(statusMsg);
                                canvasTestStatusLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #dc3545;");
                            } else {
                                canvasTestStatusLabel.setText("Status: ✗ Error (HTTP " + responseCode + ")");
                                canvasTestStatusLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #dc3545;");
                            }
                        }
                    });
                } finally {
                    conn.disconnect();
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    if (canvasTestStatusLabel != null) {
                        canvasTestStatusLabel.setText("Status: ✗ Cannot reach endpoint");
                        canvasTestStatusLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #dc3545;");
                    }
                });
            }
        }).start();
    }
    
    private String readResponse(HttpURLConnection conn) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
