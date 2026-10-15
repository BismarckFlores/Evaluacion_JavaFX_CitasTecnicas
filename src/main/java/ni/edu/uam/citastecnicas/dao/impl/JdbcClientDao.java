package ni.edu.uam.citastecnicas.dao.impl;

import ni.edu.uam.citastecnicas.config.DB;
import ni.edu.uam.citastecnicas.dao.ClientDao;
import ni.edu.uam.citastecnicas.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcClientDao implements ClientDao {
    private static final String SELECT = "SELECT id, nombres, apellidos, telefono, correo, activo FROM cliente";


    @Override
    public int insert(Cliente client) throws SQLException {
        String sql = """
                INSERT INTO cliente
                    (nombres, apellidos, telefono, correo, activo)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (
                Connection connection = DB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            bindClient(statement, client);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("id");
                }

                throw new SQLException(
                        "No se pudo obtener el ID del cliente registrado."
                );
            }
        }
    }

    @Override
    public boolean update(Cliente client) throws SQLException {
        String sql = """
                UPDATE cliente
                SET nombres = ?, apellidos = ?, telefono = ?,
                    correo = ?, activo = ?
                WHERE id = ?
                """;

        try (
                Connection connection = DB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            bindClient(statement, client);
            statement.setInt(6, client.getId());

            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id = ?";

        try (
                Connection connection = DB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);

            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public Optional<Cliente> findById(int id) throws SQLException {
        String sql = SELECT + " WHERE id = ?";

        try (
                Connection connection = DB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapClient(result));
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public List<Cliente> findAll() throws SQLException {
        String sql = SELECT + " ORDER BY id";

        try (
                Connection connection = DB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            return readClients(statement);
        }
    }

    @Override
    public List<Cliente> search(String text) throws SQLException {
        if (text == null || text.isBlank()) {
            return findAll();
        }

        String sql = SELECT + """
                 WHERE nombres ILIKE ? ESCAPE '!'
                    OR apellidos ILIKE ? ESCAPE '!'
                    OR correo ILIKE ? ESCAPE '!'
                 ORDER BY id
                """;

        String pattern = "%" + text.trim()
                .replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_") + "%";

        try (
                Connection connection = DB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);

            return readClients(statement);
        }
    }

    private void bindClient(PreparedStatement statement, Cliente client) throws SQLException {
        statement.setString(1, client.getFirstName());
        statement.setString(2, client.getLastName());
        statement.setString(3, client.getPhone());
        statement.setString(4, client.getEmail());
        statement.setBoolean(5, client.isActive());
    }

    private List<Cliente> readClients(PreparedStatement statement)
            throws SQLException {
        List<Cliente> clients = new ArrayList<>();

        try (ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                clients.add(mapClient(result));
            }
        }

        return clients;
    }

    private Cliente mapClient(ResultSet result) throws SQLException {
        return new Cliente(
                result.getInt("id"),
                result.getString("nombres"),
                result.getString("apellidos"),
                result.getString("telefono"),
                result.getString("correo"),
                result.getBoolean("activo")
        );
    }
}
