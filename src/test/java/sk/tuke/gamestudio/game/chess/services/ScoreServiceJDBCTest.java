package sk.tuke.gamestudio.game.chess.services;

import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.ScoreService;
import sk.tuke.gamestudio.service.ScoreServiceJDBC;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScoreServiceJDBCTest extends JdbcServiceTestBase {

    private final ScoreService scoreService = new ScoreServiceJDBC();

    @Test
    void addScoreAndGetTopScores() {
        Score score = new Score("chess", "player1", 10, new Date(0));
        scoreService.addScore(score);

        List<Score> scores = scoreService.getTopScores("chess");
        assertEquals(1, scores.size());
        Score s = scores.get(0);
        assertEquals("chess", s.getGame());
        assertEquals("player1", s.getPlayer());
        assertEquals(10, s.getPoints());
    }

    @Test
    void getTopScoresReturnsTop10SortedByPointsDesc() {
        for (int i = 1; i <= 15; i++) {
            scoreService.addScore(new Score("chess", "p" + i, i, new Date(0)));
        }
        List<Score> scores = scoreService.getTopScores("chess");
        assertEquals(10, scores.size());
        assertTrue(scores.get(0).getPoints() >= scores.get(1).getPoints());
        assertTrue(scores.get(0).getPoints() > scores.get(9).getPoints());
    }

    @Test
    void resetRemovesAllScores() {
        scoreService.addScore(new Score("chess", "player1", 10, new Date(0)));
        scoreService.reset();
        List<Score> scores = scoreService.getTopScores("chess");
        assertTrue(scores.isEmpty());
    }
}

