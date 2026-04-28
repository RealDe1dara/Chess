package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.Rating;

import java.util.List;

@Service
@Transactional
public class RatingServiceJPA implements RatingService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void setRating(Rating rating) throws RatingException {
        try {
            List<Rating> existingRatings = entityManager
                    .createNamedQuery("Rating.getRating", Rating.class)
                    .setParameter("game", rating.getGame())
                    .setParameter("player", rating.getPlayer())
                    .getResultList();

            if (!existingRatings.isEmpty()) {
                Rating existing = existingRatings.get(0);
                existing.setRating(rating.getRating());
                existing.setRatedOn(rating.getRatedOn());
                entityManager.merge(existing);
            } else {
                entityManager.persist(rating);
            }
        } catch (Exception e) {
            throw new RatingException("Error setting rating", e);
        }
    }

    @Override
    public double getAverageRating(String game) throws RatingException {
        try {
            Number result = (Number) entityManager.createNamedQuery("Rating.getAverageRatingByGame")
                    .setParameter("game", game)
                    .getSingleResult();
            return result != null ? result.doubleValue() : 0;
        } catch (Exception e) {
            throw new RatingException("Error retrieving average rating", e);
        }
    }

    @Override
    public int getRating(String game, String player) throws RatingException {
        try {
            Rating result = (Rating) entityManager.createNamedQuery("Rating.getRating", Rating.class)
                    .setParameter("game", game)
                    .setParameter("player", player)
                    .getSingleResult();

            return result.getRating();
        } catch (NoResultException e) {
            return 0;
        } catch (Exception e) {
            throw new RatingException("Error retrieving rating", e);
        }
    }

    @Override
    public void reset() throws RatingException {
        try {
            entityManager.createNamedQuery("Rating.resetRatings").executeUpdate();
        } catch (Exception e) {
            throw new RatingException("Error resetting ratings", e);
        }
    }
}
