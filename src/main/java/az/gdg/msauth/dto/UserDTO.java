package az.gdg.msauth.dto;

import az.gdg.msauth.security.role.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    private String username;
    private String email;
    private String name;
    private String surname;
    private String password;
    private Role role;
}
