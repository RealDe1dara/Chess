package sk.tuke.gamestudio.entity;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(
        name = "elo",
        uniqueConstraints = @UniqueConstraint(columnNames = {"game", "player"})
)
@NamedQuery(name = "Elo.getTopPlayersByGame",
        query = "SELECT e FROM Elo e WHERE e.game=:game ORDER BY e.elo DESC")
@NamedQuery(name = "Elo.getPlayerElo",
        query = "SELECT e FROM Elo e WHERE e.game=:game AND e.player=:player")
@NamedQuery(name = "Elo.resetElo",
        query = "DELETE FROM Elo")
public class Elo implements Serializable {
    @Id
    @GeneratedValue
    private int ident;

    private String game;
    private String player;
    private int elo = 100;

    public Elo() {}

    public Elo(String game, String player, int elo) {
        this.game = game;
        this.player = player;
        this.elo = elo;
    }

    public int getIdent() { return ident; }
    public void setIdent(int ident) { this.ident = ident; }

    public String getGame() { return game; }

    public void setGame(String game) { this.game = game; }

    public String getPlayer() { return player; }

    public void setPlayer(String player) { this.player = player; }

    public int getElo() { return elo; }

    public void setElo(int elo) { this.elo = elo; }

    @Override
    public String toString() {
        return "Elo{" +
                "game='" + game + '\'' +
                ", player='" + player + '\'' +
                ", elo=" + elo +
                '}';
    }
}