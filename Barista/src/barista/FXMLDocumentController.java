/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barista;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

/**
 *
 * @author Arik
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML MenuBar menuBar;
    
    @FXML
    private Label label;
    
    @FXML
    private void handleExitAction(ActionEvent event) {
        Platform.exit();
    }
    
    @FXML
    private void handleOpenProjectAction(ActionEvent event) {
        // select project directory
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Window currentWindow = menuBar.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(currentWindow);
        
        if (selectedDirectory != null)
        {
            // use selected project directory
            System.out.println(selectedDirectory.getAbsolutePath());
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
}
