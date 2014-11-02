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
import javafx.application.Application;

/**
 *
 * @author Arik Poznanski
 */
public class SettingsFiles {

    // get application folder
    // currently we assume that it is the current working directory
    public static String getApplicationFolder() {
//        try {
//            // return folder of application executable
//            return new File(MainApp.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
//        } catch (URISyntaxException ex) {
//            Logger.getLogger(MainApp.class.getName()).log(Level.SEVERE, null, ex);
//        }

        // return current working directory 
        return System.getProperty("user.dir");
    }

    // get application settings file name
    public static String getApplicationSettingsFileName() {
        // generate application settings file name
        String applicationFolder = getApplicationFolder();
        Path applicationSettingsFilePath = FileSystems.getDefault().getPath(applicationFolder, "application.settings");

        return applicationSettingsFilePath.toString();
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
            Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, "", ex);
        } catch (IOException ex) {
            Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, null, ex);
        }

        return null;
    }

    public static void writeApplicationSettings(BaristaMessages.ApplicationSettings applicationSettings) {
        String applicationSettingsFileName = SettingsFiles.getApplicationSettingsFileName();

        // write application settings objects to file
        try (FileWriter fileWriter = new FileWriter(applicationSettingsFileName)) {
            // write ApplicationSettings object in protobuf properties format
            TextFormat.print(applicationSettings, fileWriter);
            fileWriter.flush();
        } catch (IOException ex) {
            Logger.getLogger(SettingsFiles.class.getName()).log(Level.SEVERE, String.format("Error while writing to file %s", applicationSettingsFileName), ex);
        }
    }

    // reads current application settings, or create a new one if none exist
    // update the settings with given function
    // write application settings to file
    public static void updateApplicationSettings(UpdateBuilderCallbackInterface<BaristaMessages.ApplicationSettings.Builder> updateBuilderFunction) {

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
        writeApplicationSettings(applicationSettings);
    }
}
