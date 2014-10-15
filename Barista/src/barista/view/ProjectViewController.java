package barista.view;

import barista.BaristaMessages;
import barista.MainApp;
import barista.model.Configuration;
import barista.model.ProtobufProperty;
import barista.utils.StringUtils;
import caffe.Caffe.NetParameter;
import caffe.Caffe.SolverParameter;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.TextFormat;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.Callback;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;

/**
 *
 * @author Arik Poznanski
 */
public class ProjectViewController {

    @FXML
    private Label projectFolderLabel;

    @FXML
    private TextField projectDescriptionTextField;

    @FXML
    private Button saveProjectSettingsButton;

    @FXML
    private Button revertProjectSettingsButton;

    @FXML
    private Button saveConfigurationSettingsButton;

    @FXML
    private Button revertConfigurationSettingsButton;

    @FXML
    private TableView<Configuration> configurationTable;

    @FXML
    private TableColumn<Configuration, String> nameColumn;

    @FXML
    private TreeTableViewWithItems<ProtobufProperty> solverTreeTableView;

    @FXML
    private TreeTableViewWithItems<ProtobufProperty> trainTreeTableView;

    @FXML
    private TreeTableViewWithItems<ProtobufProperty> testTreeTableView;

    // Reference to the main application.
    private MainApp mainApp;

    /**
     * The constructor. The constructor is called before the initialize()
     * method.
     */
    public ProjectViewController() {
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
        showConfigurationDetails(null, null, false);

        // listen for selection changes and show the configuration details when changed
        configurationTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showConfigurationDetails(newValue, oldValue, false));

