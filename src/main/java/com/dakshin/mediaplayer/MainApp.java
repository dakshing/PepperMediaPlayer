package com.dakshin.mediaplayer;

import java.io.File;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class MainApp extends Application {
    
    private double xOffset = 0;
    private double yOffset = 0;
    static FXMLLoader loader;
    
    @Override
    public void start(final Stage stage) throws Exception {
        loader=new FXMLLoader(new File("C:\\Users\\Dakshin\\Documents\\NetBeansProjects\\MediaPlayer\\src\\main\\resources\\fxml\\Scene.fxml").toURI().toURL());
        Parent root = loader.load();
        
        stage.initStyle(StageStyle.TRANSPARENT);        
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                stage.setX(event.getScreenX() - xOffset);
                stage.setY(event.getScreenY() - yOffset);
            }
        });
        Scene scene = new Scene(root);
        scene.getStylesheets().add("background.css");
        
        stage.setScene(scene);
        stage.getScene().setFill(Color.TRANSPARENT);
        stage.getIcons().add(new Image("resources/pepper.png"));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
