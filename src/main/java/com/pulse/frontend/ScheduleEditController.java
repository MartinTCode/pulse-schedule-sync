package com.pulse.frontend;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ScheduleEditController implements Initializable {
    @FXML private Button tillbakaTillSchemanKnapp;

    @FXML private Label visaStatusSpara;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        visaStatusSpara.setVisible(false);
    }

    @FXML private void onTillbakaTillSchemanKnappClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Schemaoversikt_ImportTimeEdit.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) tillbakaTillSchemanKnapp.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            visaStatusSpara.setText("Fel vid återgång till schemaöversikt.");
            visaStatusSpara.setVisible(true);

        }

    }

}
    

