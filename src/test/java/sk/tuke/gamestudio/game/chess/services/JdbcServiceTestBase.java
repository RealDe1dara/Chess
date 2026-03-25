package sk.tuke.gamestudio.game.chess.services;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class JdbcServiceTestBase {

    protected static final String URL = "jdbc:postgresql://localhost/gamestudio";
    protected static final String USER = "postgres";
    protected static final String PASSWORD = "postgres";

    @BeforeEach
    @AfterEach
    void cleanDatabase() throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM score");
            statement.executeUpdate("DELETE FROM comment");
            statement.executeUpdate("DELETE FROM rating");
            statement.executeUpdate("DELETE FROM elo");
        }
    }
}

