package ni.edu.uam.citastecnicas.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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
import ni.edu.uam.citastecnicas.service.ServiceException;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Controlador de la interfaz gráfica para la gestión de clientes.
 * Se comunica directamente con la capa de servicio (ClientService).
 *
 * Responsable: Donald Jahdiel Hernández Mercado (@JayWFx)
 */
public class ClientController {

    private static final Pattern PATRON_CORREO =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final String ESTILO_ERROR =
            "-fx-border-color: #d32f2f; -fx-border-width: 1.5; -fx-border-radius: 3;";

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
    private Cliente clienteSeleccionado;

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

        configurarSeleccionTabla();
        configurarListenersValidacionVisual();
        cargarClientes();
    }

    /**
     * Configura el enlace de cada columna de la tabla con las propiedades del modelo Cliente.
     */
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

    /**
     * Detecta la selección en la tabla y carga los datos en el formulario.
     */
    private void configurarSeleccionTabla() {
        tblClientes.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo != null) {
                clienteSeleccionado = nuevo;
                cargarClienteEnFormulario(nuevo);
            }
        });
    }

    /**
     * Limpia el estilo de error en tiempo real cuando el usuario escribe en un campo.
     */
    private void configurarListenersValidacionVisual() {
        txtNombres.textProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo != null && !nuevo.isBlank()) {
                limpiarErrorVisual(txtNombres, lblErrorNombres);
            }
        });

        txtApellidos.textProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo != null && !nuevo.isBlank()) {
                limpiarErrorVisual(txtApellidos, lblErrorApellidos);
            }
        });

        txtTelefono.textProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo != null && !nuevo.isBlank()) {
                limpiarErrorVisual(txtTelefono, lblErrorTelefono);
            }
        });

        txtCorreo.textProperty().addListener((obs, viejo, nuevo) -> {
            if (nuevo != null && PATRON_CORREO.matcher(nuevo.trim()).matches()) {
                limpiarErrorVisual(txtCorreo, lblErrorCorreo);
            }
        });
    }

    /**
     * Carga todos los clientes de la base de datos a través de ClientService.
     */
    public void cargarClientes() {
        try {
            List<Cliente> clientes = clientService.findAll();
            listaClientes.setAll(clientes);
            actualizarContador(clientes.size());
            actualizarMensajeEstado("Clientes cargados correctamente (" + clientes.size() + ").");
        } catch (ServiceException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error al cargar clientes", e.getMessage());
            actualizarMensajeEstado("Error al cargar datos desde la base de datos.");
        }
    }

    /**
     * Llena el formulario con los datos del cliente seleccionado para su edición.
     */
    private void cargarClienteEnFormulario(Cliente cliente) {
        limpiarErroresVisuales();

        txtId.setText(String.valueOf(cliente.getId()));
        txtNombres.setText(cliente.getFirstName());
        txtApellidos.setText(cliente.getLastName());
        txtTelefono.setText(cliente.getPhone());
        txtCorreo.setText(cliente.getEmail());
        chkActivo.setSelected(cliente.isActive());

        btnGuardar.setDisable(true);
        btnActualizar.setDisable(false);
        btnEliminar.setDisable(false);

        actualizarMensajeEstado("Editando cliente ID: " + cliente.getId());
    }

    /**
     * Valida visualmente los campos obligatorios y el formato de correo.
     * Retorna true si todos los campos son válidos.
     */
    private boolean validarFormularioVisual() {
        boolean valido = true;
        limpiarErroresVisuales();

        String nombres = txtNombres.getText() != null ? txtNombres.getText().trim() : "";
        String apellidos = txtApellidos.getText() != null ? txtApellidos.getText().trim() : "";
        String telefono = txtTelefono.getText() != null ? txtTelefono.getText().trim() : "";
        String correo = txtCorreo.getText() != null ? txtCorreo.getText().trim() : "";

        if (nombres.isEmpty()) {
            mostrarErrorVisual(txtNombres, lblErrorNombres, "Los nombres son obligatorios.");
            valido = false;
        } else if (nombres.length() > 80) {
            mostrarErrorVisual(txtNombres, lblErrorNombres, "Máximo 80 caracteres.");
            valido = false;
        }

        if (apellidos.isEmpty()) {
            mostrarErrorVisual(txtApellidos, lblErrorApellidos, "Los apellidos son obligatorios.");
            valido = false;
        } else if (apellidos.length() > 80) {
            mostrarErrorVisual(txtApellidos, lblErrorApellidos, "Máximo 80 caracteres.");
            valido = false;
        }

        if (telefono.isEmpty()) {
            mostrarErrorVisual(txtTelefono, lblErrorTelefono, "El teléfono es obligatorio.");
            valido = false;
        } else if (telefono.length() > 20) {
            mostrarErrorVisual(txtTelefono, lblErrorTelefono, "Máximo 20 caracteres.");
            valido = false;
        }

        if (correo.isEmpty()) {
            mostrarErrorVisual(txtCorreo, lblErrorCorreo, "El correo es obligatorio.");
            valido = false;
        } else if (!PATRON_CORREO.matcher(correo).matches()) {
            mostrarErrorVisual(txtCorreo, lblErrorCorreo, "Formato inválido (ej: usuario@correo.com).");
            valido = false;
        } else if (correo.length() > 120) {
            mostrarErrorVisual(txtCorreo, lblErrorCorreo, "Máximo 120 caracteres.");
            valido = false;
        }

        return valido;
    }

    private void mostrarErrorVisual(TextField campo, Label labelError, String mensaje) {
        campo.setStyle(ESTILO_ERROR);
        if (labelError != null) {
            labelError.setText(mensaje);
            labelError.setVisible(true);
            labelError.setManaged(true);
        }
    }

    private void limpiarErrorVisual(TextField campo, Label labelError) {
        campo.setStyle(null);
        if (labelError != null) {
            labelError.setText("");
            labelError.setVisible(false);
            labelError.setManaged(false);
        }
    }

    private void limpiarErroresVisuales() {
        limpiarErrorVisual(txtNombres, lblErrorNombres);
        limpiarErrorVisual(txtApellidos, lblErrorApellidos);
        limpiarErrorVisual(txtTelefono, lblErrorTelefono);
        limpiarErrorVisual(txtCorreo, lblErrorCorreo);
    }

    @FXML
    private void onGuardar() {
        if (!validarFormularioVisual()) {
            return;
        }

        Cliente nuevoCliente = new Cliente(
                0,
                txtNombres.getText().trim(),
                txtApellidos.getText().trim(),
                txtTelefono.getText().trim(),
                txtCorreo.getText().trim(),
                chkActivo.isSelected()
        );

        try {
            int nuevoId = clientService.insert(nuevoCliente);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Cliente registrado",
                    "El cliente fue registrado exitosamente con el ID " + nuevoId + ".");
            limpiarFormulario();
            cargarClientes();
        } catch (ServiceException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "No se pudo registrar", e.getMessage());
        }
    }

    @FXML
    private void onActualizar() {
        if (clienteSeleccionado == null || txtId.getText().isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida",
                    "Por favor seleccioná un cliente de la tabla para actualizar.");
            return;
        }

        if (!validarFormularioVisual()) {
            return;
        }

        clienteSeleccionado.setFirstName(txtNombres.getText().trim());
        clienteSeleccionado.setLastName(txtApellidos.getText().trim());
        clienteSeleccionado.setPhone(txtTelefono.getText().trim());
        clienteSeleccionado.setEmail(txtCorreo.getText().trim());
        clienteSeleccionado.setActive(chkActivo.isSelected());

        try {
            clientService.update(clienteSeleccionado);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Cliente actualizado",
                    "Los datos del cliente fueron actualizados correctamente.");
            limpiarFormulario();
            cargarClientes();
        } catch (ServiceException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "No se pudo actualizar", e.getMessage());
        }
    }

    @FXML
    private void onLimpiar() {
        limpiarFormulario();
        actualizarMensajeEstado("Formulario listo para nuevo registro.");
    }

    @FXML
    private void onEliminar() {
        // En el Paso 3 se incluye la confirmación previa (RF08) y validación de citas asociadas (RF08 / 23503)
    }

    @FXML
    private void onBuscar() {
        // En el Paso 3 se conecta la búsqueda en tiempo real (RF05)
    }

    @FXML
    private void onLimpiarBusqueda() {
        if (txtBuscar != null) {
            txtBuscar.clear();
            cargarClientes();
        }
    }

    private void limpiarFormulario() {
        limpiarErroresVisuales();
        txtId.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        chkActivo.setSelected(true);

        clienteSeleccionado = null;
        tblClientes.getSelectionModel().clearSelection();

        btnGuardar.setDisable(false);
        btnActualizar.setDisable(true);
        btnEliminar.setDisable(true);
    }

    private void actualizarContador(int total) {
        if (lblContador != null) {
            lblContador.setText("Total: " + total + (total == 1 ? " cliente" : " clientes"));
        }
    }

    private void actualizarMensajeEstado(String mensaje) {
        if (lblEstado != null) {
            lblEstado.setText(mensaje);
        }
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String contenido) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(contenido);
        alerta.showAndWait();
    }
}
