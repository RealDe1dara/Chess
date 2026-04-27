package sk.tuke.gamestudio.server.webservice;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.tuke.gamestudio.entity.GameUser;
import sk.tuke.gamestudio.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final String SESSION_USER_ID = "userId";
    private static final String SESSION_USERNAME = "username";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody CredentialsRequest request, HttpSession session) {
        try {
            GameUser user = authService.register(request.username(), request.password());
            applySession(session, user);
            return ResponseEntity.ok(new AuthResponse(true, toDto(user), "Registration successful."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, null, e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody CredentialsRequest request, HttpSession session) {
        try {
            GameUser user = authService.login(request.username(), request.password());
            applySession(session, user);
            return ResponseEntity.ok(new AuthResponse(true, toDto(user), "Login successful."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, null, e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public AuthResponse logout(HttpSession session) {
        session.invalidate();
        return new AuthResponse(false, null, "Logged out.");
    }

    @GetMapping("/me")
    public AuthResponse me(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        String username = (String) session.getAttribute(SESSION_USERNAME);
        if (userId == null || username == null) {
            return new AuthResponse(false, null, "Not authenticated.");
        }
        return new AuthResponse(true, new UserDto(userId, username), null);
    }

    @GetMapping("/profile")
    public ResponseEntity<AuthResponse> profile(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, null, "Not authenticated."));
        }
        try {
            GameUser user = authService.findUserById(userId);
            session.setAttribute(SESSION_USERNAME, user.getUsername());
            return ResponseEntity.ok(new AuthResponse(true, toDto(user), null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, null, e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<AuthResponse> updateProfile(@RequestBody UpdateProfileRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, null, "Not authenticated."));
        }
        try {
            GameUser user = authService.updateUsername(userId, request.username());
            session.setAttribute(SESSION_USERNAME, user.getUsername());
            return ResponseEntity.ok(new AuthResponse(true, toDto(user), "Profile updated."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, null, e.getMessage()));
        }
    }

    @PostMapping("/password")
    public ResponseEntity<AuthResponse> changePassword(@RequestBody ChangePasswordRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(false, null, "Not authenticated."));
        }
        try {
            authService.changePassword(userId, request.currentPassword(), request.newPassword());
            return ResponseEntity.ok(new AuthResponse(true, null, "Password changed."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new AuthResponse(false, null, e.getMessage()));
        }
    }

    private void applySession(HttpSession session, GameUser user) {
        session.setAttribute(SESSION_USER_ID, user.getId());
        session.setAttribute(SESSION_USERNAME, user.getUsername());
    }

    private UserDto toDto(GameUser user) {
        return new UserDto(user.getId(), user.getUsername());
    }

    public record CredentialsRequest(String username, String password) {}

    public record UpdateProfileRequest(String username) {}

    public record ChangePasswordRequest(String currentPassword, String newPassword) {}

    public record UserDto(Long id, String username) {}

    public record AuthResponse(boolean authenticated, UserDto user, String message) {}
}
