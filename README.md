# CitasTécnicas — Evaluación JavaFX + PostgreSQL

## Integrantes y responsabilidades

| Integrante | Usuario GitHub | Qué hace |
|------------|----------------|----------|
| **Bismarck de Jesús Flores Delgado** | [@BismarckFlores](https://github.com/BismarckFlores) | **Todo el backend de clientes y citas:** `config/`, `model/`, `dao/`, `dao/impl/` y `service/`. PostgreSQL, `gestion_citas.sql`, JDBC, CRUD, búsquedas, filtros, validaciones e integridad referencial. Pruebas del backend e integración general. |
| **Francisco Castro Cuadra Castro** | [@FCuadra-JAG](https://github.com/FCuadra-JAG) | `App.java`, `MainController.java`, `main.fxml` y navegación principal. **Pantallas de citas:** `controller/`, `fxml/` y `css/`; formularios, tabla, búsqueda y filtros. |
| **Donald Jahdiel Hernández Mercado** | [@JayWFx](https://github.com/JayWFx) | **Pantalla de clientes:** `controller/`, `fxml/` y `css/`; formulario, tabla, botones de CRUD, búsqueda por nombres/apellidos/correo y validación visual de campos. |
| **Reynolds Imanol Rodríguez Aguirre** | [@ReyRod1505](https://github.com/ReyRod1505) | **Componentes compartidos de interfaz:** alertas, confirmaciones y estilos en `controller/`, `fxml/` y `css/`. Pruebas de los flujos de clientes y citas, y reporte de errores reproducibles. |

## Estructura inicial del proyecto

```text
CitasTecnicas/
├── pom.xml
├── README.md
├── .gitignore
├── gestion_citas.sql
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── module-info.java
    │   │   └── ni/edu/uam/citastecnicas/
    │   │       ├── App.java
    │   │       ├── MainController.java
    │   │       ├── config/
    │   │       ├── model/
    │   │       ├── dao/
    │   │       │   └── impl/
    │   │       ├── service/
    │   │       └── controller/
    │   └── resources/
    │       └── ni/edu/uam/citastecnicas/
    │           ├── fxml/
    │           │   └── main.fxml
    │           └── css/
    └── test/
        └── java/ni/edu/uam/citastecnicas/
```
