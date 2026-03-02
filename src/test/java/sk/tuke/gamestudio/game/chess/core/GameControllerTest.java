package sk.tuke.gamestudio.game.chess.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sk.tuke.gamestudio.game.chess.core.pieces.*;

import static org.junit.jupiter.api.Assertions.*;

public class GameControllerTest {

    private GameController controller;
    private Board board;

    @BeforeEach
    public void setUp() {
        controller = new GameController();
        board = controller.getBoard();
    }

    @Test
    public void testInitialGameStateIsActive() {
        assertEquals(GameState.ACTIVE, controller.getGameState());
    }

    @Test
    public void testInitialCurrentPlayerIsWhite() {
        assertEquals(Color.WHITE, controller.getCurrentPlayer().getColor());
    }

    @Test
    public void testWhiteHas16Pieces() {
        assertEquals(16, controller.getWhitePlayer().getPieces().size());
    }

    @Test
    public void testBlackHas16Pieces() {
        assertEquals(16, controller.getBlackPlayer().getPieces().size());
    }

    @Test
    public void testWhiteKingPosition() {
        King king = controller.getWhitePlayer().getKing();
        assertNotNull(king);
        assertEquals(7, king.getSquare().getRow());
        assertEquals(4, king.getSquare().getColumn());
    }

    @Test
    public void testBlackKingPosition() {
        King king = controller.getBlackPlayer().getKing();
        assertNotNull(king);
        assertEquals(0, king.getSquare().getRow());
        assertEquals(4, king.getSquare().getColumn());
    }

    @Test
    public void testWhitePawnsOnRow6() {
        for (int col = 0; col < 8; col++) {
            Piece piece = board.getSquare(6, col).getPiece();
            assertNotNull(piece);
            assertInstanceOf(Pawn.class, piece);
            assertEquals(Color.WHITE, piece.getColor());
        }
    }

    @Test
    public void testBlackPawnsOnRow1() {
        for (int col = 0; col < 8; col++) {
            Piece piece = board.getSquare(1, col).getPiece();
            assertNotNull(piece);
            assertInstanceOf(Pawn.class, piece);
            assertEquals(Color.BLACK, piece.getColor());
        }
    }

    @Test
    public void testWhiteBackRankPieces() {
        assertInstanceOf(Rook.class, board.getSquare(7, 0).getPiece());
        assertInstanceOf(Knight.class, board.getSquare(7, 1).getPiece());
        assertInstanceOf(Bishop.class, board.getSquare(7, 2).getPiece());
        assertInstanceOf(Queen.class, board.getSquare(7, 3).getPiece());
        assertInstanceOf(King.class, board.getSquare(7, 4).getPiece());
        assertInstanceOf(Bishop.class, board.getSquare(7, 5).getPiece());
        assertInstanceOf(Knight.class, board.getSquare(7, 6).getPiece());
        assertInstanceOf(Rook.class, board.getSquare(7, 7).getPiece());
    }

    @Test
    public void testBlackBackRankPieces() {
        assertInstanceOf(Rook.class, board.getSquare(0, 0).getPiece());
        assertInstanceOf(Knight.class, board.getSquare(0, 1).getPiece());
        assertInstanceOf(Bishop.class, board.getSquare(0, 2).getPiece());
        assertInstanceOf(Queen.class, board.getSquare(0, 3).getPiece());
        assertInstanceOf(King.class, board.getSquare(0, 4).getPiece());
        assertInstanceOf(Bishop.class, board.getSquare(0, 5).getPiece());
        assertInstanceOf(Knight.class, board.getSquare(0, 6).getPiece());
        assertInstanceOf(Rook.class, board.getSquare(0, 7).getPiece());
    }

    @Test
    public void testEmptySquaresInMiddle() {
        for (int row = 2; row <= 5; row++) {
            for (int col = 0; col < 8; col++) {
                assertNull(board.getSquare(row, col).getPiece());
            }
        }
    }

    @Test
    public void testBasicPawnMove() {
        Piece pawn = board.getSquare(6, 4).getPiece();
        boolean result = controller.movePiece(pawn, board.getSquare(5, 4));
        assertTrue(result);
        assertNull(board.getSquare(6, 4).getPiece());
        assertEquals(pawn, board.getSquare(5, 4).getPiece());
    }

    @Test
    public void testPawnDoubleMove() {
        Piece pawn = board.getSquare(6, 4).getPiece();
        boolean result = controller.movePiece(pawn, board.getSquare(4, 4));
        assertTrue(result);
        assertNull(board.getSquare(6, 4).getPiece());
        assertEquals(pawn, board.getSquare(4, 4).getPiece());
    }

    @Test
    public void testCannotMoveOpponentPiece() {
        Piece blackPawn = board.getSquare(1, 4).getPiece();
        boolean result = controller.movePiece(blackPawn, board.getSquare(3, 4));
        assertFalse(result);
    }

    @Test
    public void testInvalidMoveRejected() {
        Piece pawn = board.getSquare(6, 4).getPiece();
        boolean result = controller.movePiece(pawn, board.getSquare(3, 4));
        assertFalse(result);
    }

