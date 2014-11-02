/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package barista.utils;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import org.controlsfx.dialog.Dialogs;

/**
 *
 * @author Arik Poznanski
 */
public class ProcessUtils {

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

    // get unix process id
    // note: this function is not portable to Windows
    public static int getUnixPID(Process process) throws Exception {
        if (process.getClass().getName().equals("java.lang.UNIXProcess")) {
            Class cl = process.getClass();
            Field field = cl.getDeclaredField("pid");
            field.setAccessible(true);
            Object pidObject = field.get(process);
            return (Integer) pidObject;
        } else {
            throw new IllegalArgumentException("Needs to be a UNIXProcess");
        }
    }

    // kill process 
    // note: this function is not portable to Windows
    public static int killUnixProcess(Process process) throws Exception {
        int pid = getUnixPID(process);
        return Runtime.getRuntime().exec("kill " + pid).waitFor();
    }

    // kill process group 
    // note: this function is not portable to Windows
    public static int killUnixProcessGroup(Process process) throws Exception {
        int pid = getUnixPID(process);
        return Runtime.getRuntime().exec("kill -TERM -" + pid).waitFor();
    }

    // kill process tree
    // note: this function is not portable to Windows
    public static int killProcessTree(Process process) throws Exception {
        int pid = getUnixPID(process);

        // path to kill script inside application folder
        String killProcessTreeScript = "kill_process_tree.sh";
        String killProcessTreeScriptFullPath = FileSystems.getDefault().getPath(getApplicationFolder(), "scripts", killProcessTreeScript).toString();

        // check kill script exists
        if (!new File(killProcessTreeScriptFullPath).exists()) {
            Dialogs.create()
                    .title("No Kill Script")
                    .masthead("Could not find kill script")
                    .message("Please make sure the file " + killProcessTreeScript + " exists inside the application folder.")
                    .showWarning();

            return -1;
        }

        // build command line
        List<String> commandArgs = new ArrayList<>();
        commandArgs.add(killProcessTreeScriptFullPath);
        commandArgs.add(Integer.toString(pid));

        // run kill script
        return Runtime.getRuntime().exec(commandArgs.toArray(new String[0])).waitFor();
    }
}
