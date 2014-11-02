package barista.view;

import barista.BaristaMessages;
import barista.MainApp;
import barista.model.Configuration;
import barista.model.ProtobufProperty;
import barista.utils.BindingsUtils;
import barista.utils.ProcessUtils;
import barista.utils.StreamGobbler;
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
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellEditEvent;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.util.Callback;
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
    private TextField configurationFolderTextField;

    @FXML
    private TextField configurationNameTextField;

    @FXML
    private TextField configurationDescriptionTextField;

    @FXML
    private Button runConfigurationButton;

    @FXML
    private Button cancelRunConfigurationButton;

    @FXML
    private Button saveConfigurationSettingsButton;

    @FXML
    private Button revertConfigurationSettingsButton;

    @FXML
    private Button cloneConfigurationButton;

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

    @FXML
    private TextArea outputTextArea;

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

        // clear configuration details
        showConfigurationDetails(null, null, false);

        // Add observable list data to the table, with sorting support 
        SortedList<Configuration> sortedData = new SortedList<>(mainApp.getConfigurationList());
        sortedData.comparatorProperty().bind(configurationTable.comparatorProperty());
        configurationTable.setItems(sortedData);

        // add default sort by name
        configurationTable.getSortOrder().add(nameColumn);

        // listen to changes in lastRunningConfiguration
        mainApp.lastRunningConfigurationProperty().addListener(
                (observable, oldValue, newValue) -> {

                    // remove old binding
                    runConfigurationButton.disableProperty().unbind();
                    cancelRunConfigurationButton.disableProperty().unbind();

                    // bind to isRunning of new configuration, if available
                    if (newValue != null) {
                        // when is run button disabled:
                        // - no configuration is selected OR
                        // - lastRunningConfiguration is running
                        runConfigurationButton.disableProperty().bind(
                                Bindings.or(
                                        Bindings.isNull(mainApp.currentConfigurationProperty()),
                                        newValue.isRunningProperty()));

                        // when is cancel button enabled:
                        // - if a configuration is selected AND
                        // - lastRunning = selected config AND
                        // - lastRunning isRunning
                        cancelRunConfigurationButton.disableProperty().bind(Bindings.not(
                                        BindingsUtils.and(
                                                Bindings.isNotNull(mainApp.currentConfigurationProperty()),
                                                Bindings.equal(mainApp.lastRunningConfigurationProperty(), mainApp.currentConfigurationProperty()),
                                                newValue.isRunningProperty())));
                    } else {

                        // when is run button disabled (when no lastRunningConfiguration):
                        // - no configuration is selected OR
                        runConfigurationButton.disableProperty().bind(Bindings.isNull(mainApp.currentConfigurationProperty()));

                        // when is cancel button disabled (when no lastRunningConfiguration):
                        // - always (i.e. until there is a lastRunningConfiguration)
                        cancelRunConfigurationButton.disableProperty().bind(Bindings.isNull(mainApp.lastRunningConfigurationProperty()));

                    }
                });

