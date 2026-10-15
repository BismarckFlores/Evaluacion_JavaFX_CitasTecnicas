package ni.edu.uam.citastecnicas.util;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;

public final class Alerts {
    private Alerts() { }

    public static void show(Node source, Alert.AlertType type, String title, String text) {
        create(source, type, title, text, ButtonType.OK).showAndWait();
    }

    public static boolean confirm(Node source, String title, String text) {
        return create(source, Alert.AlertType.CONFIRMATION, title, text,
                ButtonType.OK, ButtonType.CANCEL).showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private static Alert create(Node source, Alert.AlertType type, String title,
                                String text, ButtonType... buttons) {
        Alert alert = new Alert(type, text, buttons);
        alert.setTitle(title);
        alert.setHeaderText(null);
        if (source != null && source.getScene() != null
                && source.getScene().getWindow() != null) {
            alert.initOwner(source.getScene().getWindow());
        }
        var pane = alert.getDialogPane();
        pane.getStylesheets().add(Alerts.class.getResource(
                "/ni/edu/uam/citastecnicas/css/root.css").toExternalForm());
        pane.getStyleClass().add("app-dialog");
        pane.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        ((Button) pane.lookupButton(ButtonType.OK)).getStyleClass().add("button-primary");
        return alert;
    }
}
