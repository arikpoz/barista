/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barista;

import barista.BaristaMessages.ApplicationSettings;
import barista.BaristaMessages.ProjectSettings;
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
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

/**
 *
 * @author Arik
 */
public class FXMLDocumentController implements Initializable {

    private StringProperty projectFolder = new SimpleStringProperty();
    public final String getProjectFolder() { return projectFolder.get(); }
    public final void setProjectFolder(String value) { projectFolder.set(value); }
    public StringProperty projectFolderProperty() { return projectFolder; }

    private StringProperty projectDescription = new SimpleStringProperty();
    public final String getProjectDescription() { return projectDescription.get(); }
    public final void setProjectDescription(String value) { projectDescription.set(value); }
    public StringProperty projectDescriptionProperty() { return projectDescription; }

    private BooleanProperty projectSettingsAreUnchanged = new SimpleBooleanProperty();
    public final Boolean getProjectSettingsAreUnchanged() { return projectSettingsAreUnchanged.get(); }
    public final void setProjectSettingsAreUnchanged(Boolean value) { projectSettingsAreUnchanged.set(value); }
    public BooleanProperty projectSettingsAreUnchangedProperty() { return projectSettingsAreUnchanged; }
    
    @FXML
    private TextField projectDescriptionTextField;
    
    @FXML 
    private MenuBar menuBar;
    
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
            loadProject(selectedDirectory.getAbsolutePath());
        }
        else
        {
            // empty project directory
            loadProject("");
        }
    }
    
    @FXML
    private void handleSaveProjectSettingsChangesAction(ActionEvent event)
    {
        // generate project settings object
        ProjectSettings.Builder projectSettingsBuilder = ProjectSettings.newBuilder();
        projectSettingsBuilder.setProjectDescription(getProjectDescription());
        ProjectSettings projectSettings = projectSettingsBuilder.build();
        
        // save project settings to file
        String projectSettingsFileName = getProjectSettingsFileName();

        if (projectSettingsFileName != null)
        {
            // write project settings object to file
            try (FileWriter fileWriter = new FileWriter(projectSettingsFileName)) 
            {
                // write ProjectSettings object in protobuf properties format
                TextFormat.print(projectSettings, fileWriter);
                fileWriter.flush();
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, String.format("Error while writing to file %s", projectSettingsFileName) , ex);
            }
            
            setProjectSettingsAreUnchanged(true);
        }
    }
    
    private void loadProject(String projectDirectory) 
    {
        // update project directory label
        setProjectFolder(projectDirectory);
        
        // load project settings from file
        ProjectSettings projectSettings = readProjectSettings();
        if (projectSettings != null)
        {
            setProjectDescription(projectSettings.getProjectDescription());
        }

        // mark project settings as unchanged, since we just load them
        setProjectSettingsAreUnchanged(true);
        
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

        projectDescription.addListener(
            new ChangeListener<String>() 
            {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) 
                {
                    // if we have a selected folder
                    if (getProjectFolder() != "")
                    {
                        if (oldValue != newValue)
                        {
                            setProjectSettingsAreUnchanged(false);
                        }
                    }
                }
            });
        
        // set bindings
        Bindings.bindBidirectional(projectDescriptionTextField.textProperty(), projectDescriptionProperty());
        
        setProjectSettingsAreUnchanged(true);
        
        ApplicationSettings applicationSettings = readApplicationSettings();
        
        if (applicationSettings != null)
        {
            loadProject(applicationSettings.getLastProjectDirectory());
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
    
    // get project settings file name
    private String getProjectSettingsFileName()
    {
        String projectFolder = getProjectFolder();
        if (( projectFolder != null) && (projectFolder != null) )
        {
            Path projectSettingsFilePath = FileSystems.getDefault().getPath(getProjectFolder(), "project.settings");
            return projectSettingsFilePath.toString();
        }
        
        return null;
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
    
    private ProjectSettings readProjectSettings() 
    {
        FileReader fileReader = null;
        try 
        {
            String projectSettingsFileName = getProjectSettingsFileName();
            
            if (new File(projectSettingsFileName).isFile())
            {
                ProjectSettings.Builder projectSettingsBuilder = ProjectSettings.newBuilder();

                // read from file using protobuf properties format
                fileReader = new FileReader(projectSettingsFileName);
                TextFormat.merge(fileReader, projectSettingsBuilder);
                fileReader.close();
                
                return projectSettingsBuilder.build();
            }
            
        } 
        catch (FileNotFoundException ex) 
        {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, "File not found while reading project.settings", ex);
        } 
        catch (IOException ex) 
        {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, "Got IO exception while reading project.settings", ex);
        } 
        
        return null;
    }
}
