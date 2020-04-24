package az.gdg.msauth.service.impl;

import az.gdg.msauth.dao.UserRepository;
import az.gdg.msauth.exception.WrongDataException;
import az.gdg.msauth.mapper.UserMapper;
import az.gdg.msauth.model.dto.MailDTO;
import az.gdg.msauth.model.dto.UserDTO;
import az.gdg.msauth.model.dto.UserInfoForBlogService;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;


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
        logger.info("ActionLog.sign up user.start : email{}", userDTO.getEmail());

        UserEntity checkedEmail = userRepository.findByEmail(userDTO.getEmail());
        if (checkedEmail != null) {
            logger.error("ActionLog.WrongDataException.thrown");
            throw new WrongDataException("This email already exists");
        }

        String token = tokenUtil.generateTokenWithEmail(userDTO.getEmail());
        String password = new BCryptPasswordEncoder().encode(userDTO.getPassword());
        UserEntity userEntity = UserEntity
                .builder()
                .name(userDTO.getName())
                .surname(userDTO.getSurname())
                .username(userDTO.getEmail())
                .email(userDTO.getEmail())
                .password(password)
                .role(Role.ROLE_USER)
                .status(Status.REGISTERED)
                .build();

        userRepository.save(userEntity);

        MailDTO mail = new MailDTO().builder()
                .mailTo(Collections.singletonList(userDTO.getEmail()))
                .mailSubject("Your registration letter")
                .mailBody("<h2>" + "Verify Account" + "</h2>" + "</br>" +
                        "<a href=" +
                        "https://gdg-ms-auth.herokuapp.com/user/verify-account?email=" + userDTO.getEmail() +
                        "&code=" + token + ">" +
                        "https://gdg-ms-auth.herokuapp.com/user/verify-account?email=" + userDTO.getEmail() +
                        "&code=" + token + "</a>")
                .build();

        emailService.sendToQueue(mail);
        logger.info("ActionLog.sign up user.stop.success : email{}", userDTO.getEmail());

    }

    public String getCustomerIdByEmail(String token, String email) {
        logger.info("ActionLog.getCustomerIdByEmail.start : email{}", email);
        UserInfo userInfo = authenticationService.validateToken(token);
        if (!userInfo.getRole().equals("ROLE_ADMIN")) {
            logger.error("ActionLog.AuthenticationException.Thrown");
            throw new AuthenticationException("You do not have rights for access");
        }

        UserEntity foundUser = userRepository.findByEmail(email);
        if (foundUser != null) {
            logger.info("ActionLog.getCustomerIdByEmail.stop.success : email{}", email);
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

        logger.info("ActionLog.verifyAccount.stop.success : token{}", token);

    }

    @Override
    public void sendResetPasswordLinkToMail(String email) {
        logger.info("ActionLog.sendResetPasswordLinkToMail.start : email {}", email);
        UserEntity user = userRepository.findByEmail(email);

        if (user != null) {

            String token = tokenUtil.generateTokenWithEmail(email);
            MailDTO mail = new MailDTO().builder()
                    .mailTo(Collections.singletonList(email))
                    .mailSubject("Your reset password letter")
                    .mailBody("<h2>" + "Reset Password" + "</h2>" + "</br>" +
                            "<a href=" +
                            "http://localhost:5500/reset.html?token=" + token + ">" +
                            "http://localhost:5500/reset.html?token=" + token + "</a>")
                    .build();

            emailService.sendToQueue(mail);
        } else {
            logger.error("ActionLog.WrongDataException.thrown");
            throw new WrongDataException("No such user found!");
        }

        logger.info("ActionLog.sendResetPasswordLinkToMail.stop.success : email{}", email);

    }

    @Override
    public void resetPassword(String token, String password) {
        logger.info("ActionLog.resetPassword.start : token{}", token);
        String email = tokenUtil.getEmailFromToken(token);

        UserEntity user = userRepository.findByEmail(email);

        if (user != null) {
            String newPassword = new BCryptPasswordEncoder().encode(password);
            user.setPassword(newPassword);
            userRepository.save(user);
        } else {
            logger.info("ActionLog.WrongDataException.thrown");
            throw new WrongDataException("No found such user!");
        }

        logger.info("ActionLog.resetPassword.stop.success : token{}", token);

    }

    @Override
    public UserInfoForBlogService getUserById(int id) {
        UserEntity user = userRepository.findById(id).get();

        if (user != null) {
            return UserMapper.INSTANCE.entityToDto(user);

        } else {
            throw new WrongDataException("No found such user");
        }
    }
}
