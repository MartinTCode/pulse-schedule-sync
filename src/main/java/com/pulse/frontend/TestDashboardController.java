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
    
    @FXML
    private TextField scheduleUrlField;
    
    @FXML
    private Label scheduleStatusLabel;
    
    @FXML
    private TextField transferUrlField;
    
    @FXML
    private Label transferStatusLabel;
    
    private static final String HEALTH_URL = "http://localhost:8080/health";
    private static final String CANVAS_TEST_URL = "http://localhost:8080/health/canvas-auth";
    private static final String SCHEDULE_URL = "http://localhost:8080/api/timeedit/schedule";
    private static final String TRANSFER_URL = "http://localhost:8080/api/canvas/publish";
    
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
        
        if (scheduleUrlField != null) {
            scheduleUrlField.setText(SCHEDULE_URL);
            scheduleUrlField.setEditable(false);
        }
        
        if (transferUrlField != null) {
            transferUrlField.setText(TRANSFER_URL);
            transferUrlField.setEditable(false);
        }
        // Check server status
        checkServerStatus();
        
        testScheduleEndpoint();
        testTransferEndpoint();
        // testCanvasConnection();
        // Health checks can be done manually by visiting the endpoints in browser
        // this to avoid overloading the API rate
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
                                statusLabel.setText("REST Backend: Online ");
                                statusLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #28a745; -fx-font-weight: bold;");
                            } else {
                                statusLabel.setText("REST Backend: Offline ");
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
                        statusLabel.setText("REST Backend: Offline ");
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
                                canvasTestStatusLabel.setText("Status: Connected (check response in browser)");
                                canvasTestStatusLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #28a745;");
                            } else if (responseCode == 503) {
                                // Try to extract error message from response
                                String statusMsg = "Status: Connection Failed";
                                if (responseBody.contains("CANVAS_TOKEN")) {
                                    statusMsg = "Status: Missing CANVAS_TOKEN in .env";
                                } else if (responseBody.contains("CANVAS_BASE_URL")) {
                                    statusMsg = "Status: Missing CANVAS_BASE_URL in .env";
                                } else if (responseBody.contains("authentication failed")) {
                                    statusMsg = "Status: Authentication Failed (invalid token)";
                                }
                                canvasTestStatusLabel.setText(statusMsg);
                                canvasTestStatusLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #dc3545;");
                            } else {
                                canvasTestStatusLabel.setText("Status: Error (HTTP " + responseCode + ")");
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
                        canvasTestStatusLabel.setText("Status: Cannot reach endpoint");
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
    
    private void testScheduleEndpoint() {
        new Thread(() -> {
            try {
                URL url = new URI(SCHEDULE_URL).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                try {
                    conn.setConnectTimeout(2000);
                    conn.setReadTimeout(2000);
                    conn.setRequestMethod("GET");
                    
                    int responseCode = conn.getResponseCode();
                    boolean isOnline = (responseCode >= 200 && responseCode < 300);
                    
                    javafx.application.Platform.runLater(() -> {
                        if (scheduleStatusLabel != null) {
                            if (isOnline) {
                                scheduleStatusLabel.setText("Status: Endpoint responding");
                                scheduleStatusLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #28a745;");
                            } else {
                                scheduleStatusLabel.setText("Status: Error (HTTP " + responseCode + ")");
                                scheduleStatusLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #dc3545;");
                            }
                        }
                    });
                } finally {
                    conn.disconnect();
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    if (scheduleStatusLabel != null) {
                        scheduleStatusLabel.setText("Status: Cannot reach endpoint");
                        scheduleStatusLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #dc3545;");
                    }
                });
            }
        }).start();
    }
    
    private void testTransferEndpoint() {
        new Thread(() -> {
            try {
                URL url = new URI(TRANSFER_URL).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                try {
                    conn.setConnectTimeout(2000);
                    conn.setReadTimeout(2000);
                    conn.setRequestMethod("POST");
                    
                    int responseCode = conn.getResponseCode();
                    boolean isOnline = (responseCode >= 200 && responseCode < 300);
                    
                    javafx.application.Platform.runLater(() -> {
                        if (transferStatusLabel != null) {
                            if (isOnline) {
                                transferStatusLabel.setText("Status: Endpoint responding");
                                transferStatusLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #28a745;");
                            } else {
                                transferStatusLabel.setText("Status: Error (HTTP " + responseCode + ")");
                                transferStatusLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #dc3545;");
                            }
                        }
                    });
                } finally {
                    conn.disconnect();
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    if (transferStatusLabel != null) {
                        transferStatusLabel.setText("Status: Cannot reach endpoint");
                        transferStatusLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #dc3545;");
                    }
                });
            }
        }).start();
    }
}
