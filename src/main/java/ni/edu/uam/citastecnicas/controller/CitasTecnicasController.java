package ni.edu.uam.citastecnicas.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import ni.edu.uam.citastecnicas.model.CitaTecnica;
import ni.edu.uam.citastecnicas.model.Cliente;
import ni.edu.uam.citastecnicas.service.AppointmentService;
import ni.edu.uam.citastecnicas.service.ClientService;
import ni.edu.uam.citastecnicas.service.ServiceException;
import ni.edu.uam.citastecnicas.util.Alerts;
import ni.edu.uam.citastecnicas.util.Form;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controller for the CitasTecnicas view (CitasTecnicas.fxml).
 *
 * <p>All persistence operations go through {@link AppointmentService} and
 * {@link ClientService}. No DAO classes, SQL statements, or JDBC imports
 * are used here.</p>
 */
public class CitasTecnicasController implements Initializable {

    // ─────────────────────── Services (injected via setter) ───────────────────
    private AppointmentService appointmentService;
    private ClientService clientService;

    // ─────────────────────── Form controls ────────────────────────────────────
    @FXML private ComboBox<Cliente>    clientComboBox;
    @FXML private DatePicker           datePicker;
    @FXML private TextField            timeField;
    @FXML private TextField            serviceTypeField;
    @FXML private TextArea             descriptionArea;
    @FXML private ComboBox<String>     statusComboBox;

    // ─────────────────────── Filter controls ──────────────────────────────────
    @FXML private ComboBox<String>     filterStatusComboBox;
    @FXML private DatePicker           filterDatePicker;

    // ─────────────────────── Buttons ──────────────────────────────────────────
    @FXML private Button               saveButton;
    @FXML private Button               editButton;
    @FXML private Button               deleteButton;

    // ─────────────────────── Table ────────────────────────────────────────────
    @FXML private TableView<CitaTecnica>        appointmentsTable;
    @FXML private TableColumn<CitaTecnica, Integer> colId;
    @FXML private TableColumn<CitaTecnica, String>  colClient;
    @FXML private TableColumn<CitaTecnica, String>  colDate;
    @FXML private TableColumn<CitaTecnica, String>  colTime;
    @FXML private TableColumn<CitaTecnica, String>  colService;
    @FXML private TableColumn<CitaTecnica, String>  colStatus;

    // ─────────────────────── Status label ─────────────────────────────────────
    @FXML private Label recordCountLabel;

    // ─────────────────────── Internal state ───────────────────────────────────
    /** Keeps track of the appointment currently selected for editing. */
    private CitaTecnica selectedAppointment;
    private final Form form = new Form();
    @FXML private Label clientError;
    @FXML private Label dateError;
    @FXML private Label timeError;
    @FXML private Label serviceError;
    @FXML private Label descriptionError;
    @FXML private Label statusError;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm")
                    .withResolverStyle(ResolverStyle.STRICT);

    @FXML private TextField searchField;
    private final ObservableList<CitaTecnica> masterData = FXCollections.observableArrayList();
    private FilteredList<CitaTecnica> filteredData;
    // ═══════════════════════════════════════════════════════════════════════════
    //  Initializable
    // ═══════════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableColumns();
        filteredData = new FilteredList<>(masterData, a -> true);
        SortedList<CitaTecnica> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(appointmentsTable.comparatorProperty());
        appointmentsTable.setItems(sortedData);
        configureStatusComboBoxes();
        configureValidation();
        searchField.textProperty().addListener((observable, previous, text) -> onSearch());
        // Services must be injected before the scene is shown; see setServices().
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Public API – called by whoever loads this FXML to wire the services
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Injects the required services and triggers the initial data load.
     * Call this method right after {@code FXMLLoader.load()}.
     *
     * @param appointmentService the appointment service implementation
     * @param clientService      the client service implementation
     */
    public void setServices(AppointmentService appointmentService,
                            ClientService clientService) {
        this.appointmentService = appointmentService;
        this.clientService = clientService;
        refreshData();
    }

