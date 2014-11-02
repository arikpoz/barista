package barista.model;

/**
 *
 * @author Arik Poznanski
 */
@FunctionalInterface
public interface UpdateBuilderCallbackInterface <E> {
    public void updateBuilderObject(E builder);
}

