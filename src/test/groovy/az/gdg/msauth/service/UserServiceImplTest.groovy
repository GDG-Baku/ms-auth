package az.gdg.msauth.service

import az.gdg.msauth.dao.UserRepository
import az.gdg.msauth.dto.UserDTO
import az.gdg.msauth.entity.UserEntity
import az.gdg.msauth.exception.WrongDataException
import az.gdg.msauth.security.dto.UserInfo
import az.gdg.msauth.security.exception.AuthenticationException
import az.gdg.msauth.security.service.impl.AuthenticationServiceImpl
import az.gdg.msauth.service.impl.UserServiceImpl
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Title

@Title("Testing for user service")
class UserServiceImplTest extends Specification {

    UserRepository userRepository
    AuthenticationServiceImpl authenticationServiceImpl
    UserServiceImpl userService

    def setup() {
        userRepository = Mock()
        authenticationServiceImpl = Mock()
        userService = new UserServiceImpl(userRepository, authenticationServiceImpl)
    }


    def "doesn't throw exception if email doesn't exist in database"() {

        given:
        def userDto = new UserDTO()
        def entity = Optional.empty()
        userDto.setEmail("isgandarli_murad@mail.ru")
        userDto.setPassword("pw")
        1 * userRepository.findByEmail(userDto.getEmail()) >> entity

        when: "send dto object to service "
        userService.signUp(userDto)

        then: "duplicate e-mail address exception is not thrown"
        notThrown(WrongDataException)
    }

    def "throw exception if email  exists in database"() {

        given:
        def userDto = new UserDTO()
        def entity = Optional.of(UserEntity)
        userDto.setEmail("isgandarli_murad@mail.ru")
        1 * userRepository.findByEmail(userDto.getEmail()) >> entity

        when: "send dto object to service "
        userService.signUp(userDto)

        then: "duplicate e-mail address exception is thrown"
        thrown(WrongDataException)
    }

    def "throw exception if user's role is not admin"() {
        given:
        def userInfo = new UserInfo("admin@mail.ru", "ROLE_USER", "1", "asdfghjkl")
        def token = "asdfghjkl"
        def email = "admin@mail.ru"

        when:
        userService.getCustomerIdByEmail(token,email)

        then:
        3 * authenticationServiceImpl.validateToken(token) >> userInfo
        0 * authenticationServiceImpl.validateToken(token).getRole() >> "ROLE_USER"
        0 * authenticationServiceImpl.validateToken(token).getRole().equals("ROLE_ADMIN") >> false

        thrown(AuthenticationException)

    }

    @Ignore    // ignore it permanently
    def "do not throw any exception if user's role is admin and email is found"() {
        given:
        def userInfo = new UserInfo("admin@mail.ru", "ROLE_ADMIN", "1", "asdfghjkl")
        def entity = Optional.of(UserEntity)
        //def entity = new UserEntity()
        def token = "asdfghjkl"
        def email = "admin@mail.ru"

        when:
        userService.getCustomerIdByEmail(token,email)

        then:
        3 * authenticationServiceImpl.validateToken(token) >> userInfo
        0 * authenticationServiceImpl.validateToken(token).getRole() >> "ROLE_ADMIN"
        0 * authenticationServiceImpl.validateToken(token).getRole().equals("ROLE_ADMIN") >> true
        //2 * userRepository.findByEmail(email) >> entity
        1 * userRepository.findByEmail(email) >> entity
        //0 * userRepository.findByEmail(email).orElseThrow() >> ent
        //notThrown(AuthenticationException)
        notThrown(WrongDataException)

    }


}
