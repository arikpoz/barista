/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barista.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Arik
 */
public class ProtobufPropertyList {
    
    // <editor-fold desc="protobufPropertyList property" defaultstate="collapsed">
    /**
     * The data as an observable list of Configurations.
     */
    private ObservableList<ProtobufProperty> protobufPropertyList = FXCollections.observableArrayList();

    /**
     * Returns the data as an observable list of Configurations.
     *
     * @return
     */
    public ObservableList<ProtobufProperty> getProtobufPropertyList() {
        return protobufPropertyList;
    }
    // </editor-fold>
}
