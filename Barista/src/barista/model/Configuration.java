/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barista.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Arik
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
        this.trainFileName = new SimpleStringProperty("");
        this.testFileName = new SimpleStringProperty("");
    }

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

    private final StringProperty trainFileName;

    public final String getTrainFileName() {
        return trainFileName.get();
    }

    public final void setTrainFileName(String value) {
        this.trainFileName.set(value);
    }

    public StringProperty trainFileNameProperty() {
        return trainFileName;
    }

    private final StringProperty testFileName;

    public final String getTestFileName() {
        return testFileName.get();
    }

    public final void setTestFileName(String value) {
        this.testFileName.set(value);
    }

    public StringProperty testFileNameProperty() {
        return testFileName;
    }

}
