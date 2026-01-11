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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ScheduleUrlController implements Initializable {

    @FXML private TextField skrivUrlFalt;
    @FXML private Button laddaSchemaKnapp;
    @FXML private Label visaStatusLadda;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set initial visibility of status label
        visaStatusLadda.setVisible(false);

    }
    // Event handler for loading schedule button
    @FXML private void onLaddaSchemaKnappClick() {
        String url = skrivUrlFalt.getText();
        if (url == null || url.isBlank()) {
            visaStatusLadda.setText("Vänligen ange en giltig URL.");
            visaStatusLadda.setVisible(true);
            return;
        }

        visaStatusLadda.setText("Laddar schema från URL...");
        visaStatusLadda.setVisible(true);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Schemaoversikt_ImportTimeEdit.fxml"));
        
            Parent root = loader.load();

            Stage stage = (Stage) laddaSchemaKnapp.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            visaStatusLadda.setText("Fel vid inläsning av schema: ");
            visaStatusLadda.setVisible(true);
        }



    }
    
}
