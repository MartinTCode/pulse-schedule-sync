package com.pulse.frontend;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.pulse.frontend.model.ScheduleRow;

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



public class ScheduleOverviewController implements Initializable {

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

        // === Test data untill API is connected ===
        schemaTabell.getItems().addAll (
            new ScheduleRow("D0018E", "Andersson", "2026-03-15", "08:00", "10:00", "A109", false),
            new ScheduleRow("D0020E", "Karlsson", "2026-03-16", "10:15", "12:00", "B203", true)

        );
    }

    @FXML private void onTillbakaUrlKnappClick() {
        // Handle going back to URL input screen
        try {
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
}
