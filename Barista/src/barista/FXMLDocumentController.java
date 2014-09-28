/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barista;

import barista.BaristaMessages.ApplicationSettings;
import com.google.protobuf.TextFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

/**
 *
 * @author Arik
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML 
    private MenuBar menuBar;
    
    @FXML
    private Label projectDirectoryLabel;
    
    @FXML
    private void handleExitAction(ActionEvent event) 
    {
        Platform.exit();
    }
    
    @FXML
    private void handleOpenProjectAction(ActionEvent event) 
    {
        // select project directory
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Window currentWindow = menuBar.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(currentWindow);
        
        if (selectedDirectory != null)
        {
            // use selected project directory
            setProjectDirectory(selectedDirectory.getAbsolutePath());
        }
        else
        {
            // empty project directory
            setProjectDirectory("");
        }
    }
    
    private void setProjectDirectory(String projectDirectory) 
    {
        // update project directory label
        projectDirectoryLabel.setText(projectDirectory);
        
        // generate application settings object
        ApplicationSettings.Builder applicationSettingsBuilder = ApplicationSettings.newBuilder();
        applicationSettingsBuilder.setLastProjectDirectory(projectDirectory);
        ApplicationSettings applicationSettings = applicationSettingsBuilder.build();
        
        String applicationSettingsFileName = getApplicationSettingsFileName();

        // write application settings objects to file
        try (FileWriter fileWriter = new FileWriter(applicationSettingsFileName)) 
        {
            // write ApplicationSettings object in protobuf properties format
            TextFormat.print(applicationSettings, fileWriter);
            fileWriter.flush();
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, String.format("Error while writing to file %s", applicationSettingsFileName) , ex);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        ApplicationSettings applicationSettings = readApplicationSettings();
        
        if (applicationSettings != null)
        {
            setProjectDirectory(applicationSettings.getLastProjectDirectory());
        }
    }    

    // get application settings file name
    private String getApplicationSettingsFileName()
    {
        // generate application settings file name
        String applicationDirectory = System.getProperty("user.dir");
        Path applicationSettingsFilePath = FileSystems.getDefault().getPath(applicationDirectory, "application.settings");
        
        return applicationSettingsFilePath.toString();
    }
    
    private ApplicationSettings readApplicationSettings() 
    {
        FileReader fileReader = null;
        try 
        {
            String applicationSettingsFileName = getApplicationSettingsFileName();
            
            if (new File(applicationSettingsFileName).isFile())
            {
                ApplicationSettings.Builder applicationSettingsBuilder = ApplicationSettings.newBuilder();

                // read from file using protobuf properties format
                fileReader = new FileReader(applicationSettingsFileName);
                TextFormat.merge(fileReader, applicationSettingsBuilder);
                fileReader.close();
                
                return applicationSettingsBuilder.build();
            }
            
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, "", ex);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        return null;
    }
    
}
