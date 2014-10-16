package barista.utils;

/**
 *
 * @author arikp
 */
@FunctionalInterface
public interface LineHandlerInterface{
    public void handleLine(String type, String line);
}

