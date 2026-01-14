package com.pulse.frontend;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.pulse.frontend.model.ScheduleRow;
import com.pulse.domain.TransferRequest;
import com.pulse.frontend.api.TransferApiClient;
import com.pulse.frontend.model.AppState;
import com.pulse.integration.timeedit.dto.TimeEditEventDTO;
import com.pulse.integration.timeedit.dto.TimeEditScheduleDTO;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableCell;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.util.List;
import java.util.Optional;
import javafx.scene.control.ButtonType;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;




public class ScheduleOverviewController implements Initializable {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

        //Table and columns
        @FXML private TableView<ScheduleRow> schemaTabell;
        @FXML private TableColumn<ScheduleRow, String> aktivitetKolumn;
        @FXML private TableColumn<ScheduleRow, String> larareKolumn;
        @FXML private TableColumn<ScheduleRow, String> startDatumKolumn;
        @FXML private TableColumn<ScheduleRow, String> slutDatumKolumn;
        @FXML private TableColumn<ScheduleRow, String> starttidKolumn;
        @FXML private TableColumn<ScheduleRow, String> sluttidKolumn;
        @FXML private TableColumn<ScheduleRow, String> platsKolumn;
        @FXML private TableColumn<ScheduleRow, Boolean> andradKolumn; 
        
        // Buttons
        @FXML private Button tillbakaUrlKnapp;
        @FXML private Button andraHandelseKnapp;
        @FXML private Button publiceraSchemaKnapp;

        //Status label
        @FXML private Label visaStatusSchema;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Controller for main schedule overview screen
        // Test functionality has been moved to TestDashboardController

        visaStatusSchema.setVisible(false);

        // Set up table columns to bind to ScheduleRow properties
        aktivitetKolumn.setCellValueFactory(cellData -> cellData.getValue().aktivitetProperty());
        larareKolumn.setCellValueFactory(cellData -> cellData.getValue().larareProperty());
        startDatumKolumn.setCellValueFactory(cellData -> cellData.getValue().startDatumProperty());
        slutDatumKolumn.setCellValueFactory(cellData -> cellData.getValue().slutDatumProperty());
        starttidKolumn.setCellValueFactory(cellData -> cellData.getValue().startTidProperty());
        sluttidKolumn.setCellValueFactory(cellData -> cellData.getValue().slutTidProperty());
        platsKolumn.setCellValueFactory(cellData -> cellData.getValue().platsProperty());
        andradKolumn.setCellValueFactory(cellData -> cellData.getValue().andradProperty());


