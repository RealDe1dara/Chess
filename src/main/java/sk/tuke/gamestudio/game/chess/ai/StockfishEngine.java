package sk.tuke.gamestudio.game.chess.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class StockfishEngine {

    private static final Logger log = LoggerFactory.getLogger(StockfishEngine.class);
    private static final String API_URL = "https://stockfish.online/api/s/v2.php";
    private static final int MAX_DEPTH = 15;

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private StockfishEngine() {}

    public static String bestMove(String fen, int depth) throws IOException, InterruptedException {
        int d = Math.max(1, Math.min(depth, MAX_DEPTH));
        String url = API_URL
                + "?fen="   + URLEncoder.encode(fen, StandardCharsets.UTF_8)
                + "&depth=" + d;

        log.debug("Stockfish request: {}", url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("Stockfish response (depth {}): {}", d, response.body());

        if (response.statusCode() != 200) {
            log.warn("Stockfish API returned HTTP {}", response.statusCode());
            return null;
        }

        JsonNode root = MAPPER.readTree(response.body());

        if (!root.path("success").asBoolean(false)) {
            log.warn("Stockfish API success=false: {}", response.body());
            return null;
        }

        // "bestmove e2e4 ponder e7e5" → take the second token
        String bestmoveField = root.path("bestmove").asText("");
        String[] parts = bestmoveField.split("\\s+");
        if (parts.length >= 2 && !"(none)".equals(parts[1])) {
            log.info("Stockfish chose: {}", parts[1]);
            return parts[1];
        }

        return null;
    }
}
