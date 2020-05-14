package az.gdg.msauth.validation.user;

import az.gdg.msauth.model.dto.UserDTO;
import az.gdg.msauth.util.CheckViolationHelper;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
public class UserValidator implements
        ConstraintValidator<UserConstraint, UserDTO> {


    private final CheckViolationHelper violationHelper;

    public UserValidator(CheckViolationHelper violationHelper) {
        this.violationHelper = violationHelper;
    }

    @Override
    public boolean isValid(UserDTO value, ConstraintValidatorContext context) {
        return isNameValid(value.getFirstName(), context) &&
                isSurnameValid(value.getLastName(), context) &&
                isMailValid(value.getMail(), context) &&
                isPasswordValid(value.getPassword(), context);
    }

    private boolean isNameValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.isEmpty() || !name.matches("[A-Z][a-z]*")) {
            violationHelper.addViolation(context, "name", "Name is not valid");
            return false;
        }
        return true;
    }

    private boolean isSurnameValid(String surname, ConstraintValidatorContext context) {
        if (surname == null || surname.isEmpty() || !surname.matches("[A-Z][a-z]*")) {
            violationHelper.addViolation(context, "surname", "Surname is not valid");
            return false;
        }
        return true;
    }

    private boolean isMailValid(String mail, ConstraintValidatorContext context) {
        if (mail == null ||
                mail.isEmpty() ||
                !mail.matches("^([a-zA-Z0-9_\\\\.-]+)@([a-zA-Z0-9-]+).([a-z]{2,8})(.[a-z]{2,8})?$")) {
            violationHelper.addViolation(context, "mail", "Mail is not valid");
            return false;
        }
        return true;
    }

    private boolean isPasswordValid(String password, ConstraintValidatorContext context) {
        if (password == null ||
                password.isEmpty() ||
                !password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[./_`~|{}?:;!(),><*@#$%^&+='])" +
                        "(?=\\S+$).{8,}$")) {
            violationHelper.addViolation(context, "password", "Password is not valid");
            return false;
        }
        return true;
    }


}
