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
    private TreeTableView solverTreeTableView;

    @FXML
    private TreeTableColumn nameSolverColumn;

    @FXML
    private TreeTableColumn typeSolverColumn;

    @FXML
    private TreeTableColumn valueSolverColumn;

    private TreeItem solverRootItem;

    @FXML
    private TreeTableView trainTreeTableView;

    @FXML
    private TreeTableView testTreeTableView;

    // Reference to the main application.
    private MainApp mainApp;

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

        solverRootItem = new TreeItem();
        solverTreeTableView.setRoot(solverRootItem);
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

                ProtobufPropertyList solverPropertyList = loadProtoObjectToProtoPropertyList(solverParameter);
                ProtobufPropertyList trainPropertyList = loadProtoObjectToProtoPropertyList(trainNetParameter);
                ProtobufPropertyList testPropertyList = loadProtoObjectToProtoPropertyList(testNetParameter);

                // TODO remove when debugging is over
                //System.out.println(solverParameter.toString());
                //System.out.println(trainNetParameter.toString());
                //System.out.println(testNetParameter.toString());
            }

        } else {
            // configuration is null
        }
    }

    private ProtobufPropertyList loadProtoObjectToProtoPropertyList(GeneratedMessage protobufMessage) {

        ProtobufPropertyList protobufPropertyList = new ProtobufPropertyList();

        Descriptor descriptor = protobufMessage.getDescriptorForType();

        List<FieldDescriptor> fieldsDescriptors = descriptor.getFields();

        for (FieldDescriptor fieldDescriptor : fieldsDescriptors) {

            ProtobufProperty protobufProperty = new ProtobufProperty();
            protobufProperty.setName(fieldDescriptor.getName());
            protobufProperty.setType(fieldDescriptor.getType().name());

            if (protobufProperty.getType() == "MESSAGE") {
                protobufProperty.setIsMessage(true);
                protobufProperty.setType(fieldDescriptor.getMessageType().getName());
            } else {
                protobufProperty.setIsMessage(false);
            }

            protobufProperty.setIsOptional(fieldDescriptor.isOptional());
            protobufProperty.setIsRepeated(fieldDescriptor.isRepeated());

            protobufProperty.setHasValue((protobufProperty.getIsOptional()) && (protobufMessage.hasField(fieldDescriptor)));

            // handle repeated message
            if ((protobufProperty.getIsMessage()) && (protobufProperty.getIsRepeated())) {
                ObservableList<ProtobufPropertyList> objectsList = FXCollections.observableArrayList();

                // for each item
                int count = protobufMessage.getRepeatedFieldCount(fieldDescriptor);
                for (int i = 0; i < count; i++) {
                    GeneratedMessage currentMessageItem = (GeneratedMessage) protobufMessage.getRepeatedField(fieldDescriptor, i);
                    ProtobufPropertyList currentObject = loadProtoObjectToProtoPropertyList(currentMessageItem);
                    objectsList.add(currentObject);
                }

                protobufProperty.setValue(objectsList);

            } else if (protobufProperty.getIsRepeated()) {
                // handle repeated
                ObservableList<Object> objectsList = FXCollections.observableArrayList();

                // for each item
                int count = protobufMessage.getRepeatedFieldCount(fieldDescriptor);
                for (int i = 0; i < count; i++) {
                    Object currentItem = protobufMessage.getRepeatedField(fieldDescriptor, i);
                    objectsList.add(currentItem);
                }

                protobufProperty.setValue(objectsList);

            } else if (protobufProperty.getIsMessage()) {
                // handle message
                GeneratedMessage singleMessageItem = (GeneratedMessage) protobufMessage.getField(fieldDescriptor);
                ProtobufPropertyList singleObject = loadProtoObjectToProtoPropertyList(singleMessageItem);
                protobufProperty.setValue(singleObject);
            } else if (protobufProperty.getHasValue()) {
                protobufProperty.setValue(protobufMessage.getField(fieldDescriptor));
            } else if (fieldDescriptor.hasDefaultValue()) {
                protobufProperty.setValue(fieldDescriptor.getDefaultValue());
            }

            //System.out.println(protobufProperty);
            protobufPropertyList.getProtobufPropertyList().add(protobufProperty);
        }

        return protobufPropertyList;
    }
}
