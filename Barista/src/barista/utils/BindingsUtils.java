package barista.utils;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableBooleanValue;

/**
 *
 * @author Arik Poznanski
 */
public class BindingsUtils {

    // Bindings.and for three arguments
    public static BooleanBinding and(ObservableBooleanValue op1, ObservableBooleanValue op2, ObservableBooleanValue op3) {
        return Bindings.and(op1, Bindings.and(op2, op3));
    }
}
