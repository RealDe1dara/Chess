package sk.tuke.gamestudio.game.chess.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    private final Board board = new Board();

    @Test
    public void testBoardHas64Squares() {
        int count = 0;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (board.getSquare(row, col) != null) {
                    count++;
                }
            }
        }
        assertEquals(64, count);
    }

    @Test
    public void testAllSquaresNotNull() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                assertNotNull(board.getSquare(row, col));
            }
        }
    }

    @Test
    public void testSquareColorsAlternate() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Square square = board.getSquare(row, col);
                if ((row + col) % 2 == 0) {
                    assertEquals(Color.WHITE, square.getColor());
                } else {
                    assertEquals(Color.BLACK, square.getColor());
                }
            }
        }
    }

    @Test
    public void testOutOfBoundsReturnsNull() {
        assertNull(board.getSquare(-1, 0));
        assertNull(board.getSquare(0, -1));
        assertNull(board.getSquare(8, 0));
        assertNull(board.getSquare(0, 8));
        assertNull(board.getSquare(-1, -1));
        assertNull(board.getSquare(8, 8));
    }

    @Test
    public void testSquareCoordinates() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Square square = board.getSquare(row, col);
                assertEquals(row, square.getRow());
                assertEquals(col, square.getColumn());
            }
        }
    }

    @Test
    public void testInitialBoardHasNoPieces() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                assertNull(board.getSquare(row, col).getPiece());
            }
        }
    }

    @Test
    public void testLastMoveInitiallyNull() {
        assertNull(board.getLastMove());
    }
}
