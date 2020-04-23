package az.gdg.msauth.service;

import az.gdg.msauth.model.dto.ResetPasswordDTO;
import az.gdg.msauth.model.dto.UserDTO;
import az.gdg.msauth.model.dto.UserInfoForBlogService;

public interface UserService {

    public void signUp(UserDTO userDTO);

    public String getCustomerIdByEmail(String token, String email);

    public void verifyAccount(String email, String code);

    public void sendResetPasswordLinkToMail(String email);

    public void resetPassword(ResetPasswordDTO resetPasswordDTO);

    public UserInfoForBlogService getUserById(int id);


}
