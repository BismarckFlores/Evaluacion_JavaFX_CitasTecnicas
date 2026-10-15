package ni.edu.uam.citastecnicas.dao;

import ni.edu.uam.citastecnicas.model.CitaTecnica;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentDao {

    int insert(CitaTecnica appointment) throws SQLException;

    boolean update(CitaTecnica appointment) throws SQLException;

    boolean delete(int id) throws SQLException;

    Optional<CitaTecnica> findById(int id) throws SQLException;

    List<CitaTecnica> findAll() throws SQLException;

    List<CitaTecnica> filter(String status, LocalDate date)
            throws SQLException;
}