    @Test
    public void testTurnSwitchesAfterMove() {
        Piece pawn = board.getSquare(6, 4).getPiece();
        controller.movePiece(pawn, board.getSquare(4, 4));
        assertEquals(Color.BLACK, controller.getCurrentPlayer().getColor());
    }

    @Test
    public void testCaptureRemovesPiece() {
        controller.movePiece(board.getSquare(6, 4).getPiece(), board.getSquare(4, 4));
        controller.movePiece(board.getSquare(1, 3).getPiece(), board.getSquare(3, 3));
        Piece whitePawn = board.getSquare(4, 4).getPiece();
        boolean result = controller.movePiece(whitePawn, board.getSquare(3, 3));
        assertTrue(result);
        assertEquals(whitePawn, board.getSquare(3, 3).getPiece());
        assertEquals(15, controller.getBlackPlayer().getPieces().size());
    }

    @Test
    public void testCheckDetection() {
        controller.movePiece(board.getSquare(6, 4).getPiece(), board.getSquare(4, 4));
        controller.movePiece(board.getSquare(1, 5).getPiece(), board.getSquare(3, 5));
        controller.movePiece(board.getSquare(7, 3).getPiece(), board.getSquare(3, 7));
        assertTrue(controller.isInCheck(Color.BLACK));
        assertEquals(GameState.ACTIVE, controller.getGameState());
    }

    @Test
    public void testMustMoveOutOfCheck() {
        controller.movePiece(board.getSquare(6, 4).getPiece(), board.getSquare(4, 4));
        controller.movePiece(board.getSquare(1, 5).getPiece(), board.getSquare(3, 5));
        controller.movePiece(board.getSquare(7, 3).getPiece(), board.getSquare(3, 7));
        assertTrue(controller.isInCheck(Color.BLACK));

        Piece a7Pawn = board.getSquare(1, 0).getPiece();
        assertFalse(controller.movePiece(a7Pawn, board.getSquare(2, 0)));

        Piece g7Pawn = board.getSquare(1, 6).getPiece();
        assertTrue(controller.movePiece(g7Pawn, board.getSquare(2, 6)));
        assertFalse(controller.isInCheck(Color.BLACK));
    }

    @Test
    public void testFoolsMateCheckmate() {
        controller.movePiece(board.getSquare(6, 5).getPiece(), board.getSquare(4, 5));
        controller.movePiece(board.getSquare(1, 4).getPiece(), board.getSquare(3, 4));
        controller.movePiece(board.getSquare(6, 6).getPiece(), board.getSquare(4, 6));
        controller.movePiece(board.getSquare(0, 3).getPiece(), board.getSquare(4, 7));
        assertEquals(GameState.BLACK_WON, controller.getGameState());
    }

    @Test
    public void testScholarsMateCheckmate() {
        controller.movePiece(board.getSquare(6, 4).getPiece(), board.getSquare(4, 4));
        controller.movePiece(board.getSquare(1, 4).getPiece(), board.getSquare(3, 4));
        controller.movePiece(board.getSquare(7, 5).getPiece(), board.getSquare(4, 2));
        controller.movePiece(board.getSquare(0, 1).getPiece(), board.getSquare(2, 2));
        controller.movePiece(board.getSquare(7, 3).getPiece(), board.getSquare(3, 7));
        controller.movePiece(board.getSquare(0, 6).getPiece(), board.getSquare(2, 5));
        controller.movePiece(board.getSquare(3, 7).getPiece(), board.getSquare(1, 5));
        assertEquals(GameState.WHITE_WON, controller.getGameState());
    }

    @Test
    public void testCannotMovePinnedPiece() {
        controller.movePiece(board.getSquare(6, 4).getPiece(), board.getSquare(4, 4));
        controller.movePiece(board.getSquare(1, 4).getPiece(), board.getSquare(3, 4));
        controller.movePiece(board.getSquare(7, 6).getPiece(), board.getSquare(5, 5));
        controller.movePiece(board.getSquare(0, 5).getPiece(), board.getSquare(4, 1));

        Piece d2Pawn = board.getSquare(6, 3).getPiece();
        assertFalse(controller.movePiece(d2Pawn, board.getSquare(5, 3)));
        assertFalse(controller.movePiece(d2Pawn, board.getSquare(4, 3)));
    }

    @Test
    public void testKnightMove() {
        Piece knight = board.getSquare(7, 1).getPiece();
        boolean result = controller.movePiece(knight, board.getSquare(5, 2));
        assertTrue(result);
        assertNull(board.getSquare(7, 1).getPiece());
        assertEquals(knight, board.getSquare(5, 2).getPiece());
    }

    @Test
    public void testCannotCaptureOwnPiece() {
        Piece rook = board.getSquare(7, 0).getPiece();
        boolean result = controller.movePiece(rook, board.getSquare(6, 0));
        assertFalse(result);
    }

    @Test
    public void testGameNotOverAfterOneMove() {
        controller.movePiece(board.getSquare(6, 4).getPiece(), board.getSquare(4, 4));
        assertEquals(GameState.ACTIVE, controller.getGameState());
    }

    @Test
    public void testNoCheckAtStart() {
        assertFalse(controller.isInCheck(Color.WHITE));
        assertFalse(controller.isInCheck(Color.BLACK));
    }
}
