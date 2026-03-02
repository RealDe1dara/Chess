package sk.tuke.gamestudio.game.chess.core.pieces;

import sk.tuke.gamestudio.game.chess.core.Board;
import sk.tuke.gamestudio.game.chess.core.Color;
import sk.tuke.gamestudio.game.chess.core.Move;
import sk.tuke.gamestudio.game.chess.core.Square;

import java.util.List;

public class Bishop extends Piece {

    public Bishop(Color color, Square square) {
        super(color, square);
    }

    @Override
    public List<Move> getValidMoves(Board board) {
        int[][] directions = {
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };
        return getSlidingMoves(board, directions, 8);
    }
}
