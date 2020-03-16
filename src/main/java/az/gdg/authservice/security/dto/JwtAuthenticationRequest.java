package az.gdg.authservice.security.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthenticationRequest {
    private String email;

    private String password;

}