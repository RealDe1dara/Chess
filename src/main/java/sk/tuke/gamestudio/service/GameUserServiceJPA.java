package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.GameUser;

@Service
@Transactional
public class GameUserServiceJPA implements GameUserService {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public GameUser createUser(GameUser user) throws GameUserException {
        try {
            entityManager.persist(user);
            return user;
        } catch (Exception e) {
            throw new GameUserException("Error creating user", e);
        }
    }

    @Override
    public GameUser getUserById(Long id) throws GameUserException {
        try {
            return entityManager.find(GameUser.class, id);
        } catch (Exception e) {
            throw new GameUserException("Error loading user by id", e);
        }
    }

    @Override
    public GameUser getUserByUsername(String username) throws GameUserException {
        try {
            return entityManager.createQuery(
                            "SELECT u FROM GameUser u WHERE LOWER(u.username)=LOWER(:username)",
                            GameUser.class
                    )
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new GameUserException("Error loading user by username", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) throws GameUserException {
        try {
            Long count = entityManager.createQuery(
                            "SELECT COUNT(u) FROM GameUser u WHERE LOWER(u.username)=LOWER(:username)",
                            Long.class
                    )
                    .setParameter("username", username)
                    .getSingleResult();
            return count != null && count > 0;
        } catch (Exception e) {
            throw new GameUserException("Error checking username availability", e);
        }
    }

    @Override
    public GameUser updateUser(GameUser user) throws GameUserException {
        try {
            return entityManager.merge(user);
        } catch (Exception e) {
            throw new GameUserException("Error updating user", e);
        }
    }
}
