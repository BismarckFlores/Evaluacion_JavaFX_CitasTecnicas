package ni.edu.uam.citastecnicas;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onStartClick() {
        welcomeText.setText("Bienvenido a CitasTecnicas");
    }
}
