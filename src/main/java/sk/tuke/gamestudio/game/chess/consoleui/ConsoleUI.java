package sk.tuke.gamestudio.game.chess.consoleui;

import sk.tuke.gamestudio.game.chess.core.*;
import sk.tuke.gamestudio.game.chess.core.pieces.*;

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

    public ConsoleUI() {
        this.controller = new GameController();
        this.scanner = new Scanner(System.in);
    }



    public void play() {
        boolean again;
        do {
            gameLoop();

            this.controller = new GameController();

            System.out.print("  Play again? (y/n): ");
            String answer = scanner.nextLine().trim();
            again = answer.equalsIgnoreCase("y") || answer.equalsIgnoreCase("yes");
        }while(again);
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
    }

    private boolean handleInput(){
        if (controller.isInCheck(controller.getCurrentPlayer())) {
            System.out.println("  ⚠  CHECK!  ⚠");
            System.out.println();
        }

        Color currentColor = controller.getCurrentPlayer().getColor();
        String colorName = currentColor == Color.WHITE ? "White" : "Black";

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