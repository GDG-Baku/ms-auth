package az.gdg.msauth.controller;

import az.gdg.msauth.dto.UserDTO;
import az.gdg.msauth.security.dto.UserInfo;
import az.gdg.msauth.security.service.AuthenticationService;
import az.gdg.msauth.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Api("User Controller")
public class UserController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService, AuthenticationService authenticationService){
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @ApiOperation("sign up new user")
    @PostMapping(value = "/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@RequestBody UserDTO userDTO){
        logger.debug("Sign up user start");
        userService.signUp(userDTO);
        logger.debug("Sign up user end");
    }

    @ApiOperation("get user info")
    @GetMapping("/info")
    public UserInfo getCustomerInfo(@RequestHeader("X-Auth-Token") String token) {
        logger.debug("Token validation start");
        return authenticationService.validateToken(token);
    }

    @ApiOperation("get userId by email")
    @GetMapping("/id/by/email/{email}")
    public String getCustomerIdByEmail(
            @RequestHeader("X-Auth-Token") String token,
            @PathVariable(name = "email") String email) {
        logger.debug("Get Customer's id by email");
        return userService.getCustomerIdByEmail(token, email);
    }



}
