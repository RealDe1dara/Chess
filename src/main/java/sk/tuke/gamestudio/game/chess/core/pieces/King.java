package sk.tuke.gamestudio.game.chess.core.pieces;

import sk.tuke.gamestudio.game.chess.core.Board;
import sk.tuke.gamestudio.game.chess.core.Color;
import sk.tuke.gamestudio.game.chess.core.Move;
import sk.tuke.gamestudio.game.chess.core.MoveType;
import sk.tuke.gamestudio.game.chess.core.Square;

import java.util.List;

public class King extends Piece {

    private boolean isFirstMove = true;

    public King(Color color, Square square) {
        super(color, square);
    }

    @Override
    public List<Move> getValidMoves(Board board) {
        int[][] directions = {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}
        };
        List<Move> validMoves = getSlidingMoves(board, directions, 1);

        int row = getSquare().getRow();
        int column = getSquare().getColumn();

        if (this.isFirstMove) {
            Square rookSquare = board.getSquare(row, 7);
            if (rookSquare != null && rookSquare.getPiece() instanceof Rook rook
                    && rook.getIsFirstMove()) {
                Square fSquare = board.getSquare(row, column + 1);
                Square gSquare = board.getSquare(row, column + 2);
                if (fSquare.getPiece() == null && gSquare.getPiece() == null) {
                    validMoves.add(new Move(this, null, this.getSquare(), gSquare, MoveType.CASTLING));
                }
            }

            rookSquare = board.getSquare(row, 0);
            if (rookSquare != null && rookSquare.getPiece() instanceof Rook rook
                    && rook.getIsFirstMove()) {
                Square dSquare = board.getSquare(row, column - 1);
                Square cSquare = board.getSquare(row, column - 2);
                Square bSquare = board.getSquare(row, column - 3);
                if (dSquare.getPiece() == null
                        && cSquare.getPiece() == null
                        && bSquare.getPiece() == null) {
                    validMoves.add(new Move(this, null, this.getSquare(), cSquare, MoveType.CASTLING));
                }
            }
        }

        return validMoves;
    }

    public boolean getIsFirstMove() {
        return isFirstMove;
    }

    @Override
    public void onMove() {
        this.isFirstMove = false;
    }
}
