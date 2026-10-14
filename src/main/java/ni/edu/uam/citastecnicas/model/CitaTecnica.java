package ni.edu.uam.citastecnicas.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CitaTecnica {
    private int id;
    private Cliente client;
    private LocalDate date;
    private LocalTime time;
    private String serviceType;
    private String description;
    private String status = "Pendiente";
}
