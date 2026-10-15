package ni.edu.uam.citastecnicas.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ni.edu.uam.citastecnicas.dao.AppointmentDao;
import ni.edu.uam.citastecnicas.dao.ClientDao;
import ni.edu.uam.citastecnicas.model.CitaTecnica;
import ni.edu.uam.citastecnicas.model.Cliente;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class AppointmentService {

    public static final List<String> STATUSES = List.of(
            "Pendiente",
            "En proceso",
            "Finalizada",
            "Cancelada"
    );

    @NonNull
    private final AppointmentDao dao;

    @NonNull
    private final ClientDao clients;

    public int insert(CitaTecnica appointment) throws ServiceException {
        try {
            validate(appointment);

            int id = dao.insert(appointment);
            appointment.setId(id);

            return id;
        } catch (SQLException e) {
            throw mapError(e);
        }
    }

    public void update(CitaTecnica appointment) throws ServiceException {
        if (appointment == null) {
            throw new ServiceException("Seleccioná una cita.");
        }

        validateId(appointment.getId());

        try {
            validate(appointment);

            if (!dao.update(appointment)) {
                throw new ServiceException("La cita ya no existe.");
            }
        } catch (SQLException e) {
            throw mapError(e);
        }
    }

    public void delete(int id) throws ServiceException {
        validateId(id);

        try {
            if (!dao.delete(id)) {
                throw new ServiceException("La cita ya no existe.");
            }
        } catch (SQLException e) {
            throw mapError(e);
        }
    }

    public CitaTecnica findById(int id) throws ServiceException {
        validateId(id);

        try {
            return dao.findById(id).orElseThrow(
                    () -> new ServiceException("La cita no existe.")
            );
        } catch (SQLException e) {
            throw mapError(e);
        }
    }

    public List<CitaTecnica> findAll() throws ServiceException {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            throw mapError(e);
        }
    }

    public List<CitaTecnica> filter(String status, LocalDate date)
            throws ServiceException {
        String normalizedStatus =
                status == null || status.isBlank()
                        ? null
                        : validateStatus(status);

        try {
            return dao.filter(normalizedStatus, date);
        } catch (SQLException e) {
            throw mapError(e);
        }
    }

    private void validate(CitaTecnica appointment)
            throws ServiceException, SQLException {
        if (appointment == null) {
            throw new ServiceException("Ingresá los datos de la cita.");
        }

        if (appointment.getClient() == null
                || appointment.getClient().getId() <= 0) {
            throw new ServiceException("Seleccioná un cliente válido.");
        }

        if (appointment.getDate() == null) {
            throw new ServiceException("Seleccioná la fecha de la cita.");
        }

        if (appointment.getTime() == null) {
            throw new ServiceException("Ingresá la hora de la cita.");
        }

        String serviceType = required(
                appointment.getServiceType(), "servicio", 80
        );

        String description = required(
                appointment.getDescription(), "descripción", 255
        );

        String status = validateStatus(appointment.getStatus());

        Cliente client = clients.findById(
                appointment.getClient().getId()
        ).orElseThrow(
                () -> new ServiceException(
                        "El cliente seleccionado ya no existe."
                )
        );

        appointment.setClient(client);
        appointment.setServiceType(serviceType);
        appointment.setDescription(description);
        appointment.setStatus(status);
    }

    private String required(String value, String field, int maxLength)
            throws ServiceException {
        if (value == null || value.isBlank()) {
            throw new ServiceException("Completá el campo: " + field + ".");
        }

        String normalized = value.strip();

        if (normalized.length() > maxLength) {
            throw new ServiceException(
                    "El campo " + field + " no puede superar "
                            + maxLength + " caracteres."
            );
        }

        return normalized;
    }

    private String validateStatus(String status) throws ServiceException {
        if (status == null || !STATUSES.contains(status.strip())) {
            throw new ServiceException("Seleccioná un estado válido.");
        }

        return status.strip();
    }

    private void validateId(int id) throws ServiceException {
        if (id <= 0) {
            throw new ServiceException("Seleccioná una cita válida.");
        }
    }

    private ServiceException mapError(SQLException e) {
        if ("23503".equals(e.getSQLState())) {
            return new ServiceException(
                    "El cliente seleccionado ya no existe.", e
            );
        }

        if ("23514".equals(e.getSQLState())) {
            return new ServiceException(
                    "Seleccioná un estado válido.", e
            );
        }

        if ("23502".equals(e.getSQLState())) {
            return new ServiceException(
                    "Completá todos los campos obligatorios.", e
            );
        }

        return new ServiceException(
                "No se pudo completar la operación con la base de datos.", e
        );
    }
}