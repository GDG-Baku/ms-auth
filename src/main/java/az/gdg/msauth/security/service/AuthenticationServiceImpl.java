package az.gdg.msauth.security.service;

import az.gdg.msauth.controller.UserController;
import az.gdg.msauth.dao.UserRepository;
import az.gdg.msauth.entity.UserEntity;
import az.gdg.msauth.exception.WrongDataException;
import az.gdg.msauth.security.dto.JwtAuthenticationRequest;
import az.gdg.msauth.security.dto.JwtAuthenticationResponse;
import az.gdg.msauth.security.dto.UserInfo;
import az.gdg.msauth.security.exception.AuthenticationException;
import az.gdg.msauth.security.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthenticationServiceImpl implements AuthenticationService{

    private final TokenUtil tokenUtil;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    public AuthenticationServiceImpl(TokenUtil tokenUtil,
                                     UserRepository userRepository, AuthenticationManager authenticationManager) {
        this.tokenUtil = tokenUtil;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    public JwtAuthenticationResponse createAuthenticationToken(JwtAuthenticationRequest request) {
        logger.info("ActionLog.CreateAuthenticationToken.Start");

        authenticate(request.getEmail(), request.getPassword());
        UserEntity userEntity = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new WrongDataException("Email is not registered"));
        String userId = userEntity.getId().toString();
        String role = userEntity.getRole().toString();
        String token = tokenUtil.generateToken(request.getEmail(), userId, role);

        logger.info("ActionLog.CreateAuthenticationToken.Stop.Success");
        return new JwtAuthenticationResponse(token);
    }

    public void authenticate(String username, String password) {
        logger.info("ActionLog.Authenticate.Start");
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            logger.warn("ActionLog.AuthenticationException.User is Disabled.Catched");
            throw new AuthenticationException("User is disabled", e);
        } catch (BadCredentialsException e) {
            logger.warn("ActionLog.AuthenticationException.Bad Credentials.Catched");
            throw new AuthenticationException("Bad credentials", e);
        }

        logger.info("ActionLog.Authenticate.Stop.Success");
    }

    public UserInfo validateToken(String token) {
        logger.info("ActionLog.ValidateToken.Start");
        tokenUtil.isTokenValid(token);
        logger.info("ActionLog.ValidateToken.Stop.Success");

        return tokenUtil.getUserInfoFromToken(token);
    }


}
