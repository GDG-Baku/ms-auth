package az.gdg.msauth.service

import az.gdg.msauth.dao.UserRepository
import az.gdg.msauth.dto.UserDTO
import az.gdg.msauth.exception.WrongDataException
import az.gdg.msauth.security.service.AuthenticationServiceImpl
import spock.lang.Specification
import spock.lang.Title

@Title("Testing for user service")
class UserServiceImplTest extends Specification {

    UserRepository userRepository
    AuthenticationServiceImpl authenticationService
    UserServiceImpl userService

    def setup() {
        userRepository = Mock()
        authenticationService = Mock()
        userService = new UserServiceImpl(userRepository, authenticationService)
    }

    def "doesn't throw exception if email doesn't exist in database"() {

        given:
        //String checkedEmail
        def userDto = new UserDTO()
        userDto.setEmail("isgandarli_murad@mail.ru")


        when: "send dto object to service "
        userService.signUp(userDto)


        then: ""
        //1*userRepository.findByEmail(userDto.getEmail())  >> checkedEmail
        1 * userRepository.findByEmail(userDto.getEmail()) >> userDto.getEmail()
        System.out.println(userDto.getEmail())
        notThrown(WrongDataException)
    }


}
