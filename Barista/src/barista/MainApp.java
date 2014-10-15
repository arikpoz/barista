package barista;

import barista.model.Configuration;
import barista.view.ApplicationSettingsController;
import barista.view.ProjectOverviewController;
import barista.view.RootLayoutController;
import caffe.Caffe.NetParameter;
import caffe.Caffe.SolverParameter;
import com.google.protobuf.TextFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

// test git
/**
 *
 * @author Arik Poznanski
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    // <editor-fold desc="projectFolder javafx property" defaultstate="collapsed">
    private final StringProperty projectFolder = new SimpleStringProperty();

    public final String getProjectFolder() {
        return projectFolder.get();
    }

    public final void setProjectFolder(String value) {
        projectFolder.set(value);
    }

    public StringProperty projectFolderProperty() {
        return projectFolder;
    }
    // </editor-fold>

    // <editor-fold desc="projectDescription javafx property" defaultstate="collapsed">
    private final StringProperty projectDescription = new SimpleStringProperty();

    public final String getProjectDescription() {
        return projectDescription.get();
    }

    public final void setProjectDescription(String value) {
        projectDescription.set(value);
    }

    public StringProperty projectDescriptionProperty() {
        return projectDescription;
    }
    // </editor-fold>

    // <editor-fold desc="projectSettingsAreUnchanged javafx property" defaultstate="collapsed">
    private final BooleanProperty projectSettingsAreUnchanged = new SimpleBooleanProperty();

    public final Boolean getProjectSettingsAreUnchanged() {
        return projectSettingsAreUnchanged.get();
    }

    public final void setProjectSettingsAreUnchanged(Boolean value) {
        projectSettingsAreUnchanged.set(value);
    }

    public BooleanProperty projectSettingsAreUnchangedProperty() {
        return projectSettingsAreUnchanged;
    }
    // </editor-fold>

    // <editor-fold desc="isApplicationSettingsOpened javafx property" defaultstate="collapsed">
    private final BooleanProperty isApplicationSettingsOpened = new SimpleBooleanProperty();

    public final Boolean getIsApplicationSettingsOpened() {
        return isApplicationSettingsOpened.get();
    }

    public final void setIsApplicationSettingsOpened(Boolean value) {
        isApplicationSettingsOpened.set(value);
    }

    public BooleanProperty isApplicationSettingsOpenedProperty() {
        return isApplicationSettingsOpened;
    }
    // </editor-fold>

    // <editor-fold desc="configurationList property" defaultstate="collapsed">
    /**
     * The data as an observable list of Configurations.
     */
    private ObservableList<Configuration> configurationList = FXCollections.observableArrayList();

    /**
     * Returns the data as an observable list of Configurations.
     *
     * @return
     */
    public ObservableList<Configuration> getConfigurationList() {
        return configurationList;
    }
    // </editor-fold>

    // <editor-fold desc="currentConfiguration property" defaultstate="collapsed">
    private Configuration currentConfiguration;

    public final Configuration getCurrentConfiguration() {
        return currentConfiguration;
    }

    public final void setCurrentConfiguration(Configuration value) {
        currentConfiguration = value;
    }
    // </editor-fold>

    // <editor-fold desc="previous gui state" defaultstate="collapsed">
    // holds the screen which was shown before application settings screen has been opened
    // used to return to the proper screen, without reloading it
    private Node previousScreen;

    // holds the state of the isApplicationSettingsOpened flag before application settings screen has been opened
    private boolean previousIsApplicationSettingsOpened;

    // </editor-fold>
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Barista v0.1");

        initRootLayout();

        showProjectOverview();

        projectDescriptionProperty().addListener(
                (observable, oldValue, newValue) -> {
                    // if we have a selected folder
                    if (!getProjectFolder().equalsIgnoreCase("")) {
                        if (oldValue == null ? newValue != null : !oldValue.equals(newValue)) {
                            setProjectSettingsAreUnchanged(false);
                        }
                    }
                });

        setProjectSettingsAreUnchanged(true);

        BaristaMessages.ApplicationSettings applicationSettings = readApplicationSettings();

        if (applicationSettings != null) {
            loadProject(applicationSettings.getLastProjectFolder());
        }
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            // Give the controller access to the main app.
            RootLayoutController controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the project overview inside the root layout.
     */
    public void showProjectOverview() {
        try {
            // Load project overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/ProjectOverview.fxml"));
            Node projectOverview = (Node) loader.load();

            // Set project overview into the center of root layout.
            rootLayout.setCenter(projectOverview);

            // set IsApplicationSettingsOpened flag to false, which enables the relevant menu items
            setIsApplicationSettingsOpened(false);

            // Give the controller access to the main app.
            ProjectOverviewController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveCurrentScreenState() {

        // save current screen so we can easily get back to it
        previousScreen = rootLayout.getCenter();

        // save state of isApplicationSettingsOpened flag
        previousIsApplicationSettingsOpened = getIsApplicationSettingsOpened();
    }

    /**
     * Shows the application settings inside the root layout.
     */
    public void showApplicationSettings() {
        try {
            // load application settings screen
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/ApplicationSettings.fxml"));
            Node applicationSettings = (Node) loader.load();

            // save current screen state, so we can get back to it when we close the application settings screen
            saveCurrentScreenState();

            // set application settings into the center of root layout.
            rootLayout.setCenter(applicationSettings);

            // set IsApplicationSettingsOpened flag to true, which disabled the relevant menu items
            setIsApplicationSettingsOpened(true);

            // give the controller access to the main app.
            ApplicationSettingsController controller = loader.getController();
            controller.setMainApp(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the previous screen inside the root layout.
     */
    public void showPreviousScreen() {

        // set previous screen into the center of root layout.
        rootLayout.setCenter(previousScreen);

        // set IsApplicationSettingsOpened flag to true, which disabled the relevant menu items
        setIsApplicationSettingsOpened(previousIsApplicationSettingsOpened);
    }

    /**
     * Returns the main stage.
     *
     * @return
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    // get project settings file name
    public String getProjectSettingsFileName() {
        String currentProjectFolder = getProjectFolder();
        if ((currentProjectFolder != null) && (!currentProjectFolder.equalsIgnoreCase(""))) {
            Path projectSettingsFilePath = FileSystems.getDefault().getPath(getProjectFolder(), "project.settings");
            return projectSettingsFilePath.toString();
        }

        return null;
    }

    private BaristaMessages.ProjectSettings readProjectSettings() {
        FileReader fileReader = null;
        try {
            String projectSettingsFileName = getProjectSettingsFileName();

            if (new File(projectSettingsFileName).isFile()) {
                BaristaMessages.ProjectSettings.Builder projectSettingsBuilder = BaristaMessages.ProjectSettings.newBuilder();

                // read from file using protobuf properties format
                fileReader = new FileReader(projectSettingsFileName);
                TextFormat.merge(fileReader, projectSettingsBuilder);
                fileReader.close();

                return projectSettingsBuilder.build();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, "File not found while reading project.settings", ex);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, "Got IO exception while reading project.settings", ex);
        }

        return null;
    }

    public BaristaMessages.ApplicationSettings readApplicationSettings() {
        FileReader fileReader = null;
        try {
            String applicationSettingsFileName = getApplicationSettingsFileName();

            if (new File(applicationSettingsFileName).isFile()) {
                BaristaMessages.ApplicationSettings.Builder applicationSettingsBuilder = BaristaMessages.ApplicationSettings.newBuilder();

                // read from file using protobuf properties format
                fileReader = new FileReader(applicationSettingsFileName);
                TextFormat.merge(fileReader, applicationSettingsBuilder);
                fileReader.close();

                return applicationSettingsBuilder.build();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, "", ex);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public void writeApplicationSettings(BaristaMessages.ApplicationSettings applicationSettings) {
        String applicationSettingsFileName = getApplicationSettingsFileName();

        // write application settings objects to file
        try (FileWriter fileWriter = new FileWriter(applicationSettingsFileName)) {
            // write ApplicationSettings object in protobuf properties format
            TextFormat.print(applicationSettings, fileWriter);
            fileWriter.flush();
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, String.format("Error while writing to file %s", applicationSettingsFileName), ex);
        }
    }

    public SolverParameter readSolverParameter(String solverFileName) {
        FileReader fileReader = null;
        try {

            if (new File(solverFileName).isFile()) {
                SolverParameter.Builder solverParameterBuilder = SolverParameter.newBuilder();

                // read from file using protobuf properties format
                fileReader = new FileReader(solverFileName);
                TextFormat.merge(fileReader, solverParameterBuilder);
                fileReader.close();

                return solverParameterBuilder.build();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, "", ex);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public NetParameter readNetParameter(String netFileName) {
        FileReader fileReader = null;
        try {

            if (new File(netFileName).isFile()) {
                NetParameter.Builder netParameterBuilder = NetParameter.newBuilder();

                // read from file using protobuf properties format
                fileReader = new FileReader(netFileName);
                TextFormat.merge(fileReader, netParameterBuilder);
                fileReader.close();

                return netParameterBuilder.build();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, "", ex);
        } catch (IOException ex) {
            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    // get application settings file name
    private String getApplicationSettingsFileName() {
        // generate application settings file name
        String applicationFolder = System.getProperty("user.dir");
        Path applicationSettingsFilePath = FileSystems.getDefault().getPath(applicationFolder, "application.settings");

        return applicationSettingsFilePath.toString();
    }

    public void loadProjectSettings() {
        // load project settings from file
        BaristaMessages.ProjectSettings projectSettings = readProjectSettings();
        if (projectSettings != null) {
            setProjectDescription(projectSettings.getProjectDescription());
        }

        // mark project settings as unchanged, since we just load them
        setProjectSettingsAreUnchanged(true);
    }

    public void loadProject(String projectFolder) {
        // update project directory label
        setProjectFolder(projectFolder);

        loadProjectSettings();

        // generate application settings object
        BaristaMessages.ApplicationSettings.Builder applicationSettingsBuilder = BaristaMessages.ApplicationSettings.newBuilder();
        applicationSettingsBuilder.setLastProjectFolder(projectFolder);
        BaristaMessages.ApplicationSettings applicationSettings = applicationSettingsBuilder.build();

        // save application settings
        writeApplicationSettings(applicationSettings);

        // load configurations list
        loadConfigurations();
    }

    private String findSolverFileName(String configurationName) {

        Path configurationFilePath = FileSystems.getDefault().getPath(getProjectFolder(), configurationName);
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

        if ((files == null) || (files.length == 0)) {
            return null;
        } else {
            return files[0].getAbsolutePath();
        }
    }

    private void loadConfigurations() {

        // clear old configurations
        configurationList.clear();

        // enumerate sub-folders in project folder
        File folder = new File(getProjectFolder());
        File[] files = folder.listFiles();

        // for each file in project folder
        for (File file : files) {
            if (file.isDirectory()) {

                String configurationName = file.getName();

                // find solver file name
                String solverFileName = findSolverFileName(configurationName);

                // only add configurations which have a solver file
                if (solverFileName != null) {
                    configurationList.add(new Configuration(configurationName, solverFileName));
                }
            }
        }
    }
}
