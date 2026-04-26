package sk.tuke.gamestudio.server.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sk.tuke.gamestudio.game.chess.core.Color;
import sk.tuke.gamestudio.game.chess.core.GameController;
import sk.tuke.gamestudio.game.chess.core.GameState;
import sk.tuke.gamestudio.game.chess.core.Square;
import sk.tuke.gamestudio.game.chess.core.pieces.Piece;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.RatingService;
import sk.tuke.gamestudio.service.ScoreService;

import java.util.Date;
import java.util.List;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
public class ChessController {
    private static final String GAME = "chess";
    private static final int[] INDICES = {0, 1, 2, 3, 4, 5, 6, 7};

    private final ScoreService scoreService;
    private final RatingService ratingService;

    private GameController gameController = new GameController();
    private Color drawProposedBy;
    private String gameMessage = "";
    private String playerName = "Player";

    public ChessController(ScoreService scoreService, RatingService ratingService) {
        this.scoreService = scoreService;
        this.ratingService = ratingService;
    }

    @RequestMapping("/chess")
    public String chessMenu(Model model) {
        model.addAttribute("topScores", scoreService.getTopScores(GAME));
        model.addAttribute("averageRating", ratingService.getAverageRating(GAME));
        model.addAttribute("playerName", playerName);
        return "chess-menu";
    }

    @PostMapping("/chess/rate")
    public String rateGame(@RequestParam("player") String player,
                           @RequestParam("value") int value,
                           RedirectAttributes redirectAttributes) {
        if (player == null || player.isBlank()) {
            redirectAttributes.addFlashAttribute("menuMessage", "Player name is required.");
            return "redirect:/chess";
        }
        if (value < 1 || value > 5) {
            redirectAttributes.addFlashAttribute("menuMessage", "Rating must be from 1 to 5.");
            return "redirect:/chess";
        }

        playerName = player.trim();
        ratingService.setRating(new Rating(GAME, playerName, value, new Date()));
        redirectAttributes.addFlashAttribute("menuMessage", "Rating saved.");
        return "redirect:/chess";
    }

    @RequestMapping("/chess/play")
    public String chess(@RequestParam(value = "command", required = false) String command,
                        @RequestParam(value = "squareFrom", required = false) String squareFrom,
                        @RequestParam(value = "squareTo", required = false) String squareTo,
                        @RequestParam(value = "piece", required = false) String piece,
                        Model model) {
        if (command != null && !command.isBlank()) {
            handleCommand(
                    command.trim().toLowerCase(),
                    squareFrom,
                    squareTo,
                    piece
            );
        }

        model.addAttribute("gameController", gameController);
        model.addAttribute("board", gameController.getBoard());
        model.addAttribute("rows", INDICES);
        model.addAttribute("columns", INDICES);
        model.addAttribute("gameState", gameController.getGameState());
        model.addAttribute("currentColor", gameController.getCurrentPlayer().getColor());
        model.addAttribute("drawProposedBy", drawProposedBy);
        model.addAttribute("message", gameMessage);
        model.addAttribute("promotionPending", gameController.isPromotionPending());
        model.addAttribute("playerName", playerName);
        return "chess";
    }

    public String pieceSymbol(Piece piece) {
        if (piece == null) {
            return "";
        }
        return switch (piece.getClass().getSimpleName()) {
            case "King" -> piece.getColor() == Color.WHITE ? "\u2654" : "\u265A";
            case "Queen" -> piece.getColor() == Color.WHITE ? "\u2655" : "\u265B";
            case "Rook" -> piece.getColor() == Color.WHITE ? "\u2656" : "\u265C";
            case "Bishop" -> piece.getColor() == Color.WHITE ? "\u2657" : "\u265D";
            case "Knight" -> piece.getColor() == Color.WHITE ? "\u2658" : "\u265E";
            case "Pawn" -> piece.getColor() == Color.WHITE ? "\u2659" : "\u265F";
            default -> "";
        };
    }

    public String squareLabel(int row, int column) {
        return String.valueOf((char) ('a' + column)) + (8 - row);
    }

