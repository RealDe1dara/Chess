package sk.tuke.gamestudio.game.chess.core.pieces;

import sk.tuke.gamestudio.game.chess.core.Board;
import sk.tuke.gamestudio.game.chess.core.Color;
import sk.tuke.gamestudio.game.chess.core.Move;
import sk.tuke.gamestudio.game.chess.core.MoveType;
import sk.tuke.gamestudio.game.chess.core.Square;

import java.util.ArrayList;
import java.util.List;

public abstract class Piece {

    private final Color color;
    private Square square;

    public Piece(Color color, Square square) {
        this.square = square;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public Square getSquare() {
        return square;
    }

    public void setSquare(Square square) {
        this.square = square;
    }

    public abstract List<Move> getValidMoves(Board board);

    public void onMove() {
    }

    protected List<Move> getSlidingMoves(Board board, int[][] directions, int maxDistance) {
        List<Move> moves = new ArrayList<>();
        int row = getSquare().getRow();
        int column = getSquare().getColumn();

        for (int[] dir : directions) {
            for (int i = 1; i <= maxDistance; i++) {
                Square target = board.getSquare(row + i * dir[0], column + i * dir[1]);

                if (target == null) {
                    break;
                }

                if (target.getPiece() == null) {
                    moves.add(new Move(this, null, this.getSquare(), target, MoveType.NORMAL));
                } else {
                    if (target.getPiece().getColor() != this.getColor()) {
                        moves.add(new Move(this, target.getPiece(), this.getSquare(), target, MoveType.CAPTURE));
                    }
                    break;
                }
            }
        }
        return moves;
    }
}
