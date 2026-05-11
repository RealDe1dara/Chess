package sk.tuke.gamestudio.server.webservice;

import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {

    // ConcurrentHashMap is thread-safe: concurrent login/logout requests from multiple
    // tabs won't corrupt the map.
    private final ConcurrentHashMap<String, TokenData> tokens = new ConcurrentHashMap<>();

    public String issue(Long userId, String username) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, new TokenData(userId, username));
        return token;
    }

    public void revoke(String token) {
        tokens.remove(token);
    }

    public Long getUserId(String token) {
        TokenData data = tokens.get(token);
        return data != null ? data.userId() : null;
    }

    public String getUsername(String token) {
        TokenData data = tokens.get(token);
        return data != null ? data.username() : null;
    }

    public void updateUsername(Long userId, String newUsername) {
        tokens.replaceAll((token, data) ->
            data.userId().equals(userId) ? new TokenData(userId, newUsername) : data
        );
    }

    public record TokenData(Long userId, String username) {}
}
