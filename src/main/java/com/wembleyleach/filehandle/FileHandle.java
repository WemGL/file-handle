package com.wembleyleach.filehandle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FileHandle extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/FileHandle.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/application.css").toExternalForm());
        primaryStage.setTitle("File Handle");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/file-handle-logo.png")));
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.toFront();
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
