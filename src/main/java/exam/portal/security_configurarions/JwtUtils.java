package exam.portal.security_configurarions;

import exam.portal.generic.GenericDao;
import exam.portal.model.TokenManagement;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.Key;
import java.util.*;

@Component
public class JwtUtils {
    private final Logger LOGGER = LoggerFactory.getLogger(JwtUtils.class);

    private final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";
    @Autowired
    private GenericDao<TokenManagement> tokenManagementGenericDao;

    public String generateTokenWithRoles(UserDetails userDetails, List<String> authorities,String email) {
        List<String> roles = authorities.stream()
                .filter(r -> r.startsWith("ROLE_"))
                .map(r -> r.substring(5))
                .toList();

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 1 day
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }
    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    public String extractBearerToken() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader;
            }
        }
        return null;
    }
    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        return getClaims(token).get("roles", List.class);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSignKey())
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmployeeCode(String token) {
        return getClaims(token).get("employeeCode", String.class);
    }

    public String extractTenantId(String token) {
        return getClaims(token).get("tenantId", String.class);
    }

    public Date getTokenExpireTime(String token) {
        return getClaims(token).getExpiration();
    }

    public long getTokenExpireTimeMillis(String token) {
        return getClaims(token).getExpiration().getTime();
    }
    public boolean validateTokenFromDb(String jwtToken) {
            TokenManagement token = validatedTokenFromDb(jwtToken);
            if (token  != null){
                return true;
            }
        return false;
    }
    private TokenManagement validatedTokenFromDb(String jwtToken) {
        TokenManagement token = null;
        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            jwtToken = jwtToken.substring(7);
            Date tokenExpireTime = getTokenExpireTime(jwtToken);
            if (tokenExpireTime == null) {
                LOGGER.error("invalid token");
                throw new ServiceException("invalid token");
            }
            Map<String, Object> param = new HashMap<>();
            param.put("jwtToken", jwtToken);
            param.put("tokenExpireTime", tokenExpireTime);
            String jpql = "select t from TokenManagement t where t.sessionToken = :jwtToken and t.sessionTokenExpireTime = :tokenExpireTime and t.isBlackListed = false";
            token = Optional.ofNullable(tokenManagementGenericDao.findOneWithQuery(jpql, param)).orElse(null);
            return token;
        }
        LOGGER.info("invalid token");
        return token;
       // throw new ServiceException("Invalid token");
    }
}
