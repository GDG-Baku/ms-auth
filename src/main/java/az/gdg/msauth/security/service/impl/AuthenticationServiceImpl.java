package az.gdg.msauth.security.service.impl;

import az.gdg.msauth.dao.UserRepository;
import az.gdg.msauth.exception.WrongDataException;
import az.gdg.msauth.model.entity.UserEntity;
import az.gdg.msauth.security.exception.AuthenticationException;
import az.gdg.msauth.security.model.dto.JwtAuthenticationRequest;
import az.gdg.msauth.security.model.dto.JwtAuthenticationResponse;
import az.gdg.msauth.security.model.dto.UserInfo;
import az.gdg.msauth.security.service.AuthenticationService;
import az.gdg.msauth.security.util.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private final TokenUtil tokenUtil;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public AuthenticationServiceImpl(TokenUtil tokenUtil,
                                     UserRepository userRepository, AuthenticationManager authenticationManager) {
        this.tokenUtil = tokenUtil;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    public JwtAuthenticationResponse createAuthenticationToken(JwtAuthenticationRequest request) {
        logger.info("ActionLog.createAuthenticationToken.start : email {}", request.getEmail());

        authenticate(request.getEmail(), request.getPassword());
        UserEntity userEntity = userRepository.findByEmail(request.getEmail());

        if (userEntity != null) {

            switch (userEntity.getStatus().toString()) {
                case "CONFIRMED":
                    String userId = userEntity.getId().toString();
                    String role = userEntity.getRole().toString();
                    String status = userEntity.getStatus().toString();
                    String token = tokenUtil.generateToken(request.getEmail(), userId, role, status);

                    logger.info("ActionLog.createAuthenticationToken.stop.success : email {}", request.getEmail());
                    return new JwtAuthenticationResponse(token);
                case "REGISTERED":
                    throw new AuthenticationException("Your registration is not verified," +
                            " please check your email for verification link which has been sent");
                case "BLOCKED":
                    throw new AuthenticationException("Your account has been blocked by admins, please contact us");
                default:

            }


        } else {
            logger.info("ActionLog.createAuthenticationToken.stop.WrongDataException.thrown");
            throw new WrongDataException("Email is not registered");
        }

        return null;
    }

    public void authenticate(String username, String password) {
        logger.info("ActionLog.authenticate.start : username {}", username);

        if (username != null && password != null) {
            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            } catch (BadCredentialsException e) {
                logger.error("ActionLog.AuthenticationException.bad credentials.thrown");

                throw new AuthenticationException("Bad credentials", e);
            }
        } else {
            throw new WrongDataException("Username or Password is null!");
        }

        logger.info("ActionLog.authenticate.stop.success : username {}", username);
    }

    public UserInfo validateToken(String token) {
        logger.info("ActionLog.validateToken.start : token {}", token);
        tokenUtil.isTokenValid(token);
        logger.info("ActionLog.validateToken.stop.success : token {}", token);

        return tokenUtil.getUserInfoFromToken(token);
    }


}
