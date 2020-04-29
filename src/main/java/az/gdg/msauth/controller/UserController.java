package az.gdg.msauth.controller;

import az.gdg.msauth.model.dto.UserDTO;
import az.gdg.msauth.model.dto.UserInfoForBlogService;
import az.gdg.msauth.security.model.dto.UserInfo;
import az.gdg.msauth.security.service.AuthenticationService;
import az.gdg.msauth.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
@CrossOrigin(exposedHeaders = "Access-Control-Allow-Origin")
@Api("User Controller")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final AuthenticationService authenticationService;

    public UserController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @ApiOperation("sign up new user")
    @PostMapping(value = "/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@RequestBody @Valid UserDTO userDTO) {
        logger.debug("signUp user start : email {}", userDTO.getEmail());
        userService.signUp(userDTO);
        logger.debug("signUp user end : email {}", userDTO.getEmail());
    }

    @ApiOperation("get user info")
    @GetMapping("/info")
    public UserInfo getUserInfo(@RequestHeader("X-Auth-Token") String token) {
        logger.debug("getUserInfo start : token {}", token);
        return authenticationService.validateToken(token);
    }

    @ApiOperation("get userId by email")
    @GetMapping("/id/by/email/{email}")
    public String getUserIdByEmail(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable(name = "email") String email) {
        logger.debug("getUserIdByEmail by email start : email {}", email);
        return userService.getCustomerIdByEmail(token, email);
    }

    @ApiOperation("get user by id for blog service")
    @GetMapping("/{userId}")
    public UserInfoForBlogService getUserById(@PathVariable("userId") int userId) {
        logger.debug("getUserById start : userId {}", userId);
        return userService.getUserById(userId);
    }

    @ApiOperation("verify account when user registers")
    @GetMapping(value = "/verify-account")
    public String verifyAccount(@RequestParam("token") String token) {
        logger.debug("verifyAccount start : token {}", token);
        userService.verifyAccount(token);
        logger.debug("verifyAccount stop : token {}", token);
        return "Your account is verified, now you can log in";
    }

    @ApiOperation("send reset password link to mail")
    @PostMapping(value = "/forgot-password")
    public void sendResetPasswordLinkToMail(@RequestBody String email) {
        logger.debug("sendResetPasswordLinkToMail start : email {}", email);
        userService.sendResetPasswordLinkToMail(email);
        logger.debug("sendResetPasswordLinkToMail stop : email {}", email);
    }

    @ApiOperation("reset password")
    @PostMapping(value = "/reset-password")
    public void resetPassword(@RequestHeader("X-Auth-Token") String token,
                              @RequestBody String password) {
        logger.debug("resetPassword start : token {}", token);
        userService.resetPassword(token, password);
        logger.debug("resetPassword stop : token {}", token);
    }

}
