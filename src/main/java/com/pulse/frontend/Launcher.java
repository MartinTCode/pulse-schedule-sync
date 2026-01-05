// src/main/java/com/pulse/frontend/Launcher.java
package com.pulse.frontend;

import com.pulse.server.RestServer;
import org.glassfish.grizzly.http.server.HttpServer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Launcher extends Application {

    private HttpServer server;

    @Override
    public void start(Stage stage) throws Exception {

        // Start embedded REST server
        server = RestServer.startServer();

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/ScheduleOverview.fxml")
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("TimeEdit → Canvas Transfer");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // Ensure server shuts down when UI closes
        if (server != null) {
            server.shutdownNow();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
