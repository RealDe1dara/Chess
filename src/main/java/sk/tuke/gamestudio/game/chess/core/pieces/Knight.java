package sk.tuke.gamestudio.game.chess.core.pieces;

import sk.tuke.gamestudio.game.chess.core.Board;
import sk.tuke.gamestudio.game.chess.core.Color;
import sk.tuke.gamestudio.game.chess.core.Move;
import sk.tuke.gamestudio.game.chess.core.Square;

import java.util.List;

public class Knight extends Piece {

    public Knight(Color color, Square square) {
        super(color, square);
    }

    @Override
    public List<Move> getValidMoves(Board board) {
        int[][] directions = {
                {2, 1}, {1, 2}, {-1, 2}, {-2, 1},
                {-2, -1}, {-1, -2}, {1, -2}, {2, -1}
        };
        return getSlidingMoves(board, directions, 1);
    }
}
