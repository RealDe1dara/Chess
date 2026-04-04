package sk.tuke.gamestudio;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import sk.tuke.gamestudio.game.chess.consoleui.ConsoleUI;

@SpringBootApplication
public class SpringClient {

	public static void main(String[] args) {
		SpringApplication.run(SpringClient.class, args);
	}

	@Bean
	public CommandLineRunner runner(ConsoleUI ui) {
		return args -> ui.play();
	}

	@Bean
	public ConsoleUI consoleUI() {
		return new ConsoleUI();
	}
}


