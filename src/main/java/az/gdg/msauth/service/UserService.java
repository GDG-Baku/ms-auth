package az.gdg.msauth.service;

import az.gdg.msauth.model.dto.UserDTO;
import az.gdg.msauth.model.dto.UserDetail;

import java.util.List;

public interface UserService {

    void signUp(UserDTO userDTO);

    String getUserIdByEmail(String token, String email);

    void verifyAccount(String token);

    void sendResetPasswordLinkToMail(String email);

    void resetPassword(String token, String password);

    UserDetail getUserById(int id);

    void addPopularity(Integer userId);

    List<UserDetail> getPopularUsers();


}
