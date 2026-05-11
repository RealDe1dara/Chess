package sk.tuke.gamestudio.server.webservice;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.GameSession;
import sk.tuke.gamestudio.game.chess.ai.ChessAiPlayer;
import sk.tuke.gamestudio.game.chess.core.*;
import sk.tuke.gamestudio.game.chess.core.pieces.Piece;
import sk.tuke.gamestudio.service.GameSessionService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/game")
public class ChessController {

    private static final String GAME = "chess";
    private static final int MAX_CHAT_MESSAGES = 200;

    private final ConcurrentHashMap<Long, GameController> activeGames = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Color> drawProposals = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<ChatMessage>> gameChats = new ConcurrentHashMap<>();

    private final GameSessionService gameSessionService;
    private final GameSseBroadcaster sseBroadcaster;
    private final ChessAiPlayer aiPlayer;
    private final TokenStore tokenStore;

    public ChessController(GameSessionService gameSessionService, GameSseBroadcaster sseBroadcaster,
                           ChessAiPlayer aiPlayer, TokenStore tokenStore) {
        this.gameSessionService = gameSessionService;
        this.sseBroadcaster = sseBroadcaster;
        this.aiPlayer = aiPlayer;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/create")
    @Transactional
    public ResponseEntity<?> create(@RequestBody CreateRequest req, HttpServletRequest request) {
        String username = resolveUsername(request);
        if (username == null) return unauthorized();

        String mode = switch (req.mode() == null ? "" : req.mode()) {
            case "COMPUTER" -> "COMPUTER";
            case "REMOTE"   -> "REMOTE";
            default         -> "LOCAL";
        };

        GameSession entity = new GameSession();
        entity.setMode(mode);
        entity.setCreatedAt(new Date());

        if ("COMPUTER".equals(mode)) {
            String rawColor = req.humanColor() == null ? "WHITE" : req.humanColor().toUpperCase();
            boolean humanWhite = "RANDOM".equals(rawColor) ? new Random().nextBoolean() : !"BLACK".equals(rawColor);

            entity.setPlayerWhite(humanWhite ? username : "Computer");
            entity.setPlayerBlack(humanWhite ? "Computer" : username);
            entity.setStatus("ACTIVE");
            entity.setTimeLimitSeconds(0);
            entity.setComputerDifficulty(req.difficulty() != null ? req.difficulty() : "MID");

            gameSessionService.persist(entity);

            GameController gc = new GameController();
            activeGames.put(entity.getId(), gc);

            if (!humanWhite) {
                aiPlayer.makeMove(entity, gc);
            }

            return ResponseEntity.ok(toDto(entity, gc, "Game created."));
        }

        int limitSeconds = (req.timeLimitSeconds() != null && req.timeLimitSeconds() > 0)
                           ? req.timeLimitSeconds() : 0;

        entity.setTimeLimitSeconds(limitSeconds);

        if (limitSeconds > 0) {
            long ms = (long) limitSeconds * 1000;
            entity.setTimeWhiteMs(ms);
            entity.setTimeBlackMs(ms);
        }

        if ("LOCAL".equals(mode)) {
            entity.setPlayerWhite(username);
            entity.setPlayerBlack(username);
            entity.setStatus("ACTIVE");
            if (limitSeconds > 0) entity.setLastMoveAt(System.currentTimeMillis());
        } else {
            boolean creatorIsWhite = new Random().nextBoolean();
            entity.setPlayerWhite(creatorIsWhite ? username : null);
            entity.setPlayerBlack(creatorIsWhite ? null : username);
            entity.setStatus("WAITING");
        }

        gameSessionService.persist(entity);

        GameController gc = new GameController();
        activeGames.put(entity.getId(), gc);

        return ResponseEntity.ok(toDto(entity, gc, "Game created."));
    }

    @PostMapping("/{id}/join")
    @Transactional
    public ResponseEntity<?> join(@PathVariable Long id, HttpServletRequest request) {
        String username = resolveUsername(request);
        if (username == null) return unauthorized();

        GameSession entity = gameSessionService.findById(id);
        if (entity == null) return ResponseEntity.notFound().build();
        if (!"WAITING".equals(entity.getStatus()))
            return badRequest("This game is not waiting for a player.");
        if (username.equals(entity.getPlayerWhite()) || username.equals(entity.getPlayerBlack()))
            return badRequest("You created this game — share the ID with a friend.");

        if (entity.getPlayerWhite() == null) {
            entity.setPlayerWhite(username);
        } else {
            entity.setPlayerBlack(username);
        }
        entity.setStatus("ACTIVE");

        if (entity.getTimeLimitSeconds() != null && entity.getTimeLimitSeconds() > 0) {
            entity.setLastMoveAt(System.currentTimeMillis());
        }

        GameController gc = activeGames.computeIfAbsent(id, k -> new GameController());
        GameStateDto state = toDto(entity, gc, "Game started!");
        sseBroadcaster.broadcast(id, state);

        return ResponseEntity.ok(state);
    }

    @GetMapping("/{id}/state")
    @Transactional
    public ResponseEntity<?> state(@PathVariable Long id, HttpServletRequest request) {
        if (resolveUsername(request) == null) return unauthorized();

        GameSession entity = gameSessionService.findById(id);
        if (entity == null) return ResponseEntity.notFound().build();

        GameController gc = activeGames.get(id);
        return ResponseEntity.ok(toDto(entity, gc, ""));
    }

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@PathVariable Long id) {
        return sseBroadcaster.subscribe(id);
    }

