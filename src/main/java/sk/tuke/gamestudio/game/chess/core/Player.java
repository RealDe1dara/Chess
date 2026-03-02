package sk.tuke.gamestudio.game.chess.core;

import sk.tuke.gamestudio.game.chess.core.pieces.King;
import sk.tuke.gamestudio.game.chess.core.pieces.Piece;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final Color color;
    private final List<Piece> pieces = new ArrayList<>();

    public Player(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public void addPiece(Piece piece) {
        pieces.add(piece);
    }

    public void removePiece(Piece piece) {
        pieces.remove(piece);
    }

    public King getKing() {
        for (Piece piece : pieces) {
            if (piece instanceof King king) {
                return king;
            }
        }
        return null;
    }
}
