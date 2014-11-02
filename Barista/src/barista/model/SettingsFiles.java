package barista.model;

import barista.BaristaMessages;
import barista.utils.ProcessUtils;
import com.google.protobuf.TextFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arik Poznanski
 */
public class SettingsFiles {

    // get application settings file name
    public static String getApplicationSettingsFileName() {
        String applicationFolder = ProcessUtils.getApplicationFolder();
        Path applicationSettingsFilePath = FileSystems.getDefault().getPath(applicationFolder, "application.settings");
        return applicationSettingsFilePath.toString();
    }

    // get project settings file name
    public static String getProjectSettingsFileName(String projectFolder) {
        if ((projectFolder != null) && (!projectFolder.equalsIgnoreCase(""))) {
            Path projectSettingsFilePath = FileSystems.getDefault().getPath(projectFolder, "project.settings");
            return projectSettingsFilePath.toString();
        }
        return null;
    }

    // get configuration settings file name
    public static String getConfigurationSettingsFileName(String projectFolder, String configurationFolder) {
        if ((projectFolder != null) && (!projectFolder.equalsIgnoreCase(""))) {
            Path configurationSettingsFilePath = FileSystems.getDefault().getPath(projectFolder, configurationFolder, "configuration.settings");
            return configurationSettingsFilePath.toString();
        }

        return null;
    }

