package sk.tuke.gamestudio.service;

import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class RatingServiceJDBC implements RatingService {
    public static final String URL = "jdbc:postgresql://localhost/gamestudio";
    public static final String USER = "postgres";
    public static final String PASSWORD = "postgres";
    public static final String SELECT_BY_PLAYER = "SELECT rating FROM rating WHERE game = ? AND player = ?";
    public static final String SELECT_AVERAGE = "SELECT AVG(rating) FROM rating WHERE game = ?;";
    public static final String DELETE = "DELETE FROM rating";
    public static final String INSERT = "INSERT INTO rating (game, player, rating, ratedOn) VALUES (?, ?, ?, ?);";
    public static final String UPDATE = "UPDATE rating SET rating = ?, ratedOn = ?  WHERE player = ? AND game = ?;";

    @Override
    public void setRating(Rating rating) throws RatingException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            if (getRating(rating.getGame(), rating.getPlayer()) == 0) {
                try (PreparedStatement statement = connection.prepareStatement(INSERT)) {
                    statement.setString(1, rating.getGame());
                    statement.setString(2, rating.getPlayer());
                    statement.setInt(3, rating.getRating());
                    statement.setTimestamp(4, new Timestamp(rating.getRatedOn().getTime()));
                    statement.executeUpdate();
                }
            } else {
                try (PreparedStatement statement = connection.prepareStatement(UPDATE)) {
                    statement.setInt(1, rating.getRating());
                    statement.setTimestamp(2, new Timestamp(rating.getRatedOn().getTime()));
                    statement.setString(3, rating.getPlayer());
                    statement.setString(4, rating.getGame());
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RatingException("Problem setting rating", e);
        }
    }

    @Override
    public int getAverageRating(String game) throws RatingException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(SELECT_AVERAGE);
        ){
            statement.setString(1, game);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 0;
                }
            }
        } catch (SQLException e) {
            throw new RatingException("Problem getting average rating", e);
        }
    }

    @Override
    public int getRating(String game, String player) throws RatingException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_PLAYER);
        ){
            statement.setString(1, game);
            statement.setString(2, player);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 0;
                }
            }
        } catch (SQLException e) {
            throw new RatingException("Problem getting rating", e);
        }
    }

    @Override
    public void reset() throws RatingException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            Statement statement = connection.createStatement();
        ) {
            statement.executeUpdate(DELETE);
        } catch (SQLException e) {
            throw new RatingException("Problem deleting rating", e);
        }
    }
}
