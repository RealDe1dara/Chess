package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import sk.tuke.gamestudio.entity.Elo;
import sk.tuke.gamestudio.entity.GameSession;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.chess.core.Color;
import sk.tuke.gamestudio.game.chess.core.GameController;
import sk.tuke.gamestudio.game.chess.core.GameState;

import java.util.Date;
import java.util.List;
import java.util.Objects;

public class GameSessionService {

    private static final String GAME = "chess";
    private static final int ELO_K_FACTOR = 32;

    @PersistenceContext
    private EntityManager em;

    private final ScoreService scoreService;
    private final EloService eloService;

    public GameSessionService(ScoreService scoreService, EloService eloService) {
        this.scoreService = scoreService;
        this.eloService = eloService;
    }

    public GameSession findById(Long id) {
        return em.find(GameSession.class, id);
    }

    public void persist(GameSession entity) {
        em.persist(entity);
        em.flush();
    }

    public String checkTimeout(GameSession entity, GameController gc) {
        if (entity.getTimeLimitSeconds() == null || entity.getTimeLimitSeconds() <= 0
                || entity.getLastMoveAt() == null) return null;

        Color movingColor = gc.getCurrentPlayer().getColor();
        long elapsed = System.currentTimeMillis() - entity.getLastMoveAt();

        if (movingColor == Color.WHITE) {
            long remaining = Math.max(0, entity.getTimeWhiteMs() - elapsed);
            entity.setTimeWhiteMs(remaining);
            if (remaining == 0) {
                gc.timeoutColor(Color.WHITE);
                finalizeIfEnded(entity, gc);
                return "Time's up — Black wins!";
            }
        } else {
            long remaining = Math.max(0, entity.getTimeBlackMs() - elapsed);
            entity.setTimeBlackMs(remaining);
            if (remaining == 0) {
                gc.timeoutColor(Color.BLACK);
                finalizeIfEnded(entity, gc);
                return "Time's up — White wins!";
            }
        }
        return null;
    }

    public void finalizeIfEnded(GameSession entity, GameController gc) {
        if (gc.getGameState() == GameState.ACTIVE || gc.isPromotionPending()) return;

        entity.setStatus("FINISHED");
        entity.setFinishedAt(new Date());

        String white = entity.getPlayerWhite();
        String black = entity.getPlayerBlack();
        Date now = new Date();
        boolean updateStats = "REMOTE".equals(entity.getMode()) && black != null && white != null;

        if (!updateStats) {
            switch (gc.getGameState()) {
                case WHITE_WON -> entity.setResult("WHITE_WON");
                case BLACK_WON -> entity.setResult("BLACK_WON");
                case DRAW      -> entity.setResult("DRAW");
            }
            return;
        }

        switch (gc.getGameState()) {
            case WHITE_WON -> { entity.setResult("WHITE_WON"); saveResult(white, black, 1, 0, 1.0, now); }
            case BLACK_WON -> { entity.setResult("BLACK_WON"); saveResult(black, white, 1, 0, 1.0, now); }
            case DRAW      -> { entity.setResult("DRAW");      saveResult(white, black, 0, 0, 0.5, now); }
        }
    }

    private void saveResult(String playerA, String playerB, int ptsA, int ptsB, double actualA, Date now) {
        if (playerA == null || playerB == null || playerA.equals(playerB)) return;

        scoreService.addScore(new Score(GAME, playerA, ptsA, now));
        scoreService.addScore(new Score(GAME, playerB, ptsB, now));

        int eloA = Objects.requireNonNullElse(eloService.getElo(GAME, playerA), 1000);
        int eloB = Objects.requireNonNullElse(eloService.getElo(GAME, playerB), 1000);

        double expectedA = 1.0 / (1 + Math.pow(10, (eloB - eloA) / 400.0));
        double actualB   = 1.0 - actualA;
        double expectedB = 1.0 - expectedA;

        int newA = Math.max(1, (int) Math.round(eloA + ELO_K_FACTOR * (actualA - expectedA)));
        int newB = Math.max(1, (int) Math.round(eloB + ELO_K_FACTOR * (actualB - expectedB)));

        eloService.setElo(new Elo(GAME, playerA, newA));
        eloService.setElo(new Elo(GAME, playerB, newB));
    }
}
