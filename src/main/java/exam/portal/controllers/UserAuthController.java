package exam.portal.controllers;

import exam.portal.dtos.request.LoginDto;
import exam.portal.dtos.request.UserDto;
import exam.portal.global_exceptions_handler.CustomResponseEntity;
import exam.portal.services.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/user-auth")
public class UserAuthController {

    private final UserAuthService authService;

    @Autowired
    public UserAuthController(UserAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public CustomResponseEntity register(@RequestBody UserDto userDto){
        return this.authService.register(userDto);
    }


    @GetMapping("/validateToken")
    public boolean validateToken(@RequestHeader("Authorization") String bearerToken) {
        return authService.findBySessionToken(bearerToken);
    }

    @PostMapping("/login")
    public CustomResponseEntity login(@Valid @RequestBody LoginDto loginDto) {
        return this.authService.login(loginDto);
    }

    @PostMapping("/logout")
    public CustomResponseEntity logout(HttpServletRequest request) {
        return authService.logout(request);
    }

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a public endpoint. No token required.";
    }

    @GetMapping("/secure")
    public String secureEndpoint() {
        return "This is a secure endpoint. Valid token required.";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('HR')")
    public String adminEndpoint() {
        return "This is an ADMIN endpoint. SUPER_ADMIN role required.";
    }



}
