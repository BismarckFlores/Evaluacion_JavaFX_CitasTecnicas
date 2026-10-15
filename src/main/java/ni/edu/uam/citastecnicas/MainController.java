package ni.edu.uam.citastecnicas;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import ni.edu.uam.citastecnicas.controller.CitasTecnicasController;
import ni.edu.uam.citastecnicas.controller.ClientController;
import ni.edu.uam.citastecnicas.dao.ClientDao;
import ni.edu.uam.citastecnicas.dao.impl.JdbcAppointmentDao;
import ni.edu.uam.citastecnicas.dao.impl.JdbcClientDao;
import ni.edu.uam.citastecnicas.service.AppointmentService;
import ni.edu.uam.citastecnicas.service.ClientService;

import java.io.IOException;

public class MainController {
    private final ClientService clientService;
    private final AppointmentService appointmentService;

    @FXML
    private Label welcomeText;

    public MainController() {
        ClientDao clientDao = new JdbcClientDao();
        clientService = new ClientService(clientDao);
        appointmentService = new AppointmentService(
                new JdbcAppointmentDao(), clientDao
        );
    }

    @FXML
    protected void onStartClick() {
        try {
            FXMLLoader clientLoader = new FXMLLoader(
                    getClass().getResource("fxml/clients.fxml")
            );
            clientLoader.setControllerFactory(type -> {
                if (type == ClientController.class) {
                    return new ClientController(clientService);
                }
                throw new IllegalArgumentException(
                        "Controlador de clientes inesperado: " + type.getName()
                );
            });
            Parent clientRoot = clientLoader.load();
            ClientController clientController = clientLoader.getController();

            FXMLLoader appointmentLoader = new FXMLLoader(
                    getClass().getResource("fxml/CitasTecnicas.fxml")
            );
            Parent appointmentRoot = appointmentLoader.load();
            CitasTecnicasController appointmentController =
                    appointmentLoader.getController();
            appointmentController.setServices(appointmentService, clientService);

            Tab clientTab = new Tab("Clientes", clientRoot);
            Tab appointmentTab = new Tab("Citas", appointmentRoot);
            TabPane modules = new TabPane(clientTab, appointmentTab);
            modules.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            modules.getSelectionModel().selectedItemProperty().addListener(
                    (observable, previous, selected) -> {
                        if (selected == appointmentTab) {
                            appointmentController.refreshData();
                        } else if (selected == clientTab) {
                            clientController.cargarClientes();
                        }
                    }
            );

            Stage stage = (Stage) welcomeText.getScene().getWindow();
            Scene scene = new Scene(modules, 1150, 760);
            stage.setTitle("TecnoAsistencia Nicaragua S.A. — Clientes y Citas");
            stage.setScene(scene);
            stage.centerOnScreen();
        } catch (IOException e) {
            welcomeText.setText("Error al cargar los módulos: " + e.getMessage());
        }
    }
}
