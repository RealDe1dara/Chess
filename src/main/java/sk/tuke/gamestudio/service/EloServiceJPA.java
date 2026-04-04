package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.Elo;

import java.util.List;

@Service
@Transactional
public class EloServiceJPA implements EloService {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void setElo(Elo elo) throws EloException {
        try {
            entityManager.merge(elo);
        } catch (Exception e) {
            throw new EloException("Error setting Elo rating", e);
        }
    }

    @Override
    public int getElo(String game, String player) throws EloException {
        try {
            List<Elo> results = entityManager.createNamedQuery("Elo.getPlayerElo", Elo.class)
                    .setParameter("game", game)
                    .setParameter("player", player)
                    .getResultList();
            
            if (!results.isEmpty()) {
                return results.get(0).getElo();
            }
            return 0;
        } catch (Exception e) {
            throw new EloException("Error retrieving Elo rating", e);
        }
    }

    @Override
    public void reset() throws EloException {
        try {
            entityManager.createNamedQuery("Elo.resetElo").executeUpdate();
        } catch (Exception e) {
            throw new EloException("Error resetting Elo ratings", e);
        }
    }

    @Override
    public List<Elo> getTopElo(String game) throws EloException {
        try {
            return entityManager.createNamedQuery("Elo.getTopPlayersByGame", Elo.class)
                    .setParameter("game", game)
                    .getResultList();
        } catch (Exception e) {
            throw new EloException("Error retrieving top Elo ratings", e);
        }
    }
}
