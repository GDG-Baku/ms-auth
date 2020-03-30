package az.gdg.msauth.security.service

import az.gdg.msauth.dao.UserRepository
import az.gdg.msauth.entity.UserEntity
import az.gdg.msauth.exception.WrongDataException
import az.gdg.msauth.security.dto.JwtAuthenticationRequest
import az.gdg.msauth.security.dto.JwtAuthenticationResponse
import az.gdg.msauth.security.dto.UserInfo
import az.gdg.msauth.security.role.Role
import az.gdg.msauth.security.service.impl.AuthenticationServiceImpl
import az.gdg.msauth.security.util.TokenUtil
import org.springframework.security.authentication.AuthenticationManager
import spock.lang.Specification
import spock.lang.Title

@Title("Testing for authentication service")
class AuthenticationServiceImplTest extends Specification {


    TokenUtil tokenUtil
    UserRepository userRepository
    AuthenticationManager authenticationManager
    AuthenticationServiceImpl authenticationServiceImp

    def setup() {
        tokenUtil = Mock()
        userRepository = Mock()
        authenticationManager = Mock()
        authenticationServiceImp = new AuthenticationServiceImpl(tokenUtil, userRepository, authenticationManager)
    }


    def "return userinfo in validateToken() method if token is valid"() {
        given:
        def userInfo = new UserInfo("asdfghjkl", "ROLE_USER", "1", "admin@mail.ru")
        String token = "asdfghjkl"
        1 * tokenUtil.isTokenValid(token) >> true

        when:
        authenticationServiceImp.validateToken(token)

        then:
        tokenUtil.getUserInfoFromToken(token) >> userInfo

    }

    def "don't return userinfo in validateToken() method if token is invalid"() {
        given:
        String token = "asdfghjkl"
        1 * tokenUtil.isTokenValid(token) >> false

        when:
        authenticationServiceImp.validateToken(token)

        then:
        tokenUtil.getUserInfoFromToken(token) >> null

    }

    def "don't throw WrongDataException in createAuthenticationToken() method if userEntity is found and return token"() {
        given:
        def request = new JwtAuthenticationRequest("asdfg@mail.ru", "12345")
        def entity = new UserEntity(1, null, null, null, null, null, "ROLE_USER" as Role, null, null)
        def token = "asdfghjklyutryrwrtututu"
        1 * userRepository.findByEmail(request.getEmail()) >> entity


        when:
        authenticationServiceImp.createAuthenticationToken(request)

        then:
        new JwtAuthenticationResponse(token) >> token
        notThrown(WrongDataException)

    }

    def "throw WrongDataException in createAuthenticationToken() method if userEntity is not found"() {
        given:
        def request = new JwtAuthenticationRequest("asdfg@mail.ru", "12345")
        def entity = null
        1 * userRepository.findByEmail(request.getEmail()) >> entity

        when:
        authenticationServiceImp.createAuthenticationToken(request)

        then:
        thrown(WrongDataException)

    }

    def "throw exception in authenticate() method if username is null"() {
        given:
        def username = null
        def password = "pw"
        0 * Objects.requireNonNull(username)
        when:
        authenticationServiceImp.authenticate(username, password)

        then:
        thrown(NullPointerException)

    }

    def "throw exception in authenticate() method if password is null"() {
        given:
        def username = "example@mail.ru"
        def password = null
        0 * Objects.requireNonNull(password)

        when:
        authenticationServiceImp.authenticate(username, password)

        then:
        thrown(NullPointerException)

    }

    def "don't throw exception in authenticate() method if password and username are not null"() {
        given:
        def username = "example@mail.ru"
        def password = "12345"
        0 * Objects.requireNonNull(password)
        0 * Objects.requireNonNull(username)

        when:
        authenticationServiceImp.authenticate(username, password)

        then:
        notThrown(NullPointerException)

    }


}