    public void refreshData() {
        clearForm();
        loadClients();
        onFilter();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  FXML Action Handlers
    // ═══════════════════════════════════════════════════════════════════════════

    /** Saves a new appointment after visual validation. */
    @FXML
    private void onSave() {
        if (!validateFormFields()) return;

        CitaTecnica appointment = buildAppointmentFromForm();

        try {
            appointmentService.insert(appointment);
            showSuccess("Cita registrada correctamente.");
            clearForm();
            onFilter();
        } catch (ServiceException e) {
            showError("Error al guardar", e.getMessage());
        }
    }

    /** Updates the currently selected appointment after visual validation. */
    @FXML
    private void onEdit() {
        if (selectedAppointment == null) {
            showError("Sin selección", "Seleccioná una cita de la tabla para editarla.");
            return;
        }
        if (!validateFormFields()) return;

        CitaTecnica updated = buildAppointmentFromForm();
        updated.setId(selectedAppointment.getId());

        try {
            appointmentService.update(updated);
            showSuccess("Cita actualizada correctamente.");
            clearForm();
            onFilter();
        } catch (ServiceException e) {
            showError("Error al editar", e.getMessage());
        }
    }

    /** Asks for confirmation, then deletes the selected appointment. */
    @FXML
    private void onDelete() {
        if (selectedAppointment == null) {
            showError("Sin selección", "Seleccioná una cita de la tabla para eliminarla.");
            return;
        }

        boolean confirmed = showConfirmation(
                "Eliminar cita",
                "¿Estás seguro de que deseas eliminar la cita #"
                        + selectedAppointment.getId() + "?\n"
                        + "Esta acción no se puede deshacer."
        );
        if (!confirmed) return;

        try {
            appointmentService.delete(selectedAppointment.getId());
            showSuccess("Cita eliminada correctamente.");
            clearForm();
            onFilter();
        } catch (ServiceException e) {
            showError("Error al eliminar", e.getMessage());
        }
    }

    /** Clears all form fields and deselects any table row. */
    @FXML
    private void onClear() {
        clearForm();
    }

    /** Applies status and/or date filters to the table. */
    @FXML
    private void onFilter() {
        String status = filterStatusComboBox.getValue();
        LocalDate date = filterDatePicker.getValue();

        // Both filters empty → load everything
        if ((status == null || status.isBlank()) && date == null) {
            loadAllAppointments();
            return;
        }

        try {
            List<CitaTecnica> results = appointmentService.filter(status, date);
            populateTable(results);
        } catch (ServiceException e) {
            showError("Error al filtrar", e.getMessage());
        }
    }

    /** Clears filter controls and reloads the full table. */
    @FXML
    private void onClearFilters() {
        searchField.clear();
        filterStatusComboBox.setValue(null);
        filterDatePicker.setValue(null);
        loadAllAppointments();
    }
    /** Populates the form when the user clicks a row in the table. */
    @FXML
    private void onTableRowClicked(MouseEvent event) {
        CitaTecnica selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        selectedAppointment = selected;
        populateFormFromAppointment(selected);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Private helpers – Data loading
    // ═══════════════════════════════════════════════════════════════════════════

    private void loadClients() {
        try {
            List<Cliente> clients = clientService.findAll();
            clientComboBox.setItems(FXCollections.observableArrayList(clients));
        } catch (ServiceException e) {
            showError("Error al cargar clientes", e.getMessage());
        }
    }

    private void loadAllAppointments() {
        try {
            List<CitaTecnica> appointments = appointmentService.findAll();
            populateTable(appointments);
        } catch (ServiceException e) {
            showError("Error al cargar citas", e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Private helpers – Table
    // ═══════════════════════════════════════════════════════════════════════════

    private void configureTableColumns() {
        // ID column: direct integer property
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        // Client full name: computed from nested object
        colClient.setCellValueFactory(data -> {
            CitaTecnica appt = data.getValue();
            String fullName = appt.getClient() != null
                    ? appt.getClient().getFirstName() + " " + appt.getClient().getLastName()
                    : "";
            return new javafx.beans.property.SimpleStringProperty(fullName);
        });

        // Date: formatted string
        colDate.setCellValueFactory(data -> {
            LocalDate date = data.getValue().getDate();
            String formatted = date != null
                    ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    : "";
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        // Time: formatted string
        colTime.setCellValueFactory(data -> {
            LocalTime time = data.getValue().getTime();
            String formatted = time != null ? time.format(TIME_FORMATTER) : "";
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        // Service type: direct property
        colService.setCellValueFactory(new PropertyValueFactory<>("serviceType"));

        // Status: direct property with colored badge
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                getStyleClass().removeAll(
                        "status-pending", "status-progress",
                        "status-success", "status-neutral"
                );
                if (empty || status == null) {
                    setText(null);
                } else {
                    setText(status);
                    getStyleClass().add(resolveStatusClass(status));
                }
            }
        });
    }

    private void populateTable(List<CitaTecnica> appointments) {
        masterData.setAll(appointments);
        onSearch();
    }

    @FXML
    private void onSearch() {
        String text = searchField.getText() == null
                ? "" : searchField.getText().strip().toLowerCase(Locale.ROOT);

        filteredData.setPredicate(a -> {
            if (text.isEmpty()) return true;
            String client = a.getClient() != null
                    ? (a.getClient().getFirstName() + " " + a.getClient().getLastName())
                            .toLowerCase(Locale.ROOT)
                    : "";
            String service = a.getServiceType() != null
                    ? a.getServiceType().toLowerCase(Locale.ROOT) : "";
            String desc = a.getDescription() != null
                    ? a.getDescription().toLowerCase(Locale.ROOT) : "";
            return client.contains(text) || service.contains(text) || desc.contains(text);
        });

        int n = filteredData.size();
        recordCountLabel.setText(n + " registro" + (n == 1 ? "" : "s"));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Private helpers – Form
    // ═══════════════════════════════════════════════════════════════════════════

    private void configureStatusComboBoxes() {
        ObservableList<String> statuses =
                FXCollections.observableArrayList(AppointmentService.STATUSES);

        statusComboBox.setItems(statuses);

        // Filter combo also has an "all" option represented by blank/null
        ObservableList<String> filterStatuses =
                FXCollections.observableArrayList(AppointmentService.STATUSES);
        filterStatusComboBox.setItems(filterStatuses);

        // Display clients by full name in the combo
        clientComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Cliente c) {
                return c == null ? "" : c.getFirstName() + " " + c.getLastName();
            }

            @Override
            public Cliente fromString(String s) {
                return null; // not editable
            }
        });
    }

    /**
     * Validates that all mandatory fields are filled.
     *
     * @return {@code true} if the form is valid, {@code false} otherwise
     */
    private boolean validateFormFields() {
        return form.validate();
    }

    private boolean allowedDate(LocalDate day) {
        return day != null && (!day.isBefore(LocalDate.now())
                || (selectedAppointment != null && day.equals(selectedAppointment.getDate())));
    }

    private void configureValidation() {
        Form.limit(timeField, 5);
        Form.limit(serviceTypeField, 80);
        Form.limit(descriptionArea, 255);
        Form.limit(searchField, 255);
        datePicker.setEditable(false);
        filterDatePicker.setEditable(false);
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate day, boolean empty) {
                super.updateItem(day, empty);
                setDisable(empty || !allowedDate(day));
            }
        });
        form.add(clientComboBox, clientError, clientComboBox.valueProperty(),
                () -> clientComboBox.getValue() != null && clientComboBox.getItems().contains(clientComboBox.getValue())
                        ? "" : "Seleccioná un cliente de la lista.");
        form.add(datePicker, dateError, datePicker.valueProperty(),
                () -> allowedDate(datePicker.getValue()) ? "" : "Seleccioná hoy o una fecha futura.");
        form.add(timeField, timeError, timeField.textProperty(),
                () -> isValidTime(timeField.getText().strip()) ? "" : "Usá HH:mm, entre 00:00 y 23:59.");
        form.add(serviceTypeField, serviceError, serviceTypeField.textProperty(),
                () -> serviceTypeField.getText().isBlank() ? "El servicio es obligatorio." : "");
        form.add(descriptionArea, descriptionError, descriptionArea.textProperty(),
                () -> descriptionArea.getText().isBlank() ? "La descripción es obligatoria." : "");
        form.add(statusComboBox, statusError, statusComboBox.valueProperty(),
                () -> AppointmentService.STATUSES.contains(statusComboBox.getValue() == null ? "" : statusComboBox.getValue())
                        ? "" : "Seleccioná un estado de la lista.");
    }

    /** Builds a {@link CitaTecnica} from the current form values (no validation). */
    private CitaTecnica buildAppointmentFromForm() {
        CitaTecnica appt = new CitaTecnica();
        appt.setClient(clientComboBox.getValue());
        appt.setDate(datePicker.getValue());
        appt.setTime(LocalTime.parse(timeField.getText().strip(), TIME_FORMATTER));
        appt.setServiceType(serviceTypeField.getText().strip());
        appt.setDescription(descriptionArea.getText().strip());
        appt.setStatus(statusComboBox.getValue());
        return appt;
    }

    /** Fills the form controls with the data of the given appointment. */
    private void populateFormFromAppointment(CitaTecnica appt) {
        // Match client by id in the combo items
        clientComboBox.getItems().stream()
                .filter(c -> appt.getClient() != null
                        && c.getId() == appt.getClient().getId())
                .findFirst()
                .ifPresent(clientComboBox::setValue);

        datePicker.setValue(appt.getDate());
        timeField.setText(appt.getTime() != null
                ? appt.getTime().format(TIME_FORMATTER) : "");
        serviceTypeField.setText(appt.getServiceType());
        descriptionArea.setText(appt.getDescription());
        statusComboBox.setValue(appt.getStatus());
        form.reset();
    }

    /** Resets all form controls and clears the selected appointment. */
    private void clearForm() {
        selectedAppointment = null;
        clientComboBox.setValue(null);
        datePicker.setValue(null);
        timeField.clear();
        serviceTypeField.clear();
        descriptionArea.clear();
        statusComboBox.setValue(null);
        appointmentsTable.getSelectionModel().clearSelection();
        form.reset();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Private helpers – Alerts
    // ═══════════════════════════════════════════════════════════════════════════

    private void showError(String title, String message) {
        Alerts.show(appointmentsTable, Alert.AlertType.ERROR, title, message);
    }

    private void showSuccess(String message) {
        Alerts.show(appointmentsTable, Alert.AlertType.INFORMATION, "Operación exitosa", message);
    }

    private boolean showConfirmation(String title, String message) {
        return Alerts.confirm(appointmentsTable, title, message);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Private helpers – Utilities
    // ═══════════════════════════════════════════════════════════════════════════

    private boolean isValidTime(String text) {
        try {
            LocalTime.parse(text, TIME_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private String resolveStatusClass(String status) {
        return switch (status) {
            case "Pendiente" -> "status-pending";
            case "En proceso" -> "status-progress";
            case "Finalizada" -> "status-success";
            default -> "status-neutral";
        };
    }
}
