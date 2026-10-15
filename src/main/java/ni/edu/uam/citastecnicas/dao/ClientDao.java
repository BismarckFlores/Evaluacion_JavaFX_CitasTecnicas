package ni.edu.uam.citastecnicas.dao;

import ni.edu.uam.citastecnicas.model.Cliente;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ClientDao {

    int insert(Cliente client) throws SQLException;

    boolean update(Cliente client) throws SQLException;

    boolean delete(int id) throws SQLException;

    Optional<Cliente> findById(int id) throws SQLException;

    List<Cliente> findAll() throws SQLException;

    List<Cliente> search(String text) throws SQLException;
}