    // get application settings object
    public static BaristaMessages.ApplicationSettings readApplicationSettings() {
        FileReader fileReader;
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
            Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, "File not found while reading application.settings", ex);
        } catch (IOException ex) {
            Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, "Got IO exception while reading application.settings", ex);
        }

        return null;
    }

    public static BaristaMessages.ProjectSettings readProjectSettings(String projectFolder) {
        FileReader fileReader;
        try {
            String projectSettingsFileName = getProjectSettingsFileName(projectFolder);

            if (new File(projectSettingsFileName).isFile()) {
                BaristaMessages.ProjectSettings.Builder projectSettingsBuilder = BaristaMessages.ProjectSettings.newBuilder();

                // read from file using protobuf properties format
                fileReader = new FileReader(projectSettingsFileName);
                TextFormat.merge(fileReader, projectSettingsBuilder);
                fileReader.close();

                return projectSettingsBuilder.build();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, "File not found while reading project.settings", ex);
        } catch (IOException ex) {
            Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, "Got IO exception while reading project.settings", ex);
        }
        return null;
    }

    public static BaristaMessages.ConfigurationSettings readConfigurationSettings(String projectFolder, String configurationFolder) {
        FileReader fileReader;
        try {
            String configurationSettingsFileName = getConfigurationSettingsFileName(projectFolder, configurationFolder);

            if (new File(configurationSettingsFileName).isFile()) {
                BaristaMessages.ConfigurationSettings.Builder configurationSettingsBuilder = BaristaMessages.ConfigurationSettings.newBuilder();

                // read from file using protobuf properties format
                fileReader = new FileReader(configurationSettingsFileName);
                TextFormat.merge(fileReader, configurationSettingsBuilder);
                fileReader.close();

                return configurationSettingsBuilder.build();
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, "File not found while reading configuration.settings", ex);
        } catch (IOException ex) {
            Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, "Got IO exception while reading configuration.settings", ex);
        }

        return null;
    }

    public static boolean writeApplicationSettings(BaristaMessages.ApplicationSettings applicationSettings) {
        String applicationSettingsFileName = getApplicationSettingsFileName();

        if (applicationSettingsFileName != null) {
            // write application settings objects to file
            try (FileWriter fileWriter = new FileWriter(applicationSettingsFileName)) {
                // write ApplicationSettings object in protobuf properties format
                TextFormat.print(applicationSettings, fileWriter);
                fileWriter.flush();
            } catch (IOException ex) {
                Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, String.format("Error while writing to file %s", applicationSettingsFileName), ex);
                return false;
            }
        } else {
            Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, "No application settings file");
            return false;
        }
        return true;
    }

    public static boolean writeProjectSettings(String projectFolder, BaristaMessages.ProjectSettings projectSettings) {
        String projectSettingsFileName = getProjectSettingsFileName(projectFolder);

        if (projectSettingsFileName != null) {
            // write project settings objects to file
            try (FileWriter fileWriter = new FileWriter(projectSettingsFileName)) {
                // write ProjectSettings object in protobuf properties format
                TextFormat.print(projectSettings, fileWriter);
                fileWriter.flush();
            } catch (IOException ex) {
                Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, String.format("Error while writing to file %s", projectSettingsFileName), ex);
                return false;
            }
        } else {
            Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, "No project settings file");
            return false;
        }
        return true;
    }

    public static boolean writeConfigurationSettings(String projectFolder, String configurationFolder, BaristaMessages.ConfigurationSettings configurationSettings) {
        String configurationSettingsFileName = getConfigurationSettingsFileName(projectFolder, configurationFolder);

        if (configurationSettingsFileName != null) {
            // write configuration settings objects to file
            try (FileWriter fileWriter = new FileWriter(configurationSettingsFileName)) {
                // write ConfigurationSettings object in protobuf properties format
                TextFormat.print(configurationSettings, fileWriter);
                fileWriter.flush();
            } catch (IOException ex) {
                Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, String.format("Error while writing to file %s", configurationSettingsFileName), ex);
                return false;
            }
        } else {
            Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, "No configuration settings file");
            return false;
        }
        return true;
    }

    // reads current application settings, or create a new one if none exist
    // update the settings with given function
    // write application settings to file
    public static boolean updateApplicationSettings(UpdateBuilderCallbackInterface<BaristaMessages.ApplicationSettings.Builder> updateBuilderFunction) {

        // get current application settings
        BaristaMessages.ApplicationSettings applicationSettings = readApplicationSettings();

        // generate application settings object
        BaristaMessages.ApplicationSettings.Builder applicationSettingsBuilder;
        if (applicationSettings != null) {
            applicationSettingsBuilder = BaristaMessages.ApplicationSettings.newBuilder(applicationSettings);
        } else {
            applicationSettingsBuilder = BaristaMessages.ApplicationSettings.newBuilder();
        }

        updateBuilderFunction.updateBuilderObject(applicationSettingsBuilder);

        applicationSettings = applicationSettingsBuilder.build();

        // save application settings
        return writeApplicationSettings(applicationSettings);
    }

    // reads current project settings, or create a new one if none exist
    // update the settings with given function
    // write project settings to file
    public static boolean updateProjectSettings(String projectFolder, UpdateBuilderCallbackInterface<BaristaMessages.ProjectSettings.Builder> updateBuilderFunction) {

        // get current project settings
        BaristaMessages.ProjectSettings projectSettings = readProjectSettings(projectFolder);

        // generate project settings object
        BaristaMessages.ProjectSettings.Builder projectSettingsBuilder;
        if (projectSettings != null) {
            projectSettingsBuilder = BaristaMessages.ProjectSettings.newBuilder(projectSettings);
        } else {
            projectSettingsBuilder = BaristaMessages.ProjectSettings.newBuilder();
        }

        updateBuilderFunction.updateBuilderObject(projectSettingsBuilder);

        projectSettings = projectSettingsBuilder.build();

        // save project settings
        return writeProjectSettings(projectFolder, projectSettings);
    }

    // reads current configuration settings, or create a new one if none exist
    // update the settings with given function
    // write configuration settings to file
    public static boolean updateConfigurationSettings(String projectFolder, String configurationFolder, UpdateBuilderCallbackInterface<BaristaMessages.ConfigurationSettings.Builder> updateBuilderFunction) {

        // get current configuration settings
        BaristaMessages.ConfigurationSettings configurationSettings = readConfigurationSettings(projectFolder, configurationFolder);

        // generate configuration settings object
        BaristaMessages.ConfigurationSettings.Builder configurationSettingsBuilder;
        if (configurationSettings != null) {
            configurationSettingsBuilder = BaristaMessages.ConfigurationSettings.newBuilder(configurationSettings);
        } else {
            configurationSettingsBuilder = BaristaMessages.ConfigurationSettings.newBuilder();
        }

        updateBuilderFunction.updateBuilderObject(configurationSettingsBuilder);
        
        configurationSettings = configurationSettingsBuilder.build();

        // save configuration settings
        return writeConfigurationSettings(projectFolder, configurationFolder, configurationSettings);
    }
}
