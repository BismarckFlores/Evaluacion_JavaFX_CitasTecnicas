package ni.edu.uam.citastecnicas;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import ni.edu.uam.citastecnicas.controller.CitasTecnicasController;
import ni.edu.uam.citastecnicas.controller.ClientController;
import ni.edu.uam.citastecnicas.dao.ClientDao;
import ni.edu.uam.citastecnicas.dao.impl.JdbcAppointmentDao;
import ni.edu.uam.citastecnicas.dao.impl.JdbcClientDao;
import ni.edu.uam.citastecnicas.service.AppointmentService;
import ni.edu.uam.citastecnicas.service.ClientService;

import java.io.IOException;
import java.net.URL;

public class MainController {

    private static final String BASE = "/ni/edu/uam/citastecnicas/fxml/";
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    @FXML private BorderPane root;
    @FXML private Button appointmentsButton;
    @FXML private Button clientsButton;

    private final ClientService clientService;
    private final AppointmentService appointmentService;

    public MainController() {
        ClientDao clients = new JdbcClientDao();
        clientService = new ClientService(clients);
        appointmentService = new AppointmentService(new JdbcAppointmentDao(), clients);
    }

    @FXML
    private void initialize() {
        mostrarCitas();
    }

    @FXML
    private void mostrarCitas() {
        if (cargarVista("CitasTecnicas.fxml")) {
            selectModule(appointmentsButton);
        }
    }

    @FXML
    private void mostrarClientes() {
        if (cargarVista("clients.fxml")) {
            selectModule(clientsButton);
        }
    }

    private boolean cargarVista(String file) {
        try {
            URL url = getClass().getResource(BASE + file);
            if (url == null) {
                throw new IOException("No se encontró la pantalla: " + file);
            }

            FXMLLoader loader = new FXMLLoader(url);
            loader.setControllerFactory(type -> {
                if (type == ClientController.class) {
                    return new ClientController(clientService);
                }
                if (type == CitasTecnicasController.class) {
                    return new CitasTecnicasController();
                }
                throw new IllegalArgumentException(
                        "Controlador inesperado: " + type.getName()
                );
            });
            Parent view = loader.load();

            if (loader.getController() instanceof CitasTecnicasController controller) {
                controller.setServices(appointmentService, clientService);
            }

            root.setCenter(view);
            return true;
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("No se pudo abrir el módulo");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/ni/edu/uam/citastecnicas/css/root.css")
                            .toExternalForm()
            );
            alert.showAndWait();
            return false;
        }
    }

    private void selectModule(Button selected) {
        appointmentsButton.pseudoClassStateChanged(
                SELECTED, selected == appointmentsButton
        );
        clientsButton.pseudoClassStateChanged(
                SELECTED, selected == clientsButton
        );
    }
}
