package sk.tuke.gamestudio.service;

public class GameUserException extends RuntimeException {
    public GameUserException(String message) {
        super(message);
    }

    public GameUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
