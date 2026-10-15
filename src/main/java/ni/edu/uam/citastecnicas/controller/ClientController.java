package ni.edu.uam.citastecnicas.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.citastecnicas.dao.impl.JdbcClientDao;
import ni.edu.uam.citastecnicas.model.Cliente;
import ni.edu.uam.citastecnicas.service.ClientService;

/**
 * Controlador de la interfaz gráfica para la gestión de clientes.
 * Se comunica directamente con la capa de servicio (ClientService).
 *
 * Responsable: Donald Jahdiel Hernández Mercado (@JayWFx)
 */
public class ClientController {

    @FXML
    private TextField txtBuscar;

    @FXML
    private Button btnLimpiarBusqueda;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtNombres;

    @FXML
    private Label lblErrorNombres;

    @FXML
    private TextField txtApellidos;

    @FXML
    private Label lblErrorApellidos;

    @FXML
    private TextField txtTelefono;

    @FXML
    private Label lblErrorTelefono;

    @FXML
    private TextField txtCorreo;

    @FXML
    private Label lblErrorCorreo;

    @FXML
    private CheckBox chkActivo;

    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnActualizar;

    @FXML
    private Button btnLimpiar;

    @FXML
    private Button btnEliminar;

    @FXML
    private Label lblContador;

    @FXML
    private TableView<Cliente> tblClientes;

    @FXML
    private TableColumn<Cliente, Integer> colId;

    @FXML
    private TableColumn<Cliente, String> colNombres;

    @FXML
    private TableColumn<Cliente, String> colApellidos;

    @FXML
    private TableColumn<Cliente, String> colTelefono;

    @FXML
    private TableColumn<Cliente, String> colCorreo;

    @FXML
    private TableColumn<Cliente, String> colEstado;

    @FXML
    private Label lblEstado;

    private final ClientService clientService;
    private final ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();

    public ClientController() {
        this.clientService = new ClientService(new JdbcClientDao());
    }

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @FXML
    public void initialize() {
        configurarColumnas();
        tblClientes.setItems(listaClientes);
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombres.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEstado.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isActive() ? "Activo" : "Inactivo")
        );
    }

    @FXML
    private void onBuscar() {
        // Implementación de búsqueda con servicio en siguiente paso
    }

    @FXML
    private void onLimpiarBusqueda() {
        if (txtBuscar != null) {
            txtBuscar.clear();
        }
    }

    @FXML
    private void onGuardar() {
        // Implementación de registro con validaciones en siguiente paso
    }

    @FXML
    private void onActualizar() {
        // Implementación de edición con servicio en siguiente paso
    }

    @FXML
    private void onLimpiar() {
        limpiarFormulario();
    }

    @FXML
    private void onEliminar() {
        // Implementación de eliminación con confirmación en siguiente paso
    }

    private void limpiarFormulario() {
        txtId.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        chkActivo.setSelected(true);

        tblClientes.getSelectionModel().clearSelection();
        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }
}