//        // listen to changes in currentConfiguration
//        mainApp.currentConfigurationProperty().addListener(
//                (observable, oldValue, newValue) -> {
//
//                });
        // set bindings
        Bindings.bindBidirectional(projectFolderLabel.textProperty(), mainApp.projectFolderProperty());
        Bindings.bindBidirectional(projectDescriptionTextField.textProperty(), mainApp.projectDescriptionProperty());
        saveProjectSettingsButton.disableProperty().bind(mainApp.projectSettingsAreUnchangedProperty());
        revertProjectSettingsButton.disableProperty().bind(mainApp.projectSettingsAreUnchangedProperty());

        // when is run button disabled (when no lastRunningConfiguration):
        // - no configuration is selected OR
        runConfigurationButton.disableProperty().bind(Bindings.isNull(mainApp.currentConfigurationProperty()));

        // when is cancel button disabled (when no lastRunningConfiguration):
        // - always (i.e. until there is a lastRunningConfiguration)
        cancelRunConfigurationButton.disableProperty().bind(Bindings.isNull(mainApp.lastRunningConfigurationProperty()));

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
        if (StringUtils.isBlank(caffeFolder)) {
            Dialogs.create()
                    .title("Caffe Folder Not Chosen")
                    .masthead("Caffe folder setting is empty")
                    .message("Please go to application settings and choose a valid caffe folder.")
                    .showWarning();

            return;
        }

        // get solver file name
        String solverFileName = configuration.getSolverFileName();
        if (StringUtils.isBlank(solverFileName)) {
            Dialogs.create()
                    .title("No Solver File")
                    .masthead("Solver file name is empty")
                    .message("Please make sure the selected configuration folder has a solver file in it.")
                    .showWarning();

            return;
        }

        // check solver file exists
        if (!new File(solverFileName).exists()) {
            Dialogs.create()
                    .title("No Solver File")
                    .masthead("Solver file does not exist")
                    .message("Please make sure the selected configuration folder has a solver file in it.")
                    .showWarning();

            return;
        }

        // path to train script inside application folder
        String runTrainScript = "run_train.sh";
        String runTrainScriptFullPath = FileSystems.getDefault().getPath(ProcessUtils.getApplicationFolder(), "scripts", runTrainScript).toString();

        // check train script exists
        if (!new File(runTrainScriptFullPath).exists()) {
            Dialogs.create()
                    .title("No Train Script")
                    .masthead("Could not find train script")
                    .message("Please make sure the file " + runTrainScript + " exists inside the application folder.")
                    .showWarning();

            return;
        }

        // set last running configuration
        mainApp.setLastRunningConfiguration(configuration);

        // generate log file name
        String logFileName = String.format("log_%s.txt", new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()));

        // build command line
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(runTrainScriptFullPath);
        commandArgs.add(caffeFolder);
        commandArgs.add(solverFileName);
        commandArgs.add(logFileName);

        String commandLine = String.join(" ", commandArgs);

        // start process from different thread
        // this is done so the UI stays responsive while the thread is being run
        new Thread(new Runnable() {
            public void run() {
                try {

                    // prepare process for run
                    ProcessBuilder processBuilder = new ProcessBuilder(commandArgs.toArray(new String[0]));

                    // set current directory
                    String configurationFolder = mainApp.getConfigurationFolder(configuration.getFolderName());
                    processBuilder.directory(new File(configurationFolder));

                    Platform.runLater(() -> {
                        // clear output section
                        clearOutputSection();

                        // print command line to output
                        writeToOutputSection("Working directory: ");
                        writeToOutputSection(processBuilder.directory().getAbsolutePath());
                        writeToOutputSection("Running command line: ");
                        writeToOutputSection(commandLine);

                        // mark configuration as running
                        configuration.setIsRunning(true);

                    });

                    // run command line
                    Process process = processBuilder.start();

                    configuration.setRunningProcess(process);

                    // process stdout
                    StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "stdout",
                            (type, line) -> {
                                Platform.runLater(() -> writeToOutputSection(type + ": " + line));
                            }
                    );
                    outputGobbler.start();

                    // process stderr
                    StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "stderr",
                            (type, line) -> {
                                Platform.runLater(() -> writeToOutputSection(type + ": " + line));
                            }
                    );
                    errorGobbler.start();

                    // wait for process to end
                    int exitVal = process.waitFor();

                    Platform.runLater(() -> {

                        // mark configuration finished running
                        configuration.setIsRunning(false);

                        // print exit value
                        writeToOutputSection("Run exit value: " + exitVal);
                    });

                } catch (IOException ex) {
                    Logger.getLogger(ProjectViewController.class.getName()).log(Level.SEVERE, "Failed while running command: " + commandLine, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ProjectViewController.class.getName()).log(Level.SEVERE, "Failed while running command: " + commandLine, ex);
                }
            }
        }).start();

    }

    @FXML
    private void handleCancelRunConfigurationAction(ActionEvent event) {

        // get current configuration
        Configuration configuration = mainApp.getCurrentConfiguration();

        if ((configuration != null) && (configuration.getIsRunning())) {
            Process currentRunningProcess = configuration.getRunningProcess();
            if ((currentRunningProcess != null) && (currentRunningProcess.isAlive())) {
                try {
                    int pid = ProcessUtils.getUnixPID(currentRunningProcess);
                    writeToOutputSection("Killing process tree starting with pid " + pid);
                    ProcessUtils.killProcessTree(currentRunningProcess);
                    writeToOutputSection("Done killing");
                } catch (Exception ex) {
                    Logger.getLogger(ProjectViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    // helper function to add a line to the output section
    private void writeToOutputSection(String line) {
        outputTextArea.appendText(line + System.lineSeparator());
    }

    // helper function to clear output section
    private void clearOutputSection() {
        outputTextArea.clear();
    }

    public void writeConfigurationSettings(BaristaMessages.ConfigurationSettings configurationSettings, String configurationFolder) {
        String configurationSettingsFileName = mainApp.getConfigurationSettingsFileName(configurationFolder);

        // write configuration settings objects to file
        try (FileWriter fileWriter = new FileWriter(configurationSettingsFileName)) {
            // write ConfigurationSettings object in protobuf properties format
            TextFormat.print(configurationSettings, fileWriter);
            fileWriter.flush();
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, String.format("Error while writing to file %s", configurationSettingsFileName), ex);
        }
    }

    
    @FXML
    private void handleSaveConfigurationSettingsAction(ActionEvent event) {

        // get current configuration
        Configuration configuration = mainApp.getCurrentConfiguration();

        // check if folder name has changed
        if (!configuration.getNewFolderName().equals(configuration.getFolderName())) {

            // rename configuration folder 
            String currentProjectFolder = mainApp.getProjectFolder();
            Path currentConfigurationFolderPath = FileSystems.getDefault().getPath(currentProjectFolder, configuration.getFolderName());
            Path newConfigurationFolderPath = FileSystems.getDefault().getPath(currentProjectFolder, configuration.getNewFolderName());

            try {
                Files.move(currentConfigurationFolderPath, newConfigurationFolderPath);
            } catch (IOException ex) {

                Dialogs.create()
                        .title("Folder renamed failed")
                        .masthead("Failed renaming configuraiton folder")
                        .message("Got exception while renaming configuration folder. Aborting. Save operation was aborted.")
                        .showException(ex);

                Logger.getLogger(ProjectViewController.class.getName()).log(Level.SEVERE, String.format("Failed to rename folder [%s] to [%s]", currentConfigurationFolderPath.toString(), newConfigurationFolderPath.toString()), ex);
                return;
            }

            // update folder name field
            configuration.setFolderName(configuration.getNewFolderName());
        }

        // get current configuration settings
        String configurationSettingsFileName = mainApp.getConfigurationSettingsFileName(configuration.getFolderName());
        BaristaMessages.ConfigurationSettings configurationSettings = mainApp.readConfigurationSettings(configurationSettingsFileName);

        // generate configuration settings object
        BaristaMessages.ConfigurationSettings.Builder configurationSettingsBuilder;
        if (configurationSettings != null) {
            configurationSettingsBuilder = BaristaMessages.ConfigurationSettings.newBuilder(configurationSettings);
        } else {
            configurationSettingsBuilder = BaristaMessages.ConfigurationSettings.newBuilder();
        }
        configurationSettingsBuilder.setConfigurationName(configuration.getName());
        configurationSettingsBuilder.setConfigurationDescription(configuration.getDescription());
        configurationSettings = configurationSettingsBuilder.build();

        // save configuration settings
        writeConfigurationSettings(configurationSettings, configuration.getFolderName());
        
        // handle saving solver, train and test files
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
            String trainFileName = FileSystems.getDefault().getPath(mainApp.getProjectFolder(), configuration.getFolderName(), solverParameter.getField(trainNetFieldDescriptor).toString()).toString();
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
            String testFileName = FileSystems.getDefault().getPath(mainApp.getProjectFolder(), configuration.getFolderName(), solverParameter.getField(testNetFieldDescriptor).toString()).toString();
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

        // reset new folder name to current folder name
        configuration.setNewFolderName(configuration.getFolderName());

        // reload configuration settings
        String configurationSettingsFileName = mainApp.getConfigurationSettingsFileName(configuration.getFolderName());
        BaristaMessages.ConfigurationSettings configurationSettings = mainApp.readConfigurationSettings(configurationSettingsFileName);
        configuration.setName(configurationSettings.getConfigurationName());
        configuration.setDescription(configurationSettings.getConfigurationDescription());
        
        // force reload configuration from file
        showConfigurationDetails(configuration, configuration, true);

        // mark current configuration as unchanged
        mainApp.getCurrentConfiguration().setConfigurationSettingsAreUnchanged(true);
    }

    @FXML
    private void handleCloneConfigurationAction(ActionEvent event) {

        // TODO: implement clone configuration
    }

    /**
     * Fills all text fields to show details about the configuration. If the
     * specified configuration is null, all text fields are cleared.
     *
     * @param configuration the configuration or null
     */
    private void showConfigurationDetails(Configuration newConfiguration, Configuration oldConfiguration, boolean forceLoad) {

        mainApp.setCurrentConfiguration(newConfiguration);

        if (newConfiguration != null) {
            // Fill the labels with info from the configuration object.

            // put something in lastRunningConfiguration in case its empty
            if (mainApp.getLastRunningConfiguration() == null) {
                mainApp.setLastRunningConfiguration(newConfiguration);
            }

            // check if we already loaded this configuration
            if ((!newConfiguration.getIsLoaded()) || (forceLoad)) {
                // handle case when configuration is not loaded

                // find solver file name
                String solverFileName = newConfiguration.getSolverFileName();
                if (solverFileName != null) {

                    // build solver object
                    SolverParameter solverParameter = mainApp.readSolverParameter(solverFileName);

                    // build train object
                    Path trainFilePath = FileSystems.getDefault().getPath(mainApp.getProjectFolder(), newConfiguration.getFolderName(), solverParameter.getTrainNet());
                    NetParameter trainNetParameter = mainApp.readNetParameter(trainFilePath.toString());

                    // build test object
                    Path testFilePath = FileSystems.getDefault().getPath(mainApp.getProjectFolder(), newConfiguration.getFolderName(), solverParameter.getTestNet());
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

            // set binding on configuration details 
            if (oldConfiguration != null) {
                Bindings.unbindBidirectional(configurationFolderTextField.textProperty(), oldConfiguration.newFolderNameProperty());
                Bindings.unbindBidirectional(configurationNameTextField.textProperty(), oldConfiguration.nameProperty());
                Bindings.unbindBidirectional(configurationDescriptionTextField.textProperty(), oldConfiguration.descriptionProperty());
            }

            Bindings.bindBidirectional(configurationFolderTextField.textProperty(), newConfiguration.newFolderNameProperty());
            Bindings.bindBidirectional(configurationNameTextField.textProperty(), newConfiguration.nameProperty());
            Bindings.bindBidirectional(configurationDescriptionTextField.textProperty(), newConfiguration.descriptionProperty());

            // reset bindings
            saveConfigurationSettingsButton.disableProperty().unbind();
            revertConfigurationSettingsButton.disableProperty().unbind();
            cloneConfigurationButton.disableProperty().unbind();
            saveConfigurationSettingsButton.disableProperty().bind(newConfiguration.configurationSettingsAreUnchangedProperty());
            revertConfigurationSettingsButton.disableProperty().bind(newConfiguration.configurationSettingsAreUnchangedProperty());
            cloneConfigurationButton.disableProperty().bind(Bindings.not(newConfiguration.configurationSettingsAreUnchangedProperty()));

        } else {

            // configuration is null so we should empty the configuration details
            // clear solver tree
            populateTreeTableView(solverTreeTableView, null);

            // clear train tree
            populateTreeTableView(trainTreeTableView, null);

            // clear test tree
            populateTreeTableView(testTreeTableView, null);

            // remove binding on configuration details 
            if (oldConfiguration != null) {
                Bindings.unbindBidirectional(configurationFolderTextField.textProperty(), oldConfiguration.newFolderNameProperty());
                Bindings.unbindBidirectional(configurationNameTextField.textProperty(), oldConfiguration.nameProperty());
                Bindings.unbindBidirectional(configurationDescriptionTextField.textProperty(), oldConfiguration.descriptionProperty());
            }

            // reset bindings
            saveConfigurationSettingsButton.disableProperty().unbind();
            revertConfigurationSettingsButton.disableProperty().unbind();
            cloneConfigurationButton.disableProperty().unbind();
            saveConfigurationSettingsButton.disableProperty().set(true);
            revertConfigurationSettingsButton.disableProperty().set(true);
            cloneConfigurationButton.disableProperty().set(true);
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
