module ni.edu.uam.citastecnicas {
    requires static lombok;
    requires javafx.controls;
    requires javafx.fxml;

    requires java.sql;
    requires org.postgresql.jdbc;
    requires io.github.cdimascio.dotenv.java;

    opens ni.edu.uam.citastecnicas to javafx.fxml;
    opens ni.edu.uam.citastecnicas.controller to javafx.fxml;
    opens ni.edu.uam.citastecnicas.model to javafx.base;

    exports ni.edu.uam.citastecnicas;
    exports ni.edu.uam.citastecnicas.controller;
    exports ni.edu.uam.citastecnicas.model;
}