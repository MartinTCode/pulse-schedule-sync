package com.pulse.frontend;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.pulse.frontend.model.ScheduleRow;
import com.pulse.frontend.model.AppState;
import com.pulse.integration.timeedit.dto.TimeEditEventDTO;
import com.pulse.integration.timeedit.dto.TimeEditScheduleDTO;

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
import java.util.Optional;
import javafx.scene.control.ButtonType;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;




public class ScheduleOverviewController implements Initializable {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

        //Table and columns
        @FXML private TableView<ScheduleRow> schemaTabell;
        @FXML private TableColumn<ScheduleRow, String> kursKolumn;
        @FXML private TableColumn<ScheduleRow, String> larareKolumn;
        @FXML private TableColumn<ScheduleRow, String> datumKolumn;
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
        kursKolumn.setCellValueFactory(cellData -> cellData.getValue().kursProperty());
        larareKolumn.setCellValueFactory(cellData -> cellData.getValue().larareProperty());
        datumKolumn.setCellValueFactory(cellData -> cellData.getValue().datumProperty());
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
        String date = start != null ? start.toLocalDate().toString() : "";
        String startTime = start != null ? start.toLocalTime().format(TIME_FMT) : "";
        String endTime = end != null ? end.toLocalTime().format(TIME_FMT) : "";

        String title = e != null && e.getTitle() != null ? e.getTitle() : "";
        String location = e != null && e.getLocation() != null ? e.getLocation() : "";
        return new ScheduleRow(title, "", date, startTime, endTime, location, false);
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

    @FXML private void onPubliceraSchemaKnappClick() {
        // Handle publishing schedule to Canvas
        boolean finnsAndringar = schemaTabell.getItems().stream().anyMatch(ScheduleRow::isAndrad);

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

        visaStatusSchema.setText("Schema publicerat till Canvas.");
        visaStatusSchema.setStyle("-fx-text-fill: green;");
        visaStatusSchema.setVisible(true);

        // TODO: Implement actual API call to publish schedule to Canvas

    }

}
