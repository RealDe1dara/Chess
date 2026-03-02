package sk.tuke.gamestudio.game.chess.core;

import sk.tuke.gamestudio.game.chess.core.pieces.Piece;

public class Square {

    private final int row;
    private final int column;
    private final Color color;
    private Piece piece;

    public Square(int row, int column) {
        this.row = row;
        this.column = column;
        this.piece = null;
        if ((row + column) % 2 == 0) {
            this.color = Color.WHITE;
        } else {
            this.color = Color.BLACK;
        }
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public Piece getPiece() {
        return piece;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public Color getColor() {
        return color;
    }
}
