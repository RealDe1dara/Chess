package sk.tuke.gamestudio.game.chess.ai;

import sk.tuke.gamestudio.entity.GameSession;
import sk.tuke.gamestudio.game.chess.core.GameController;
import sk.tuke.gamestudio.game.chess.core.Square;
import sk.tuke.gamestudio.game.chess.core.pieces.Piece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class ChessAiPlayer {

    public void makeMove(GameSession entity, GameController gc) {
        boolean moved = false;

        try {
            String fen = gc.toFen();
            int depth = switch (entity.getComputerDifficulty() == null ? "MID" : entity.getComputerDifficulty()) {
                case "EASY"   -> 2;
                case "HARD"   -> 10;
                case "EXPERT" -> 15;
                default       -> 5;
            };
            String uciMove = StockfishEngine.bestMove(fen, depth);
            if (uciMove != null) {
                Square from = parseSquare(uciMove.substring(0, 2), gc);
                Square to   = parseSquare(uciMove.substring(2, 4), gc);
                if (from != null && to != null) {
                    Piece piece = from.getPiece();
                    if (piece != null && gc.movePiece(piece, to)) {
                        if (gc.isPromotionPending()) {
                            String promoteTo = uciMove.length() >= 5 ? String.valueOf(uciMove.charAt(4)) : "q";
                            gc.promotePawn(promoteTo);
                        }
                        moved = true;
                    }
                }
            }
        } catch (Exception ignored) {
            // Stockfish not installed or failed — fall through to random
        }

        if (!moved) makeRandomMove(gc);
    }

    private void makeRandomMove(GameController gc) {
        List<Piece> pieces = new ArrayList<>(gc.getCurrentPlayer().getPieces());
        Collections.shuffle(pieces);
        for (Piece piece : pieces) {
            if (piece.getSquare() == null) continue;
            List<Square> targets = gc.getValidTargets(piece);
            if (targets.isEmpty()) continue;
            Square target = targets.get(new Random().nextInt(targets.size()));
            if (gc.movePiece(piece, target)) {
                if (gc.isPromotionPending()) gc.promotePawn("Q");
                return;
            }
        }
    }

    private Square parseSquare(String sq, GameController gc) {
        if (sq == null || sq.length() != 2) return null;
        char file = sq.charAt(0);
        char rank = sq.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') return null;
        return gc.getBoard().getSquare(8 - (rank - '0'), file - 'a');
    }
}
