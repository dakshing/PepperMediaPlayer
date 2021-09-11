/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.dakshin.mediaplayer;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Dakshin
 */
public class PlayListController implements Initializable {
    
    @FXML
    HBox hbox1; 
    
    @FXML
    protected void closeWind(ActionEvent event){
        ((Stage)((Button)event.getSource()).getScene().getWindow()).close();;
    }

    @FXML
    protected void minimize(ActionEvent event){
        ((Stage)((Button)event.getSource()).getScene().getWindow()).setIconified(true);
    }
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Glow glow=new Glow();
        glow.setLevel(0.9);
        hbox1.setEffect(glow);
    }    
    
}
