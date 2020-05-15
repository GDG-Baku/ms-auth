package az.gdg.msauth.service.impl;

import az.gdg.msauth.dao.UserRepository;
import az.gdg.msauth.exception.ExceedLimitException;
import az.gdg.msauth.exception.NotFoundException;
import az.gdg.msauth.exception.WrongDataException;
import az.gdg.msauth.mapper.UserMapper;
import az.gdg.msauth.model.dto.UserDTO;
import az.gdg.msauth.model.dto.UserDetail;
import az.gdg.msauth.model.entity.UserEntity;
import az.gdg.msauth.security.model.Role;
import az.gdg.msauth.security.model.Status;
import az.gdg.msauth.security.model.dto.UserInfo;
import az.gdg.msauth.security.service.AuthenticationService;
import az.gdg.msauth.security.util.TokenUtil;
import az.gdg.msauth.service.MailService;
import az.gdg.msauth.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
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
    private final MailService mailService;

    public UserServiceImpl(UserRepository userRepository, AuthenticationService authenticationService,
                           MailService mailService, TokenUtil tokenUtil) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
        this.mailService = mailService;
        this.tokenUtil = tokenUtil;
    }

    public void signUp(UserDTO userDTO) {
        logger.info("ActionLog.signUp user.start : email {} ", userDTO.getMail());

        if (userDTO.getAreTermsAndConditionsConfirmed()) {
            UserEntity checkedEmail = userRepository.findByMail(userDTO.getMail());
            if (checkedEmail != null) {
                throw new WrongDataException("This email already exists");
            }

            String token = tokenUtil.generateTokenWithEmail(userDTO.getMail());
            String password = new BCryptPasswordEncoder().encode(userDTO.getPassword());
            UserEntity userEntity = UserEntity
                    .builder()
                    .firstName(userDTO.getFirstName())
                    .lastName(userDTO.getLastName())
                    .username(userDTO.getMail())
                    .mail(userDTO.getMail())
                    .remainingQuackCount(500)
                    .remainingHateCount(500)
                    .password(password)
                    .popularity(0)
                    .role(Role.ROLE_USER)
                    .status(Status.REGISTERED)
                    .build();

            userRepository.save(userEntity);

            mailService.sendMail("<h2>" + "Verify Account" + "</h2>" + "</br>" +
                            "<a href=" +
                            "https://gdg-ms-auth.herokuapp.com/user/verify-account?token=" + token + ">" +
                            "https://gdg-ms-auth.herokuapp.com/user/verify-account?token=" + token + "</a>",
                    userDTO.getMail(), "Your verification letter");

            logger.info("ActionLog.signUp user.stop.success : email {}", userDTO.getMail());
        } else {
            throw new WrongDataException("Not allowed sign up operation, if you don't agree our terms and conditions");
        }


    }

    @Override
    public void verifyAccount(String token) {
        logger.info("ActionLog.verifyAccount.start");
        String email = tokenUtil.getMailFromToken(token);
        UserEntity user = userRepository.findByMail(email);

        if (user != null) {
            user.setStatus(Status.CONFIRMED);
            userRepository.save(user);
        } else {
            throw new NotFoundException("Not found such user");
        }

        logger.info("ActionLog.verifyAccount.stop.success");

    }

    @Override
    public void sendResetPasswordLinkToMail(String email) {
        logger.info("ActionLog.sendResetPasswordLinkToMail.start : email {}", email);
        UserEntity user = userRepository.findByMail(email);

        if (user != null) {

            String token = tokenUtil.generateTokenWithEmail(email);

            mailService.sendMail("<h2>" + "Reset Password" + "</h2>" + "</br>" +
                            "<a href=" +
                            "http://virustat.org/reset.html?token=" + token + ">" +
                            "http://virustat.org/reset.html?token=" + token + "</a>",
                    email, "Your reset password letter");

        } else {
            throw new NotFoundException("Not found such user!");
        }

        logger.info("ActionLog.sendResetPasswordLinkToMail.stop.success : email {}", email);

    }

    @Override
    public void resetPassword(String token, String password) {
        logger.info("ActionLog.resetPassword.start");
        String email = tokenUtil.getMailFromToken(token);

        UserEntity user = userRepository.findByMail(email);

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
            throw new NotFoundException("Not found such user!");
        }

        mailService.sendMail("<h2>" + "Your password has been changed successfully" + "</h2>",
                email, "Successfully Changed");

        logger.info("ActionLog.resetPassword.stop.success");

    }

    @Override
    public UserDetail getUserById(int id) {
        logger.info("ActionLog.getUserById.start : id {}", id);
        Optional<UserEntity> user = userRepository.findById(id);

        if (user.isPresent()) {
            return UserMapper.INSTANCE.entityToDto(user.get());

        } else {
            throw new NotFoundException("Not found such user");
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
            throw new NotFoundException("Not found such user");
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

    @Override
    public void updateRemainingQuackCount(String token) {
        logger.info("ActionLog.updateRemainingQuackCount.start");
        UserInfo userInfo = tokenUtil.getUserInfoFromToken(token);
        Integer userId = Integer.parseInt(userInfo.getUserId());

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Not found such user")
        );

        if (userEntity.getRemainingQuackCount() > 0) {
            userEntity.setRemainingQuackCount(userEntity.getRemainingQuackCount() - 1);
            userRepository.save(userEntity);
        } else {
            throw new ExceedLimitException("You've already used your daily quacks!");
        }

        logger.info("ActionLog.updateRemainingQuackCount.stop.success");


    }

    @Override
    public void updateRemainingHateCount(String token) {
        logger.info("ActionLog.updateRemainingHateCount.start");
        UserInfo userInfo = tokenUtil.getUserInfoFromToken(token);
        Integer userId = Integer.parseInt(userInfo.getUserId());

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Not found such user")
        );

        if (userEntity.getRemainingHateCount() > 0) {
            userEntity.setRemainingHateCount(userEntity.getRemainingHateCount() - 1);
            userRepository.save(userEntity);
        } else {
            throw new ExceedLimitException("You've already used your daily hates!");
        }


        logger.info("ActionLog.updateRemainingHateCount.stop.success");

    }

    @Override
    public Integer getRemainingQuackCount(String token) {
        logger.info("ActionLog.getRemainingQuackCount.start");
        UserInfo userInfo = tokenUtil.getUserInfoFromToken(token);
        Integer userId = Integer.parseInt(userInfo.getUserId());

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Not found such user")
        );

        logger.info("ActionLog.getRemainingQuackCount.stop.success");
        return userEntity.getRemainingQuackCount();
    }

    @Override
    public Integer getRemainingHateCount(String token) {
        logger.info("ActionLog.getRemainingHateCount.start");
        UserInfo userInfo = tokenUtil.getUserInfoFromToken(token);
        Integer userId = Integer.parseInt(userInfo.getUserId());

        UserEntity userEntity = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Not found such user")
        );

        logger.info("ActionLog.getRemainingHateCount.stop.success");
        return userEntity.getRemainingHateCount();
    }

    @Override
    @Scheduled(cron = "0 52 23 * * ?")  // at 23:59 every day
    public void refreshRemainingQuackAndHateCount() {
        logger.info("ActionLog.refreshRemainingQuackAndHateCount.start");
        if (!userRepository.findAll().isEmpty()) {
            userRepository.findAll()
                    .forEach(userEntity -> {
                        userEntity.setRemainingQuackCount(500);
                        userEntity.setRemainingHateCount(500);
                        userRepository.save(userEntity);
                    });
        } else {
            throw new NotFoundException("There isn't any user in database");
        }

        logger.info("ActionLog.refreshRemainingQuackAndHateCount.stop.success");
    }


}
