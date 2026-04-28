package sk.tuke.gamestudio.server.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.service.ScoreService;

import java.util.List;

@RestController
@RequestMapping("/api/score")
public class ScoreServiceRest {

    @Autowired
    private ScoreService scoreService;

    @GetMapping("/{game}")
    public List<Score> getTopScores(@PathVariable String game) {
        return scoreService.getTopScores(game);
    }

    @GetMapping("/recent/{game}")
    public List<Score> getRecentScores(@PathVariable String game, @RequestParam(defaultValue = "20") int limit) {
        return scoreService.getRecentScores(game, limit);
    }

    @GetMapping("/player/{game}/{player}")
    public List<Score> getScoresByPlayer(@PathVariable String game,
                                         @PathVariable String player,
                                         @RequestParam(defaultValue = "20") int limit) {
        return scoreService.getScoresByPlayer(game, player, limit);
    }

    @PostMapping
    public void addScore(@RequestBody Score score) {
        scoreService.addScore(score);
    }
}
