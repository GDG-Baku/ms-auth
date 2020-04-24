package az.gdg.msauth.service;

import az.gdg.msauth.model.dto.UserDTO;
import az.gdg.msauth.model.dto.UserInfoForBlogService;

public interface UserService {

    void signUp(UserDTO userDTO);

    String getCustomerIdByEmail(String token, String email);

    void verifyAccount(String email, String code);

    void sendResetPasswordLinkToMail(String email);

    void resetPassword(String token, String password);

    UserInfoForBlogService getUserById(int id);


}
