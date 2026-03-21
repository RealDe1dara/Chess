package sk.tuke.gamestudio.entity;

public class Elo {
    private String game;
    private String player;
    private int elo;

    public Elo(String game, String player, int elo) {
        this.game = game;
        this.player = player;
        this.elo = elo;
    }

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