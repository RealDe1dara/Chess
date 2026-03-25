package sk.tuke.gamestudio.game.chess.services;

import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.service.RatingService;
import sk.tuke.gamestudio.service.RatingServiceJDBC;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class RatingServiceJDBCTest extends JdbcServiceTestBase {

    private final RatingService ratingService = new RatingServiceJDBC();

    @Test
    void setRatingAndGetRating() {
        Rating r = new Rating("chess", "player1", 3, new Date(0));
        ratingService.setRating(r);
        assertEquals(3, ratingService.getRating("chess", "player1"));
    }

    @Test
    void setRatingUpdatesExistingRow() {
        Rating r1 = new Rating("chess", "player1", 3, new Date(0));
        ratingService.setRating(r1);
        Rating r2 = new Rating("chess", "player1", 5, new Date(0));
        ratingService.setRating(r2);
        assertEquals(5, ratingService.getRating("chess", "player1"));
    }

    @Test
    void getAverageRatingReturnsAverageForGame() {
        ratingService.setRating(new Rating("chess", "p1", 3, new Date(0)));
        ratingService.setRating(new Rating("chess", "p2", 5, new Date(0)));
        ratingService.setRating(new Rating("other", "p3", 1, new Date(0)));

        int avg = ratingService.getAverageRating("chess");
        assertEquals(4, avg);
    }

    @Test
    void getRatingReturnsZeroWhenNotRated() {
        assertEquals(0, ratingService.getRating("chess", "unknown"));
    }

    @Test
    void resetRemovesAllRatings() {
        ratingService.setRating(new Rating("chess", "p1", 3, new Date(0)));
        ratingService.reset();
        assertEquals(0, ratingService.getRating("chess", "p1"));
        assertEquals(0, ratingService.getAverageRating("chess"));
    }
}

