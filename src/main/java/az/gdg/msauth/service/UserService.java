package az.gdg.msauth.service;

import az.gdg.msauth.dto.UserDTO;

public interface UserService {

    public void signUp(UserDTO userDTO);

    public String getCustomerIdByEmail(String token, String email);
}
