package br.com.rideflow.auth;

import br.com.rideflow.auth.dto.AuthResponse;
import br.com.rideflow.auth.dto.LoginRequest;
import br.com.rideflow.auth.dto.RegisterRequest;
import br.com.rideflow.user.User;
import br.com.rideflow.user.UserRepository;
import br.com.rideflow.user.UserRole;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, UserRole role) {
        String email = User.normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException();
        }

        User user = User.register(
                request.name(),
                email,
                passwordEncoder.encode(request.password()),
                role
        );

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new EmailAlreadyRegisteredException();
        }

        return tokenFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(User.normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return tokenFor(user);
    }

    private AuthResponse tokenFor(User user) {
        return AuthResponse.bearer(
                jwtService.generateAccessToken(user),
                jwtService.accessTokenTtlSeconds()
        );
    }
}
