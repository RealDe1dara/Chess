package sk.tuke.gamestudio.game.chess.services;

import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.entity.Elo;
import sk.tuke.gamestudio.service.EloService;
import sk.tuke.gamestudio.service.EloServiceJDBC;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EloServiceJDBCTest extends JdbcServiceTestBase {

    private final EloService eloService = new EloServiceJDBC();

    @Test
    void setEloInsertsAndUpdatesRow() {
        Elo e1 = new Elo("chess", "player1", 1200);
        eloService.setElo(e1);
        assertEquals(1200, eloService.getElo("chess", "player1"));

        Elo e2 = new Elo("chess", "player1", 1300);
        eloService.setElo(e2);
        assertEquals(1300, eloService.getElo("chess", "player1"));
    }

    @Test
    void getEloReturnsDefaultWhenNoRow() {
        assertEquals(100, eloService.getElo("chess", "unknown"));
    }

    @Test
    void getTopEloReturnsTop10Sorted() {
        for (int i = 1; i <= 15; i++) {
            eloService.setElo(new Elo("chess", "p" + i, 1000 + i));
        }
        List<Elo> top = eloService.getTopElo("chess");
        assertEquals(10, top.size());
        assertTrue(top.get(0).getElo() >= top.get(1).getElo());
        assertTrue(top.get(0).getElo() > top.get(9).getElo());
    }

    @Test
    void resetRemovesAllElo() {
        eloService.setElo(new Elo("chess", "p1", 1200));
        eloService.reset();
        assertTrue(eloService.getTopElo("chess").isEmpty());
        assertEquals(100, eloService.getElo("chess", "p1"));
    }
}

