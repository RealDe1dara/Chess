package sk.tuke.gamestudio.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import sk.tuke.gamestudio.game.chess.ai.ChessAiPlayer;
import sk.tuke.gamestudio.service.AuthService;
import sk.tuke.gamestudio.service.*;

@SpringBootApplication
@EntityScan("sk.tuke.gamestudio.entity")
public class GameStudioServer {
    public static void main(String[] args) {
        SpringApplication.run(GameStudioServer.class, args);
    }

    @Bean
    public ScoreService scoreService() {
        return new ScoreServiceJPA();
    }

    @Bean
    public RatingService ratingService() {
        return new RatingServiceJPA();
    }

    @Bean
    public EloService eloService() {
        return new EloServiceJPA();
    }

    @Bean
    public CommentService commentService() {
        return new CommentServiceJPA();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(8);
    }

    @Bean
    public GameUserService gameUserService() {
        return new GameUserServiceJPA();
    }

    @Bean
    public AuthService authService(GameUserService gameUserService, PasswordEncoder passwordEncoder,
                                   ScoreService scoreService, EloService eloService,
                                   RatingService ratingService, CommentService commentService) {
        return new AuthService(gameUserService, passwordEncoder, scoreService, eloService, ratingService, commentService);
    }

    @Bean
    public GameSessionService gameSessionService(ScoreService scoreService, EloService eloService) {
        return new GameSessionService(scoreService, eloService);
    }

    @Bean
    public ChessAiPlayer chessAiPlayer() {
        return new ChessAiPlayer();
    }

}
