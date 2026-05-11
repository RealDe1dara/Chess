package sk.tuke.gamestudio.entity;

import jakarta.persistence.*;
import java.util.Date;

/**
 * Stores metadata for one game session in the database.
 * The actual live board (GameController) lives in memory inside ChessController.
 * This entity persists who played, when, and what the outcome was — enough to
 * update scores/ELO when a game finishes and to show open games in the lobby.
 */
@Entity
@Table(name = "game_session")
public class GameSession {

    @Id
    @GeneratedValue
    private Long id;

    private String playerWhite;

    /** null while WAITING for a remote opponent to join */
    private String playerBlack;

    /** "LOCAL" (same device, pass-and-play) or "REMOTE" (two separate browsers/PCs) */
    private String mode;

    /** "WAITING" → "ACTIVE" → "FINISHED" */
    private String status;

    /** "EASY", "MID", "HARD", "EXPERT" — only set when mode = "COMPUTER" */
    private String computerDifficulty;

    /** null while active; "WHITE_WON" / "BLACK_WON" / "DRAW" when finished */
    private String result;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date finishedAt;

    // ── Time control (0 / null = no limit) ───────────────────────────────────────

    /** Seconds per player.  0 or null means unlimited. */
    private Integer timeLimitSeconds;

    /** White's remaining time in milliseconds.  null when there is no limit. */
    private Long timeWhiteMs;

    /** Black's remaining time in milliseconds.  null when there is no limit. */
    private Long timeBlackMs;

    /**
     * Epoch-millisecond timestamp of when the current player's clock started.
     * Set when the game goes ACTIVE (LOCAL: on create; REMOTE: on join).
     * Updated after every move to mark when the next player's clock started.
     * null when there is no time limit.
     */
    private Long lastMoveAt;

    public GameSession() {}

    public Long getId() { return id; }

    public String getPlayerWhite() { return playerWhite; }
    public void setPlayerWhite(String playerWhite) { this.playerWhite = playerWhite; }

    public String getPlayerBlack() { return playerBlack; }
    public void setPlayerBlack(String playerBlack) { this.playerBlack = playerBlack; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getComputerDifficulty() { return computerDifficulty; }
    public void setComputerDifficulty(String computerDifficulty) { this.computerDifficulty = computerDifficulty; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Date finishedAt) { this.finishedAt = finishedAt; }

    public Integer getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(Integer timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }

    public Long getTimeWhiteMs() { return timeWhiteMs; }
    public void setTimeWhiteMs(Long timeWhiteMs) { this.timeWhiteMs = timeWhiteMs; }

    public Long getTimeBlackMs() { return timeBlackMs; }
    public void setTimeBlackMs(Long timeBlackMs) { this.timeBlackMs = timeBlackMs; }

    public Long getLastMoveAt() { return lastMoveAt; }
    public void setLastMoveAt(Long lastMoveAt) { this.lastMoveAt = lastMoveAt; }
}
