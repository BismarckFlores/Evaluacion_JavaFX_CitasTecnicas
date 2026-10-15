package ni.edu.uam.citastecnicas;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onStartClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/clients.fxml"));
            Parent root = fxmlLoader.load();
            Stage stage = (Stage) welcomeText.getScene().getWindow();
            Scene scene = new Scene(root, 1020, 680);
            stage.setTitle("TecnoAsistencia Nicaragua S.A. — Gestión de Clientes");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            welcomeText.setText("Error al cargar módulo de clientes: " + e.getMessage());
        }
    }
}
