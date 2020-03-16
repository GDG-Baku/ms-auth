package az.gdg.authservice.service;

import az.gdg.authservice.dao.UserRepository;
import az.gdg.authservice.dto.UserDTO;
import az.gdg.authservice.entity.UserEntity;
import az.gdg.authservice.exception.WrongDataException;
import az.gdg.authservice.security.role.Role;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void signUp(UserDTO userDTO) {

        Optional<UserEntity> checkedEmail = userRepository.findByEmail(userDTO.getEmail());
        Optional<UserEntity> checkedUsername = userRepository.findByUsername(userDTO.getUsername());
        if (checkedEmail.isPresent()) {
            throw new WrongDataException("This email already exists");
        }
        if (checkedUsername.isPresent()) {
            throw new WrongDataException("This username already exists");
        }

        String password = new BCryptPasswordEncoder().encode(userDTO.getPassword());
        UserEntity customerEntity = UserEntity
                .builder()
                .name(userDTO.getName())
                .surname(userDTO.getSurname())
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .password(password)
                .role(Role.ROLE_USER)
                .createdAt(new java.sql.Date(new Date().getTime()))
                .build();

        userRepository.save(customerEntity);


    }
}
