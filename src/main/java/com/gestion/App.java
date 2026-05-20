package com.gestion;

import com.gestion.config.AppConfig;
import com.gestion.utils.ThemeManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        var nunitoRegular    = getClass().getResourceAsStream("/fonts/Nunito-Regular.ttf");
        var nunitoBold       = getClass().getResourceAsStream("/fonts/Nunito-Bold.ttf");
        var nunitoExtraBold  = getClass().getResourceAsStream("/fonts/Nunito-ExtraBold.ttf");
        if (nunitoRegular   != null) Font.loadFont(nunitoRegular,   14);
        if (nunitoBold      != null) Font.loadFont(nunitoBold,      14);
        if (nunitoExtraBold != null) Font.loadFont(nunitoExtraBold, 14);

        AppConfig.setPrimaryStage(primaryStage);
        primaryStage.initStyle(StageStyle.UNDECORATED);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 860, 560);
        ThemeManager.apply(scene);

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
