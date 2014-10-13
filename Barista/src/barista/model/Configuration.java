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
     * @param name
     */
    public Configuration(String name, String solverFileName) {
        this.name = new SimpleStringProperty(name);
        this.solverFileName = new SimpleStringProperty(solverFileName);
        this.setConfigurationSettingsAreUnchanged(true);
        this.setIsLoaded(false);
    }

    // <editor-fold desc="name javafx property" defaultstate="collapsed">
    private final StringProperty name;

    public final String getName() {
        return name.get();
    }

    public final void setName(String value) {
        this.name.set(value);
    }

    public StringProperty nameProperty() {
        return name;
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
