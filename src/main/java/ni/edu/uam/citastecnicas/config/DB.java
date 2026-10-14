package ni.edu.uam.citastecnicas.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

import java.sql.*;

public class DB {
    private DB() {}

    public static Connection getConnection() throws SQLException {
        try {
            Dotenv env = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            String url = getRequired(env, "DB_URL");
            String user = getRequired(env, "DB_USER");
            String password = getRequired(env, "DB_PASSWORD");

            return DriverManager.getConnection(url, user, password);
        } catch (DotenvException e) {
            throw new SQLException(
                    "No se pudo leer la configuración del archivo .env.", e
            );
        }
    }

    public static boolean testConnection() throws SQLException {
        try (
                Connection connection = getConnection();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT 1")
        ) {
            return result.next() && result.getInt(1) == 1;
        }
    }

    private static String getRequired(Dotenv env, String key)
            throws SQLException {
        String value = env.get(key);

        if (value == null || value.isBlank()) {
            throw new SQLException(
                    "Falta configurar la variable " + key + "."
            );
        }

        return value;
    }
}
