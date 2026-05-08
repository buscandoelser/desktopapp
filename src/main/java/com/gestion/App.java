package com.gestion;

import com.gestion.config.AppConfig;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        AppConfig.setPrimaryStage(primaryStage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 860, 560);
        scene.getStylesheets().add(getClass().getResource("/css/dark-futuristic.css").toExternalForm());

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/logo-buscando-ser.png")));
        primaryStage.setTitle("Sistema de Gestión de Internos");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        AppConfig.clearSession();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
