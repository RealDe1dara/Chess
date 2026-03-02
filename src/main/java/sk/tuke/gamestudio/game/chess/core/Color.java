package sk.tuke.gamestudio.game.chess.core;

public enum Color {
    WHITE,
    BLACK;

    public int getForwardDir() {
        return this == WHITE ? -1 : 1;
    }
}
