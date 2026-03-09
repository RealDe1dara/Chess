package sk.tuke.gamestudio.game.chess;

import sk.tuke.gamestudio.game.chess.consoleui.ConsoleUI;
import sk.tuke.gamestudio.game.chess.core.GameController;

public class ChessApp {
    public static void main(String[] args) {
//        GameController controller = new GameController();
        ConsoleUI ui = new ConsoleUI();
        ui.play();
    }
}