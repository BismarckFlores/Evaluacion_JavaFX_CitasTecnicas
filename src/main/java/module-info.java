module ni.edu.uam.citastecnicas {
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires org.postgresql.jdbc;
    requires io.github.cdimascio.dotenv.java;

    opens ni.edu.uam.citastecnicas to javafx.fxml;
    exports ni.edu.uam.citastecnicas;
}