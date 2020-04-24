package az.gdg.msauth.controller;

import az.gdg.msauth.model.dto.UserDTO;
import az.gdg.msauth.security.model.dto.UserInfo;
import az.gdg.msauth.model.dto.UserInfoForBlogService;
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

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @ApiOperation("sign up new user")
    @PostMapping(value = "/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@RequestBody @Valid UserDTO userDTO) {
        logger.debug("Sign up user start : email{}", userDTO.getEmail());
        userService.signUp(userDTO);
        logger.debug("Sign up user end : email{}", userDTO.getEmail());
    }

    @ApiOperation("get user info")
    @GetMapping("/info")
    public UserInfo getUserInfo(@RequestHeader("X-Auth-Token") String token) {
        logger.debug("Token validation start : token{}", token);
        return authenticationService.validateToken(token);
    }

    @ApiOperation("get userId by email")
    @GetMapping("/id/by/email/{email}")
    public String getUserIdByEmail(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable(name = "email") String email) {
        logger.debug("Get customer's id by email : email{}", email);
        return userService.getCustomerIdByEmail(token, email);
    }

    @ApiOperation("get user by id for blog service")
    @GetMapping("/{userId}")
    public UserInfoForBlogService getUserById(@PathVariable("userId") int userId) {
        logger.debug("User info for blog service : userId{}", userId);
        return userService.getUserById(userId);
    }

    @ApiOperation("verify account when user registers")
    @GetMapping(value = "/verify-account")
    public String verifyAccount(@RequestParam("email") String email, @RequestParam("code") String code) {
        logger.debug("Verify account start : email{}", email);
        userService.verifyAccount(email, code);
        logger.debug("Verify account stop : email{}", email);
        return "Your account is verified, now you can log in";
    }

    @ApiOperation("send reset password link to mail")
    @GetMapping(value = "/forgot-password")
    public void sendResetPasswordLinkToMail(@RequestParam("email") String email) {
        logger.debug("Send reset password link to mail start : email{}", email);
        userService.sendResetPasswordLinkToMail(email);
        logger.debug("Send reset password link to mail stop : email{}", email);
    }

    @ApiOperation("reset password")
    @PostMapping(value = "/reset-password")
    public void resetPassword(@RequestHeader("X-Auth-Token") String token,
                              @RequestBody String password) {
        logger.debug("ResetPassword start");
        userService.resetPassword(token, password);
        logger.debug("ResetPassword stop");
    }

}
