package sk.tuke.gamestudio.game.chess.core;

import sk.tuke.gamestudio.game.chess.core.pieces.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameController {

    private final Board board;
    private final Player whitePlayer;
    private final Player blackPlayer;
    private Player currentPlayer;
    private GameState gameState;
    private Pawn promotionPendingPawn;
    private final Map<String, Integer> positionHistory = new HashMap<>();
    private DrawReason drawReason;
    private WinReason winReason;
    private final GameTimer gameTimer;

    public GameController() {
        this.board = new Board();
        this.whitePlayer = new Player(Color.WHITE);
        this.blackPlayer = new Player(Color.BLACK);
        this.currentPlayer = whitePlayer;
        this.gameState = GameState.ACTIVE;
        this.gameTimer = null;
        setupInitialPosition();
        recordPosition();
    }

    public GameController(int timeMinutes) {
        this.board = new Board();
        this.whitePlayer = new Player(Color.WHITE);
        this.blackPlayer = new Player(Color.BLACK);
        this.currentPlayer = whitePlayer;
        this.gameState = GameState.ACTIVE;
        this.gameTimer = new GameTimer(timeMinutes);
        setupInitialPosition();
        recordPosition();
    }

    public boolean checkTimeExpired() {
        if (gameTimer == null) {
            return false;
        }
        Color activeColor = currentPlayer.getColor();
        if (!gameTimer.hasTimeRemaining(activeColor, activeColor)) {
            gameState = (currentPlayer == whitePlayer) ? GameState.BLACK_WON : GameState.WHITE_WON;
            winReason = WinReason.TIMEOUT;
            return true;
        }
        return false;
    }

    public boolean movePiece(Piece piece, Square target) {
        if (gameState != GameState.ACTIVE || piece == null || target == null) {
            return false;
        }
        if (piece.getColor() != currentPlayer.getColor()) {
            return false;
        }
        Move move = findValidMove(piece, target);
        if (move == null) {
            return false;
        }
        executeMove(move);
        return true;
    }

    private void executeMove(Move move) {
        Piece piece = move.getMovedPiece();
        Square oldSquare = move.getOldSquare();
        Square target = move.getNewSquare();
        Piece capturedPiece = move.getCapturedPiece();

        if (move.hasType(MoveType.EN_PASSANT)) {
            Square enemyPawnSquare = capturedPiece.getSquare();
            enemyPawnSquare.setPiece(null);
            capturedPiece.setSquare(null);
            getPlayerByColor(capturedPiece.getColor()).removePiece(capturedPiece);
        } else if (capturedPiece != null) {
            Square capturedSquare = capturedPiece.getSquare();
            if (capturedSquare != null) {
                capturedSquare.setPiece(null);
            }
            capturedPiece.setSquare(null);
            getPlayerByColor(capturedPiece.getColor()).removePiece(capturedPiece);
        }

        if (move.hasType(MoveType.CASTLING)) {
            executeCastlingRook(oldSquare, target);
        }

        oldSquare.setPiece(null);
        target.setPiece(piece);
        piece.setSquare(target);
        piece.onMove();

        board.setLastMove(move);

        if (move.hasType(MoveType.PROMOTION)) {
            promotionPendingPawn = (Pawn) piece;
            return;
        }

        if (gameTimer != null) {
            gameTimer.recordMove(currentPlayer.getColor());
        }

        switchCurrentPlayer();
        updateGameState();
    }

    public boolean isPromotionPending() {
        return promotionPendingPawn != null;
    }

    public void promotePawn(String pieceType) {
        if (promotionPendingPawn == null) {
            return;
        }

        Square square = promotionPendingPawn.getSquare();
        Color color = promotionPendingPawn.getColor();
        Player player = getPlayerByColor(color);

        Piece newPiece = switch (pieceType.toUpperCase()) {
            case "R", "ROOK" -> new Rook(color, square);
            case "B", "BISHOP" -> new Bishop(color, square);
            case "N", "KNIGHT" -> new Knight(color, square);
            default -> new Queen(color, square);
        };

        square.setPiece(newPiece);
        player.removePiece(promotionPendingPawn);
        player.addPiece(newPiece);
        promotionPendingPawn = null;

        switchCurrentPlayer();
        updateGameState();
    }

    private Move findValidMove(Piece piece, Square target) {
        List<Move> pseudoLegalMoves = piece.getValidMoves(board);
        for (Move move : pseudoLegalMoves) {
            if (move.getNewSquare() == target && isMoveLegal(move)) {
                return move;
            }
        }
        return null;
    }

    private boolean isMoveLegal(Move move) {
        Piece movedPiece = move.getMovedPiece();
        Square from = move.getOldSquare();
        Square to = move.getNewSquare();
        Piece capturedPiece = move.getCapturedPiece();
        Player opponent = getPlayerByColor(movedPiece.getColor() == Color.WHITE ? Color.BLACK : Color.WHITE);

        if (move.hasType(MoveType.CASTLING)) {
            if (isSquareAttacked(from, opponent)) return false;
            int dir = (to.getColumn() > from.getColumn()) ? 1 : -1;
            Square passing = board.getSquare(from.getRow(), from.getColumn() + dir);
            Piece origPassing = passing.getPiece();
            from.setPiece(null);
            passing.setPiece(movedPiece);
            movedPiece.setSquare(passing);
            boolean passingAttacked = isSquareAttacked(passing, opponent);
            passing.setPiece(origPassing);
            from.setPiece(movedPiece);
            movedPiece.setSquare(from);
            if (passingAttacked) return false;
        }

        Piece origFromPiece = from.getPiece();
        Piece origToPiece = to.getPiece();

        from.setPiece(null);
        to.setPiece(movedPiece);
        movedPiece.setSquare(to);

        Square epSquare = null;
        Piece epPiece = null;
        Square capturedOrigSquare = null;

        if (move.hasType(MoveType.EN_PASSANT) && capturedPiece != null) {
            capturedOrigSquare = capturedPiece.getSquare();
            epSquare = capturedOrigSquare;
            epPiece = epSquare.getPiece();
            epSquare.setPiece(null);
            capturedPiece.setSquare(null);
        } else if (capturedPiece != null) {
            capturedOrigSquare = capturedPiece.getSquare();
            capturedPiece.setSquare(null);
        }

        King king = getPlayerByColor(movedPiece.getColor()).getKing();
        boolean inCheck = isSquareAttacked(king.getSquare(), opponent);

        from.setPiece(origFromPiece);
        to.setPiece(origToPiece);
        movedPiece.setSquare(from);

        if (move.hasType(MoveType.EN_PASSANT) && epSquare != null) {
            epSquare.setPiece(epPiece);
            capturedPiece.setSquare(capturedOrigSquare);
        } else if (capturedPiece != null) {
            capturedPiece.setSquare(capturedOrigSquare);
        }

        return !inCheck;
    }

    public boolean isSquareAttacked(Square square, Player opponentPlayer) {
        List<Piece> opponentPieces = opponentPlayer.getPieces();
        for (Piece piece : opponentPieces) {
            if (piece.getSquare() == null) {
                continue;
            }
            for (Move move : piece.getValidMoves(board)) {
                if (square == move.getNewSquare()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isInCheck(Player player) {
        King king = player.getKing();
        if (king == null || king.getSquare() == null) return false;
        return isSquareAttacked(king.getSquare(), getOpponentPlayer(player));
    }

    private boolean hasAnyLegalMoves(Player player) {
        for (Piece piece : player.getPieces()) {
            if (piece.getSquare() == null) continue;
            for (Move move : piece.getValidMoves(board)) {
                if (isMoveLegal(move)) return true;
            }
        }
        return false;
    }

    private void executeCastlingRook(Square kingFrom, Square kingTo) {
        int row = kingFrom.getRow();
        int diff = kingTo.getColumn() - kingFrom.getColumn();

        if (diff == 2) {
            Square rookFrom = board.getSquare(row, 7);
            Square rookTo = board.getSquare(row, 5);
            Rook rook = (Rook) rookFrom.getPiece();
            rookFrom.setPiece(null);
            rookTo.setPiece(rook);
            rook.setSquare(rookTo);
            rook.onMove();
        } else if (diff == -2) {
            Square rookFrom = board.getSquare(row, 0);
            Square rookTo = board.getSquare(row, 3);
            Rook rook = (Rook) rookFrom.getPiece();
            rookFrom.setPiece(null);
            rookTo.setPiece(rook);
            rook.setSquare(rookTo);
            rook.onMove();
        }
    }

    private void updateGameState() {
        recordPosition();

        boolean hasLegalMoves = hasAnyLegalMoves(currentPlayer);
        boolean inCheck = isInCheck(currentPlayer);

        if (!hasLegalMoves) {
            if (inCheck) {
                gameState = (currentPlayer == whitePlayer) ? GameState.BLACK_WON : GameState.WHITE_WON;
                winReason = WinReason.CHECKMATE;
            } else {
                gameState = GameState.DRAW;
                drawReason = DrawReason.STALEMATE;
            }
            return;
        }

        if (isInsufficientMaterial()) {
            gameState = GameState.DRAW;
            drawReason = DrawReason.INSUFFICIENT_MATERIAL;
            return;
        }

        if (isThreefoldRepetition()) {
            gameState = GameState.DRAW;
            drawReason = DrawReason.THREEFOLD_REPETITION;
        }
    }

    private void recordPosition() {
        String key = generatePositionKey();
        positionHistory.merge(key, 1, Integer::sum);
    }

    private boolean isThreefoldRepetition() {
        String key = generatePositionKey();
        return positionHistory.getOrDefault(key, 0) >= 3;
    }

    private String generatePositionKey() {
        StringBuilder sb = new StringBuilder();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getSquare(row, col).getPiece();
                if (piece == null) {
                    sb.append('.');
                } else {
                    char c;
                    if (piece instanceof King) c = 'K';
                    else if (piece instanceof Queen) c = 'Q';
                    else if (piece instanceof Rook) c = 'R';
                    else if (piece instanceof Bishop) c = 'B';
                    else if (piece instanceof Knight) c = 'N';
                    else if (piece instanceof Pawn) c = 'P';
                    else c = '?';
                    sb.append(piece.getColor() == Color.WHITE ? c : Character.toLowerCase(c));
                }
            }
        }

        sb.append(currentPlayer.getColor() == Color.WHITE ? 'w' : 'b');

        King whiteKing = whitePlayer.getKing();
        King blackKing = blackPlayer.getKing();
        if (whiteKing != null && whiteKing.getIsFirstMove()) {
            Square rookH1 = board.getSquare(7, 7);
            if (rookH1.getPiece() instanceof Rook r && r.getColor() == Color.WHITE && r.getIsFirstMove())
                sb.append('K');
            Square rookA1 = board.getSquare(7, 0);
            if (rookA1.getPiece() instanceof Rook r && r.getColor() == Color.WHITE && r.getIsFirstMove())
                sb.append('Q');
        }
        if (blackKing != null && blackKing.getIsFirstMove()) {
            Square rookH8 = board.getSquare(0, 7);
            if (rookH8.getPiece() instanceof Rook r && r.getColor() == Color.BLACK && r.getIsFirstMove())
                sb.append('k');
            Square rookA8 = board.getSquare(0, 0);
            if (rookA8.getPiece() instanceof Rook r && r.getColor() == Color.BLACK && r.getIsFirstMove())
                sb.append('q');
        }

        Move lastMove = board.getLastMove();
        if (lastMove != null && lastMove.hasType(MoveType.PAWN_DOUBLE)) {
            sb.append('e').append(lastMove.getNewSquare().getColumn());
        }

        return sb.toString();
    }

    private boolean isInsufficientMaterial() {
        int whiteMinors = 0;
        int blackMinors = 0;
        Bishop whiteBishop = null;
        Bishop blackBishop = null;

        for (Piece p : whitePlayer.getPieces()) {
            if (p.getSquare() == null || p instanceof King) continue;
            if (p instanceof Bishop b) { whiteMinors++; whiteBishop = b; }
            else if (p instanceof Knight) whiteMinors++;
            else return false;
        }

        for (Piece p : blackPlayer.getPieces()) {
            if (p.getSquare() == null || p instanceof King) continue;
            if (p instanceof Bishop b) { blackMinors++; blackBishop = b; }
            else if (p instanceof Knight) blackMinors++;
            else return false;
        }

        if (whiteMinors == 0 && blackMinors == 0) return true;

        if (whiteMinors == 0 && blackMinors == 1) return true;
        if (blackMinors == 0 && whiteMinors == 1) return true;

        if (whiteMinors == 1 && blackMinors == 1 && whiteBishop != null && blackBishop != null) {
            return whiteBishop.getSquare().getColor() == blackBishop.getSquare().getColor();
        }

        return false;
    }

    public DrawReason getDrawReason() {
        return drawReason;
    }

    public WinReason getWinReason() {
        return winReason;
    }

    public void drawByAgreement() {
        if (gameState != GameState.ACTIVE) return;
        gameState = GameState.DRAW;
        drawReason = DrawReason.AGREEMENT;
    }

    public void resign() {
        if (gameState != GameState.ACTIVE) return;
        gameState = (currentPlayer == whitePlayer) ? GameState.BLACK_WON : GameState.WHITE_WON;
        winReason = WinReason.RESIGN;
    }

    public void passTurn() {
        if (gameState != GameState.ACTIVE) return;
        switchCurrentPlayer();
    }

    private void switchCurrentPlayer() {
        currentPlayer = (currentPlayer == whitePlayer) ? blackPlayer : whitePlayer;
    }

    private Player getPlayerByColor(Color color) {
        return (color == Color.WHITE) ? whitePlayer : blackPlayer;
    }

    public Player getOpponentPlayer(Player player) {
        return (player == whitePlayer) ? blackPlayer : whitePlayer;
    }

    public Board getBoard() {
        return board;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public GameState getGameState() {
        return gameState;
    }

    public GameTimer getGameTimer() {
        return gameTimer;
    }

    private void setupInitialPosition() {
        for (int col = 0; col < 8; col++) {
            Square pawnSquare = board.getSquare(6, col);
            Pawn pawn = new Pawn(Color.WHITE, pawnSquare);
            pawnSquare.setPiece(pawn);
            whitePlayer.addPiece(pawn);

            Square square = board.getSquare(7, col);
            Piece piece = switch (col) {
                case 0, 7 -> new Rook(Color.WHITE, square);
                case 1, 6 -> new Knight(Color.WHITE, square);
                case 2, 5 -> new Bishop(Color.WHITE, square);
                case 3 -> new Queen(Color.WHITE, square);
                case 4 -> new King(Color.WHITE, square);
                default -> null;
            };
            square.setPiece(piece);
            whitePlayer.addPiece(piece);
        }

        for (int col = 0; col < 8; col++) {
            Square pawnSquare = board.getSquare(1, col);
            Pawn pawn = new Pawn(Color.BLACK, pawnSquare);
            pawnSquare.setPiece(pawn);
            blackPlayer.addPiece(pawn);

            Square square = board.getSquare(0, col);
            Piece piece = switch (col) {
                case 0, 7 -> new Rook(Color.BLACK, square);
                case 1, 6 -> new Knight(Color.BLACK, square);
                case 2, 5 -> new Bishop(Color.BLACK, square);
                case 3 -> new Queen(Color.BLACK, square);
                case 4 -> new King(Color.BLACK, square);
                default -> null;
            };
            square.setPiece(piece);
            blackPlayer.addPiece(piece);
        }
    }
}

