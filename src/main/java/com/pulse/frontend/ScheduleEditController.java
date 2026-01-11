package com.pulse.frontend;

import com.pulse.frontend.model.ScheduleRow;

import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalDate;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class ScheduleEditController implements Initializable {

    @FXML private Button tillbakaTillSchemanKnapp;
    @FXML private Button sparaAndringKnapp;

    @FXML private Label visaStatusSpara;
    @FXML private Label visaKurs;

    @FXML private DatePicker visaDatum;
    @FXML private TextField visaStarttid;
    @FXML private TextField visaSluttid;
    @FXML private TextField visaPlats;
    @FXML private TextArea visaBeskrivning;

    private ScheduleRow aktuellHandelse;
    private Scene previousScene;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        visaStatusSpara.setVisible(false);
    }

    public void setPreviousScene(Scene scene) {
        this.previousScene = scene;
    }

    //Gets ScheduleRow from overview
    public void setScheduleRow(ScheduleRow aktuellHandelse) {
        this.aktuellHandelse = aktuellHandelse;

        //Show current event details in fields
        visaKurs.setText("Kurs: " + aktuellHandelse.getKurs());

        visaDatum.setValue(LocalDate.parse(aktuellHandelse.getDatum()));
        visaStarttid.setText(aktuellHandelse.getStartTid());
        visaSluttid.setText(aktuellHandelse.getSlutTid());
        visaPlats.setText(aktuellHandelse.getPlats());


    }

    //Event handler for saving changes button
    @FXML private void onSparaAndringKnappClick() {
        if (aktuellHandelse == null) {
            visaStatusSpara.setText("Ingen händelse vald att spara.");
            visaStatusSpara.setVisible(true);
            return;   
        }  
        
        // Confirm with user before saving changes
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Bekräfta sparande av ändringar");
        confirm.setHeaderText("Är du säker på att du vill spara ändringarna?");
        confirm.setContentText("Ändringarna sparas lokalt och kan publiceras sen.");

        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isEmpty() || result.get() != ButtonType.OK) {
        visaStatusSpara.setText("Ändringar sparades inte.");
        visaStatusSpara.setStyle("-fx-text-fill: orange;");
        visaStatusSpara.setVisible(true);
        return;
        }

        //Update ScheduleRow with edited details (Later API call to save changes)
        aktuellHandelse.setDatum(visaDatum.getValue().toString());
        aktuellHandelse.setStartTid(visaStarttid.getText());
        aktuellHandelse.setSlutTid(visaSluttid.getText());
        aktuellHandelse.setPlats(visaPlats.getText());
        aktuellHandelse.setAndrad(true);

        visaStatusSpara.setText("Ändringar sparade lokalt. Publicera schema för att skicka till Canvas.");
        visaStatusSpara.setVisible(true);
        visaStatusSpara.setStyle("-fx-text-fill: green;");


    }

    @FXML private void onTillbakaTillSchemanKnappClick() {
        Stage stage = (Stage) tillbakaTillSchemanKnapp.getScene().getWindow();
        stage.setScene(previousScene);   

    }

}
    

