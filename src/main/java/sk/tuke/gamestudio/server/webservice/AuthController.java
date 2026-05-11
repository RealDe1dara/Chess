package sk.tuke.gamestudio.server.webservice;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.GameUser;
import sk.tuke.gamestudio.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenStore tokenStore;

    public AuthController(AuthService authService, TokenStore tokenStore) {
        this.authService = authService;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody CredentialsRequest request) {
        try {
            GameUser user = authService.register(request.username(), request.password());
            String token = tokenStore.issue(user.getId(), user.getUsername());
            return ResponseEntity.ok(new AuthResponse(true, toDto(user), "Registration successful.", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, null, e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody CredentialsRequest request) {
        try {
            GameUser user = authService.login(request.username(), request.password());
            // Each login issues a fresh token so multiple tabs stay independent.
            String token = tokenStore.issue(user.getId(), user.getUsername());
            return ResponseEntity.ok(new AuthResponse(true, toDto(user), "Login successful.", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, null, e.getMessage(), null));
        }
    }

    @PostMapping("/logout")
    public AuthResponse logout(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) tokenStore.revoke(token);
        return new AuthResponse(false, null, "Logged out.", null);
    }

    @GetMapping("/me")
    public AuthResponse me(HttpServletRequest request) {
        String token = extractToken(request);
        Long userId = tokenStore.getUserId(token);
        String username = tokenStore.getUsername(token);
        if (userId == null || username == null) {
            return new AuthResponse(false, null, "Not authenticated.", null);
        }
        return new AuthResponse(true, new UserDto(userId, username), null, null);
    }

    @PutMapping("/profile")
    public ResponseEntity<AuthResponse> updateProfile(
            @RequestBody UpdateProfileRequest updateRequest,
            HttpServletRequest httpRequest) {
        Long userId = resolveUserId(httpRequest);
        if (userId == null) return unauthenticated();
        try {
            GameUser user = authService.updateUsername(userId, updateRequest.username());
            // Update ALL active tokens for this user (one per open tab) so every tab
            // immediately reflects the new username without requiring re-login.
            tokenStore.updateUsername(userId, user.getUsername());
            return ResponseEntity.ok(new AuthResponse(true, toDto(user), "Profile updated.", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, null, e.getMessage(), null));
        }
    }

    @PostMapping("/password")
    public ResponseEntity<AuthResponse> changePassword(
            @RequestBody ChangePasswordRequest changeRequest,
            HttpServletRequest httpRequest) {
        Long userId = resolveUserId(httpRequest);
        if (userId == null) return unauthenticated();
        try {
            authService.changePassword(userId, changeRequest.currentPassword(), changeRequest.newPassword());
            return ResponseEntity.ok(new AuthResponse(true, null, "Password changed.", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, null, e.getMessage(), null));
        }
    }

    private String extractToken(HttpServletRequest request) {
        if (request == null) return null;
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        return header.substring(7);
    }

    private Long resolveUserId(HttpServletRequest request) {
        return tokenStore.getUserId(extractToken(request));
    }

    private ResponseEntity<AuthResponse> unauthenticated() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(false, null, "Not authenticated.", null));
    }

    private UserDto toDto(GameUser user) {
        return new UserDto(user.getId(), user.getUsername());
    }

    public record CredentialsRequest(String username, String password) {}
    public record UpdateProfileRequest(String username) {}
    public record ChangePasswordRequest(String currentPassword, String newPassword) {}
    public record UserDto(Long id, String username) {}
    public record AuthResponse(boolean authenticated, UserDto user, String message, String token) {}
}