        // Show ✓ instead of true/false for 'andrad' column
        andradKolumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean value, boolean empty) {
                super.updateItem(value,empty);
                setText(empty || !value ? "" : "✓");

            }

        });

        loadFromAppState();
    }

    private void loadFromAppState() {
        schemaTabell.getItems().clear();
        TimeEditScheduleDTO schedule = AppState.getCurrentSchedule();
        if (schedule == null || schedule.getEvents() == null || schedule.getEvents().isEmpty()) {
            visaStatusSchema.setText("Inget schema laddat ännu.");
            visaStatusSchema.setVisible(true);
            return;
        }

        for (TimeEditEventDTO e : schedule.getEvents()) {
            schemaTabell.getItems().add(toRow(e));
        }
        visaStatusSchema.setText("Visar " + schedule.getEvents().size() + " händelser.");
        visaStatusSchema.setVisible(true);
    }

    private ScheduleRow toRow(TimeEditEventDTO e) {
        OffsetDateTime start = e != null ? e.getStart() : null;
        OffsetDateTime end = e != null ? e.getEnd() : null;
        String startDate = start != null ? start.toLocalDate().toString() : "";
        String endDate = end != null ? end.toLocalDate().toString() : "";
        String startTime = start != null ? start.toLocalTime().format(TIME_FMT) : "";
        String endTime = end != null ? end.toLocalTime().format(TIME_FMT) : "";

        String title = e != null && e.getTitle() != null ? e.getTitle() : ""; 
        String location = e != null && e.getLocation() != null ? e.getLocation() : "";
        String description = e != null && e.getDescription() != null ? e.getDescription() : "";
        return new ScheduleRow("",title, "", startDate, endDate, startTime, endTime, location, description, false);
    }

    @FXML private void onTillbakaUrlKnappClick() {
        // Handle going back to URL input screen
        try {
			AppState.clear();
            FXMLLoader loader =
            new FXMLLoader(getClass().getResource("/fxml/SkrivaInURLSchema.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) tillbakaUrlKnapp.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            visaStatusSchema.setText("Kunde inte gå tillbaka till startsidan.");
            visaStatusSchema.setVisible(true);
        }
    }

    @FXML private void onAndraHandelseKnappClick() {

        Scene currentScene = andraHandelseKnapp.getScene();
        
        ScheduleRow selected = schemaTabell.getSelectionModel().getSelectedItem();

        if (selected == null) {
            visaStatusSchema.setText("Vänligen välj en händelse att ändra.");
            visaStatusSchema.setVisible(true);
            visaStatusSchema.setStyle(null);
            return;
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GranskaHandelse.fxml"));

            Parent root = loader.load();

            ScheduleEditController controller = loader.getController();

            //Send selected ScheduleRow to edit controller
            controller.setScheduleRow(selected);

            controller.setPreviousScene(currentScene);

            Stage stage = (Stage) andraHandelseKnapp.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            visaStatusSchema.setText("Kunde inte öppna granskningsvyn.");
            visaStatusSchema.setVisible(true);
        }

    }

    /**
     * Handles publishing the schedule to Canvas when the button is clicked.
     */
    @FXML
    private void onPubliceraSchemaKnappClick() {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Bekräfta publicering av schema");
        confirm.setHeaderText("Är du säker på att du vill publicera schemat till Canvas?");
        confirm.setContentText("Alla scheman och eventuella ändringar kommer att skickas till Canvas.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            visaStatusSchema.setText("Publicering avbröts.");
            visaStatusSchema.setStyle("-fx-text-fill: orange;");
            visaStatusSchema.setVisible(true);
            return;
        }

        // Build schedule from what's currently shown in the table (includes edits)
        TimeEditScheduleDTO scheduleToPublish = buildScheduleFromTable();

        // Build TransferRequest that matches backend contract 
        TransferRequest transferRequest = new TransferRequest();

        // Canvas context from env var
        String canvasContext = System.getenv().getOrDefault("CANVAS_CONTEXT", "").trim();
        if (canvasContext.isBlank()) {
            visaStatusSchema.setText("Publicering misslyckades: CANVAS_CONTEXT saknas (t.ex. user_123).");
            visaStatusSchema.setStyle("-fx-text-fill: red;");
            visaStatusSchema.setVisible(true);
            return;
        }
        transferRequest.setCanvasContext(canvasContext);

        // Map TimeEdit events -> TransferRequest.ScheduleEvent
        TransferRequest.Schedule schedule = new TransferRequest.Schedule();
        schedule.setEvents(
                scheduleToPublish.getEvents().stream()
                        .map(ev -> {
                            TransferRequest.ScheduleEvent e = new TransferRequest.ScheduleEvent();

                            e.setExternalId(ev.getExternalId());
                            e.setTitle(ev.getTitle());
                            e.setStart(ev.getStart());
                            e.setEnd(ev.getEnd());
                            e.setLocation(ev.getLocation());
                            e.setDescription(ev.getDescription());
                            return e;
                        })
                        .collect(Collectors.toList())
        );

        transferRequest.setSchedule(schedule);

        // 3) Update UI state
        visaStatusSchema.setText("Publicerar schema till Canvas...");
        visaStatusSchema.setStyle("-fx-text-fill: orange;");
        visaStatusSchema.setVisible(true);

        publiceraSchemaKnapp.setDisable(true);

        // 4) Call backend async
        TransferApiClient client = new TransferApiClient(
                System.getenv().getOrDefault("BACKEND_BASE_URL", "http://localhost:8080")
        );

        CompletableFuture
                .supplyAsync(() -> client.publishToCanvas(transferRequest))
                .whenComplete((transferResult, err) -> Platform.runLater(() -> {
                    publiceraSchemaKnapp.setDisable(false);

                    if (err != null) {
                        visaStatusSchema.setText("Publicering misslyckades: " + err.getMessage());
                        visaStatusSchema.setStyle("-fx-text-fill: red;");
                        visaStatusSchema.setVisible(true);
                        return;
                    }

                    int ok = transferResult.getPublished();
                    int fail = transferResult.getFailureCount();
                    int total = ok + fail;

                    if (fail == 0) {
                        visaStatusSchema.setText("Schema publicerat till Canvas (" + ok + "/" + total + ").");
                        visaStatusSchema.setStyle("-fx-text-fill: green;");
                        visaStatusSchema.setVisible(true);

                        // Mark rows as no longer changed
                        schemaTabell.getItems().forEach(r -> r.setAndrad(false));
                        return;
                    }

                    visaStatusSchema.setText("Publicering klar: " + ok + " OK, " + fail + " misslyckades.");
                    visaStatusSchema.setStyle("-fx-text-fill: orange;");
                    visaStatusSchema.setVisible(true);

                    String failures = transferResult.getFailures().stream()
                            .map(f -> "- " + f.getExternalId() + ": " + f.getMessage())
                            .collect(Collectors.joining("\n"));

                    Alert a = new Alert(Alert.AlertType.WARNING);
                    a.setTitle("Publicering: vissa event misslyckades");
                    a.setHeaderText("Canvas accepterade inte alla event.");
                    a.setContentText(failures.isBlank() ? "Se logg för detaljer." : failures);
                    a.showAndWait();
                }));
    }



    /**
     * Builds a TimeEditScheduleDTO from the current table contents.
     * @return The built TimeEditScheduleDTO.
     */
    private TimeEditScheduleDTO buildScheduleFromTable() {
        TimeEditScheduleDTO base = AppState.getCurrentSchedule();
        TimeEditScheduleDTO dto = new TimeEditScheduleDTO();

        List<TimeEditEventDTO> events = schemaTabell.getItems().stream()
                .map(this::rowToEventDto)
                .collect(Collectors.toList());

        dto.setEvents(events);
        return dto;
    }

    /**
     * Maps a ScheduleRow back into a TimeEditEventDTO.
     * @param row The ScheduleRow to map.
     * @return The mapped TimeEditEventDTO.
     */
    private TimeEditEventDTO rowToEventDto(ScheduleRow row) {
        TimeEditEventDTO e = new TimeEditEventDTO();

        // Map from your table row back into DTO
        e.setTitle(row.getAktivitet());
        e.setStart(OffsetDateTime.parse(row.getStartDatum() + "T" + row.getStartTid() + ":00+00:00"));
        e.setEnd(OffsetDateTime.parse(row.getSlutDatum() + "T" + row.getSlutTid() + ":00+00:00"));
        e.setLocation(row.getPlats());
        e.setDescription(row.getBeskrivning());

        return e;
    }
}

