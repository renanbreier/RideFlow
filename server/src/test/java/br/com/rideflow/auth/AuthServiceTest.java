package br.com.rideflow.auth;

import br.com.rideflow.auth.dto.AuthResponse;
import br.com.rideflow.auth.dto.LoginRequest;
import br.com.rideflow.auth.dto.RegisterRequest;
import br.com.rideflow.user.User;
import br.com.rideflow.user.UserRepository;
import br.com.rideflow.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void shouldRegisterPassengerWithNormalizedEmailAndEncodedPassword() {
        RegisterRequest request = new RegisterRequest("Theo", "  THEO@example.com ", "password123");
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(3600L);

        AuthResponse response = authService.register(request, UserRole.ROLE_PASSENGER);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.type()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(3600L);
        verify(userRepository).existsByEmail("theo@example.com");
        verify(userRepository).saveAndFlush(any(User.class));
    }

    @Test
    void shouldRejectAnEmailThatIsAlreadyRegistered() {
        RegisterRequest request = new RegisterRequest("Theo", "theo@example.com", "password123");
        when(userRepository.existsByEmail("theo@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, UserRole.ROLE_DRIVER))
                .isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldLoginWithValidCredentials() {
        User user = User.register("Theo", "theo@example.com", "encoded-password", UserRole.ROLE_DRIVER);
        when(userRepository.findByEmail("theo@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.accessTokenTtlSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(new LoginRequest("THEO@example.com", "password123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
    }

    @Test
    void shouldNotRevealWhetherTheEmailOrPasswordIsWrong() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("missing@example.com", "password123")))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("E-mail ou senha inválidos");
    }
}
