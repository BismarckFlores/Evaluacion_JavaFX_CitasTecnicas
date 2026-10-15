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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.citastecnicas.dao.impl.JdbcClientDao;
import ni.edu.uam.citastecnicas.model.Cliente;
import ni.edu.uam.citastecnicas.service.ClientService;
import ni.edu.uam.citastecnicas.service.ServiceException;
import ni.edu.uam.citastecnicas.util.Alerts;
import ni.edu.uam.citastecnicas.util.Form;

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

    private final Form form = new Form();
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
        configurarListenerBusqueda();

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
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                getStyleClass().removeAll("status-success", "status-neutral");
                setText(empty ? null : status);
                if (!empty && status != null) {
                    getStyleClass().add(
                            "Activo".equals(status) ? "status-success" : "status-neutral"
                    );
                }
            }
        });
    }

    /**
     * Detecta la selección en la tabla y carga los datos en el formulario para editar o eliminar.
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
     * Limita la longitud y valida al salir del campo; vuelve a validar al corregirlo.
     */
    private void configurarListenersValidacionVisual() {
        Form.limit(txtNombres, 80);
        Form.limit(txtApellidos, 80);
        Form.limit(txtTelefono, 20);
        Form.limit(txtCorreo, 120);
        Form.limit(txtBuscar, 255);
        form.add(txtNombres, lblErrorNombres, txtNombres.textProperty(),
                () -> nameError(txtNombres.getText()));
        form.add(txtApellidos, lblErrorApellidos, txtApellidos.textProperty(),
                () -> nameError(txtApellidos.getText()));
        form.add(txtTelefono, lblErrorTelefono, txtTelefono.textProperty(), () -> {
            String phone = txtTelefono.getText().strip();
            if (phone.isEmpty()) return "El teléfono es obligatorio.";
            if (!phone.matches("\\+?[0-9 ()-]+")) return "Usá números, espacios, +, guiones o paréntesis.";
            int digits = phone.replaceAll("[^0-9]", "").length();
            return digits >= 7 && digits <= 15 ? "" : "Ingresá entre 7 y 15 dígitos.";
        });
        form.add(txtCorreo, lblErrorCorreo, txtCorreo.textProperty(), () -> {
            String email = txtCorreo.getText().strip();
            if (email.isEmpty()) return "El correo es obligatorio.";
            return PATRON_CORREO.matcher(email).matches() ? "" : "Formato inválido (ej: usuario@correo.com).";
        });
    }

    private String nameError(String text) {
        if (text.isBlank()) return "Este campo es obligatorio.";
        return text.strip().matches("[\\p{L}\\p{M}]+(?:[ '\u2019-][\\p{L}\\p{M}]+)*")
                ? "" : "Usá letras, espacios, apóstrofes o guiones.";
    }

    /**
     * Filtra la tabla automáticamente mientras el usuario escribe en la barra de búsqueda (RF05).
     */
    private void configurarListenerBusqueda() {
        txtBuscar.textProperty().addListener((obs, viejo, nuevo) -> {
            ejecutarBusqueda();
        });
    }

    /**
     * Carga todos los clientes de la base de datos a través de ClientService (RF02).
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

        form.reset();
        actualizarMensajeEstado("Editando cliente ID: " + cliente.getId());
    }

    /**
     * Valida visualmente los campos obligatorios y el formato de correo (RF07).
     * Retorna true si todos los campos son válidos.
     */
    private boolean validarFormularioVisual() {
        return form.validate();
    }

    private void limpiarErroresVisuales() {
        form.reset();
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

        Cliente clienteActualizado = new Cliente(
                clienteSeleccionado.getId(),
                txtNombres.getText().trim(),
                txtApellidos.getText().trim(),
                txtTelefono.getText().trim(),
                txtCorreo.getText().trim(),
                chkActivo.isSelected()
        );

        try {
            clientService.update(clienteActualizado);
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

    /**
     * Eliminación física con confirmación previa y control de integridad referencial (RF08).
     * Si el cliente tiene citas técnicas asociadas, la base de datos y la capa de servicio
     * rechazan la eliminación explicando el motivo al usuario.
     */
    @FXML
    private void onEliminar() {
        if (clienteSeleccionado == null || txtId.getText().isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección requerida",
                    "Por favor seleccioná un cliente de la tabla para eliminar.");
            return;
        }

        if (Alerts.confirm(tblClientes, "Confirmar eliminación",
                "¿Eliminar permanentemente al cliente " + clienteSeleccionado.getFirstName()
                        + " " + clienteSeleccionado.getLastName() + "?")) {
            try {
                clientService.delete(clienteSeleccionado.getId());
                mostrarAlerta(Alert.AlertType.INFORMATION, "Cliente eliminado",
                        "El cliente fue eliminado exitosamente del sistema.");
                limpiarFormulario();
                cargarClientes();
            } catch (ServiceException e) {
                // Alerta clara explicando el motivo (ej. cliente con citas asociadas)
                mostrarAlerta(Alert.AlertType.ERROR, "Eliminación restringida", e.getMessage());
                actualizarMensajeEstado("Operación denegada: " + e.getMessage());
            }
        }
    }

    /**
     * Búsqueda por coincidencia parcial de nombres, apellidos o correo (RF05).
     */
    @FXML
    private void onBuscar() {
        ejecutarBusqueda();
    }

    private void ejecutarBusqueda() {
        String texto = txtBuscar.getText();
        if (texto == null || texto.isBlank()) {
            cargarClientes();
            return;
        }

        try {
            List<Cliente> resultados = clientService.search(texto.trim());
            listaClientes.setAll(resultados);
            actualizarContador(resultados.size());
            actualizarMensajeEstado("Búsqueda: '" + texto.trim() + "' (" + resultados.size() + " encontrados).");
        } catch (ServiceException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de búsqueda", e.getMessage());
        }
    }

    @FXML
    private void onLimpiarBusqueda() {
        if (txtBuscar != null) {
            txtBuscar.clear();
        }
        cargarClientes();
    }

    private void limpiarFormulario() {
        limpiarErroresVisuales();
        txtId.clear();
        txtNombres.clear();
        txtApellidos.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        chkActivo.setSelected(true);

        form.reset();
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
        Alerts.show(tblClientes, tipo, titulo, contenido);
    }
}
