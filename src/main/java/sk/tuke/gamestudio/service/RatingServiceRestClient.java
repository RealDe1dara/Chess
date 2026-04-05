package sk.tuke.gamestudio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.entity.Rating;

import java.util.Objects;

@Service
public class RatingServiceRestClient implements RatingService {
    private final String url = "http://localhost:8080/api/rating";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void setRating(Rating rating) throws RatingException {
        try {
            restTemplate.postForEntity(url, rating, Void.class);
        } catch (Exception e) {
            throw new RatingException("Error setting rating via REST", e);
        }
    }

    @Override
    public int getAverageRating(String game) throws RatingException {
        try {
            Integer result = restTemplate.getForObject(url + "/average/" + game, Integer.class);
            return result != null ? result : 0;
        } catch (Exception e) {
            throw new RatingException("Error getting average rating via REST", e);
        }
    }

    @Override
    public int getRating(String game, String player) throws RatingException {
        try {
            Integer result = restTemplate.getForObject(url + "/" + game + "/" + player, Integer.class);
            return result != null ? result : 0;
        } catch (Exception e) {
            throw new RatingException("Error getting rating via REST", e);
        }
    }

    @Override
    public void reset() throws RatingException {
        throw new RatingException("Reset not supported via REST client");
    }
}

