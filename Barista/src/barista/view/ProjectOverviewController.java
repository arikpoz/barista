/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barista.view;

import barista.BaristaMessages;
import barista.MainApp;
import barista.model.Configuration;
import com.google.protobuf.TextFormat;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 *
 * @author Arik
 */
public class ProjectOverviewController {

    @FXML
    private Label projectFolderLabel;
    
    @FXML
    private TextField projectDescriptionTextField;

    @FXML
    private Button saveProjectSettingsButton;
    
    @FXML
    private TableView<Configuration> configurationTable;

    @FXML
    private TableColumn<Configuration, String> nameColumn;

    @FXML
    private TextField nameTextField;

    @FXML
    private TextField solverFileNameTextField;

    // Reference to the main application.
    private MainApp mainApp;

    /**
     * The constructor. The constructor is called before the initialize()
     * method.
     */
    public ProjectOverviewController() {
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {

        // initialize the configuration table 
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        
        // clear configuration details
        showConfigurationDetails(null);

        // listen for selection changes and show the configuration details when changed
        configurationTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showConfigurationDetails(newValue));
    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        // Add observable list data to the table
        configurationTable.setItems(mainApp.getConfigurationList());

        // set bindings
        Bindings.bindBidirectional(projectFolderLabel.textProperty(), mainApp.projectFolderProperty());
        Bindings.bindBidirectional(projectDescriptionTextField.textProperty(), mainApp.projectDescriptionProperty());
        Bindings.bindBidirectional(saveProjectSettingsButton.disableProperty(), mainApp.projectSettingsAreUnchangedProperty());
    }

    @FXML
    private void handleSaveProjectSettingsAction(ActionEvent event) {
        // generate project settings object
        BaristaMessages.ProjectSettings.Builder projectSettingsBuilder = BaristaMessages.ProjectSettings.newBuilder();
        projectSettingsBuilder.setProjectDescription(mainApp.getProjectDescription());
        BaristaMessages.ProjectSettings projectSettings = projectSettingsBuilder.build();

        // save project settings to file
        String projectSettingsFileName = mainApp.getProjectSettingsFileName();

        if (projectSettingsFileName != null) {
            // write project settings object to file
            try (FileWriter fileWriter = new FileWriter(projectSettingsFileName)) {
                // write ProjectSettings object in protobuf properties format
                TextFormat.print(projectSettings, fileWriter);
                fileWriter.flush();
            } catch (IOException ex) {
                Logger.getLogger(ProjectOverviewController.class.getName()).log(Level.SEVERE, String.format("Error while writing to file %s", projectSettingsFileName), ex);
            }

            mainApp.setProjectSettingsAreUnchanged(true);
        }
    }
    
    /**
    * Fills all text fields to show details about the configuration.
    * If the specified configuration is null, all text fields are cleared.
    * 
    * @param configuration the configuration or null
    */
   private void showConfigurationDetails(Configuration configuration) {
       if (configuration != null) {
           // Fill the labels with info from the configuration object.
           nameTextField.setText(configuration.getName());
           solverFileNameTextField.setText(configuration.getSolverFileName());
       } else {
           // configuration is null, remove all the text.
           nameTextField.setText("");
           solverFileNameTextField.setText("");
       }
   }
}
