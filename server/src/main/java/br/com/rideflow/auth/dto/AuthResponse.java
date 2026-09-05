package br.com.rideflow.auth.dto;

public record AuthResponse(String accessToken, String type, long expiresIn) {

    public static AuthResponse bearer(String accessToken, long expiresIn) {
        return new AuthResponse(accessToken, "Bearer", expiresIn);
    }
}
