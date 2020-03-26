package az.gdg.msauth.security.util;

import az.gdg.msauth.security.controller.AuthenticationController;
import az.gdg.msauth.security.dto.UserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Clock;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Component
public class TokenUtil {

    private Clock clock = DefaultClock.INSTANCE;
    private static final Logger logger = LoggerFactory.getLogger(TokenUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public UserInfo getUserInfoFromToken(String token) {
        logger.info("ActionLog.GetUserInfoFromToken.Start");
        String userId = getClaimFromToken(token, Claims::getId);
        String email = getClaimFromToken(token, Claims::getSubject);
        String role = getAllClaimsFromToken(token).get("role").toString();
        logger.info("ActionLog.GetUserInfoFromToken.Stop.Success");
        return UserInfo
                .builder()
                .token(token)
                .role(role)
                .userId(userId)
                .email(email)
                .build();
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        logger.info("ActionLog.GetClaimFromToken.Start");
        Claims claims = getAllClaimsFromToken(token);
        logger.info("ActionLog.GetClaimFromToken.Stop.Success");
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        logger.info("ActionLog.GetAllClaimsFromToken.Start");
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(String username, String userId, String role) {
        logger.info("ActionLog.GenerateToken.Start");
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        logger.info("ActionLog.GenerateToken.Stop.Success");
        return doGenerateToken(claims, username, userId);
    }

    public String doGenerateToken(Map<String, Object> claims,
                                  String subject, String userId) {
        logger.info("ActionLog.DoGenerateToken.Start");
        Date createdDate = clock.now();
        Date expirationDate = calculateExpirationDate(createdDate);
        logger.info("ActionLog.DoGenerateToken.Stop.Success");
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)//username
                .setId(userId)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    private Date calculateExpirationDate(Date createdDate) {
        logger.info("ActionLog.CalculateExpirationDate.Start");
        return new Date(createdDate.getTime() + expiration * 100);
    }

    public boolean isTokenValid(String token) {
        logger.info("ActionLog.IsTokenValid.Start");
        if (Objects.isNull(token)) {
            return false;
        }
        logger.info("ActionLog.IsTokenValid.Stop.Success");
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        logger.info("ActionLog.IsTokenExpired.Start");
        Date expirationDate = getExpirationDateFromToken(token);
        logger.info("ActionLog.IsTokenExpired.Stop.Success");
        return expirationDate.before(clock.now());
    }

    private Date getExpirationDateFromToken(String token) {
        logger.info("ActionLog.GetExpirationDateFromToken.Start");
        return getClaimFromToken(token, Claims::getExpiration);
    }


}