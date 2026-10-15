package ni.edu.uam.citastecnicas.util;

import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class Form {
    private static final PseudoClass INVALID = PseudoClass.getPseudoClass("invalid");
    private final List<Field> fields = new ArrayList<>();

    public static void limit(TextInputControl input, int max) {
        input.setTooltip(new Tooltip("Máximo " + max + " caracteres."));
        input.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= max ? change : null));
    }

    public void add(Control control, Label error, ObservableValue<?> value, Supplier<String> rule) {
        Field field = new Field(control, error, rule);
        fields.add(field);
        control.focusedProperty().addListener((obs, old, focused) -> {
            if (!focused) field.validate();
        });
        value.addListener((obs, old, current) -> {
            if (field.touched) field.validate();
        });
    }

    public boolean validate() {
        Control first = null;
        for (Field field : fields) {
            if (!field.validate() && first == null) first = field.control;
        }
        if (first != null) first.requestFocus();
        return first == null;
    }

    public void reset() {
        for (Field field : fields) {
            field.touched = false;
            field.display("");
        }
    }

    private static final class Field {
        private final Control control;
        private final Label error;
        private final Supplier<String> rule;
        private boolean touched;

        private Field(Control control, Label error, Supplier<String> rule) {
            this.control = control;
            this.error = error;
            this.rule = rule;
        }

        private boolean validate() {
            touched = true;
            String message = rule.get();
            display(message);
            return message.isEmpty();
        }

        private void display(String message) {
            control.pseudoClassStateChanged(INVALID, !message.isEmpty());
            error.setText(message);
            error.setVisible(!message.isEmpty());
            error.setManaged(!message.isEmpty());
        }
    }
}
