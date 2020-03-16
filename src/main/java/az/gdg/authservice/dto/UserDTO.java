package az.gdg.authservice.dto;

import az.gdg.authservice.security.role.Role;
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
