package az.gdg.msauth.security.util;

import az.gdg.msauth.security.model.dto.UserInfo;
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

    private static final Logger logger = LoggerFactory.getLogger(TokenUtil.class);
    private final Clock clock = DefaultClock.INSTANCE;
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public UserInfo getUserInfoFromToken(String token) {
        logger.info("UtilLog.getUserInfoFromToken.start : token{}", token);
        String userId = getClaimFromToken(token, Claims::getId);
        String email = getClaimFromToken(token, Claims::getSubject);
        String role = getAllClaimsFromToken(token).get("role").toString();
        String status = getAllClaimsFromToken(token).get("status").toString();
        logger.info("UtilLog.getUserInfoFromToken.Stop.success : token{}", token);
        return UserInfo
                .builder()
                .token(token)
                .role(role)
                .status(status)
                .userId(userId)
                .email(email)
                .build();
    }

    public String getEmailFromResetPasswordToken(String token) {
        logger.info("UtilLog.getEmailFromResetPasswordToken.start : token{}", token);
        String email = getAllClaimsFromToken(token).get("email").toString();
        logger.info("UtilLog.getEmailFromResetPasswordToken.start.success : token{}", token);
        return email;
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        logger.info("UtilLog.getClaimFromToken.start : token{}", token);
        Claims claims = getAllClaimsFromToken(token);
        logger.info("UtilLog.getClaimFromToken.stop.success : token{}", token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        logger.info("UtilLog.getAllClaimsFromToken.start : token{}", token);
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(String username, String userId, String role, String status) {
        logger.info("UtilLog.generateToken.start : username{}", username);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        claims.put("status", status);
        logger.info("UtilLog.generateToken.stop.success : username{}", username);
        return doGenerateToken(claims, username, userId);
    }

    public String doGenerateToken(Map<String, Object> claims,
                                  String subject, String userId) {
        logger.info("UtilLog.doGenerateToken.start : subject{}", subject);
        Date createdDate = clock.now();
        Date expirationDate = calculateExpirationDate(createdDate);
        logger.info("UtilLog.doGenerateToken.stop.success : subject{}", subject);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)//username
                .setId(userId)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public String generateTokenForResetPasswordURL(String email) {
        logger.info("UtilLog.generateTokenForResetPasswordURL.start : email{}", email);
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        logger.info("UtilLog.generateTokenForResetPasswordURL.stop.success : email{}", email);
        return doGenerateTokenForResetPasswordURL(claims);
    }

    public String doGenerateTokenForResetPasswordURL(Map<String, Object> claims) {
        logger.info("UtilLog.doGenerateTokenForResetPasswordURL.start");
        Date createdDate = clock.now();
        Date expirationDate = calculateExpirationDate(createdDate);
        logger.info("UtilLog.doGenerateTokenForResetPasswordURL.stop.success");
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    private Date calculateExpirationDate(Date createdDate) {
        logger.info("UtilLog.calculateExpirationDate.start : createdDate{}", createdDate);
        return new Date(createdDate.getTime() + expiration * 100);
    }

    public boolean isTokenValid(String token) {
        logger.info("UtilLog.isTokenValid.start : token{}", token);
        if (Objects.isNull(token)) {
            return false;
        }
        logger.info("UtilLog.isTokenValid.stop.success : token{}", token);
        return !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        logger.info("UtilLog.isTokenExpired.start : token{}", token);
        Date expirationDate = getExpirationDateFromToken(token);
        logger.info("UtilLog.isTokenExpired.stop.success : token{]", token);
        return expirationDate.before(clock.now());
    }

    private Date getExpirationDateFromToken(String token) {
        logger.info("UtilLog.getExpirationDateFromToken.start : token{}", token);
        return getClaimFromToken(token, Claims::getExpiration);
    }


}