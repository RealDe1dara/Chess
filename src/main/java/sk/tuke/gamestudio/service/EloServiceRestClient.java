package sk.tuke.gamestudio.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sk.tuke.gamestudio.entity.Elo;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class EloServiceRestClient implements EloService {
    private final String url = "http://localhost:8080/api/elo";

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void setElo(Elo elo) throws EloException {
        try {
            restTemplate.postForEntity(url, elo, Void.class);
        } catch (Exception e) {
            throw new EloException("Error setting Elo via REST", e);
        }
    }

    @Override
    public int getElo(String game, String player) throws EloException {
        try {
            Integer result = restTemplate.getForObject(url + "/" + game + "/" + player, Integer.class);
            return result != null ? result : 100;
        } catch (Exception e) {
            return 100;
        }
    }

    @Override
    public void reset() throws EloException {
        throw new EloException("Reset not supported via REST client");
    }

    @Override
    public List<Elo> getTopElo(String game) throws EloException {
        try {
            Elo[] result = restTemplate.getForObject(url + "/top/" + game, Elo[].class);
            return Arrays.asList(Objects.requireNonNull(result));
        } catch (Exception e) {
            throw new EloException("Error getting top Elo via REST", e);
        }
    }
}
