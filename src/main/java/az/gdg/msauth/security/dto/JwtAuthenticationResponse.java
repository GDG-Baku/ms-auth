package az.gdg.msauth.security.dto;

import lombok.Getter;

@Getter
public class JwtAuthenticationResponse {

    private final String token;

    public JwtAuthenticationResponse(String token) {
        this.token = token;
    }
}