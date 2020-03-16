package az.gdg.authservice.controller;

import az.gdg.authservice.dto.UserDTO;
import az.gdg.authservice.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Api("User Controller")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }

    @ApiOperation("sign up new user")
    @PostMapping(value = "/signUp")
    @ResponseStatus(HttpStatus.CREATED)
    public void signUp(@RequestBody UserDTO userDTO){
        userService.signUp(userDTO);
    }



}
