package az.gdg.authservice.security.controller;

import az.gdg.authservice.security.dto.JwtAuthenticationRequest;
import az.gdg.authservice.security.dto.JwtAuthenticationResponse;
import az.gdg.authservice.security.service.AuthenticationService;
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
    @PostMapping("/signIn")
    public JwtAuthenticationResponse signIn(@RequestBody JwtAuthenticationRequest request) {
        return service.createAuthenticationToken(request);
    }

}