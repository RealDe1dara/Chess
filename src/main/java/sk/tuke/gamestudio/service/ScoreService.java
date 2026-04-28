package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Score;

import java.util.List;

public interface ScoreService {
    void addScore(Score score) throws ScoreException;
    List<Score> getTopScores(String game) throws ScoreException;
    List<Score> getRecentScores(String game, int limit) throws ScoreException;
    List<Score> getScoresByPlayer(String game, String player, int limit) throws ScoreException;
    void reset() throws ScoreException;
}
