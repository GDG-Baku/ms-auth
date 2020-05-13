package az.gdg.msauth.service.impl;

import az.gdg.msauth.dao.UserRepository;
import az.gdg.msauth.exception.WrongDataException;
import az.gdg.msauth.mapper.UserMapper;
import az.gdg.msauth.model.dto.UserDTO;
import az.gdg.msauth.model.dto.UserDetail;
import az.gdg.msauth.model.entity.UserEntity;
import az.gdg.msauth.security.exception.AuthenticationException;
import az.gdg.msauth.security.model.Role;
import az.gdg.msauth.security.model.Status;
import az.gdg.msauth.security.model.dto.UserInfo;
import az.gdg.msauth.security.service.AuthenticationService;
import az.gdg.msauth.security.util.TokenUtil;
import az.gdg.msauth.service.EmailService;
import az.gdg.msauth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final TokenUtil tokenUtil;
    private final AuthenticationService authenticationService;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository, AuthenticationService authenticationService,
                           EmailService emailService, TokenUtil tokenUtil) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.emailService = emailService;
        this.tokenUtil = tokenUtil;
    }

    public void signUp(UserDTO userDTO) {
        logger.info("ActionLog.signUp user.start : email {} ", userDTO.getEmail());

        if (userDTO.getTermsAndConditions()) {
            UserEntity checkedEmail = userRepository.findByEmail(userDTO.getEmail());
            if (checkedEmail != null) {
                logger.error("ActionLog.WrongDataException.thrown");
                throw new WrongDataException("This email already exists");
            }

            String token = tokenUtil.generateTokenWithEmail(userDTO.getEmail());
            String password = new BCryptPasswordEncoder().encode(userDTO.getPassword());
            UserEntity userEntity = UserEntity
                    .builder()
                    .firstName(userDTO.getFirstName())
                    .lastName(userDTO.getLastName())
                    .username(userDTO.getEmail())
                    .email(userDTO.getEmail())
                    .termsAndConditions(true)
                    .password(password)
                    .popularity(0)
                    .role(Role.ROLE_USER)
                    .status(Status.REGISTERED)
                    .build();

            userRepository.save(userEntity);

            emailService.sendEmail("<h2>" + "Verify Account" + "</h2>" + "</br>" +
                            "<a href=" +
                            "https://gdg-ms-auth.herokuapp.com/user/verify-account?token=" + token + ">" +
                            "https://gdg-ms-auth.herokuapp.com/user/verify-account?token=" + token + "</a>",
                    userDTO.getEmail(), "Your verification letter");

            logger.info("ActionLog.signUp user.stop.success : email {}", userDTO.getEmail());
        } else {
            logger.error("Thrown.WrongDataException");
            throw new WrongDataException("Not allowed sign up operation, if you don't agree our terms and conditions");
        }


    }

    public String getUserIdByEmail(String token, String email) {
        logger.info("ActionLog.getCustomerIdByEmail.start : email {}", email);
        UserInfo userInfo = authenticationService.validateToken(token);
        if (!userInfo.getRole().equals("ROLE_ADMIN")) {
            logger.error("ActionLog.AuthenticationException.Thrown");
            throw new AuthenticationException("You do not have rights for access");
        }

        UserEntity foundUser = userRepository.findByEmail(email);
        if (foundUser != null) {
            logger.info("ActionLog.getCustomerIdByEmail.stop.success : email {}", email);
            return foundUser.getId().toString();
        } else {
            logger.error("ActionLog.WrongDataException.thrown");
            throw new WrongDataException("No such email is found");
        }

    }

    @Override
    public void verifyAccount(String token) {
        logger.info("ActionLog.verifyAccount.start : token {}", token);
        String email = tokenUtil.getEmailFromToken(token);
        UserEntity user = userRepository.findByEmail(email);

        if (user != null) {
            user.setStatus(Status.CONFIRMED);
            userRepository.save(user);
        } else {
            logger.error("ActionLog.WrongDataException.thrown");
            throw new WrongDataException("No found such user");
        }

        logger.info("ActionLog.verifyAccount.stop.success : token {}", token);

    }

    @Override
    public void sendResetPasswordLinkToMail(String email) {
        logger.info("ActionLog.sendResetPasswordLinkToMail.start : email {}", email);
        UserEntity user = userRepository.findByEmail(email);

        if (user != null) {

            String token = tokenUtil.generateTokenWithEmail(email);

            emailService.sendEmail("<h2>" + "Reset Password" + "</h2>" + "</br>" +
                            "<a href=" +
                            "http://virustat.org/reset.html?token=" + token + ">" +
                            "http://virustat.org/reset.html?token=" + token + "</a>",
                    email, "Your reset password letter");

        } else {
            logger.error("ActionLog.WrongDataException.thrown");
            throw new WrongDataException("No such user found!");
        }

        logger.info("ActionLog.sendResetPasswordLinkToMail.stop.success : email {}", email);

    }

    @Override
    public void resetPassword(String token, String password) {
        logger.info("ActionLog.resetPassword.start : token {}", token);
        String email = tokenUtil.getEmailFromToken(token);

        UserEntity user = userRepository.findByEmail(email);

        if (user != null) {
            boolean check = new BCryptPasswordEncoder().matches(password, user.getPassword());

            if (!check) {
                String newPassword = new BCryptPasswordEncoder().encode(password);
                user.setPassword(newPassword);
                userRepository.save(user);
            } else {
                throw new WrongDataException("Please, enter the password different from last one");
            }


        } else {
            logger.info("ActionLog.WrongDataException.thrown");
            throw new WrongDataException("No found such user!");
        }

        emailService.sendEmail("<h2>" + "Your password has been changed successfully" + "</h2>",
                email, "Successfully Changed");

        logger.info("ActionLog.resetPassword.stop.success : token {}", token);

    }

    @Override
    public UserDetail getUserById(int id) {
        logger.info("ActionLog.getUserById.start : id {}", id);
        Optional<UserEntity> user = userRepository.findById(id);

        if (user.isPresent()) {
            return UserMapper.INSTANCE.entityToDto(user.get());

        } else {

            throw new WrongDataException("No found such user");
        }
    }

    @Override
    public List<UserDetail> getUsersById(List<Integer> userIds) {
        logger.info("ActionLog.getUsersById.start : userIds {}", userIds);
        List<UserDetail> userDetails = new ArrayList<>();
        for (Integer userId : userIds) {
            Optional<UserEntity> user = userRepository.findById(userId);
            if (user.isPresent()) {
                userDetails.add(UserMapper.INSTANCE.entityToDto(user.get()));
            } else {
                continue;
            }

        }

        logger.info("ActionLog.getUsersById.stop.success : userDetails {}", userDetails);

        return userDetails;
    }

    @Override
    public void addPopularity(Integer userId) {
        logger.info("ActionLog.addPopularity.start : userId {}", userId);
        Optional<UserEntity> user = userRepository.findById(userId);

        if (user.isPresent()) {
            UserEntity userEntity = user.get();
            userEntity.setPopularity(userEntity.getPopularity() + 1);
            userRepository.save(userEntity);
        } else {
            logger.error("Thrown.WrongDataException");
            throw new WrongDataException("No found such user");
        }

        logger.info("ActionLog.addPopularity.stop.success : userId {}", userId);
    }

    @Override
    @Cacheable(value = "populars")
    public List<UserDetail> getPopularUsers() {
        logger.info("ActionLog.getPopularUsers.start");
        List<UserEntity> users = userRepository.findFirst3ByOrderByPopularityDesc();

        List<UserDetail> populars = UserMapper.INSTANCE.entityToDtoList(users);
        logger.info("ActionLog.getPopularUsers.stop.success");
        return populars;
    }


}
