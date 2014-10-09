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
        this(null);
    }

    /**
     * Constructor with some initial data.
     *
     * @param name
     */
    public Configuration(String name) {
        this.name = new SimpleStringProperty(name);
        this.solverFileName = new SimpleStringProperty("");
        this.setConfigurationSettingsAreUnchanged(true);
    }

    // <editor-fold desc="name property" defaultstate="collapsed">
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

    // <editor-fold desc="solverFileName property" defaultstate="collapsed">
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

    // <editor-fold desc="configurationSettingsAreUnchanged property" defaultstate="collapsed">
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

}
