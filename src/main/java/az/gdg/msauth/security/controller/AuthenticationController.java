package az.gdg.msauth.security.controller;

import az.gdg.msauth.security.dto.JwtAuthenticationRequest;
import az.gdg.msauth.security.dto.JwtAuthenticationResponse;
import az.gdg.msauth.security.dto.UserInfo;
import az.gdg.msauth.security.service.AuthenticationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/auth")
@RestController
@CrossOrigin
@Api(value = "Authentication Controller")
public class AuthenticationController {

    private final AuthenticationService service;

    public AuthenticationController(AuthenticationService service) {
        this.service = service;
    }

    @ApiOperation("Create token if input credentials are valid")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/sign-in")
    public JwtAuthenticationResponse signIn(@RequestBody JwtAuthenticationRequest request) {
        return service.createAuthenticationToken(request);
    }

    @ApiOperation("if token is valid, returns user information")
    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/validate")
    public UserInfo validateToken(@RequestHeader("X-Auth-Token") String token) {
        return service.validateToken(token);
    }

}