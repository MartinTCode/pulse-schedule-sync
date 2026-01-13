package com.pulse.frontend;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pulse.frontend.api.ApiException;
import com.pulse.frontend.api.ScheduleApiClient;
import com.pulse.frontend.model.AppState;
import com.pulse.integration.timeedit.dto.TimeEditScheduleDTO;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ScheduleUrlController implements Initializable {

	private static final Logger logger = LoggerFactory.getLogger(ScheduleUrlController.class);
	private static final String API_BASE_URL = "http://localhost:8080";

    @FXML private BorderPane root;
    @FXML private TextField skrivUrlFalt;
    @FXML private Button laddaSchemaKnapp;
    @FXML private Label visaStatusLadda;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set initial visibility of status label
        visaStatusLadda.setVisible(false);
        Platform.runLater(() -> root.requestFocus());

    }
    // Event handler for loading schedule button
    @FXML private void onLaddaSchemaKnappClick() {
        String url = skrivUrlFalt.getText();
        if (url == null || url.isBlank()) {
            visaStatusLadda.setText("Vänligen ange en giltig URL.");
            visaStatusLadda.setVisible(true);
            return;
        }

        laddaSchemaKnapp.setDisable(true);
        visaStatusLadda.setText("Laddar schema från server...");
        visaStatusLadda.setVisible(true);

        ScheduleApiClient apiClient = new ScheduleApiClient(API_BASE_URL);
        Task<TimeEditScheduleDTO> task = new Task<>() {
            @Override
            protected TimeEditScheduleDTO call() {
                return apiClient.fetchTimeEditSchedule(url);
            }
        };

        task.setOnSucceeded(evt -> {
            TimeEditScheduleDTO schedule = task.getValue();
            AppState.setCurrentSchedule(schedule);
            visaStatusLadda.setText("Schema laddat.");
            visaStatusLadda.setVisible(true);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Schemaoversikt_ImportTimeEdit.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) laddaSchemaKnapp.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                logger.error("Failed to load overview scene", e);
                visaStatusLadda.setText("Fel vid inläsning av schemaöversikt.");
                visaStatusLadda.setVisible(true);
            } finally {
                laddaSchemaKnapp.setDisable(false);
            }
        });

        task.setOnFailed(evt -> {
            Throwable ex = task.getException();
            String userMessage = "Kunde inte ladda schema.";
            if (ex instanceof ApiException apiEx) {
                userMessage = apiErrorToSwedish(apiEx);
                logger.warn("Schedule fetch failed: status={}, code={}, message={}", apiEx.getHttpStatus(), apiEx.getErrorCode(), apiEx.getMessage());
            } else {
                logger.error("Unexpected error fetching schedule", ex);
            }
            visaStatusLadda.setText(userMessage);
            visaStatusLadda.setVisible(true);
            laddaSchemaKnapp.setDisable(false);
        });

        Thread t = new Thread(task, "schedule-fetch");
        t.setDaemon(true);
        t.start();



    }

    private static String apiErrorToSwedish(ApiException e) {
        String code = e.getErrorCode() == null ? "" : e.getErrorCode();
        return switch (code) {
            case "INVALID_TIMEEDIT_URL" -> "Ogiltig URL. Kontrollera länken och försök igen.";
            case "TIMEEDIT_UNREACHABLE" -> "Kunde inte nå TimeEdit. Försök igen senare.";
            case "TIMEEDIT_ERROR_RESPONSE" -> "TimeEdit svarade med ett fel.";
            case "TIMEEDIT_PARSE_ERROR" -> "Kunde inte tolka TimeEdit-svaret (fel format).";
            default -> "Kunde inte ladda schema (" + (e.getMessage() != null ? e.getMessage() : "okänt fel") + ")";
        };
    }
    
}
