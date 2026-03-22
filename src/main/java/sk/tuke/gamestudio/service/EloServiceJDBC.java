package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Elo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EloServiceJDBC implements EloService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "postgres";

    public static final String SELECT_BY_PLAYER = "SELECT elo FROM elo WHERE game = ? AND player = ?";
    public static final String SELECT_TOP10 = "SELECT game, player, elo FROM elo WHERE game = ? ORDER BY elo DESC LIMIT 10";
    public static final String DELETE = "DELETE FROM elo";
    public static final String UPDATE =
            "INSERT INTO elo (game, player, elo) VALUES (?, ?, ?) " +
                    "ON CONFLICT (game, player) DO UPDATE SET elo = EXCLUDED.elo";

    @Override
    public void setElo(Elo elo) throws EloException {


        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {

            statement.setString(1, elo.getGame());
            statement.setString(2, elo.getPlayer());
            statement.setInt(3, elo.getElo());
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new EloException("Problem setting ELO", e);
        }
    }

    @Override
    public int getElo(String game, String player) throws EloException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_PLAYER)) {
            statement.setString(1, game);
            statement.setString(2, player);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 100;
                }
            }
        } catch (SQLException e) {
            throw new EloException("Problem getting ELO", e);
        }
    }
    @Override
    public List<Elo> getTopElo(String game) throws EloException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(SELECT_TOP10)) {
            statement.setString(1, game);
            try (ResultSet rs = statement.executeQuery()) {
                List<Elo> topElo = new ArrayList<>();
                while (rs.next()) {
                    topElo.add(new Elo(rs.getString(1), rs.getString(2), rs.getInt(3)));
                }
                return topElo;
            }
        } catch (SQLException e) {
            throw new EloException("Problem selecting top ELO", e);
        }
    }

    @Override
    public void reset() throws EloException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(DELETE);
        } catch (SQLException e) {
            throw new EloException("Problem deleting ELO", e);
        }
    }
}