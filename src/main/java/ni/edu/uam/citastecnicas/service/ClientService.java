package ni.edu.uam.citastecnicas.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ni.edu.uam.citastecnicas.dao.ClientDao;
import ni.edu.uam.citastecnicas.model.Cliente;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class ClientService {

    private static final Pattern EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @NonNull
    private final ClientDao dao;

    public int insert(Cliente client) throws ServiceException {
        validate(client);

        try {
            int id = dao.insert(client);
            client.setId(id);
            return id;
        } catch (SQLException e) {
            throw mapError(e);
        }
    }

    public void update(Cliente client) throws ServiceException {
        if (client == null) {
            throw new ServiceException("Seleccioná un cliente.");
        }

        validateId(client.getId());
        validate(client);

        try {
            if (!dao.update(client)) {
                throw new ServiceException("El cliente ya no existe.");
            }
        } catch (SQLException e) {
            throw mapError(e);
        }
    }

    public void delete(int id) throws ServiceException {
        validateId(id);

        try {
            if (!dao.delete(id)) {
                throw new ServiceException("El cliente ya no existe.");
            }
        } catch (SQLException e) {
            throw mapError(e);
        }
    }

    public Cliente findById(int id) throws ServiceException {
        validateId(id);

        try {
            return dao.findById(id).orElseThrow(
                    () -> new ServiceException("El cliente no existe.")
            );
        } catch (SQLException e) {
            throw mapError(e);
        }
    }

    public List<Cliente> findAll() throws ServiceException {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            throw mapError(e);
        }
    }

    public List<Cliente> search(String text) throws ServiceException {
        try {
            return dao.search(text);
        } catch (SQLException e) {
            throw mapError(e);
        }
    }

    private void validate(Cliente client) throws ServiceException {
        if (client == null) {
            throw new ServiceException("Ingresá los datos del cliente.");
        }

        String firstName = required(client.getFirstName(), "nombres", 80);
        String lastName = required(client.getLastName(), "apellidos", 80);
        String phone = required(client.getPhone(), "teléfono", 20);
        String email = required(client.getEmail(), "correo", 120)
                .toLowerCase(Locale.ROOT);

        if (!EMAIL.matcher(email).matches()) {
            throw new ServiceException(
                    "Ingresá un correo válido, por ejemplo: nombre@dominio.com."
            );
        }

        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setPhone(phone);
        client.setEmail(email);
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

    private void validateId(int id) throws ServiceException {
        if (id <= 0) {
            throw new ServiceException("Seleccioná un cliente válido.");
        }
    }

    private ServiceException mapError(SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return new ServiceException(
                    "Ya existe un cliente con ese correo.", e
            );
        }

        if ("23503".equals(e.getSQLState())) {
            return new ServiceException(
                    "No se puede eliminar el cliente porque tiene citas asociadas.",
                    e
            );
        }

        return new ServiceException(
                "No se pudo completar la operación con la base de datos.", e
        );
    }
}