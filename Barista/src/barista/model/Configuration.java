package barista.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Arik Poznanski
 */
public class Configuration {

    /**
     * Default constructor.
     */
    public Configuration() {
        this(null, null);
    }

    /**
     * Constructor with some initial data.
     *
     * @param folderName - configuration folder name
     * @param solverFileName - name of solver file
     */
    public Configuration(String folderName, String solverFileName) {
        this.folderName = new SimpleStringProperty(folderName);
        this.solverFileName = new SimpleStringProperty(solverFileName);
        this.setConfigurationSettingsAreUnchanged(true);
        this.setIsLoaded(false);
        this.setIsRunning(false);
    }

    // <editor-fold desc="folderName javafx property" defaultstate="collapsed">
    private final StringProperty folderName;

    public final String getFolderName() {
        return folderName.get();
    }

    public final void setFolderName(String value) {
        this.folderName.set(value);
    }

    public StringProperty folderNameProperty() {
        return folderName;
    }
    // </editor-fold>

    // <editor-fold desc="newFolder javafx property" defaultstate="collapsed">
    private final StringProperty newFolder = new SimpleStringProperty();

    public final String getNewFolder() {
        return newFolder.get();
    }

    public final void setNewFolder(String value) {
        newFolder.set(value);
    }

    public StringProperty newFolderProperty() {
        return newFolder;
    }
    // </editor-fold>

    // <editor-fold desc="description javafx property" defaultstate="collapsed">
    private final StringProperty description = new SimpleStringProperty();

    public final String getDescription() {
        return description.get();
    }

    public final void setDescription(String value) {
        description.set(value);
    }

    public StringProperty descriptionProperty() {
        return description;
    }
    // </editor-fold>
    
    // <editor-fold desc="solverFileName javafx property" defaultstate="collapsed">
    private final StringProperty solverFileName;

    public final String getSolverFileName() {
        return solverFileName.get();
    }

    public final void setSolverFileName(String value) {
        this.solverFileName.set(value);
    }

    public StringProperty solverFileNameProperty() {
        return solverFileName;
    }
    // </editor-fold>

    // <editor-fold desc="configurationSettingsAreUnchanged javafx property" defaultstate="collapsed">
    private final BooleanProperty configurationSettingsAreUnchanged = new SimpleBooleanProperty();

    public final Boolean getConfigurationSettingsAreUnchanged() {
        return configurationSettingsAreUnchanged.get();
    }

    public final void setConfigurationSettingsAreUnchanged(Boolean value) {
        configurationSettingsAreUnchanged.set(value);
    }

    public BooleanProperty configurationSettingsAreUnchangedProperty() {
        return configurationSettingsAreUnchanged;
    }
    // </editor-fold>

    // <editor-fold desc="isLoaded javafx property" defaultstate="collapsed">
    private final BooleanProperty isLoaded = new SimpleBooleanProperty();

    public final Boolean getIsLoaded() {
        return isLoaded.get();
    }

    public final void setIsLoaded(Boolean value) {
        isLoaded.set(value);
    }

    public BooleanProperty isLoadedProperty() {
        return isLoaded;
    }
    // </editor-fold>
    
    // <editor-fold desc="isRunning javafx property" defaultstate="collapsed">
    private final BooleanProperty isRunning = new SimpleBooleanProperty();

    public final Boolean getIsRunning() {
        return isRunning.get();
    }

    public final void setIsRunning(Boolean value) {
        isRunning.set(value);
    }

    public BooleanProperty isRunningProperty() {
        return isRunning;
    }
    // </editor-fold>
    
    // <editor-fold desc="runningProcess property" defaultstate="collapsed">
    private Process runningProcess;
    
    public final Process getRunningProcess(){
        return runningProcess;
    }
    
    public final void setRunningProcess(Process value){
        runningProcess = value;
    }
    // </editor-fold>
    
    // <editor-fold desc="solverProtobufProperty property" defaultstate="collapsed">
    private ProtobufProperty solverProtobufProperty;

    public final ProtobufProperty getSolverProtobufProperty() {
        return solverProtobufProperty;
    }

    public final void setSolverProtobufProperty(ProtobufProperty value) {
        this.solverProtobufProperty = value;
    }
    // </editor-fold>
    
    // <editor-fold desc="trainProtobufProperty property" defaultstate="collapsed">
    private ProtobufProperty trainProtobufProperty;

    public final ProtobufProperty getTrainProtobufProperty() {
        return trainProtobufProperty;
    }

    public final void setTrainProtobufProperty(ProtobufProperty value) {
        this.trainProtobufProperty = value;
    }
    // </editor-fold>
    
    // <editor-fold desc="testProtobufProperty property" defaultstate="collapsed">
    private ProtobufProperty testProtobufProperty;

    public final ProtobufProperty getTestProtobufProperty() {
        return testProtobufProperty;
    }

    public final void setTestProtobufProperty(ProtobufProperty value) {
        this.testProtobufProperty = value;
    }
    // </editor-fold>

}
