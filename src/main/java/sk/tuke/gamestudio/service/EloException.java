package sk.tuke.gamestudio.service;

public class EloException extends RuntimeException {
    public EloException(String message) {
        super(message);
    }

    public EloException(String message, Throwable cause) {
        super(message, cause);
    }
}