package barista.view;

import barista.MainApp;
import java.io.File;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

/**
 *
 * @author Arik Poznanski
 */
public class MainViewController {

    @FXML
    private MenuItem openProjectMenuItem;
    
    @FXML
    private MenuItem settingsMenuItem;
    
    
    // Reference to the main application
    private MainApp mainApp;

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        
        // set bindings
        Bindings.bindBidirectional(openProjectMenuItem.disableProperty(), mainApp.isApplicationSettingsOpenedProperty());
        Bindings.bindBidirectional(settingsMenuItem.disableProperty(), mainApp.isApplicationSettingsOpenedProperty());
    }

    @FXML
    private void handleOpenProjectAction(ActionEvent event) {
        // select project folder
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Window currentWindow = mainApp.getPrimaryStage().getOwner();
        File selectedFolder = directoryChooser.showDialog(currentWindow);

        if (selectedFolder != null) {
            // use selected project directory
            mainApp.loadProject(selectedFolder.getAbsolutePath());
        } else {
            // don't change loaded project
        }
    }

    @FXML
    private void handleSettingsAction(ActionEvent event) {
        
        // load applications settings UI
        mainApp.showApplicationSettings();
    }
    
    @FXML
    private void handleExitAction(ActionEvent event) {
        Platform.exit();
    }

}
