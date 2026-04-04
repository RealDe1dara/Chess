package sk.tuke.gamestudio.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import sk.tuke.gamestudio.entity.Comment;

import java.util.List;

@Service
@Transactional
public class CommentServiceJPA implements CommentService {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void addComment(Comment comment) throws CommentException {
        try {
            entityManager.persist(comment);
        } catch (Exception e) {
            throw new CommentException("Error adding comment", e);
        }
    }

    @Override
    public List<Comment> getComments(String game) throws CommentException {
        try {
            return entityManager.createNamedQuery("Comment.getCommentsByGame", Comment.class)
                    .setParameter("game", game)
                    .getResultList();
        } catch (Exception e) {
            throw new CommentException("Error retrieving comments", e);
        }
    }

    @Override
    public void reset() throws CommentException {
        try {
            entityManager.createNamedQuery("Comment.resetComments").executeUpdate();
        } catch (Exception e) {
            throw new CommentException("Error resetting comments", e);
        }
    }
}
