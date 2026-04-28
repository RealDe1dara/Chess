package sk.tuke.gamestudio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.entity.Score;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ScoreServiceRestClient implements ScoreService {
    private final String url = "http://localhost:8080/api/score";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void addScore(Score score) {
        restTemplate.postForEntity(url, score, Score.class);
    }

    @Override
    public List<Score> getTopScores(String gameName) {
        return Arrays.asList(Objects.requireNonNull(restTemplate.getForEntity(url + "/" + gameName, Score[].class).getBody()));
    }

    @Override
    public List<Score> getRecentScores(String gameName, int limit) {
        return Arrays.asList(Objects.requireNonNull(
                restTemplate.getForEntity(url + "/recent/" + gameName + "?limit=" + Math.max(1, limit), Score[].class).getBody()));
    }

    @Override
    public List<Score> getScoresByPlayer(String gameName, String player, int limit) {
        return Arrays.asList(Objects.requireNonNull(
                restTemplate.getForEntity(url + "/player/" + gameName + "/" + player + "?limit=" + Math.max(1, limit), Score[].class).getBody()));
    }

    @Override
    public void reset() {
//        throw new UnsupportedOperationException("Not supported via web service");
        throw new ScoreException("Reset not supported via REST client");

    }
}
