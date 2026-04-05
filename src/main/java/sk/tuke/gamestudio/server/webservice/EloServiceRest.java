package sk.tuke.gamestudio.server.webservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sk.tuke.gamestudio.entity.Elo;
import sk.tuke.gamestudio.service.EloService;

import java.util.List;

@RestController
@RequestMapping("/api/elo")
public class EloServiceRest {

    @Autowired
    private EloService eloService;

    @PostMapping
    public void setElo(@RequestBody Elo elo) {
        eloService.setElo(elo);
    }

    @GetMapping("/{game}/{player}")
    public int getElo(@PathVariable String game, @PathVariable String player) {
        return eloService.getElo(game, player);
    }

    @GetMapping("/top/{game}")
    public List<Elo> getTopElo(@PathVariable String game) {
        return eloService.getTopElo(game);
    }
}