    @PostMapping("/{id}/move")
    @Transactional
    public ResponseEntity<?> move(
            @PathVariable Long id,
            @RequestBody MoveRequest req,
            HttpServletRequest request) {

        String username = resolveUsername(request);
        if (username == null) return unauthorized();

        GameSession entity = gameSessionService.findById(id);
        if (entity == null) return ResponseEntity.notFound().build();
        if (!"ACTIVE".equals(entity.getStatus()))
            return badRequest("Game is not active.");

        GameController gc = activeGames.get(id);
        if (gc == null) return serverRestarted();
        if (gc.getGameState() != GameState.ACTIVE)
            return badRequest("Game is already finished.");
        if (gc.isPromotionPending())
            return badRequest("Finish promotion first.");

        if ("REMOTE".equals(entity.getMode())) {
            Color turn = gc.getCurrentPlayer().getColor();
            boolean isWhite = username.equals(entity.getPlayerWhite());
            boolean isBlack = username.equals(entity.getPlayerBlack());
            if (turn == Color.WHITE && !isWhite) return badRequest("It's not your turn.");
            if (turn == Color.BLACK && !isBlack) return badRequest("It's not your turn.");
        } else if ("COMPUTER".equals(entity.getMode())) {
            boolean humanIsWhite = username.equals(entity.getPlayerWhite());
            boolean humanIsBlack = username.equals(entity.getPlayerBlack());
            if (!humanIsWhite && !humanIsBlack) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            Color humanColor = humanIsWhite ? Color.WHITE : Color.BLACK;
            if (gc.getCurrentPlayer().getColor() != humanColor) return badRequest("It's not your turn.");
        }

        String timeoutMsg = gameSessionService.checkTimeout(entity, gc);
        if (timeoutMsg != null) {
            GameStateDto s = toDto(entity, gc, timeoutMsg);
            sseBroadcaster.broadcast(id, s);
            return ResponseEntity.ok(s);
        }

        Square from = parseSquare(req.from(), gc);
        Square to   = parseSquare(req.to(),   gc);
        if (from == null || to == null) return badRequest("Invalid square notation.");
        if (!gc.movePiece(from.getPiece(), to)) return badRequest("Illegal move.");

        drawProposals.remove(id);

        if (entity.getTimeLimitSeconds() != null && entity.getTimeLimitSeconds() > 0) {
            entity.setLastMoveAt(System.currentTimeMillis());
        }

        gameSessionService.finalizeIfEnded(entity, gc);

        if ("COMPUTER".equals(entity.getMode())
                && gc.getGameState() == GameState.ACTIVE
                && !gc.isPromotionPending()) {
            sseBroadcaster.broadcast(id, toDto(entity, gc, "Thinking…"));
            try { Thread.sleep(600); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            aiPlayer.makeMove(entity, gc);
            gameSessionService.finalizeIfEnded(entity, gc);
        }

        String message = gc.isPromotionPending() ? "Choose promotion piece." : "";
        GameStateDto state = toDto(entity, gc, message);
        sseBroadcaster.broadcast(id, state);
        return ResponseEntity.ok(state);
    }

    @GetMapping("/{id}/valid-moves")
    public ResponseEntity<?> validMoves(
            @PathVariable Long id,
            @RequestParam String from,
            HttpServletRequest request) {

        if (resolveUsername(request) == null) return unauthorized();

        GameController gc = activeGames.get(id);
        if (gc == null) return ResponseEntity.ok(List.of());
        if (gc.isPromotionPending()) return ResponseEntity.ok(List.of());

        Square sq = parseSquare(from, gc);
        if (sq == null || sq.getPiece() == null) return ResponseEntity.ok(List.of());

        Piece piece = sq.getPiece();
        if (piece.getColor() != gc.getCurrentPlayer().getColor()) return ResponseEntity.ok(List.of());

        List<String> targets = gc.getValidTargets(piece).stream()
                .map(s -> squareLabel(s.getRow(), s.getColumn()))
                .toList();
        return ResponseEntity.ok(targets);
    }

    @PostMapping("/{id}/promote")
    @Transactional
    public ResponseEntity<?> promote(
            @PathVariable Long id,
            @RequestBody PromoteRequest req,
            HttpServletRequest request) {

        String username = resolveUsername(request);
        if (username == null) return unauthorized();

        GameSession entity = gameSessionService.findById(id);
        GameController gc = activeGames.get(id);
        if (entity == null || gc == null) return ResponseEntity.notFound().build();
        if (!gc.isPromotionPending()) return badRequest("No promotion pending.");

        if ("REMOTE".equals(entity.getMode())) {
            Color promotingColor = gc.getPromotingColor();
            boolean allowed = (promotingColor == Color.WHITE && username.equals(entity.getPlayerWhite()))
                           || (promotingColor == Color.BLACK && username.equals(entity.getPlayerBlack()));
            if (!allowed) return badRequest("It's not your promotion.");
        }

        gc.promotePawn(req.piece());
        gameSessionService.finalizeIfEnded(entity, gc);

        if ("COMPUTER".equals(entity.getMode())
                && gc.getGameState() == GameState.ACTIVE
                && !gc.isPromotionPending()) {
            aiPlayer.makeMove(entity, gc);
            gameSessionService.finalizeIfEnded(entity, gc);
        }

        GameStateDto state = toDto(entity, gc, "Pawn promoted.");
        sseBroadcaster.broadcast(id, state);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{id}/resign")
    @Transactional
    public ResponseEntity<?> resign(@PathVariable Long id, HttpServletRequest request) {
        String username = resolveUsername(request);
        if (username == null) return unauthorized();

        GameSession entity = gameSessionService.findById(id);
        GameController gc = activeGames.get(id);
        if (entity == null || gc == null) return ResponseEntity.notFound().build();
        if (!"ACTIVE".equals(entity.getStatus())) return badRequest("Game is not active.");

        if ("LOCAL".equals(entity.getMode())) {
            gc.resign();
        } else {
            Color resigningColor;
            if (username.equals(entity.getPlayerWhite())) resigningColor = Color.WHITE;
            else if (username.equals(entity.getPlayerBlack())) resigningColor = Color.BLACK;
            else return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            gc.resignColor(resigningColor);
        }

        gameSessionService.finalizeIfEnded(entity, gc);
        GameStateDto state = toDto(entity, gc, "Player resigned.");
        sseBroadcaster.broadcast(id, state);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{id}/timeout")
    @Transactional
    public ResponseEntity<?> timeout(@PathVariable Long id, HttpServletRequest request) {
        String username = resolveUsername(request);
        if (username == null) return unauthorized();

        GameSession entity = gameSessionService.findById(id);
        GameController gc = activeGames.get(id);
        if (entity == null || gc == null) return ResponseEntity.notFound().build();
        if (!"ACTIVE".equals(entity.getStatus())) return badRequest("Game is not active.");
        if (entity.getTimeLimitSeconds() == null || entity.getTimeLimitSeconds() <= 0)
            return badRequest("No time limit in this game.");

        Color timedOutColor;
        if ("LOCAL".equals(entity.getMode())) {
            timedOutColor = gc.getCurrentPlayer().getColor();
        } else {
            timedOutColor = colorOf(username, entity);
            if (timedOutColor == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        long stored  = timedOutColor == Color.WHITE ? entity.getTimeWhiteMs() : entity.getTimeBlackMs();
        long elapsed = entity.getLastMoveAt() != null ? System.currentTimeMillis() - entity.getLastMoveAt() : 0;
        long actual  = Math.max(0, stored - elapsed);
        if (actual > 500) return badRequest("Time has not run out yet.");

        if (timedOutColor == Color.WHITE) {
            entity.setTimeWhiteMs(0L);
            gc.timeoutColor(Color.WHITE);
        } else {
            entity.setTimeBlackMs(0L);
            gc.timeoutColor(Color.BLACK);
        }

        gameSessionService.finalizeIfEnded(entity, gc);
        GameStateDto state = toDto(entity, gc, "Time's up!");
        sseBroadcaster.broadcast(id, state);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{id}/draw-offer")
    @Transactional
    public ResponseEntity<?> drawOffer(@PathVariable Long id, HttpServletRequest request) {
        String username = resolveUsername(request);
        if (username == null) return unauthorized();

        GameSession entity = gameSessionService.findById(id);
        GameController gc = activeGames.get(id);
        if (entity == null || gc == null) return ResponseEntity.notFound().build();
        if (gc.isPromotionPending()) return badRequest("Finish promotion first.");
        if (!"ACTIVE".equals(entity.getStatus())) return badRequest("Game is not active.");

        Color offeringColor = colorOf(username, entity);
        if (offeringColor == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        if (offeringColor.equals(drawProposals.get(id))) return badRequest("You already offered a draw.");

        drawProposals.put(id, offeringColor);
        GameStateDto state = toDto(entity, gc, "Draw offered.");
        sseBroadcaster.broadcast(id, state);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{id}/draw-accept")
    @Transactional
    public ResponseEntity<?> drawAccept(@PathVariable Long id, HttpServletRequest request) {
        String username = resolveUsername(request);
        if (username == null) return unauthorized();

        GameSession entity = gameSessionService.findById(id);
        GameController gc = activeGames.get(id);
        if (entity == null || gc == null) return ResponseEntity.notFound().build();

        Color offeringColor = drawProposals.get(id);
        if (offeringColor == null) return badRequest("No draw offer pending.");

        if (!"LOCAL".equals(entity.getMode())) {
            Color acceptingColor = colorOf(username, entity);
            if (acceptingColor == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            if (acceptingColor == offeringColor) return badRequest("You cannot accept your own draw offer.");
        }

        gc.drawByAgreement();
        drawProposals.remove(id);
        gameSessionService.finalizeIfEnded(entity, gc);
        GameStateDto state = toDto(entity, gc, "Draw accepted.");
        sseBroadcaster.broadcast(id, state);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{id}/draw-decline")
    @Transactional
    public ResponseEntity<?> drawDecline(@PathVariable Long id, HttpServletRequest request) {
        String username = resolveUsername(request);
        if (username == null) return unauthorized();

        GameSession entity = gameSessionService.findById(id);
        GameController gc = activeGames.get(id);
        if (entity == null || gc == null) return ResponseEntity.notFound().build();

        drawProposals.remove(id);
        GameStateDto state = toDto(entity, gc, "Draw declined.");
        sseBroadcaster.broadcast(id, state);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{id}/chat")
    @Transactional
    public ResponseEntity<?> chat(
            @PathVariable Long id,
            @RequestBody ChatRequest req,
            HttpServletRequest request) {

        String username = resolveUsername(request);
        if (username == null) return unauthorized();

        String text = req.text() == null ? "" : req.text().trim();
        if (text.isEmpty())       return badRequest("Message cannot be empty.");
        if (text.length() > 500) return badRequest("Message is too long (max 500 characters).");

        GameSession entity = gameSessionService.findById(id);
        if (entity == null) return ResponseEntity.notFound().build();

        CopyOnWriteArrayList<ChatMessage> messages =
                gameChats.computeIfAbsent(id, k -> new CopyOnWriteArrayList<>());

        while (messages.size() >= MAX_CHAT_MESSAGES) messages.remove(0);

        messages.add(new ChatMessage(username, text, System.currentTimeMillis()));

        GameController gc = activeGames.get(id);
        if (gc == null) return serverRestarted();

        GameStateDto state = toDto(entity, gc, "");
        sseBroadcaster.broadcast(id, state);
        return ResponseEntity.ok(state);
    }

    private GameStateDto toDto(GameSession entity, GameController gc, String message) {
        String[][] board = null;
        String currentTurn = null;
        boolean promotionPending = false;
        String drawProposedByUser = null;
        String winReason = null;
        String drawReason = null;
        boolean whiteInCheck = false;
        boolean blackInCheck = false;
        String lastMoveFrom = null, lastMoveTo = null;

        if (gc != null) {
            board = buildBoard(gc);
            currentTurn = gc.getCurrentPlayer().getColor().name();
            promotionPending = gc.isPromotionPending();
            winReason = gc.getWinReason() != null ? gc.getWinReason().name() : null;
            drawReason = gc.getDrawReason() != null ? gc.getDrawReason().name() : null;
            whiteInCheck = gc.isInCheck(gc.getWhitePlayer());
            blackInCheck = gc.isInCheck(gc.getBlackPlayer());

            Color proposingColor = drawProposals.get(entity.getId());
            if (proposingColor != null) {
                drawProposedByUser = (proposingColor == Color.WHITE)
                        ? entity.getPlayerWhite()
                        : entity.getPlayerBlack();
            }

            Move lastMove = gc.getBoard().getLastMove();
            if (lastMove != null) {
                lastMoveFrom = squareLabel(lastMove.getOldSquare().getRow(), lastMove.getOldSquare().getColumn());
                lastMoveTo   = squareLabel(lastMove.getNewSquare().getRow(), lastMove.getNewSquare().getColumn());
            }
        }

        List<ChatMessage> chatMsgs = new ArrayList<>(
                gameChats.getOrDefault(entity.getId(), new CopyOnWriteArrayList<>()));

        return new GameStateDto(
                entity.getId(),
                entity.getPlayerWhite(),
                entity.getPlayerBlack(),
                entity.getMode(),
                entity.getStatus(),
                entity.getResult(),
                winReason,
                drawReason,
                currentTurn,
                board,
                promotionPending,
                drawProposedByUser,
                message,
                chatMsgs,
                whiteInCheck,
                blackInCheck,
                entity.getTimeLimitSeconds(),
                entity.getTimeWhiteMs(),
                entity.getTimeBlackMs(),
                entity.getComputerDifficulty(),
                lastMoveFrom,
                lastMoveTo
        );
    }

    private String[][] buildBoard(GameController gc) {
        String[][] board = new String[8][8];
        for (int r = 0; r < 8; r++)
            for (int c = 0; c < 8; c++)
                board[r][c] = encodePiece(gc.getBoard().getSquare(r, c).getPiece());
        return board;
    }

    private String encodePiece(Piece piece) {
        if (piece == null) return null;
        String color = piece.getColor() == Color.WHITE ? "W" : "B";
        String type = switch (piece.getClass().getSimpleName()) {
            case "King"   -> "K"; case "Queen"  -> "Q"; case "Rook"   -> "R";
            case "Bishop" -> "B"; case "Knight" -> "N"; case "Pawn"   -> "P";
            default       -> "?";
        };
        return color + type;
    }

    private String squareLabel(int row, int col) {
        return String.valueOf((char) ('a' + col)) + (8 - row);
    }

    private Square parseSquare(String notation, GameController gc) {
        if (notation == null) return null;
        String s = notation.trim().toLowerCase();
        if (s.length() != 2) return null;
        char file = s.charAt(0); char rank = s.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') return null;
        return gc.getBoard().getSquare(8 - (rank - '0'), file - 'a');
    }

    private Color colorOf(String username, GameSession entity) {
        if (username.equals(entity.getPlayerWhite())) return Color.WHITE;
        if (username.equals(entity.getPlayerBlack())) return Color.BLACK;
        return null;
    }

    private String resolveUsername(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        return tokenStore.getUsername(header.substring(7));
    }

    private ResponseEntity<String> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated.");
    }

    private ResponseEntity<String> badRequest(String msg) {
        return ResponseEntity.badRequest().body(msg);
    }

    private ResponseEntity<String> serverRestarted() {
        return ResponseEntity.status(HttpStatus.GONE)
                .body("Game state was lost (server restarted). Please start a new game.");
    }

    public record CreateRequest(String mode, Integer timeLimitSeconds, String difficulty, String humanColor) {}
    public record MoveRequest(String from, String to) {}
    public record PromoteRequest(String piece) {}
    public record ChatRequest(String text) {}
    public record ChatMessage(String sender, String text, long sentAt) {}

    public record GameStateDto(
            Long id, String playerWhite, String playerBlack,
            String mode, String status, String result,
            String winReason, String drawReason,
            String currentTurn, String[][] board,
            boolean promotionPending, String drawProposedBy, String message,
            List<ChatMessage> chatMessages,
            boolean whiteInCheck, boolean blackInCheck,
            Integer timeLimitSeconds, Long timeWhiteMs, Long timeBlackMs,
            String computerDifficulty,
            String lastMoveFrom, String lastMoveTo
    ) {}
}
