package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.Score;

import java.util.List;

@Service
@Transactional
public class ScoreServiceJPA implements ScoreService {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addScore(Score score) throws ScoreException {
        try {
            entityManager.persist(score);
        } catch (Exception e) {
            throw new ScoreException("Error adding score", e);
        }
    }

    @Override
    public List<Score> getTopScores(String game) throws ScoreException {
        try {
            return entityManager.createNamedQuery("Score.getTopScores", Score.class)
                    .setParameter("game", game)
                    .setMaxResults(10)
                    .getResultList();
        } catch (Exception e) {
            throw new ScoreException("Error retrieving top scores", e);
        }
    }

    @Override
    public void reset() throws ScoreException {
        try {
            entityManager.createNamedQuery("Score.resetScores").executeUpdate();
        } catch (Exception e) {
            throw new ScoreException("Error resetting scores", e);
        }
    }
}
