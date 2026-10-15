package ni.edu.uam.citastecnicas.dao.impl;

import ni.edu.uam.citastecnicas.config.DB;
import ni.edu.uam.citastecnicas.dao.AppointmentDao;
import ni.edu.uam.citastecnicas.model.CitaTecnica;
import ni.edu.uam.citastecnicas.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAppointmentDao implements AppointmentDao {

    private static final String SELECT = """
            SELECT a.id AS appointment_id,
                   a.fecha, a.hora, a.tipo_servicio, a.descripcion, a.estado,
                   c.id AS client_id,
                   c.nombres, c.apellidos, c.telefono, c.correo, c.activo
            FROM cita_tecnica a
            JOIN cliente c ON c.id = a.cliente_id
            """;

    private static final String ORDER =
            " ORDER BY a.fecha, a.hora, a.id";

    @Override
    public int insert(CitaTecnica appointment) throws SQLException {
        String sql = """
                INSERT INTO cita_tecnica
                    (cliente_id, fecha, hora, tipo_servicio, descripcion, estado)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (
                Connection connection = DB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            bindAppointment(statement, appointment);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("id");
                }

                throw new SQLException(
                        "No se pudo obtener el ID de la cita registrada."
                );
            }
        }
    }

    @Override
    public boolean update(CitaTecnica appointment) throws SQLException {
        String sql = """
                UPDATE cita_tecnica
                SET cliente_id = ?, fecha = ?, hora = ?,
                    tipo_servicio = ?, descripcion = ?, estado = ?
                WHERE id = ?
                """;

        try (
                Connection connection = DB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            bindAppointment(statement, appointment);
            statement.setInt(7, appointment.getId());

            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM cita_tecnica WHERE id = ?";

        try (
                Connection connection = DB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);

            return statement.executeUpdate() == 1;
        }
    }

    @Override
    public Optional<CitaTecnica> findById(int id) throws SQLException {
        String sql = SELECT + " WHERE a.id = ?";

        try (
                Connection connection = DB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapAppointment(result));
                }

                return Optional.empty();
            }
        }
    }

    @Override
    public List<CitaTecnica> findAll() throws SQLException {
        String sql = SELECT + ORDER;

        try (
                Connection connection = DB.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            return readAppointments(statement);
        }
    }

    @Override
    public List<CitaTecnica> filter(String status, LocalDate date)
            throws SQLException {
        String normalizedStatus =
                status == null || status.isBlank() ? null : status.strip();

        StringBuilder sql = new StringBuilder(SELECT)
                .append(" WHERE 1 = 1");

        if (normalizedStatus != null) {
            sql.append(" AND a.estado = ?");
        }

        if (date != null) {
            sql.append(" AND a.fecha = ?");
        }

        sql.append(ORDER);

        try (
                Connection connection = DB.getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql.toString())
        ) {
            int index = 1;

            if (normalizedStatus != null) {
                statement.setString(index++, normalizedStatus);
            }

            if (date != null) {
                statement.setObject(index, date);
            }

            return readAppointments(statement);
        }
    }

    private void bindAppointment(
            PreparedStatement statement,
            CitaTecnica appointment
    ) throws SQLException {
        statement.setInt(1, appointment.getClient().getId());
        statement.setObject(2, appointment.getDate());
        statement.setObject(3, appointment.getTime());
        statement.setString(4, appointment.getServiceType());
        statement.setString(5, appointment.getDescription());
        statement.setString(6, appointment.getStatus());
    }

    private List<CitaTecnica> readAppointments(
            PreparedStatement statement
    ) throws SQLException {
        List<CitaTecnica> appointments = new ArrayList<>();

        try (ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                appointments.add(mapAppointment(result));
            }
        }

        return appointments;
    }

    private CitaTecnica mapAppointment(ResultSet result)
            throws SQLException {
        Cliente client = new Cliente(
                result.getInt("client_id"),
                result.getString("nombres"),
                result.getString("apellidos"),
                result.getString("telefono"),
                result.getString("correo"),
                result.getBoolean("activo")
        );

        return new CitaTecnica(
                result.getInt("appointment_id"),
                client,
                result.getObject("fecha", LocalDate.class),
                result.getObject("hora", LocalTime.class),
                result.getString("tipo_servicio"),
                result.getString("descripcion"),
                result.getString("estado")
        );
    }
}