package sk.tuke.gamestudio.game.chess.consoleui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sk.tuke.gamestudio.entity.Comment;
import sk.tuke.gamestudio.entity.Elo;
import sk.tuke.gamestudio.entity.Rating;
import sk.tuke.gamestudio.entity.Score;
import sk.tuke.gamestudio.game.chess.core.*;
import sk.tuke.gamestudio.game.chess.core.pieces.*;
import sk.tuke.gamestudio.service.*;

import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ConsoleUI {

    private static final String TOP_BORDER    = "  ┌───┬───┬───┬───┬───┬───┬───┬───┐";
    private static final String MIDDLE_BORDER = "  ├───┼───┼───┼───┼───┼───┼───┼───┤";
    private static final String BOTTOM_BORDER = "  └───┴───┴───┴───┴───┴───┴───┴───┘";
    private static final String FILE_LABELS   = "    a   b   c   d   e   f   g   h";

    private static final Pattern MOVE_PATTERN =
            Pattern.compile("([a-hA-H][1-8])\\s+([a-hA-H][1-8])");

    private GameController controller;
    private final Scanner scanner;
    private Color drawProposedBy = null;

    private String blackName;
    private String whiteName;
    private int whiteElo;
    private int blackElo;
    @Autowired
    private ScoreService scoreService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private EloService eloService;

    private String lastMessage = "";
    private String lastGameMessage = "";

    public ConsoleUI() {
        this.controller = new GameController();
        this.scanner = new Scanner(System.in);
    }



    public void play() {

        boolean running = true;

        while (running) {
            running = handleChoice();
        }
    }

    private boolean handleChoice() {

        boolean running = true;
        String game = "chess";
        while (running) {
            clearConsole();

            if (!lastMessage.isEmpty()) {
                System.out.println(lastMessage);
                System.out.println();
            }

            printMenuHeader();
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim().toLowerCase();

            lastMessage = "";

            try {
                switch (choice) {
                    case "p", "play" -> {
                        getPlayersNames();
                        int timeMinutes = getTimeLimit();
                        whiteElo = eloService.getElo(game, whiteName);
                        blackElo = eloService.getElo(game, blackName);

                        gameLoop(timeMinutes);
                    }
                    case "score top" -> {
                        List<Score> scores = scoreService.getTopScores(game);
                        if (scores.isEmpty()) {
                            lastMessage = "No scores yet.";
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Top scores:\n");
                            for (Score s : scores) {
                                sb.append("- ").append(s.getPlayer())
                                  .append(": ").append(s.getPoints())
                                  .append(" (").append(s.getPlayedOn()).append(")\n");
                            }
                            lastMessage = sb.toString();
                        }
                    }
                    case "score reset" -> {
                        scoreService.reset();
                        lastMessage = "Scores reset done.";
                    }
                    case "comment add" -> {
                        System.out.print("Player: ");
                        String player = scanner.nextLine().trim();
                        System.out.print("Comment text: ");
                        String text = scanner.nextLine();
                        commentService.addComment(new Comment(game, player, text, new Date()));
                        lastMessage = "Comment saved.";
                    }
                    case "comment list" -> {
                        List<Comment> comments = commentService.getComments(game);
                        if (comments.isEmpty()) {
                            lastMessage = "No comments yet.";
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Comments:\n");
                            for (Comment c : comments) {
                                sb.append("- ").append(c.getPlayer())
                                  .append(": ").append(c.getComment())
                                  .append(" (").append(c.getCommentedOn()).append(")\n");
                            }
                            lastMessage = sb.toString();
                        }
                    }
                    case "comment reset" -> {
                        commentService.reset();
                        lastMessage = "Comments reset done.";
                    }
                    case "rating set" -> {
                        System.out.print("Player: ");
                        String player = scanner.nextLine().trim();
                        System.out.print("Rating (1-5): ");
                        int value = Integer.parseInt(scanner.nextLine().trim());
                        ratingService.setRating(new Rating(game, player, value, new Date()));
                        lastMessage = "Rating saved.";
                    }
                    case "rating avg" -> {
                        int avg = ratingService.getAverageRating(game);
                        if(avg == 0){
                            lastMessage =  "No ratings yet.";
                        }
                        else {
                            lastMessage = "Average rating for '" + game + "': " + avg;
                        }
                    }
                    case "rating get" -> {
                        System.out.print("Player: ");
                        String player = scanner.nextLine().trim();
                        int rating = ratingService.getRating(game, player);
                        if(rating == 0){
                            lastMessage = "Player '" + player + "' has not rated the game yet.";
                        } else {
                            lastMessage = "Rating of '" + player + "' in '" + game + "': " + rating;
                        }
                    }
                    case "rating reset" -> {
                        ratingService.reset();
                        lastMessage = "Ratings reset done.";
                    }
                    case "elo top" -> {
                        List<Elo> topElo = eloService.getTopElo(game);
                        if (topElo.isEmpty()) {
                            lastMessage = "No ELOs yet.";
                        } else {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Top players by ELO:\n");
                            for (Elo e : topElo) {
                                sb.append("- ").append(e.getPlayer())
                                  .append(": ").append(e.getElo())
                                  .append("\n");
                            }
                            lastMessage = sb.toString();
                        }
                    }
                    case "elo get" -> {
                        System.out.print("Player: ");
                        String player = scanner.nextLine().trim();
                        int elo = eloService.getElo(game, player);
                        lastMessage = "ELO of '" + player + "' in '" + game + "': " + elo;
                    }
                    case "elo reset" -> {
                        eloService.reset();
                        lastMessage = "All player ELOs reset (don't do that pls).";
                    }
                    case "x", "exit" -> {
                        running = false;
                        lastMessage = "Exiting menu...";
                    }
                    default -> lastMessage = "Unknown command. Try again.";
                }
            } catch (ScoreException | CommentException | RatingException e) {
                lastMessage = "Service error: " + e.getMessage();
            } catch (NumberFormatException e) {
                lastMessage = "Invalid number format.";
            }
        }
        return false;
    }

    private void printMenuHeader() {
        System.out.println();
        System.out.println("========== CHESS MENU ==========");
        System.out.println("p / play        - play the game");
        System.out.println("score top       - get top scores");
        System.out.println("score reset     - reset scores");
        System.out.println("comment add     - add comment(comment)");
        System.out.println("comment list    - get comments");
        System.out.println("comment reset   - reset comments");
        System.out.println("rating set      - set rating(rating, player)");
        System.out.println("rating avg      - get average rating");
        System.out.println("rating get      - get rating(player)");
        System.out.println("rating reset    - reset ratings");
        System.out.println("elo top         - get top players by elo");
        System.out.println("elo get         - get elo(player)");
        System.out.println("elo reset       - reset players elo(don't do that pls)");
        System.out.println("x / exit        - exit menu");
        System.out.println("================================");
    }

    private void gameLoop(int timeMinutes){
        this.controller = timeMinutes > 0 ? new GameController(timeMinutes) : new GameController();
        lastGameMessage = "";
        System.out.println();
        System.out.println("  ╔══════════════════════════════════╗");
        System.out.println("  ║           C H E S S              ║");
        System.out.println("  ╚══════════════════════════════════╝");
        System.out.println();
        if (timeMinutes > 0) {
            System.out.println("  ⏱  Time limit: " + timeMinutes + " minutes per player");
        }
        System.out.println("  Enter moves like: e2 e4");
        System.out.println("  Commands:  '1/2' = propose draw   'resign' = resign   'exit' = stop the game");
        System.out.println();

        while (controller.getGameState() == GameState.ACTIVE) {
            if (controller.checkTimeExpired()) {
                break;
            }

            clearConsole();
            if (!lastGameMessage.isEmpty()) {
                System.out.println(lastGameMessage);
                System.out.println();
            }

            show();

            if(!handleInput()){
                continue;
            }

            if (controller.checkTimeExpired()) {
                break;
            }

            if (controller.isPromotionPending()) {
                if (controller.checkTimeExpired()) {
                    break;
                }
                handlePromotion();
            }

            System.out.println();
        }

        clearConsole();
        show();
        showGameResult(controller.getGameState());
        drawProposedBy = null;
        saveScore();
    }

    private boolean handleInput(){
        StringBuilder msg = new StringBuilder();

        if (controller.isInCheck(controller.getCurrentPlayer())) {
            msg.append("  ⚠  CHECK!  ⚠\n\n");
        }

        Color currentColor = controller.getCurrentPlayer().getColor();
        String colorName = currentColor == Color.WHITE ? "White(" + whiteElo + ")" : "Black(" + blackElo + ")";

        if (drawProposedBy != null) {
            String proposer = drawProposedBy == Color.WHITE ? "White" : "Black";
            msg.append("  ½  ").append(proposer)
               .append(" proposes a draw! Type '1/2' to accept, or anything else to decline.\n");
        }

        if (!msg.isEmpty()) {
            System.out.print(msg);
        }

        System.out.print("  " + colorName + "'s turn: ");
        String input = scanner.nextLine().trim();
        System.out.println();
        System.out.println("=======================================================");
        System.out.println();

        lastGameMessage = "";

        if (controller.checkTimeExpired()) {
            lastGameMessage = "  ⏱  Time expired.";
            return false;
        }
        if (input.equalsIgnoreCase("exit")) {
            controller.resign();
            lastGameMessage = "  Game ended by player. " + colorName + " left the game";
            return false;
        }

        if (input.equalsIgnoreCase("resign")) {
            String winner = currentColor == Color.WHITE ? "Black" : "White";
            controller.resign();
            lastGameMessage = "  " + colorName + " resigns. " + winner + " wins!";
            return false;
        }

        if (input.equalsIgnoreCase("1/2")) {
            if (drawProposedBy == null) {
                drawProposedBy = currentColor;
                lastGameMessage = "  " + colorName + " proposes a draw!";
                controller.passTurn();
            } else {
                controller.drawByAgreement();
                lastGameMessage = "  Draw accepted!";
            }
            return false;
        }
        if(drawProposedBy != null && !input.equalsIgnoreCase("1/2")){
            lastGameMessage = "  Draw proposal declined.";
            drawProposedBy = null;
            controller.passTurn();
            return false;
        }

        Matcher matcher = MOVE_PATTERN.matcher(input);
        if (!matcher.matches()) {
            lastGameMessage = "  Wrong move type! Try again.";
            return false;
        }

        String[] parts = input.split("\\s+");
        Square fromSquare = parseSquare(parts[0]);
        Square toSquare   = parseSquare(parts[1]);
        if (fromSquare == null || toSquare == null) {
            lastGameMessage = "  Invalid square! Try again.";
            return false;
        }
        Piece selectedPiece = fromSquare.getPiece();
        if (selectedPiece == null) {
            lastGameMessage = "  No piece on that square! Try again!";
            return false;
        }
        if (selectedPiece.getColor() != currentColor) {
            lastGameMessage = "  It's not your piece! Try again!";
            return false;
        }

        if (controller.checkTimeExpired()) {
            lastGameMessage = "  ⏱  Time expired.";
            return false;
        }

        if (!controller.movePiece(selectedPiece, toSquare)) {
            Piece targetPiece = toSquare.getPiece();
            if (targetPiece != null && targetPiece.getColor() == currentColor) {
                lastGameMessage = "  You can't capture your own piece! Try again!";
            } else {
                lastGameMessage = "  Can't move like that! Try again.";
            }
            if (controller.isInCheck(controller.getCurrentPlayer())) {
                lastGameMessage += "\n  (Notice that you are under check right now!)";
            }
            return false;
        }

        lastGameMessage = "";
        return true;
    }

    private void handlePromotion() {
        System.out.println("  Pawn promotion! Choose piece (Q/R/B/N):");
        while (true) {
            System.out.print("  > ");
            String choice = scanner.nextLine().trim().toUpperCase();
            if (choice.matches("[QRBN]")) {
                controller.promotePawn(choice);
                break;
            }
            System.out.println("  Invalid choice. Enter Q, R, B, or N:");
        }
    }

    private Square parseSquare(String s) {
        if (s.length() != 2) return null;

        char colChar = Character.toLowerCase(s.charAt(0));
        char rowChar = s.charAt(1);

        if (colChar < 'a' || colChar > 'h') return null;
        if (rowChar < '1' || rowChar > '8') return null;

        int column = colChar - 'a';
        int row = 8 - (rowChar - '0');

        return controller.getBoard().getSquare(row, column);
    }

    public void show() {
        Board board = controller.getBoard();

        final String ANSI_RESET = "\u001B[0m";
        final String FG_WHITE   = "\u001B[1;97m";
        final String FG_BLACK   = "\u001B[1;30m";

        System.out.println(FILE_LABELS);
        System.out.println(TOP_BORDER);

        for (int row = 0; row < 8; row++) {
            System.out.print((8 - row) + " │");
            for (int col = 0; col < 8; col++) {
                Square square = board.getSquare(row, col);
                Piece piece = square.getPiece();

                String fg = "";
                String symbol;

                if (piece != null) {
                    symbol = getPieceSymbol(piece);
                    fg = (piece.getColor() == Color.WHITE) ? FG_WHITE : FG_BLACK;
                } else {
                    boolean isDarkSquare = (row + col) % 2 != 0;
                    symbol = isDarkSquare ? "·" : " ";

                }

                System.out.print(" " + fg + symbol + ANSI_RESET + " │");
            }
            System.out.println(" " + (8 - row));

            if (row < 7) {
                System.out.println(MIDDLE_BORDER);
            }
        }

        System.out.println(BOTTOM_BORDER);
        System.out.println(FILE_LABELS);

        GameTimer timer = controller.getGameTimer();
        if (timer != null) {
            Color activeColor = controller.getCurrentPlayer().getColor();
            String whiteTime = formatTime(timer.getRemainingTimeMs(Color.WHITE, activeColor));
            String blackTime = formatTime(timer.getRemainingTimeMs(Color.BLACK, activeColor));
            System.out.println("  ⏱  White: " + whiteTime + "  |  Black: " + blackTime);
        }
        System.out.println();

    }

    private String formatTime(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private String getPieceSymbol(Piece piece) {
        String symbol;
        if (piece instanceof King) {
            symbol =  "♚";
        } else if (piece instanceof Queen) {
            symbol =  "♛";
        } else if (piece instanceof Rook) {
            symbol = "♜";
        } else if (piece instanceof Bishop) {
            symbol =  "♝";
        } else if (piece instanceof Knight) {
            symbol = "♞";
        } else if (piece instanceof Pawn) {
            symbol ="♟";
        } else {
            symbol = "?";
        }
        return symbol;
    }

    private void getPlayersNames(){
        System.out.println("White player: ");
        whiteName = scanner.nextLine().trim();
        do {
            System.out.println("Black player: ");
            blackName = scanner.nextLine().trim();
            if(blackName.equals(whiteName)){
                System.out.println("Black player name cannot be the same as white player name. Please enter a different name.");
            }
        }while(blackName.equals(whiteName));
    }

    private int getTimeLimit() {
        System.out.println();
        System.out.println("Do you want to play with time limit?");
        System.out.print("Enter time limit in minutes (0 for no time limit): ");
        try {
            String input = scanner.nextLine().trim();
            int minutes = Integer.parseInt(input);
            return Math.max(0, minutes);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Playing without time limit.");
            return 0;
        }
    }
    private void saveScore(){
        String game = "chess";

        int whiteElo = eloService.getElo(game, whiteName);
        int blackElo = eloService.getElo(game, blackName);

        switch (controller.getGameState()){
            case DRAW -> {
                scoreService.addScore(new Score(game, whiteName, 5 , new Date()));
                scoreService.addScore(new Score(game, blackName, 5 , new Date()));

                int diff = whiteElo - blackElo;
                int change = 0;
                if(diff >= 50){
                    change = -5;
                } else if ( diff <= -50){
                    change = 5;
                }

                eloService.setElo(new Elo(game, whiteName, Math.max(0, whiteElo + change)));
                eloService.setElo(new Elo(game, blackName, Math.max(0, blackElo - change)));
            }
            case WHITE_WON -> {
                scoreService.addScore(new Score(game, whiteName, 10, new Date()));
                scoreService.addScore(new Score(game, blackName, 0 , new Date()));

                eloService.setElo(new Elo(game, whiteName, Math.max(0, whiteElo + 10)));
                eloService.setElo(new Elo(game, blackName, Math.max(0, blackElo - 10)));
            }
            case BLACK_WON -> {
                scoreService.addScore(new Score(game, blackName, 10 , new Date()));
                scoreService.addScore(new Score(game, whiteName, 0 , new Date()));

                eloService.setElo(new Elo(game, whiteName, Math.max(0, whiteElo - 10)));
                eloService.setElo(new Elo(game, blackName, Math.max(0, blackElo + 10)));
            }
        }
    }
    public void showGameResult(GameState gameState) {
        StringBuilder resultBuilder = new StringBuilder();
        resultBuilder.append("  ═══════════════════════════════════\n");
        switch (gameState) {
            case WHITE_WON -> {
                if (controller.getWinReason() == WinReason.RESIGN) {
                    resultBuilder.append("  🏳  Black resigns. White wins!  ★\n");
                } else if (controller.getWinReason() == WinReason.TIMEOUT) {
                    resultBuilder.append("  ⏱  Black's time expired! White wins!  ★\n");
                } else {
                    resultBuilder.append("  ★  Checkmate! White wins!  ★\n");
                }
            }
            case BLACK_WON -> {
                if (controller.getWinReason() == WinReason.RESIGN) {
                    resultBuilder.append("  🏳  White resigns. Black wins!  ★\n");
                } else if (controller.getWinReason() == WinReason.TIMEOUT) {
                    resultBuilder.append("  ⏱  White's time expired! Black wins!  ★\n");
                } else {
                    resultBuilder.append("  ★  Checkmate! Black wins!  ★\n");
                }
            }
            case DRAW -> {
                DrawReason reason = controller.getDrawReason();
                String reasonText = reason != null ? reason.name().replace('_', ' ').toLowerCase() : "";
                String pretty = reasonText.isEmpty()
                        ? "draw"
                        : reasonText.substring(0, 1).toUpperCase() + reasonText.substring(1);
                resultBuilder.append("  ½  Draw! ").append(pretty).append("  ½\n");
            }
            default -> resultBuilder.append("  Game over.\n");
        }
        resultBuilder.append("  ═══════════════════════════════════");

        lastMessage = resultBuilder.toString();
    }
    private void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
