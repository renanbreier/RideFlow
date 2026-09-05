package br.com.rideflow.auth;

import br.com.rideflow.auth.dto.AuthResponse;
import br.com.rideflow.auth.dto.LoginRequest;
import br.com.rideflow.auth.dto.RegisterRequest;
import br.com.rideflow.user.UserRole;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/passenger")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerPassenger(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request, UserRole.ROLE_PASSENGER);
    }

    @PostMapping("/register/driver")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerDriver(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request, UserRole.ROLE_DRIVER);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
