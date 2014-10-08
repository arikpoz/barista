/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barista.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Arik
 */
public class ProtobufProperty {

    public ProtobufProperty() {

        // listen on hasValue changes
        hasValueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // remove actual value if hasValue is set to false
                    if (newValue == false) {

                        /* if (getIsRepeated()) {
                         // if list

                         // remove all children
                         getChildren().clear();

                         } else 
                         */
                        // if complex type
                        if (getIsMessage()) {
                            // reset hasValue of all children
                            for (ProtobufProperty childProtobufProperty : getChildren()) {
                                childProtobufProperty.setHasValue(false);
                            }
                        } else if (getHasDefaultValue()) {
                            // if simple type with default value
                            setValue(getDefaultValue());

                        } else {
                            // if simple type with not default value

                            // clear value
                            setValue(null);
                        }
                    }
                });
    }

    // <editor-fold desc="name property" defaultstate="collapsed">
    private final StringProperty name = new SimpleStringProperty();

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

    // <editor-fold desc="type property" defaultstate="collapsed">
    private final StringProperty type = new SimpleStringProperty();

    public final String getType() {
        return type.get();
    }

    public final void setType(String value) {
        this.type.set(value);
    }

    public StringProperty typeProperty() {
        return type;
    }
    // </editor-fold>

    // <editor-fold desc="hasValue property" defaultstate="collapsed">
    private final BooleanProperty hasValue = new SimpleBooleanProperty();

    public final Boolean getHasValue() {
        return hasValue.get();
    }

    public final void setHasValue(Boolean value) {
        this.hasValue.set(value);
    }

    public BooleanProperty hasValueProperty() {
        return hasValue;
    }
    // </editor-fold>

    // <editor-fold desc="value property" defaultstate="collapsed">
    private final StringProperty value = new SimpleStringProperty();

    public final String getValue() {
        return value.get();
    }

    public final void setValue(String value) {
        this.value.set(value);
    }

    public StringProperty valueProperty() {
        return value;
    }
    // </editor-fold>

    // <editor-fold desc="hasDefaultValue property" defaultstate="collapsed">
    private final BooleanProperty hasDefaultValue = new SimpleBooleanProperty();

    public final Boolean getHasDefaultValue() {
        return hasDefaultValue.get();
    }

    public final void setHasDefaultValue(Boolean value) {
        this.hasDefaultValue.set(value);
    }

    public BooleanProperty hasDefaultValueProperty() {
        return hasDefaultValue;
    }
    // </editor-fold>

    // <editor-fold desc="defaultValue property" defaultstate="collapsed">
    private final StringProperty defaultValue = new SimpleStringProperty();

    public final String getDefaultValue() {
        return defaultValue.get();
    }

    public final void setDefaultValue(String value) {
        this.defaultValue.set(value);
    }

    public StringProperty defaultValueProperty() {
        return defaultValue;
    }
    // </editor-fold>

    // <editor-fold desc="isMessage property" defaultstate="collapsed">
    private final BooleanProperty isMessage = new SimpleBooleanProperty();

    public final Boolean getIsMessage() {
        return isMessage.get();
    }

    public final void setIsMessage(Boolean value) {
        this.isMessage.set(value);
    }

    public BooleanProperty isMessageProperty() {
        return isMessage;
    }
    // </editor-fold>

    // <editor-fold desc="isOptional property" defaultstate="collapsed">
    private final BooleanProperty isOptional = new SimpleBooleanProperty();

    public final Boolean getIsOptional() {
        return isOptional.get();
    }

    public final void setIsOptional(Boolean value) {
        this.isOptional.set(value);
    }

    public BooleanProperty isOptionalProperty() {
        return isOptional;
    }
    // </editor-fold>

    // <editor-fold desc="isRepeated property" defaultstate="collapsed">
    private final BooleanProperty isRepeated = new SimpleBooleanProperty();

    public final Boolean getIsRepeated() {
        return isRepeated.get();
    }

    public final void setIsRepeated(Boolean value) {
        this.isRepeated.set(value);
    }

    public BooleanProperty isRepeatedProperty() {
        return isRepeated;
    }
    // </editor-fold>

    // <editor-fold desc="children property" defaultstate="collapsed">
    /**
     * The data as an observable list of Configurations.
     */
    private ObservableList<ProtobufProperty> children = FXCollections.observableArrayList();

    /**
     * Returns the data as an observable list of Configurations.
     *
     * @return
     */
    public ObservableList<ProtobufProperty> getChildren() {
        return children;
    }
    // </editor-fold>

    /**
     * Intended only for debugging.
     *
     * <P>
     * Here, the contents of every field are placed into the result, with one
     * field per line.
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        String NEW_LINE = System.getProperty("line.separator");

        result.append(this.getClass().getName()).append(" Object {").append(NEW_LINE);
        result.append(" Name: ").append(getName()).append(NEW_LINE);
        result.append(" Type: ").append(getType()).append(NEW_LINE);
        result.append(" HasValue: ").append(getHasValue()).append(NEW_LINE);
        result.append(" Value: ").append(getValue()).append(NEW_LINE);
        result.append(" IsMessage: ").append(getIsMessage()).append(NEW_LINE);
        result.append(" IsOptional: ").append(getIsOptional()).append(NEW_LINE);
        result.append(" IsRepeated: ").append(getIsRepeated()).append(NEW_LINE);
        result.append("}");

        return result.toString();
    }

}
