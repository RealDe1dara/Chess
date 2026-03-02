package sk.tuke.gamestudio.game.chess.core;

import sk.tuke.gamestudio.game.chess.core.pieces.Piece;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class Move {

    private final Piece movedPiece;
    private final Piece capturedPiece;
    private final Square oldSquare;
    private final Square newSquare;
    private final Set<MoveType> moveTypes;

    public Move(Piece movedPiece, Piece capturedPiece, Square oldSquare, Square newSquare, MoveType... moveTypes) {
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
        this.oldSquare = oldSquare;
        this.newSquare = newSquare;
        this.moveTypes = EnumSet.noneOf(MoveType.class);
        Collections.addAll(this.moveTypes, moveTypes);
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public Piece getMovedPiece() {
        return movedPiece;
    }

    public Square getOldSquare() {
        return oldSquare;
    }

    public Square getNewSquare() {
        return newSquare;
    }

    public Set<MoveType> getMoveTypes() {
        return moveTypes;
    }

    public boolean hasType(MoveType type) {
        return moveTypes.contains(type);
    }
}
