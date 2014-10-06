/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barista.view;

import barista.BaristaMessages;
import barista.MainApp;
import barista.model.Configuration;
import barista.model.ProtobufProperty;
import barista.model.ProtobufPropertyList;
import caffe.Caffe;
import caffe.Caffe.NetParameter;
import caffe.Caffe.SolverParameter;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.TextFormat;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;

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
    private TreeTableView<ProtobufProperty> solverTreeTableView;

    @FXML
    private TreeTableColumn<ProtobufProperty, String> nameSolverColumn = new TreeTableColumn<>("Name");

    @FXML
    private TreeTableColumn<ProtobufProperty, String> typeSolverColumn = new TreeTableColumn<>("Type");

    @FXML
    private TreeTableColumn<ProtobufProperty, Object> valueSolverColumn = new TreeTableColumn<>("Value");

    @FXML
    private TreeTableView<ProtobufProperty> trainTreeTableView;

    @FXML
    private TreeTableView<ProtobufProperty> testTreeTableView;

    // Reference to the main application.
    private MainApp mainApp;

    private TreeItem<ProtobufProperty> solverRootItem;
    private TreeItem<ProtobufProperty> trainRootItem;
    private TreeItem<ProtobufProperty> testRootItem;

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

        // initialize the configuration table columns
        nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());

        // clear configuration details
        showConfigurationDetails(null);

        // listen for selection changes and show the configuration details when changed
        configurationTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showConfigurationDetails(newValue));

        // set root element for trees
        solverRootItem = new TreeItem<ProtobufProperty>();
        solverTreeTableView.setRoot(solverRootItem);
        nameSolverColumn.setCellValueFactory(new TreeItemPropertyValueFactory("name"));
        typeSolverColumn.setCellValueFactory(new TreeItemPropertyValueFactory("type"));
        valueSolverColumn.setCellValueFactory(new TreeItemPropertyValueFactory("value"));
        solverTreeTableView.getColumns().setAll(nameSolverColumn,typeSolverColumn,valueSolverColumn);

        trainRootItem = new TreeItem<ProtobufProperty>();
        trainTreeTableView.setRoot(trainRootItem);

        testRootItem = new TreeItem<ProtobufProperty>();
        testTreeTableView.setRoot(testRootItem);
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

    private File findSolverFile(String configurationName) {

        Path configurationFilePath = FileSystems.getDefault().getPath(mainApp.getProjectFolder(), configurationName);
        File folder = new File(configurationFilePath.toString());

        FilenameFilter solveFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                if ((lowercaseName.endsWith(".prototxt")) && (lowercaseName.contains("solver"))) {
                    return true;
                } else {
                    return false;
                }
            }
        };

        File[] files = folder.listFiles(solveFilter);

        if (files.length == 0) {
            return null;
        } else {
            return files[0];
        }
    }

    /**
     * Fills all text fields to show details about the configuration. If the
     * specified configuration is null, all text fields are cleared.
     *
     * @param configuration the configuration or null
     */
    private void showConfigurationDetails(Configuration configuration) {
        if (configuration != null) {
            // Fill the labels with info from the configuration object.

            // find solver file name
            File solverFile = findSolverFile(configuration.getName());
            if (solverFile != null) {

                // build solver object
                SolverParameter solverParameter = mainApp.readSolverParameter(solverFile.getAbsolutePath());

                Path trainFilePath = FileSystems.getDefault().getPath(mainApp.getProjectFolder(), configuration.getName(), solverParameter.getTrainNet());
                NetParameter trainNetParameter = mainApp.readNetParameter(trainFilePath.toString());

                Path testFilePath = FileSystems.getDefault().getPath(mainApp.getProjectFolder(), configuration.getName(), solverParameter.getTestNet());
                NetParameter testNetParameter = mainApp.readNetParameter(testFilePath.toString());

                ProtobufProperty solverProperty = initLoadProtoObjectToProtobufProperty("solver", solverParameter);
                ProtobufProperty trainProperty = initLoadProtoObjectToProtobufProperty("train", trainNetParameter);
                ProtobufProperty testProperty = initLoadProtoObjectToProtobufProperty("test", testNetParameter);

                populateTreeTableView(solverRootItem, solverProperty);
                
                // TODO remove when debugging is over
                //System.out.println(solverParameter.toString());
                //System.out.println(trainNetParameter.toString());
                //System.out.println(testNetParameter.toString());
            }

        } else {
            // configuration is null
        }
    }

    private ProtobufProperty initLoadProtoObjectToProtobufProperty(String rootName, GeneratedMessage protobufMessage) {

        Descriptor descriptor = protobufMessage.getDescriptorForType();

        // create root object to return
        ProtobufProperty rootProtobufProperty = new ProtobufProperty();
        rootProtobufProperty.setName(rootName);
        rootProtobufProperty.setType(descriptor.getName());
        rootProtobufProperty.setValue("");
        rootProtobufProperty.setHasValue(true);
        rootProtobufProperty.setIsMessage(true);
        rootProtobufProperty.setIsOptional(false);
        rootProtobufProperty.setIsRepeated(false);
        
        loadProtoObjectToProtobufProperty(rootProtobufProperty, protobufMessage);
        
        return rootProtobufProperty;
    }
    
    private void loadProtoObjectToProtobufProperty(ProtobufProperty rootProtobufProperty, GeneratedMessage protobufMessage) {

        Descriptor descriptor = protobufMessage.getDescriptorForType();
        List<FieldDescriptor> fieldsDescriptors = descriptor.getFields();
        
        // get children property
        ObservableList<ProtobufProperty> children = rootProtobufProperty.getChildren();
                
        // go over all fields in the given protobuf message
        for (FieldDescriptor fieldDescriptor : fieldsDescriptors) {

            // create new property
            ProtobufProperty protobufProperty = new ProtobufProperty();
            
            // add property as child of root 
            children.add(protobufProperty);

            // set property fields
            protobufProperty.setName(fieldDescriptor.getName());
            protobufProperty.setType(fieldDescriptor.getType().name());
            protobufProperty.setIsOptional(fieldDescriptor.isOptional());
            protobufProperty.setIsRepeated(fieldDescriptor.isRepeated());

            // set type for complex types
            if (protobufProperty.getType().equals("MESSAGE")) {
                protobufProperty.setIsMessage(true);
                protobufProperty.setType(fieldDescriptor.getMessageType().getName());
            } else {
                protobufProperty.setIsMessage(false);
            }

            // set type for repeated fields
            if (protobufProperty.getIsRepeated()) {
                protobufProperty.setType("list");
            }
            
            // property has a value if it is optional and a value exists, or if it repeated and at least one value exists
            protobufProperty.setHasValue( ( (protobufProperty.getIsOptional()) && (protobufMessage.hasField(fieldDescriptor)) ) || 
                                            ((protobufProperty.getIsRepeated()) && (protobufMessage.getRepeatedFieldCount(fieldDescriptor) > 0)) );

            // handle repeated message
            if ((protobufProperty.getIsMessage()) && (protobufProperty.getIsRepeated())) {
                
                protobufProperty.setValue("");
                
                ObservableList<ProtobufProperty> grandChildren = protobufProperty.getChildren();

                // for each item
                int count = protobufMessage.getRepeatedFieldCount(fieldDescriptor);
                for (int i = 0; i < count; i++) {
                    
                    ProtobufProperty grandChild = new ProtobufProperty();
                    grandChild.setHasValue(true);
                    grandChild.setIsMessage(true);
                    grandChild.setIsOptional(false);
                    grandChild.setIsRepeated(false);
                    grandChild.setName("[" + Integer.toString(i) + "]");
                    grandChild.setType(fieldDescriptor.getMessageType().getName());
                    GeneratedMessage singleMessageItem = (GeneratedMessage) protobufMessage.getRepeatedField(fieldDescriptor, i);
                    loadProtoObjectToProtobufProperty(grandChild,singleMessageItem);
                    grandChildren.add(grandChild);
                }

            } else if (protobufProperty.getIsRepeated()) {
                // handle repeated
                
                protobufProperty.setValue("");

                ObservableList<ProtobufProperty> grandChildren = protobufProperty.getChildren();
                
                // for each item
                int count = protobufMessage.getRepeatedFieldCount(fieldDescriptor);
                for (int i = 0; i < count; i++) {
                    
                    ProtobufProperty grandChild = new ProtobufProperty();
                    grandChild.setHasValue(true);
                    grandChild.setIsMessage(false);
                    grandChild.setIsOptional(false);
                    grandChild.setIsRepeated(false);
                    grandChild.setName("[" + Integer.toString(i) + "]");
                    grandChild.setType(fieldDescriptor.getType().name());
                    grandChild.setValue(protobufMessage.getRepeatedField(fieldDescriptor, i).toString());
                    grandChildren.add(grandChild);
                }
                
            } else if (protobufProperty.getIsMessage()) {
                // handle message
                GeneratedMessage singleMessageItem = (GeneratedMessage) protobufMessage.getField(fieldDescriptor);
                loadProtoObjectToProtobufProperty(protobufProperty, singleMessageItem);
            } else if (protobufProperty.getHasValue()) {
                protobufProperty.setValue(protobufMessage.getField(fieldDescriptor).toString());
            } else if (fieldDescriptor.hasDefaultValue()) {
                protobufProperty.setValue(fieldDescriptor.getDefaultValue().toString());
            }
        }
    }

    private void populateTreeTableView(TreeItem<ProtobufProperty> currentRootItem, ProtobufProperty protobufProperty) {
        
        currentRootItem.setValue(protobufProperty);
        
        // for each property in list
        for (ProtobufProperty childProtobufProperty : protobufProperty.getChildren()) {

            // create tree item
            TreeItem<ProtobufProperty> newItem = new TreeItem<>();

            // populate sub-tree
            populateTreeTableView(newItem, childProtobufProperty);

            // add tree item to current root
            currentRootItem.getChildren().add(newItem);
        }
    }
}
