package sk.tuke.gamestudio.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.GameUser;

@Service
@Transactional
public class AuthService {
    private final GameUserService gameUserService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(GameUserService gameUserService, PasswordEncoder passwordEncoder) {
        this.gameUserService = gameUserService;
        this.passwordEncoder = passwordEncoder;
    }

    public GameUser register(String username, String password) {
        String normalizedUsername = validateAndNormalizeUsername(username);
        validatePasswordStrength(password);

        if (gameUserService.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        GameUser user = new GameUser();
        user.setUsername(normalizedUsername);
        user.setPasswordHash(passwordEncoder.encode(password));
        return gameUserService.createUser(user);
    }

    public GameUser login(String username, String password) {
        String normalizedUsername = validateAndNormalizeUsername(username);
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        GameUser user = gameUserService.getUserByUsername(normalizedUsername);
        if (user == null) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }
        return user;
    }

    public GameUser findUserById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User is not logged in.");
        }
        GameUser user = gameUserService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User account was not found.");
        }
        return user;
    }

    public GameUser updateUsername(Long userId, String newUsername) {
        GameUser user = findUserById(userId);
        String normalizedUsername = validateAndNormalizeUsername(newUsername);

        if (!user.getUsername().equalsIgnoreCase(normalizedUsername)
                && gameUserService.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        user.setUsername(normalizedUsername);
        return gameUserService.updateUser(user);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        GameUser user = findUserById(userId);
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password is required.");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        validatePasswordStrength(newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        gameUserService.updateUser(user);
    }

    private String validateAndNormalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        String normalizedUsername = username.trim();
        if (normalizedUsername.length() < 3 || normalizedUsername.length() > 64) {
            throw new IllegalArgumentException("Username must have 3 to 64 characters.");
        }
        return normalizedUsername;
    }

    private void validatePasswordStrength(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (password.length() < 6 || password.length() > 100) {
            throw new IllegalArgumentException("Password must have 6 to 100 characters.");
        }
    }
}
