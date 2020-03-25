package az.gdg.msauth.service;

import az.gdg.msauth.dao.UserRepository;
import az.gdg.msauth.dto.UserDTO;
import az.gdg.msauth.entity.UserEntity;
import az.gdg.msauth.exception.WrongDataException;
import az.gdg.msauth.security.dto.UserInfo;
import az.gdg.msauth.security.exception.AuthenticationException;
import az.gdg.msauth.security.role.Role;
import az.gdg.msauth.security.service.AuthenticationService;
import az.gdg.msauth.security.service.AuthenticationServiceImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;

    public UserServiceImpl(UserRepository userRepository, AuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
    }

    public void signUp(UserDTO userDTO) {

        //Optional<UserEntity> checkedEmail = userRepository.findByEmail(userDTO.getEmail());
        if (false) {
            throw new WrongDataException("This email already exists");
        }

        String password = new BCryptPasswordEncoder().encode(userDTO.getPassword());
        UserEntity customerEntity = UserEntity
                .builder()
                .name(userDTO.getName())
                .surname(userDTO.getSurname())
                .username(userDTO.getEmail())
                .email(userDTO.getEmail())
                .password(password)
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(customerEntity);


    }

    public String getCustomerIdByEmail(String token, String email) {
        UserInfo userInfo = authenticationService.validateToken(token);
        String userRole = userInfo.getRole();
        if (!userRole.equals("ROLE_ADMIN")) {
            throw new AuthenticationException("You do not have rights for access");
        }
        UserEntity userEntity = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new WrongDataException("No such email is found"));
        return userEntity.getId().toString();
    }
}
