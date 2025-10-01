package exam.portal.services;

import exam.portal.dtos.request.LoginDto;
import exam.portal.dtos.request.UserDto;
import exam.portal.global_exceptions_handler.CustomResponseEntity;
import jakarta.servlet.http.HttpServletRequest;

public interface UserAuthService {

    CustomResponseEntity<?> register(UserDto userDto);

    CustomResponseEntity<?> login(LoginDto loginDto);

    CustomResponseEntity<?> logout(HttpServletRequest request);

    boolean findBySessionToken(String sessionToken);
}