        // configure properties trees 
        configurePropertiesTree(solverTreeTableView);
        configurePropertiesTree(trainTreeTableView);
        configurePropertiesTree(testTreeTableView);
    }

    /**
     * Is called by the main application to give a reference back to itself.
     *
     * @param mainApp
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;

        // Add observable list data to the table, with sorting support 
        SortedList<Configuration> sortedData = new SortedList<>(mainApp.getConfigurationList());
        sortedData.comparatorProperty().bind(configurationTable.comparatorProperty());
        configurationTable.setItems(sortedData);

        // add default sort by name
        configurationTable.getSortOrder().add(nameColumn);

        // set bindings
        Bindings.bindBidirectional(projectFolderLabel.textProperty(), mainApp.projectFolderProperty());
        Bindings.bindBidirectional(projectDescriptionTextField.textProperty(), mainApp.projectDescriptionProperty());
        Bindings.bindBidirectional(saveProjectSettingsButton.disableProperty(), mainApp.projectSettingsAreUnchangedProperty());
        Bindings.bindBidirectional(revertProjectSettingsButton.disableProperty(), mainApp.projectSettingsAreUnchangedProperty());
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
                Logger.getLogger(ProjectViewController.class.getName()).log(Level.SEVERE, String.format("Error while writing to file %s", projectSettingsFileName), ex);
            }

            mainApp.setProjectSettingsAreUnchanged(true);
        }
    }

    @FXML
    private void handleRevertProjectSettingsAction(ActionEvent event) {

        mainApp.loadProjectSettings();
    }

    @FXML
    private void handleRunConfigurationAction(ActionEvent event) {

        // get current configuration
        Configuration configuration = mainApp.getCurrentConfiguration();

        // get caffe folder
        String caffeFolder = mainApp.getCaffeFolder();
        // TODO uncomment this before commit
        if (StringUtils.isBlank(caffeFolder)){
            Dialogs.create()
                    .title("Caffe Folder Not Chosen")
                    .masthead("Caffe Folder Setting is Empty")
                    .message("Please go to application settings and choose a valid caffe folder.")
                    .showWarning();

            return;
        }

        String solverFileName = configuration.getSolverFileName();
        String trainTool = "/build/tools/train_net.bin";
        String commandLine = caffeFolder + trainTool + " " + solverFileName;

        try {
            // run command line
            Runtime.getRuntime().exec(commandLine);
        } catch (IOException ex) {
            Logger.getLogger(ProjectViewController.class.getName()).log(Level.SEVERE, "Failed while running command: " + commandLine, ex);
        }
    }

    @FXML
    private void handleSaveConfigurationSettingsAction(ActionEvent event) {

        // get current configuration
        Configuration configuration = mainApp.getCurrentConfiguration();

        // save solver file
        String solverFileName = configuration.getSolverFileName();
        if (solverFileName != null) {

            // get updated solver parameter object
            Descriptor solverParameterDescriptor = SolverParameter.getDescriptor();
            DynamicMessage solverParameter = loadProtobufPropertyToProtoObject(configuration.getSolverProtobufProperty(), solverParameterDescriptor);

            // write solver object to file
            try (FileWriter fileWriter = new FileWriter(solverFileName)) {
                TextFormat.print(solverParameter, fileWriter);
                fileWriter.flush();
            } catch (IOException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, String.format("Error while writing to file %s", solverFileName), ex);
            }

            Descriptor netParameterDescriptor = NetParameter.getDescriptor();

            // get updated train parameter object
            DynamicMessage trainParameter = loadProtobufPropertyToProtoObject(configuration.getTrainProtobufProperty(), netParameterDescriptor);

            // write train object to file
            FieldDescriptor trainNetFieldDescriptor = solverParameterDescriptor.findFieldByName("train_net");
            String trainFileName = FileSystems.getDefault().getPath(mainApp.getProjectFolder(), configuration.getName(), solverParameter.getField(trainNetFieldDescriptor).toString()).toString();
            try (FileWriter fileWriter = new FileWriter(trainFileName)) {
                TextFormat.print(trainParameter, fileWriter);
                fileWriter.flush();
            } catch (IOException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, String.format("Error while writing to file %s", trainFileName), ex);
            }

            // get updated test parameter object
            DynamicMessage testParameter = loadProtobufPropertyToProtoObject(configuration.getTestProtobufProperty(), netParameterDescriptor);

            // write test object to file
            FieldDescriptor testNetFieldDescriptor = solverParameterDescriptor.findFieldByName("test_net");
            String testFileName = FileSystems.getDefault().getPath(mainApp.getProjectFolder(), configuration.getName(), solverParameter.getField(testNetFieldDescriptor).toString()).toString();
            try (FileWriter fileWriter = new FileWriter(testFileName)) {
                TextFormat.print(testParameter, fileWriter);
                fileWriter.flush();
            } catch (IOException ex) {
                Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, String.format("Error while writing to file %s", testFileName), ex);
            }
        }

        // mark current configuration as unchanged
        mainApp.getCurrentConfiguration().setConfigurationSettingsAreUnchanged(true);
    }

    @FXML
    private void handleRevertConfigurationSettingsAction(ActionEvent event) {

        // get current configuration
        Configuration configuration = mainApp.getCurrentConfiguration();

        // force reload configuration from file
        showConfigurationDetails(configuration, configuration, true);

        // mark current configuration as unchanged
        mainApp.getCurrentConfiguration().setConfigurationSettingsAreUnchanged(true);
    }

    /**
     * Fills all text fields to show details about the configuration. If the
     * specified configuration is null, all text fields are cleared.
     *
     * @param configuration the configuration or null
     */
    private void showConfigurationDetails(Configuration newConfiguration, Configuration oldConfiguration, boolean forceLoad) {
        if (newConfiguration != null) {
            // Fill the labels with info from the configuration object.

            mainApp.setCurrentConfiguration(newConfiguration);

            // check if we already loaded this configuration
            if ((!newConfiguration.getIsLoaded()) || (forceLoad)) {
                // handle case when configuration is not loaded

                // find solver file name
                String solverFileName = newConfiguration.getSolverFileName();
                if (solverFileName != null) {

                    // build solver object
                    SolverParameter solverParameter = mainApp.readSolverParameter(solverFileName);

                    // build train object
                    Path trainFilePath = FileSystems.getDefault().getPath(mainApp.getProjectFolder(), newConfiguration.getName(), solverParameter.getTrainNet());
                    NetParameter trainNetParameter = mainApp.readNetParameter(trainFilePath.toString());

                    // build test object
                    Path testFilePath = FileSystems.getDefault().getPath(mainApp.getProjectFolder(), newConfiguration.getName(), solverParameter.getTestNet());
                    NetParameter testNetParameter = mainApp.readNetParameter(testFilePath.toString());

                    // load solver data
                    newConfiguration.setSolverProtobufProperty(initLoadProtoObjectToProtobufProperty("solver", solverParameter));

                    // load train data
                    newConfiguration.setTrainProtobufProperty(initLoadProtoObjectToProtobufProperty("train", trainNetParameter));

                    // load test data
                    newConfiguration.setTestProtobufProperty(initLoadProtoObjectToProtobufProperty("test", testNetParameter));

                    // mark configuration as loaded
                    newConfiguration.setIsLoaded(true);
                }
            }

            // populate solver tree
            populateTreeTableView(solverTreeTableView, newConfiguration.getSolverProtobufProperty());

            // populate train tree
            populateTreeTableView(trainTreeTableView, newConfiguration.getTrainProtobufProperty());

            // populate test tree
            populateTreeTableView(testTreeTableView, newConfiguration.getTestProtobufProperty());

            // remove old bindings
            if (oldConfiguration != null) {
                Bindings.unbindBidirectional(saveConfigurationSettingsButton.disableProperty(), oldConfiguration.configurationSettingsAreUnchangedProperty());
                Bindings.unbindBidirectional(revertConfigurationSettingsButton.disableProperty(), oldConfiguration.configurationSettingsAreUnchangedProperty());
            }

            // add new bindings
            Bindings.bindBidirectional(saveConfigurationSettingsButton.disableProperty(), newConfiguration.configurationSettingsAreUnchangedProperty());
            Bindings.bindBidirectional(revertConfigurationSettingsButton.disableProperty(), newConfiguration.configurationSettingsAreUnchangedProperty());

        } else {
            // configuration is null
        }
    }

    private static EnumValueDescriptor toEnumValueDescriptor(FieldDescriptor fieldDescriptor, String name) {
        EnumValueDescriptor out = fieldDescriptor.getEnumType().findValueByName(name);
        if (out == null) {
            throw new IllegalArgumentException(String.format("Failed to convert string '%s'" + " to enum value of type '%s'", name, fieldDescriptor.getEnumType().getFullName()));
        }
        return out;
    }

    private DynamicMessage loadProtobufPropertyToProtoObject(ProtobufProperty rootProtobufProperty, Descriptor descriptor) {

        // generate Builder object for DynamicMessage
        DynamicMessage.Builder dynamicMessageBuilder = DynamicMessage.newBuilder(descriptor);

        // go over children of rootProtobufProperty
        for (ProtobufProperty childProtobufProperty : rootProtobufProperty.getChildren()) {

            // get fieldDescriptor by name
            FieldDescriptor currentFieldDescriptor = descriptor.findFieldByName(childProtobufProperty.getName());

            // if field has value
            if (childProtobufProperty.getHasValue()) {
                // handle repeated message
                if ((childProtobufProperty.getIsMessage()) && (childProtobufProperty.getIsRepeated())) {

                    for (ProtobufProperty grandChildProtobufProperty : childProtobufProperty.getChildren()) {

                        Object valueToSet = loadProtobufPropertyToProtoObject(grandChildProtobufProperty, grandChildProtobufProperty.getDescriptor());
                        dynamicMessageBuilder.addRepeatedField(currentFieldDescriptor, valueToSet);
                    }
                } else if (childProtobufProperty.getIsRepeated()) {
                    // handle simple repeated

                    for (ProtobufProperty grandChildProtobufProperty : childProtobufProperty.getChildren()) {

                        Object valueToSet = convertStringToValue(grandChildProtobufProperty.getType(), grandChildProtobufProperty.getValue(), currentFieldDescriptor);
                        dynamicMessageBuilder.addRepeatedField(currentFieldDescriptor, valueToSet);
                    }

                } else if (childProtobufProperty.getIsMessage()) {
                    // handle message
                    Object valueToSet = loadProtobufPropertyToProtoObject(childProtobufProperty, childProtobufProperty.getDescriptor());
                    dynamicMessageBuilder.setField(currentFieldDescriptor, valueToSet);

                } else {
                    // handle simple value
                    Object valueToSet = convertStringToValue(childProtobufProperty.getType(), childProtobufProperty.getValue(), currentFieldDescriptor);
                    dynamicMessageBuilder.setField(currentFieldDescriptor, valueToSet);
                }

            }
        }

        return dynamicMessageBuilder.build();
    }

    private Object convertStringToValue(String valueType, String valueString, FieldDescriptor fieldDescriptor) {
        Object valueToSet = null;

        switch (valueType.toLowerCase()) {
            case "string":
                valueToSet = valueString;
                break;

            case "int32":
                valueToSet = Integer.parseInt(valueString);
                break;

            case "uint32":
                valueToSet = Integer.parseUnsignedInt(valueString);
                break;

            case "int64":
                valueToSet = Long.parseLong(valueString);
                break;

            case "float":
                valueToSet = Float.parseFloat(valueString);
                break;

            case "bool":
                valueToSet = Boolean.parseBoolean(valueString);
                break;

            case "bytes":
                throw new UnsupportedOperationException("NOT IMPLEMENTED (but could be..)!");

            case "enum":
                valueToSet = toEnumValueDescriptor(fieldDescriptor, valueString);
                break;

            default:
                throw new UnsupportedOperationException("NOT IMPLEMENTED (but could be..)!");

        }

        return valueToSet;
    }

    private ProtobufProperty initLoadProtoObjectToProtobufProperty(String rootName, GeneratedMessage protobufMessage) {

        Descriptor descriptor = protobufMessage.getDescriptorForType();

        // create root object to return
        ProtobufProperty rootProtobufProperty = new ProtobufProperty(null);
        rootProtobufProperty.setName(rootName);
        rootProtobufProperty.setType(descriptor.getName());
        rootProtobufProperty.setListItemType(null);
        rootProtobufProperty.setValue(null);
        rootProtobufProperty.setHasValue(true);
        rootProtobufProperty.setIsMessage(true);
        rootProtobufProperty.setIsOptional(false);
        rootProtobufProperty.setIsRepeated(false);
        rootProtobufProperty.setDescriptor(descriptor);

        registerForDataChanges(rootProtobufProperty);

        loadProtoObjectToProtobufProperty(rootProtobufProperty, protobufMessage, null);

        return rootProtobufProperty;
    }

    private void loadProtoObjectToProtobufProperty(ProtobufProperty rootProtobufProperty, GeneratedMessage protobufMessage, Descriptor descriptor) {

        if (protobufMessage != null) {
            descriptor = protobufMessage.getDescriptorForType();
        }
        List<FieldDescriptor> fieldsDescriptors = descriptor.getFields();

        // get children property
        ObservableList<ProtobufProperty> children = rootProtobufProperty.getChildren();

        // go over all fields in the given protobuf message
        for (FieldDescriptor fieldDescriptor : fieldsDescriptors) {

            // create new property
            ProtobufProperty protobufProperty = new ProtobufProperty(rootProtobufProperty);

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
                protobufProperty.setDescriptor(fieldDescriptor.getMessageType());
            } else {
                protobufProperty.setIsMessage(false);
            }

            // set type for repeated fields
            if (protobufProperty.getIsRepeated()) {
                if (protobufProperty.getIsMessage()) {
                    protobufProperty.setType(String.format("LIST<%s>", fieldDescriptor.getMessageType().getName()));
                    protobufProperty.setListItemType(fieldDescriptor.getMessageType().getName());
                } else {
                    protobufProperty.setType(String.format("LIST<%s>", fieldDescriptor.getType().name()));
                    protobufProperty.setListItemType(fieldDescriptor.getType().name());
                }
            }

            // if protobufMessage is not empty
            if (protobufMessage != null) {
                // property has a value if it is optional and a value exists, or if it repeated and at least one value exists
                protobufProperty.setHasValue(((protobufProperty.getIsOptional()) && (protobufMessage.hasField(fieldDescriptor)))
                        || ((protobufProperty.getIsRepeated()) && (protobufMessage.getRepeatedFieldCount(fieldDescriptor) > 0)));
            } else {
                protobufProperty.setHasValue(false);
            }

            // handle repeated message
            if ((protobufProperty.getIsMessage()) && (protobufProperty.getIsRepeated())) {

                protobufProperty.setValue("");

                ObservableList<ProtobufProperty> grandChildren = protobufProperty.getChildren();

                // for each item
                int count = 0;
                if (protobufMessage != null) {
                    count = protobufMessage.getRepeatedFieldCount(fieldDescriptor);
                }
                for (int i = 0; i < count; i++) {

                    ProtobufProperty grandChild = new ProtobufProperty(protobufProperty);
                    grandChild.setHasValue(true);
                    grandChild.setIsMessage(true);
                    grandChild.setIsOptional(false);
                    grandChild.setIsRepeated(false);
                    grandChild.setName("[" + Integer.toString(i + 1) + "]");
                    grandChild.setType(fieldDescriptor.getMessageType().getName());
                    GeneratedMessage singleMessageItem = (GeneratedMessage) protobufMessage.getRepeatedField(fieldDescriptor, i);
                    grandChild.setDescriptor(fieldDescriptor.getMessageType());
                    loadProtoObjectToProtobufProperty(grandChild, singleMessageItem, null);
                    grandChildren.add(grandChild);

                    registerForDataChanges(grandChild);
                }

            } else if (protobufProperty.getIsRepeated()) {
                // handle repeated

                protobufProperty.setValue("");

                ObservableList<ProtobufProperty> grandChildren = protobufProperty.getChildren();

                // for each item
                int count = 0;
                if (protobufMessage != null) {
                    count = protobufMessage.getRepeatedFieldCount(fieldDescriptor);
                }
                for (int i = 0; i < count; i++) {

                    ProtobufProperty grandChild = new ProtobufProperty(protobufProperty);
                    grandChild.setHasValue(true);
                    grandChild.setIsMessage(false);
                    grandChild.setIsOptional(false);
                    grandChild.setIsRepeated(false);
                    grandChild.setName("[" + Integer.toString(i + 1) + "]");
                    grandChild.setType(fieldDescriptor.getType().name());
                    grandChild.setValue(protobufMessage.getRepeatedField(fieldDescriptor, i).toString());
                    grandChildren.add(grandChild);

                    registerForDataChanges(grandChild);
                }

            } else if (protobufProperty.getIsMessage()) {
                // handle message
                if (protobufMessage != null) {
                    GeneratedMessage singleMessageItem = (GeneratedMessage) protobufMessage.getField(fieldDescriptor);
                    loadProtoObjectToProtobufProperty(protobufProperty, singleMessageItem, null);
                } else {
                    Descriptor childDescriptor = fieldDescriptor.getMessageType();
                    loadProtoObjectToProtobufProperty(protobufProperty, null, childDescriptor);
                }
            } else if (protobufProperty.getHasValue()) {
                protobufProperty.setValue(protobufMessage.getField(fieldDescriptor).toString());
            } else if (fieldDescriptor.hasDefaultValue()) {
                protobufProperty.setValue(fieldDescriptor.getDefaultValue().toString());
            }

            protobufProperty.setHasDefaultValue(fieldDescriptor.hasDefaultValue());
            if (fieldDescriptor.hasDefaultValue()) {
                protobufProperty.setDefaultValue(fieldDescriptor.getDefaultValue().toString());
            }

            registerForDataChanges(protobufProperty);
        }
    }

    private void registerForDataChanges(ProtobufProperty protobufProperty) {

        // listen for changes in hasValue property
        protobufProperty.hasValueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!Objects.equals(oldValue, newValue)) {
                        mainApp.getCurrentConfiguration().setConfigurationSettingsAreUnchanged(false);
                    }
                });

        // listen for changes in value property
        protobufProperty.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!oldValue.equals(newValue)) {
                        mainApp.getCurrentConfiguration().setConfigurationSettingsAreUnchanged(false);
                    }
                });
    }

    private void populateTreeTableView(TreeTableViewWithItems<ProtobufProperty> treeTableView, ProtobufProperty protobufProperty) {

        // clear selection
        treeTableView.getSelectionModel().clearSelection();

        // load items to tree
        if (protobufProperty != null) {
            treeTableView.setItems(protobufProperty.getChildren());
        } else {
            treeTableView.setItems(null);
        }
    }

    private void configurePropertiesTree(TreeTableViewWithItems<ProtobufProperty> treeTableView) {

        // set root element for trees
        treeTableView.setRoot(new TreeItem<>());

        // set column resize policy
        treeTableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

        // make table editable
        treeTableView.setEditable(true);

        // hide root element
        treeTableView.setShowRoot(false);

        ObservableList<TreeTableColumn<ProtobufProperty, ?>> columns = treeTableView.getColumns();

        final int NAME_COLUMN_ID = 0;
        final int TYPE_COLUMN_ID = 1;
        final int HASVALUE_COLUMN_ID = 2;
        final int VALUE_COLUMN_ID = 3;

        // generate columns
        TreeTableColumn<ProtobufProperty, String> nameColumn = (TreeTableColumn<ProtobufProperty, String>) columns.get(NAME_COLUMN_ID);
        TreeTableColumn<ProtobufProperty, String> typeColumn = (TreeTableColumn<ProtobufProperty, String>) columns.get(TYPE_COLUMN_ID);
        TreeTableColumn<ProtobufProperty, Boolean> hasValueColumn = (TreeTableColumn<ProtobufProperty, Boolean>) columns.get(HASVALUE_COLUMN_ID);
        TreeTableColumn<ProtobufProperty, String> valueColumn = (TreeTableColumn<ProtobufProperty, String>) columns.get(VALUE_COLUMN_ID);

        // configure name column
        nameColumn.setCellValueFactory(new TreeItemPropertyValueFactory("name"));

        // configure type column
        typeColumn.setCellValueFactory(new TreeItemPropertyValueFactory("type"));

        // configure hasValue column
        hasValueColumn.setCellValueFactory(new TreeItemPropertyValueFactory("hasValue"));
        hasValueColumn.setCellFactory(treeTableColumn -> {
            CheckBoxTreeTableCell<ProtobufProperty, Boolean> checkBoxTreeTableCell = new CheckBoxTreeTableCell<>();
            checkBoxTreeTableCell.setAlignment(Pos.CENTER);
            return checkBoxTreeTableCell;
        });
        hasValueColumn.setEditable(true);

        // configure value column
        valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory("value"));
        valueColumn.setEditable(true);
        valueColumn.setCellFactory(TextFieldTreeTableCell.forTreeTableColumn());
        valueColumn.setOnEditCommit(
                new EventHandler<CellEditEvent<ProtobufProperty, String>>() {
                    @Override
                    public void handle(CellEditEvent<ProtobufProperty, String> t) {
                        // update row value
                        t.getRowValue().getValue().setValue(t.getNewValue());
                    }
                });

        // configure row context menu
        Callback<ProtobufProperty, List<MenuItem>> rowMenuItemFactory = new Callback<ProtobufProperty, List<MenuItem>>() {
            @Override
            public List<MenuItem> call(final ProtobufProperty protobufProperty) {

                // create menu-items list to return
                ArrayList<MenuItem> menuItems = new ArrayList<>();

                // item is list
                if (protobufProperty.getIsRepeated()) {
                    // add menu-item "add item"
                    final MenuItem addItemMenuItem = new MenuItem("Add Item");
                    menuItems.add(addItemMenuItem);

                    // set event handler
                    addItemMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            addNewSubItem(protobufProperty, protobufProperty.getChildren().size());
                            mainApp.getCurrentConfiguration().setConfigurationSettingsAreUnchanged(false);
                        }
                    });
                }

                // if item parent is list
                ProtobufProperty parentProtobufProperty = protobufProperty.getParent();
                if ((parentProtobufProperty != null) && (parentProtobufProperty.getIsRepeated())) {
                    ObservableList<ProtobufProperty> parentChildren = parentProtobufProperty.getChildren();

                    // add menu-item "add new item before"
                    final MenuItem addNewItemBeforeMenuItem = new MenuItem("Add New Item Before");
                    menuItems.add(addNewItemBeforeMenuItem);

                    // set event handler
                    addNewItemBeforeMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            addNewSubItem(parentProtobufProperty, parentChildren.indexOf(protobufProperty));
                            mainApp.getCurrentConfiguration().setConfigurationSettingsAreUnchanged(false);
                        }
                    });

                    // add menu-item "add new item after"
                    final MenuItem addNewItemAfterMenuItem = new MenuItem("Add New Item After");
                    menuItems.add(addNewItemAfterMenuItem);

                    // set event handler
                    addNewItemAfterMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            addNewSubItem(parentProtobufProperty, parentChildren.indexOf(protobufProperty) + 1);
                            mainApp.getCurrentConfiguration().setConfigurationSettingsAreUnchanged(false);
                        }
                    });

                    // add menu-item "remove item"
                    final MenuItem removeItemMenuItem = new MenuItem("Remove Item");
                    menuItems.add(removeItemMenuItem);

                    // set event handler
                    removeItemMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {

                            // remove item from specfic location 
                            int currentIndex = parentChildren.indexOf(protobufProperty);
                            parentChildren.remove(currentIndex);

                            // update names of rest of the list
                            for (int i = currentIndex; i < parentChildren.size(); i++) {
                                parentChildren.get(i).setName("[" + Integer.toString(i + 1) + "]");
                            }

                            mainApp.getCurrentConfiguration().setConfigurationSettingsAreUnchanged(false);
                        }
                    });
                }

                return menuItems;
            }
        };

        treeTableView.setRowFactory(new ContentMenuTreeTableRowFactory<>(rowMenuItemFactory));
    }

    private void addNewSubItem(ProtobufProperty parentProtobufProperty, int insertIndex) {
        ObservableList<ProtobufProperty> parentChildren = parentProtobufProperty.getChildren();

        ProtobufProperty childProtobufProperty = new ProtobufProperty(parentProtobufProperty);
        childProtobufProperty.setHasValue(false);
        childProtobufProperty.setHasDefaultValue(false);
        childProtobufProperty.setIsMessage(parentProtobufProperty.getIsMessage());
        childProtobufProperty.setIsOptional(false);
        childProtobufProperty.setIsRepeated(false);

        childProtobufProperty.setType(parentProtobufProperty.getListItemType());
        childProtobufProperty.setListItemType(null);

        childProtobufProperty.setName("[" + Integer.toString(insertIndex + 1) + "]");
        childProtobufProperty.setDescriptor(parentProtobufProperty.getDescriptor());

        // add item to specific location 
        parentChildren.add(insertIndex, childProtobufProperty);

        // update names of rest of the list
        for (int i = insertIndex + 1; i < parentChildren.size(); i++) {
            parentChildren.get(i).setName("[" + Integer.toString(i + 1) + "]");
        }

        // if the type is complex, we need to build a suitable sub-tree 
        if (childProtobufProperty.getIsMessage()) {
            Descriptor descriptor = parentProtobufProperty.getDescriptor();
            loadProtoObjectToProtobufProperty(childProtobufProperty, null, descriptor);
        }

        registerForDataChanges(childProtobufProperty);
    }
}
