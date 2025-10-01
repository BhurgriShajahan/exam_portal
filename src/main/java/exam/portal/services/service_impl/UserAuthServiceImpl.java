package exam.portal.services.service_impl;

import exam.portal.constants.Roles;
import exam.portal.dtos.request.LoginDto;
import exam.portal.dtos.request.UserDto;
import exam.portal.generic.GenericDao;
import exam.portal.global_exceptions_handler.CustomResponseEntity;
import exam.portal.model.Role;
import exam.portal.model.TokenManagement;
import exam.portal.model.User;
import exam.portal.repositories.RoleRepository;
import exam.portal.repositories.TokenManagementRepository;
import exam.portal.repositories.UserRepository;
import exam.portal.security_configurarions.CustomUserDetailsService;
import exam.portal.security_configurarions.EncryptionUtils;
import exam.portal.security_configurarions.JwtUtils;
import exam.portal.services.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class UserAuthServiceImpl implements UserAuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserAuthServiceImpl.class);

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private final EncryptionUtils encryptionUtils;

    private final AuthenticationManager authenticationManager;

    private final CustomUserDetailsService customUserDetailsService;

    private final TokenManagementRepository tokenManagementRepository;

    @Autowired
    private GenericDao<TokenManagement> tokenManagementGenericDao;

    private final JwtUtils jwtUtil;
//
//    @Autowired
//    public AuthServiceImpl( RoleRepository roleRepository,
//                           UserRepository userRepository, EncryptionUtils encryptionUtils,
//                           AuthenticationManager authenticationManager, JwtUtils jwtUtil,
//                           CustomUserDetailsService customUserDetailsService,
//                           TokenManagementRepository tokenManagementRepository) {
//        this.roleRepository = roleRepository;
//        this.userRepository = userRepository;
//        this.encryptionUtils = encryptionUtils;
//        this.authenticationManager=authenticationManager;
//        this.jwtUtil=jwtUtil;
//        this.customUserDetailsService=customUserDetailsService;
//        this.tokenManagementRepository=tokenManagementRepository;
//    }
//


    @Override
    public CustomResponseEntity register(UserDto userDto) {
        try {

            Optional<Role> optionalRole = roleRepository.findByRole(Roles.USER);
            if (!optionalRole.isPresent()) {
                return CustomResponseEntity.error("Role not found!");
            }

            Optional<User> existingUser = userRepository.findByEmailOrUserName(userDto.getEmail(), userDto.getUserName());
            if (existingUser.isPresent()) {
                return CustomResponseEntity.error("User Already Exist");
            }
            User user = new User();
            user.setFirstName(userDto.getFirstName());
            user.setLastName(userDto.getLastName());
            user.setUserName(userDto.getUserName());
            user.setEmail(userDto.getEmail());
            user.setPhone(userDto.getPhone());
            String encryptedPassword = encryptionUtils.encrypt(userDto.getPassword());
            user.setPassword(encryptedPassword);
            user.setIsActive(userDto.getIsActive());
            user.setRoles(Set.of(optionalRole.get()));
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(null);
            User savedUser = userRepository.save(user);

            return new CustomResponseEntity("Success", "User registered successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return CustomResponseEntity.error("Error occurred while creating user");
        }
    }

    @Override
    public CustomResponseEntity login(LoginDto loginDto) {
        try {

            User user = userRepository.findByEmailOrUserName(
                    loginDto.getEmailOrUsername(), loginDto.getEmailOrUsername()
            ).orElseThrow(() -> new ServiceException("User not found"));

            String loginPassword = loginDto.getPassword();
            String decryptedStoredPassword = EncryptionUtils.decrypt(user.getPassword());

            if (!decryptedStoredPassword.equals(loginPassword)) {
                throw new ServiceException("Invalid password");
            }

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getUserName());
            Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

            List<String> roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            List<TokenManagement> activeTokens = tokenManagementRepository.findByUserAndIsBlackListedFalse(user);
            for (TokenManagement oldToken : activeTokens) {
                oldToken.setIsBlackListed(true);
                tokenManagementRepository.save(oldToken);
            }
            String email = user.getEmail();
            String token = jwtUtil.generateTokenWithRoles(userDetails, roles, email);
            Date expirationDate = new Date(jwtUtil.getTokenExpireTime(token).getTime());

            TokenManagement tokenManagement = new TokenManagement();
            tokenManagement.setUser(user);
            tokenManagement.setSessionToken(token);
            tokenManagement.setSessionTokenExpireTime(expirationDate);
            tokenManagement.setIsBlackListed(false);
            tokenManagementRepository.save(tokenManagement);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authToken);

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("token", token);
            data.put("firstName", user.getFirstName());
            data.put("lastName", user.getLastName());
            data.put("roles", roles);
            data.put("expirationTime", expirationDate.getTime());

            return new CustomResponseEntity<>(data, "Login successful");

        } catch (ServiceException se) {
            return new CustomResponseEntity("Error", se.getMessage());
        } catch (Exception e) {
            return new CustomResponseEntity("An unexpected error occurred: " + e.getMessage());
        }
    }


    @Override
    public CustomResponseEntity logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            Optional<TokenManagement> tokenRecord = tokenManagementRepository.findBySessionToken(token);
            if (tokenRecord.isPresent()) {
                TokenManagement tm = tokenRecord.get();
                tm.setIsBlackListed(true);
                tokenManagementRepository.save(tm);
                return new CustomResponseEntity("Success", "Logout successful");
            } else {
                return new CustomResponseEntity("Token not found in database");
            }
        }
        return new CustomResponseEntity("Authorization token is missing or invalid");
    }

    @Override
    public boolean findBySessionToken(String sessionToken) {
        return jwtUtil.validateTokenFromDb(sessionToken);
    }
}
