package barista.view;

import barista.MainApp;
import java.io.File;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

/**
 *
 * @author Arik Poznanski
 */
public class RootLayoutController {

    @FXML
    private MenuBar menuBar;

    // Reference to the main application
    private MainApp mainApp;

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleOpenProjectAction(ActionEvent event) {
        // select project directory
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Window currentWindow = menuBar.getScene().getWindow();
        File selectedDirectory = directoryChooser.showDialog(currentWindow);

        if (selectedDirectory != null) {
            // use selected project directory
            mainApp.loadProject(selectedDirectory.getAbsolutePath());
        } else {
            // empty project directory
            mainApp.loadProject("");
        }
    }

    @FXML
    private void handleExitAction(ActionEvent event) {
        Platform.exit();
    }

}
