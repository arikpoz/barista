package barista.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * based on the post http://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html
 * 
 * @author Arik Poznanski
 */
public class StreamGobbler extends Thread {

    InputStream is;
    String type;
    LineHandlerInterface lineHandler;

    public StreamGobbler(InputStream is, String type, LineHandlerInterface lineHandler) {
        this.is = is;
        this.type = type;
        this.lineHandler = lineHandler;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                lineHandler.handleLine(type, line);
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