    private void handleCommand(String command, String squareFrom, String squareTo, String piece) {
        gameMessage = "";

        if (gameController.checkTimeExpired()) {
            gameMessage = "Time expired.";
            return;
        }

        switch (command) {
            case "new" -> {
                resetGame();
                gameMessage = "New game started.";
            }
            case "move" -> handleMove(squareFrom, squareTo);
            case "offerdraw", "proposedraw", "draw" -> handleDrawProposal();
            case "acceptdraw" -> handleAcceptDraw();
            case "declinedraw" -> handleDeclineDraw();
            case "resign" -> {
                gameController.resign();
                drawProposedBy = null;
                gameMessage = "Player resigned.";
                saveScoreFromResult();
            }
            case "promote" -> {
                if (gameController.isPromotionPending()) {
                    gameController.promotePawn(piece == null ? "Q" : piece);
                    gameMessage = "Pawn promoted.";
                    saveScoreFromResult();
                } else {
                    gameMessage = "No pending promotion.";
                }
            }
            default -> gameMessage = "Unknown command.";
        }
    }

    private void handleMove(String squareFrom, String squareTo) {
        if (squareFrom == null || squareTo == null) {
            gameMessage = "Move requires 'squareFrom' and 'squareTo'.";
            return;
        }

        Square fromSquare = parseSquare(squareFrom);
        Square toSquare = parseSquare(squareTo);
        if (fromSquare == null || toSquare == null) {
            gameMessage = "Invalid square format. Use values like e2 and e4.";
            return;
        }

        Piece selectedPiece = fromSquare.getPiece();
        if (selectedPiece == null) {
            gameMessage = "No piece on source square.";
            return;
        }
        if (selectedPiece.getColor() != gameController.getCurrentPlayer().getColor()) {
            gameMessage = "You can move only the current player's piece.";
            return;
        }

        if (!gameController.movePiece(selectedPiece, toSquare)) {
            gameMessage = "Illegal move.";
            return;
        }

        drawProposedBy = null;
        gameMessage = gameController.isPromotionPending()
                ? "Move played. Choose promotion piece (Q/R/B/N)."
                : "Move played.";
        saveScoreFromResult();
    }

    private void handleDrawProposal() {
        if (drawProposedBy == null) {
            drawProposedBy = gameController.getCurrentPlayer().getColor();
            gameController.passTurn();
            gameMessage = "Draw proposed.";
            return;
        }

        if (drawProposedBy != gameController.getCurrentPlayer().getColor()) {
            gameController.drawByAgreement();
            drawProposedBy = null;
            gameMessage = "Draw accepted.";
            saveScoreFromResult();
            return;
        }

        gameMessage = "A draw is already proposed by the current player.";
    }

    private void handleAcceptDraw() {
        if (drawProposedBy == null) {
            gameMessage = "No active draw proposal.";
            return;
        }

        if (drawProposedBy == gameController.getCurrentPlayer().getColor()) {
            gameMessage = "The opponent must accept your draw proposal.";
            return;
        }

        gameController.drawByAgreement();
        drawProposedBy = null;
        gameMessage = "Draw accepted.";
        saveScoreFromResult();
    }

    private void handleDeclineDraw() {
        if (drawProposedBy == null) {
            gameMessage = "No active draw proposal.";
            return;
        }

        if (drawProposedBy == gameController.getCurrentPlayer().getColor()) {
            gameMessage = "The opponent must decline your draw proposal.";
            return;
        }

        drawProposedBy = null;
        gameController.passTurn();
        gameMessage = "Draw declined.";
    }

    private void resetGame() {
        gameController = new GameController();
        drawProposedBy = null;
        gameMessage = "";
    }

    private void saveScoreFromResult() {
        if (gameController.getGameState() == GameState.WHITE_WON || gameController.getGameState() == GameState.BLACK_WON) {
            scoreService.addScore(new Score(GAME, playerName, 1, new Date()));
        }
    }

    public List<Score> getTopScores() {
        return scoreService.getTopScores(GAME);
    }

    public int getAverageRating() {
        return ratingService.getAverageRating(GAME);
    }

    private Square parseSquare(String notation) {
        if (notation == null) {
            return null;
        }
        String value = notation.trim().toLowerCase();
        if (value.length() != 2) {
            return null;
        }

        char file = value.charAt(0);
        char rank = value.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            return null;
        }

        int column = file - 'a';
        int row = 8 - (rank - '0');
        return gameController.getBoard().getSquare(row, column);
    }
}
