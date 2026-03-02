package sk.tuke.gamestudio.game.chess.core;

public class Board {

    private final int rows = 8;
    private final int columns = 8;
    private final Square[][] board;
    private Move lastMove;

    public Board() {
        this.board = new Square[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                board[i][j] = new Square(i, j);
            }
        }
    }

    public Square getSquare(int row, int column) {
        if (row < 0 || row >= 8 || column < 0 || column >= 8) {
            return null;
        }
        return board[row][column];
    }

    public Move getLastMove() {
        return lastMove;
    }

    public void setLastMove(Move lastMove) {
        this.lastMove = lastMove;
    }
}
