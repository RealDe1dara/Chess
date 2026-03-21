package sk.tuke.gamestudio.game.chess.consoleui;

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
    private final ScoreService scoreService = new ScoreServiceJDBC();
    private final CommentService commentService = new CommentServiceJDBC();
    private final RatingService ratingService = new RatingServiceJDBC();
    private final EloService eloService = new EloServiceJDBC();

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
            System.out.print("Your choice: ");

            String choice = scanner.nextLine().trim().toLowerCase();


            try {
                switch (choice) {
                    case "p", "play" -> {
                        getPlayersNames();
                        whiteElo = eloService.getElo(game, whiteName);
                        blackElo = eloService.getElo(game, blackName);
                        gameLoop();
                        this.controller = new GameController();
                    }
                    case "score top" -> {
                        List<Score> scores = scoreService.getTopScores(game);
                        if (scores.isEmpty()) {
                            System.out.println("No scores yet.");
                        } else {
                            System.out.println("Top scores:");
                            for (Score s : scores) {
                                System.out.println("- " + s.getPlayer() + ": " + s.getPoints() + " (" + s.getPlayedOn() + ")");
                            }
                        }
                    }

                    case "score reset" -> {
                        scoreService.reset();
                        System.out.println("Scores reset done.");
                    }
                    case "comment add" -> {

                        System.out.print("Player: ");
                        String player = scanner.nextLine().trim();
                        System.out.print("Comment text: ");
                        String text = scanner.nextLine();

                        commentService.addComment(new Comment(game, player, text, new Date()));
                        System.out.println("Comment saved.");
                    }

                    case "comment list" -> {

                        List<Comment> comments = commentService.getComments(game);
                        if (comments.isEmpty()) {
                            System.out.println("No comments yet.");
                        } else {
                            System.out.println("Comments:");
                            for (Comment c : comments) {
                                System.out.println("- " + c.getPlayer() + ": " + c.getComment() + " (" + c.getCommentedOn() + ")");
                            }
                        }
                    }
                    case "comment reset" -> {
                        commentService.reset();
                        System.out.println("Comments reset done.");
                    }

                    case "rating set" -> {
                        System.out.print("Player: ");
                        String player = scanner.nextLine().trim();
                        System.out.print("Rating (1-5): ");
                        int value = Integer.parseInt(scanner.nextLine().trim());
                        ratingService.setRating(new Rating(game, player, value, new Date()));
                        System.out.println("Rating saved.");
                    }

                    case "rating avg" -> {
                        int avg = ratingService.getAverageRating(game);
                        System.out.println("Average rating for '" + game + "': " + avg);
                    }
                    case "rating get" -> {
                        System.out.print("Player: ");
                        String player = scanner.nextLine().trim();
                        int rating = ratingService.getRating(game, player);
                        System.out.println("Rating of '" + player + "' in '" + game + "': " + rating);
                    }

                    case "rating reset" -> {
                        ratingService.reset();
                        System.out.println("Ratings reset done.");
                    }
                    case "elo top" -> {
                        List<Elo> topElo = eloService.getTopElo(game);
                        if (topElo.isEmpty()) {
                            System.out.println("No ELOs yet.");
                        } else {
                            System.out.println("Top players by ELO:");
                            for (Elo e : topElo) {
                                System.out.println("- " + e.getPlayer() + ": " + e.getElo());
                            }
                        }
                    }
                    case "elo get" -> {
                        System.out.print("Player: ");
                        String player = scanner.nextLine().trim();
                        int elo = eloService.getElo(game, player);
                        System.out.println("ELO of '" + player + "' in '" + game + "': " + elo);
                    }
                    case "elo reset" -> {
                        eloService.reset();
                        System.out.println("All player ELOs reset (don't do that pls).");
                    }
                    case "x", "exit" -> {
                        running = false;
                        System.out.println("Exiting menu...");
                    }

                    default -> System.out.println("Unknown command. Try again.");
                }
            } catch (ScoreException | CommentException | RatingException e) {
                    System.out.println("Service error: " + e.getMessage());
                    } catch (NumberFormatException e) {
                    System.out.println("Invalid number format.");
            }
        }
        return false;
    }

    private void gameLoop(){
        System.out.println();
        System.out.println("  ╔══════════════════════════════════╗");
        System.out.println("  ║           C H E S S              ║");
        System.out.println("  ╚══════════════════════════════════╝");
        System.out.println();
        System.out.println("  Enter moves like: e2 e4");
        System.out.println("  Commands:  '1/2' = propose draw   'resign' = resign   'exit' = stop the game");
        System.out.println();

        while (controller.getGameState() == GameState.ACTIVE) {
            show();


            if(!handleInput()){
                continue;
            }

            if (controller.isPromotionPending()) {
                handlePromotion();
            }

            System.out.println();
        }

        show();
        showGameResult(controller.getGameState());
        drawProposedBy = null;
        saveScore();
    }

    private boolean handleInput(){
        if (controller.isInCheck(controller.getCurrentPlayer())) {
            System.out.println("  ⚠  CHECK!  ⚠");
            System.out.println();
        }

        Color currentColor = controller.getCurrentPlayer().getColor();
        String colorName = currentColor == Color.WHITE ? "White(" + whiteElo + ")" : "Black(" + blackElo + ")";

        if (drawProposedBy != null) {
            String proposer = drawProposedBy == Color.WHITE ? "White" : "Black";
            System.out.println("  ½  " + proposer + " proposes a draw! Type '1/2' to accept, or anything else to decline.");
        }

        System.out.print("  " + colorName + "'s turn: ");
        String input = scanner.nextLine().trim();
        System.out.println();
        System.out.println("=======================================================");
        System.out.println();
        if (input.equalsIgnoreCase("exit")) {
            controller.resign();
            System.out.println();
            System.out.println("  Game ended by player. " + colorName + " left the game");
            System.out.println();

            return false;
        }

        if (input.equalsIgnoreCase("resign")) {
            String winner = currentColor == Color.WHITE ? "Black" : "White";
            System.out.println();
            System.out.println("  " + colorName + " resigns. " + winner + " wins!");
            System.out.println();
            controller.resign();
            return false;
        }

        if (input.equalsIgnoreCase("1/2")) {
            if (drawProposedBy == null) {
                drawProposedBy = currentColor;
                System.out.println();
                System.out.println("  " + colorName + " proposes a draw!");
                controller.passTurn();
            } else {
                controller.drawByAgreement();
                System.out.println("  Draw accepted!");
            }
            System.out.println();
            return false;
        }
        if(drawProposedBy != null && !input.equalsIgnoreCase("1/2")){
            System.out.println("  Draw proposal declined.");
            System.out.println();
            drawProposedBy = null;
            controller.passTurn();
            return false;
        }


        Matcher matcher = MOVE_PATTERN.matcher(input);
        if (!matcher.matches()) {
            System.out.println("  Wrong move type! Try again.");
            System.out.println();
            return false;
        }

        String[] parts = input.split("\\s+");
        Square fromSquare = parseSquare(parts[0]);
        Square toSquare   = parseSquare(parts[1]);
        if (fromSquare == null || toSquare == null) {
            System.out.println("  Invalid square! Try again.");
            System.out.println();
            return false;
        }
        Piece selectedPiece = fromSquare.getPiece();
        if (selectedPiece == null) {
            System.out.println("  No piece on that square! Try again!");
            System.out.println();
            return false;
        }
        if (selectedPiece.getColor() != currentColor) {
            System.out.println("  It's not your piece! Try again!");
            System.out.println();
            return false;
        }
        if (!controller.movePiece(selectedPiece, toSquare)) {
            Piece targetPiece = toSquare.getPiece();
            if (targetPiece != null && targetPiece.getColor() == currentColor) {
                System.out.println("  You can't capture your own piece! Try again!");
            } else {
                System.out.println("  Can't move like that! Try again.");
            }
            if (controller.isInCheck(controller.getCurrentPlayer())) {
                System.out.println("  (Notice that you are under check right now!)");
            }
            System.out.println();
            return false;
        }
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

        System.out.println(FILE_LABELS);
        System.out.println(TOP_BORDER);

        for (int row = 0; row < 8; row++) {
            System.out.print((8 - row) + " │");
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getSquare(row, col).getPiece();
                String symbol;
                if (piece != null) {
                    symbol = getPieceSymbol(piece);
                } else {
                    boolean isDarkSquare = (row + col) % 2 != 0;
                    symbol = isDarkSquare ? "·" : " ";
                }
                System.out.print(" " + symbol + " │");
            }
            System.out.println(" " + (8 - row));

            if (row < 7) {
                System.out.println(MIDDLE_BORDER);
            }
        }

        System.out.println(BOTTOM_BORDER);
        System.out.println(FILE_LABELS);
        System.out.println();
    }

    private String getPieceSymbol(Piece piece) {
        String symbol;
        if (piece instanceof King k) {
            symbol = k.getColor() == Color.WHITE ? "♔" : "♚";
        } else if (piece instanceof Queen q) {
            symbol = q.getColor() == Color.WHITE ? "♕" : "♛";
        } else if (piece instanceof Rook r) {
            symbol = r.getColor() == Color.WHITE ? "♖" : "♜";
        } else if (piece instanceof Bishop b) {
            symbol = b.getColor() == Color.WHITE ? "♗" : "♝";
        } else if (piece instanceof Knight n) {
            symbol = n.getColor() == Color.WHITE ? "♘" : "♞";
        } else if (piece instanceof Pawn p) {
            symbol = p.getColor() == Color.WHITE ? "♙" : "♟";
        } else {
            symbol = "?";
        }
        return symbol;
    }

    private void getPlayersNames(){
        System.out.println("White player: ");
        whiteName = scanner.nextLine().trim();
        System.out.println("Black player: ");
        blackName = scanner.nextLine().trim();
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
        System.out.println("  ═══════════════════════════════════");
        switch (gameState) {
            case WHITE_WON -> {
                if (controller.getWinReason() == WinReason.RESIGN) {
                    System.out.println("  🏳  Black resigns. White wins!  ★");
                } else {
                    System.out.println("  ★  Checkmate! White wins!  ★");
                }
            }
            case BLACK_WON -> {
                if (controller.getWinReason() == WinReason.RESIGN) {
                    System.out.println("  🏳  White resigns. Black wins!  ★");
                } else {
                    System.out.println("  ★  Checkmate! Black wins!  ★");
                }
            }
            case DRAW -> {
                DrawReason reason = controller.getDrawReason();
                String reasonText = reason != null ? reason.name().replace('_', ' ').toLowerCase() : "";
                System.out.println("  ½  Draw! " + reasonText.substring(0, 1).toUpperCase() + reasonText.substring(1) + "  ½");
            }
            default -> System.out.println("  Game over.");
        }
        System.out.println("  ═══════════════════════════════════");
    }
}