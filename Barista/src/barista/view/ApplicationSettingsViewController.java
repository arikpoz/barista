/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barista.view;

import barista.BaristaMessages;
import barista.MainApp;
import barista.model.SettingsFiles;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

/**
 * FXML Controller class
 *
 * @author Arik
 */
public class ApplicationSettingsViewController implements Initializable {

    // <editor-fold desc="caffeFolder javafx property" defaultstate="collapsed">
    private final StringProperty caffeFolder = new SimpleStringProperty();

    public final String getCaffeFolder() {
        return caffeFolder.get();
    }

    public final void setCaffeFolder(String value) {
        caffeFolder.set(value);
    }

    public StringProperty caffeFolderProperty() {
        return caffeFolder;
    }
    // </editor-fold>

    // <editor-fold desc="lastProjectFolder javafx property" defaultstate="collapsed">
    private final StringProperty lastProjectFolder = new SimpleStringProperty();

    public final String getLastProjectFolder() {
        return lastProjectFolder.get();
    }

    public final void setLastProjectFolder(String value) {
        lastProjectFolder.set(value);
    }

    public StringProperty lastProjectFolderProperty() {
        return lastProjectFolder;
    }
    // </editor-fold>

    // <editor-fold desc="applicationSettingsAreUnchanged javafx property" defaultstate="collapsed">
    private final BooleanProperty applicationSettingsAreUnchanged = new SimpleBooleanProperty();

    public final Boolean getApplicationSettingsAreUnchanged() {
        return applicationSettingsAreUnchanged.get();
    }

    public final void setApplicationSettingsAreUnchanged(Boolean value) {
        applicationSettingsAreUnchanged.set(value);
    }

    public BooleanProperty applicationSettingsAreUnchangedProperty() {
        return applicationSettingsAreUnchanged;
    }
    // </editor-fold>            

    // Reference to the main application.
    private MainApp mainApp;

    @FXML
    TextField caffeFolderTextField;

    @FXML
    TextField lastProjectFolderTextField;

    @FXML
    Button saveApplicationSettingsButton;

    @FXML
    Button revertApplicationSettingsButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        // load application settings
        BaristaMessages.ApplicationSettings applicationSettings = SettingsFiles.readApplicationSettings();
        if (applicationSettings != null) {
            setCaffeFolder(applicationSettings.getCaffeFolder());
            setLastProjectFolder(applicationSettings.getLastProjectFolder());
        }

        // listen for changes
        caffeFolderProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (oldValue == null ? newValue != null : !oldValue.equals(newValue)) {
                        setApplicationSettingsAreUnchanged(false);
                    }
                });
        lastProjectFolderProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (oldValue == null ? newValue != null : !oldValue.equals(newValue)) {
                        setApplicationSettingsAreUnchanged(false);
                    }
                });

        // mark application settings as unchanged
        setApplicationSettingsAreUnchanged(true);

        // set bindings
        Bindings.bindBidirectional(caffeFolderTextField.textProperty(), caffeFolderProperty());
        Bindings.bindBidirectional(lastProjectFolderTextField.textProperty(), lastProjectFolderProperty());

        saveApplicationSettingsButton.disableProperty().bind(applicationSettingsAreUnchangedProperty());
    }

    @FXML
    private void handleSaveApplicationSettingsAction(ActionEvent event) {

        // update application settings file
        SettingsFiles.updateApplicationSettings(
                (applicationSettingsBuilder) -> {
                    applicationSettingsBuilder.setCaffeFolder(getCaffeFolder());
                    applicationSettingsBuilder.setLastProjectFolder(getLastProjectFolder());
                });

        // load previous screen
        mainApp.showPreviousScreen();
    }

    @FXML
    private void handleRevertApplicationSettingsAction(ActionEvent event) {

        // we don't save application settings, so we effectivly revert the user input
        // load previous screen
        mainApp.showPreviousScreen();
    }

    @FXML
    private void handleSelectCaffeFolderAction(ActionEvent event) {
        // select caffe folder
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Window currentWindow = mainApp.getPrimaryStage().getOwner();
        File selectedFolder = directoryChooser.showDialog(currentWindow);

        if (selectedFolder != null) {
            setCaffeFolder(selectedFolder.getAbsolutePath());
        } else {
            // don't change caffe folder setting
        }
    }

    @FXML
    private void handleSelectLastProjectFolderAction(ActionEvent event) {
        // select last project folder
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Window currentWindow = mainApp.getPrimaryStage().getOwner();
        File selectedFolder = directoryChooser.showDialog(currentWindow);

        if (selectedFolder != null) {
            setLastProjectFolder(selectedFolder.getAbsolutePath());
        } else {
            // don't change last project folder setting
        }
    }
}
