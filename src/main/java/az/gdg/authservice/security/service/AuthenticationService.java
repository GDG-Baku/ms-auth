package az.gdg.authservice.security.service;

import az.gdg.authservice.dao.UserRepository;
import az.gdg.authservice.entity.UserEntity;
import az.gdg.authservice.exception.WrongDataException;
import az.gdg.authservice.security.dto.JwtAuthenticationRequest;
import az.gdg.authservice.security.dto.JwtAuthenticationResponse;
import az.gdg.authservice.security.exception.AuthenticationException;
import az.gdg.authservice.security.util.TokenUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthenticationService {

    private final TokenUtils tokenUtils;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(TokenUtils tokenUtils,
                                 UserRepository userRepository, AuthenticationManager authenticationManager) {
        this.tokenUtils = tokenUtils;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    public JwtAuthenticationResponse createAuthenticationToken(JwtAuthenticationRequest request) {

        authenticate(request.getEmail(), request.getPassword());
        UserEntity userEntity = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new WrongDataException("Email is not registered"));
        String userId = userEntity.getId().toString();
        String role = userEntity.getRole().toString();
        String token = tokenUtils.generateToken(request.getEmail(), userId, role);
        return new JwtAuthenticationResponse(token);
    }

    public void authenticate(String username, String password) {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new AuthenticationException("User is disabled", e);
        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Bad credentials", e);
        }
    }


}
