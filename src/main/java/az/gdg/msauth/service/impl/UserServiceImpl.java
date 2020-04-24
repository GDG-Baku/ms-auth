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
import java.util.UUID;


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
        logger.info("ActionLog.sign up user.Start : email{}", userDTO.getEmail());

        UserEntity checkedEmail = userRepository.findByEmail(userDTO.getEmail());
        if (checkedEmail != null) {
            logger.error("ActionLog.WrongDataException.Thrown");
            throw new WrongDataException("This email already exists");
        }

        String password = new BCryptPasswordEncoder().encode(userDTO.getPassword());
        String code = UUID.randomUUID().toString();
        UserEntity userEntity = UserEntity
                .builder()
                .name(userDTO.getName())
                .surname(userDTO.getSurname())
                .username(userDTO.getEmail())
                .email(userDTO.getEmail())
                .password(password)
                .accountVerificationCode(code)
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
                        "&code=" + code + ">" +
                        "https://gdg-ms-auth.herokuapp.com/user/verify-account?email=" + userDTO.getEmail() +
                        "&code=" + code + "</a>")
                .build();

        emailService.sendToQueue(mail);
        logger.info("ActionLog.Sign up user.Stop.Success : email{}", userDTO.getEmail());

    }

    public String getCustomerIdByEmail(String token, String email) {
        logger.info("ActionLog.GetCustomerIdByEmail.Start : email{}", email);
        UserInfo userInfo = authenticationService.validateToken(token);
        if (!userInfo.getRole().equals("ROLE_ADMIN")) {
            logger.error("ActionLog.AuthenticationException.Thrown");
            throw new AuthenticationException("You do not have rights for access");
        }

        UserEntity foundUser = userRepository.findByEmail(email);
        if (foundUser != null) {
            logger.info("ActionLog.GetCustomerIdByEmail.Stop.Success : email{}", email);
            return foundUser.getId().toString();
        } else {
            logger.error("ActionLog.WrongDataException.Thrown");
            throw new WrongDataException("No such email is found");
        }

    }

    @Override
    public void verifyAccount(String email, String code) {
        logger.info("ActionLog.VerifyAccount.Start : email {}", email);
        UserEntity user = userRepository.findByEmail(email);

        if (user != null) {
            if (user.getAccountVerificationCode().equals(code)) {
                user.setStatus(Status.CONFIRMED);
                user.setAccountVerificationCode(UUID.randomUUID().toString());
                userRepository.save(user);
            } else {
                logger.error("ActionLog.WrongDataException.Thrown");
                throw new WrongDataException("Verification code is not valid!");
            }
        } else {
            logger.error("ActionLog.WrongDataException.Thrown");
            throw new WrongDataException("No found such user");
        }

        logger.info("ActionLog.VerifyAccount.Stop.Success : email{}", email);

    }

    @Override
    public void sendResetPasswordLinkToMail(String email) {
        logger.info("ActionLog.SendResetPasswordLinkToMail.Start : email {}", email);
        UserEntity user = userRepository.findByEmail(email);

        if (user != null) {

            String token = tokenUtil.generateTokenForResetPasswordURL(email);
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
            logger.error("ActionLog.WrongDataException.Thrown");
            throw new WrongDataException("No such user found!");
        }

        logger.info("ActionLog.SendResetPasswordLinkToMail.Stop.Success : email{}", email);

    }

    @Override
    public void resetPassword(String token, String password) {
        logger.info("ActionLog.ResetPassword.Start : token{}", token);
        String email = tokenUtil.getEmailFromResetPasswordToken(token);

        UserEntity user = userRepository.findByEmail(email);

        if (user != null) {
            String newPassword = new BCryptPasswordEncoder().encode(password);
            user.setPassword(newPassword);
            userRepository.save(user);
        } else {
            logger.info("ActionLog.WrongDataException.Thrown");
            throw new WrongDataException("No found such user!");
        }

        logger.info("ActionLog.ResetPassword.Stop.Success : token{}", token);

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
