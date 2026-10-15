package ni.edu.uam.citastecnicas.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {
    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private boolean active = true;
}
