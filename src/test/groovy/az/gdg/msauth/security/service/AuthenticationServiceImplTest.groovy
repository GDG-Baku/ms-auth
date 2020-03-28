package az.gdg.msauth.security.service

import az.gdg.msauth.dao.UserRepository
import az.gdg.msauth.security.dto.UserInfo
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


    def "return userinfo if token is valid"(){
        given:
        def userInfo = new UserInfo("admin@mail.ru", "ROLE_USER", "1", "asdfghjkl")
        String token = "asdfghjkl"
        1 * tokenUtil.isTokenValid(token) >> true

        when:
        authenticationServiceImp.validateToken(token)

        then:
        tokenUtil.getUserInfoFromToken(token) >> userInfo

    }

    def "don't return userinfo if token is invalid"(){
        given:
        def userInfo = new UserInfo("admin@mail.ru", "ROLE_USER", "1", "asdfghjkl")
        String token = "asdfghjkl"
        1 * tokenUtil.isTokenValid(token) >> false

        when:
        authenticationServiceImp.validateToken(token)

        then:
        tokenUtil.getUserInfoFromToken(token) >> null

    }




}
