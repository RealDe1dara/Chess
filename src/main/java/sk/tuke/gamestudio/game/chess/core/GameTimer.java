package sk.tuke.gamestudio.game.chess.core;

public class GameTimer {

    private long whiteRemainingMs;
    private long blackRemainingMs;
    private long lastMoveTime;

    public GameTimer(int timeMinutes) {
        this.whiteRemainingMs = (long) timeMinutes * 60 * 1000;
        this.blackRemainingMs = (long) timeMinutes * 60 * 1000;
        this.lastMoveTime = System.currentTimeMillis();
    }

    public void recordMove(Color playerColor) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastMoveTime;

        if (playerColor == Color.WHITE) {
            whiteRemainingMs -= elapsedTime;
        } else {
            blackRemainingMs -= elapsedTime;
        }

        lastMoveTime = currentTime;
    }

    public long getRemainingTimeMs(Color color) {
        if (color == Color.WHITE) {
            return Math.max(0, whiteRemainingMs);
        } else {
            return Math.max(0, blackRemainingMs);
        }
    }

    public boolean hasTimeRemaining(Color color) {
        return getRemainingTimeMs(color) > 0;
    }

    public long getRemainingTimeMs(Color color, Color activeColor) {
        long base = (color == Color.WHITE) ? whiteRemainingMs : blackRemainingMs;

        if (color == activeColor) {
            long now = System.currentTimeMillis();
            long elapsedSinceLastMove = now - lastMoveTime;
            base -= elapsedSinceLastMove;
        }

        return Math.max(0, base);
    }

    public boolean hasTimeRemaining(Color color, Color activeColor) {
        return getRemainingTimeMs(color, activeColor) > 0;
    }
}
