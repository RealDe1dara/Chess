package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Primary;
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
            List<Elo> existing = entityManager
                    .createNamedQuery("Elo.getPlayerElo", Elo.class)
                    .setParameter("game", elo.getGame())
                    .setParameter("player", elo.getPlayer())
                    .getResultList();

            // If this is a new player and elo is 0, treat it as default 100
            if (existing.isEmpty() && elo.getElo() == 0) {
                elo.setElo(100);
            }

            if (!existing.isEmpty()) {
                Elo current = existing.get(0);
                current.setElo(elo.getElo());
                entityManager.merge(current);
            } else {
                entityManager.persist(elo);
            }
        } catch (Exception e) {
            throw new EloException("Error setting ELO", e);
        }
    }

    @Override
    public int getElo(String game, String player) throws EloException {
        try {
            List<Elo> results = entityManager
                    .createNamedQuery("Elo.getPlayerElo", Elo.class)
                    .setParameter("game", game)
                    .setParameter("player", player)
                    .getResultList();

            if (!results.isEmpty()) {
                return results.get(0).getElo();
            }
            // mirror JDBC behavior: new player starts with 100
            return 100;
        } catch (Exception e) {
            throw new EloException("Problem getting ELO", e);
        }
    }

    @Override
    public void reset() throws EloException {
        try {
            entityManager.createNamedQuery("Elo.resetElo").executeUpdate();
        } catch (Exception e) {
            throw new EloException("Problem deleting ELO", e);
        }
    }

    @Override
    public List<Elo> getTopElo(String game) throws EloException {
        try {
            // same as JDBC: order by elo desc and limit 10
            return entityManager.createNamedQuery("Elo.getTopPlayersByGame", Elo.class)
                    .setParameter("game", game)
                    .setMaxResults(10)
                    .getResultList();
        } catch (Exception e) {
            throw new EloException("Problem selecting top ELO", e);
        }
    }
}
