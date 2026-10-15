package ni.edu.uam.citastecnicas;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import ni.edu.uam.citastecnicas.controller.CitasTecnicasController;
import ni.edu.uam.citastecnicas.dao.impl.JdbcAppointmentDao;   // confirmá este nombre
import ni.edu.uam.citastecnicas.dao.impl.JdbcClientDao;
import ni.edu.uam.citastecnicas.service.AppointmentService;
import ni.edu.uam.citastecnicas.service.ClientService;

import java.io.IOException;
import java.net.URL;

public class MainController {

    private static final String BASE = "/ni/edu/uam/citastecnicas/fxml/";

    @FXML private BorderPane root;

    private final ClientService clientService =
            new ClientService(new JdbcClientDao());
    private final AppointmentService appointmentService =
            new AppointmentService(new JdbcAppointmentDao(), new JdbcClientDao());

    @FXML
    private void initialize() {
        mostrarCitas();
    }

    @FXML private void mostrarCitas()     { cargarVista("CitasTecnicas.fxml"); }
    @FXML private void mostrarClientes()  { cargarVista("clients.fxml"); }
    @FXML private void mostrarServicios() { cargarVista("servicios.fxml"); }
    @FXML private void mostrarTecnicos()  { cargarVista("tecnicos.fxml"); }

    private void cargarVista(String archivo) {
        URL url = getClass().getResource(BASE + archivo);
        if (url == null) {
            new Alert(Alert.AlertType.INFORMATION,
                    "La pantalla \"" + archivo + "\" todavía no está disponible.").showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Node vista = loader.load();

            if (loader.getController() instanceof CitasTecnicasController c) {
                c.setServices(appointmentService, clientService);
            }

            root.setCenter(vista);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudo cargar " + archivo + ": " + e.getMessage()).showAndWait();
        }
    }
}