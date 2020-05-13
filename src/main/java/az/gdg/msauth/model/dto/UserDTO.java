package az.gdg.msauth.model.dto;

import az.gdg.msauth.validation.user.UserConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@UserConstraint
public class UserDTO {

    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private Boolean termsAndConditions;
}
