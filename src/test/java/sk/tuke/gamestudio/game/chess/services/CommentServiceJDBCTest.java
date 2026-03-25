package sk.tuke.gamestudio.game.chess.services;

import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.service.CommentService;
import sk.tuke.gamestudio.service.CommentServiceJDBC;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommentServiceJDBCTest extends JdbcServiceTestBase {

    private final CommentService commentService = new CommentServiceJDBC();

    @Test
    void addCommentAndGetComments() {
        Comment c = new Comment("chess", "player1", "Nice game", new Date(0));
        commentService.addComment(c);

        List<Comment> comments = commentService.getComments("chess");
        assertEquals(1, comments.size());
        Comment loaded = comments.get(0);
        assertEquals("chess", loaded.getGame());
        assertEquals("player1", loaded.getPlayer());
        assertEquals("Nice game", loaded.getComment());
    }

    @Test
    void getCommentsFiltersByGame() {
        commentService.addComment(new Comment("chess", "p1", "c1", new Date(0)));
        commentService.addComment(new Comment("other", "p2", "c2", new Date(0)));

        List<Comment> chessComments = commentService.getComments("chess");
        assertEquals(1, chessComments.size());
        assertEquals("chess", chessComments.get(0).getGame());
    }

    @Test
    void resetRemovesAllComments() {
        commentService.addComment(new Comment("chess", "p1", "c1", new Date(0)));
        commentService.reset();
        List<Comment> comments = commentService.getComments("chess");
        assertTrue(comments.isEmpty());
    }
}

