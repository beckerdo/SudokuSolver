package info.danbecker.ss;

import org.junit.jupiter.api.Test;

import info.danbecker.ss.Board.Direction;

import static info.danbecker.ss.Board.DIGITS;
import static info.danbecker.ss.Board.ROWCOL;

import org.junit.jupiter.api.BeforeEach;

import java.text.ParseException;

import static info.danbecker.ss.Utils.ROWS;
import static info.danbecker.ss.Utils.COLS;
import static info.danbecker.ss.Utils.BOXES;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {
	public static String BOARD1 = "...1....1\n38.....2.\n.2..96..4\n.4...7...\n9.......3\n...4...5.\n5..67..3.\n.3.....79\n8....2...";
	public static String BOARD2 = "...2....1\n38.....2.\n.2..96..4\n.4...7...\n9.......3\n...4...5.\n5..67..3.\n.3.....79\n8....2...";
	public static String BOARD3 = "...3....1\n38.....2.\n.2..96..4\n.4...7...\n9.......3\n...4...5.\n5..67..3.\n.3.....79\n8....2...";
	public static String COMPLETE = "123456789\n456789123\n789123456\n234567891\n567891234\n891234567\n345678912\n678912345\n912345678";
	public static String NODELIMSTRING = ".3....95...1.....354.89.7.6..32....8.....9...8..7.1...15...72.............9.2.3.7";
	
	@BeforeEach
    public void setup() {
	}
		
	@Test
    public void testBasics() throws ParseException {
		Board board1 = new Board( BOARD1 );
		Board board2 = new Board( BOARD2 );
		Board board3 = new Board( BOARD3 );

		assertEquals(-1, board1.compareTo(null));
		assertEquals( -1, board1.compareTo( board2 )  );
		assertEquals( -1, board1.compareTo( board2 ) );
		assertEquals( 1, board2.compareTo( board1 )  );
		assertEquals( 0, board1.compareTo( board1 )  );
		assertEquals( board2, board2  );

		assertEquals( BOARD3, board3.toString().replace( "0", "."));
		board3.set(ROWCOL[8][8], 0);
		assertTrue( board3.get( ROWCOL[8][8] ) < board3.set( ROWCOL[8][8], 1 ));
		
		Board board4 = new Board( NODELIMSTRING );
		assertNotNull(board4);
    }

	@Test
    public void testCounts() throws ParseException {
		Board board = new Board( BOARD1 );

		assertEquals( 2, board.rowCount( 0, 1 ));
		assertEquals( 0, board.rowCount( 1, 1 ));
		assertEquals( 1, board.colCount( 0, 5 ));
		assertEquals( 0, board.rowCount( 8, 3 ));

	
		assertEquals( 3, board.digitCount( 9 ));
		assertEquals( 2, board.digitCount( 1 ));

		assertFalse( board.digitCompleted( 1 ));
		assertFalse( board.digitCompleted( 9 ));
		assertEquals( 24, board.getOccupiedCount());

		assertFalse( board.comboCompleted( new int[] {0,8} ));
		
		board = new Board( COMPLETE );
		assertTrue( board.digitCompleted( 1 ));
		assertTrue( board.digitCompleted( 9 ));
		assertEquals( ROWS * COLS, board.getOccupiedCount());
		
		assertTrue( board.comboCompleted( new int[] {0,8} ));
}
	// Manually populated boxes, row order, for testing
	public static final RowCol[][] BOXRM = new RowCol[][] {
			new RowCol[] { ROWCOL[0][0], ROWCOL[0][1], ROWCOL[0][2], ROWCOL[1][0], ROWCOL[1][1], ROWCOL[1][2], ROWCOL[2][0], ROWCOL[2][1], ROWCOL[2][2] }, // box 0
			new RowCol[] { ROWCOL[0][3], ROWCOL[0][4], ROWCOL[0][5], ROWCOL[1][3], ROWCOL[1][4], ROWCOL[1][5], ROWCOL[2][3], ROWCOL[2][4], ROWCOL[2][5] }, // box 1
			new RowCol[] { ROWCOL[0][6], ROWCOL[0][7], ROWCOL[0][8], ROWCOL[1][6], ROWCOL[1][7], ROWCOL[1][8], ROWCOL[2][6], ROWCOL[2][7], ROWCOL[2][8] }, // box 2
			new RowCol[] { ROWCOL[3][0], ROWCOL[3][1], ROWCOL[3][2], ROWCOL[4][0], ROWCOL[4][1], ROWCOL[4][2], ROWCOL[5][0], ROWCOL[5][1], ROWCOL[5][2] },
			new RowCol[] { ROWCOL[3][3], ROWCOL[3][4], ROWCOL[3][5], ROWCOL[4][3], ROWCOL[4][4], ROWCOL[4][5], ROWCOL[5][3], ROWCOL[5][4], ROWCOL[5][5] },
			new RowCol[] { ROWCOL[3][6], ROWCOL[3][7], ROWCOL[3][8], ROWCOL[4][6], ROWCOL[4][7], ROWCOL[4][8], ROWCOL[5][6], ROWCOL[5][7], ROWCOL[5][8] },
			new RowCol[] { ROWCOL[6][0], ROWCOL[6][1], ROWCOL[6][2], ROWCOL[7][0], ROWCOL[7][1], ROWCOL[7][2], ROWCOL[8][0], ROWCOL[8][1], ROWCOL[8][2] },
			new RowCol[] { ROWCOL[6][3], ROWCOL[6][4], ROWCOL[6][5], ROWCOL[7][3], ROWCOL[7][4], ROWCOL[7][5], ROWCOL[8][3], ROWCOL[8][4], ROWCOL[8][5] },
			new RowCol[] { ROWCOL[6][6], ROWCOL[6][7], ROWCOL[6][8], ROWCOL[7][6], ROWCOL[7][7], ROWCOL[7][8], ROWCOL[8][6], ROWCOL[8][7], ROWCOL[8][8] }, // box 8
	};
	// Manually populated boxes, col order, for testing
	public static final RowCol[][] BOXCM = new RowCol[][] {
			new RowCol[] { ROWCOL[0][0], ROWCOL[1][0], ROWCOL[2][0], ROWCOL[0][1], ROWCOL[1][1], ROWCOL[2][1], ROWCOL[0][2], ROWCOL[1][2], ROWCOL[2][2] }, // box 0
			new RowCol[] { ROWCOL[0][3], ROWCOL[1][3], ROWCOL[2][3], ROWCOL[0][4], ROWCOL[1][4], ROWCOL[2][4], ROWCOL[0][5], ROWCOL[1][5], ROWCOL[2][5] }, // box 1
			new RowCol[] { ROWCOL[0][6], ROWCOL[1][6], ROWCOL[2][6], ROWCOL[0][7], ROWCOL[1][7], ROWCOL[2][7], ROWCOL[0][8], ROWCOL[1][8], ROWCOL[2][8] }, // box 2
			new RowCol[] { ROWCOL[3][0], ROWCOL[4][0], ROWCOL[5][0], ROWCOL[3][1], ROWCOL[4][1], ROWCOL[5][1], ROWCOL[3][2], ROWCOL[4][2], ROWCOL[5][2] },
			new RowCol[] { ROWCOL[3][3], ROWCOL[4][3], ROWCOL[5][3], ROWCOL[3][4], ROWCOL[4][4], ROWCOL[5][4], ROWCOL[3][5], ROWCOL[4][5], ROWCOL[5][5] },
			new RowCol[] { ROWCOL[3][6], ROWCOL[4][6], ROWCOL[5][6], ROWCOL[3][7], ROWCOL[4][7], ROWCOL[5][7], ROWCOL[3][8], ROWCOL[4][8], ROWCOL[5][8] },
			new RowCol[] { ROWCOL[6][0], ROWCOL[7][0], ROWCOL[8][0], ROWCOL[6][1], ROWCOL[7][1], ROWCOL[8][1], ROWCOL[6][2], ROWCOL[7][2], ROWCOL[8][2] },
			new RowCol[] { ROWCOL[6][3], ROWCOL[7][3], ROWCOL[8][3], ROWCOL[6][4], ROWCOL[7][4], ROWCOL[8][4], ROWCOL[6][5], ROWCOL[7][5], ROWCOL[8][5] },
			new RowCol[] { ROWCOL[6][6], ROWCOL[7][6], ROWCOL[8][6], ROWCOL[6][7], ROWCOL[7][7], ROWCOL[8][7], ROWCOL[6][8], ROWCOL[7][8], ROWCOL[8][8] }, // box 8
	};

	@Test
	public void testBoxes() {
		// Test boxes, row order
		for ( int boxi = 0; boxi < BOXES; boxi++ ) {
			for ( int celli = 0; celli < ROWS; celli++ ) {
				// System.out.println( format( "Box %d, cell %d, expect %s, found %s",
				// 	boxi, celli, BOXCM[boxi][celli], Board.BOXC[boxi][celli] ));
				assertEquals( BOXRM[boxi][celli], Board.BOXR[boxi][celli]);
				assertEquals( BOXCM[boxi][celli], Board.BOXC[boxi][celli]);
			}
		}

		// Makes sure each block returns list of boxes, and row and col correctly returns block.
		for( int boxi = 0; boxi < BOXES; boxi++ ) {
			RowCol[] boxes = Board.getBoxRowCols(boxi);
			// System.out.println( format("Blocki=%d, row,cols=%s", blocki, Arrays.toString( boxes )) );
			assertEquals( BOXES, boxes.length );
			for( int celli = 0; celli < BOXES; celli++ ) {
				// System.out.println( format("Blocki=%d, boxi=%d, row,col=[%d,%d]", blocki, boxi, rowi, coli ));
				assertEquals( boxi, boxes[celli].box());
			}
		}
		// Makes sure each block returns list of boxes, and row and col correctly returns block.
		for( int boxi = 0; boxi < BOXES; boxi++ ) {
			RowCol[] boxes = Board.getBoxRowColsC(boxi);
			// System.out.println( format("Blocki=%d, row,cols=%s", blocki, Arrays.toString( boxes )) );
			assertEquals( BOXES, boxes.length );
			for( int celli = 0; celli < BOXES; celli++ ) {
				// System.out.println( java.lang.String.format("Blocki=%d, boxi=%d, row,col=[%d,%d]", blocki, boxi, rowi, coli ));
				assertEquals( boxi, boxes[celli].box());
			}
		}
		// Make sure every location (row sorted) is in location set (col sorted)
		for( int boxi = 0; boxi < BOXES; boxi++ ) {
			RowCol[] boxes = Board.getBoxRowCols(boxi);
			RowCol[] boxesC = Board.getBoxRowColsC(boxi);
			// System.out.println( format("Blocki=%d, row,cols=%s", blocki, Arrays.toString( boxes )) );
			assertEquals( BOXES, boxes.length );
			for( int celli = 0; celli < BOXES; celli++ ) {
				// System.out.println( java.lang.String.format("Blocki=%d, boxi=%d, rowC,colC=[%d,%d]", blocki, boxi, rowiC, coliC ));
				assertEquals( boxes[celli].row() + boxes[celli].col(), boxesC[celli].row() + boxesC[celli].col() );
			}
		}
	}

	@Test
    public void testRotations() throws ParseException {
		Board origin = new Board(BOARD1);

		Board rotateRight = Board.rotateRight( origin );		
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++ ) {
				assertEquals( origin.get( ROWCOL[rowi][coli] ), rotateRight.get( ROWCOL[coli][COLS-rowi-1]));
			}
		}
		Board rotateLeft = Board.rotateLeft( origin );		
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++ ) {
				assertEquals( origin.get( ROWCOL[rowi][coli] ), rotateLeft.get( ROWCOL[ROWS-coli-1][rowi]));
			}
		}
		// Double rotation
		rotateRight = Board.rotateRight( rotateRight );
		rotateLeft = Board.rotateLeft( rotateLeft );
		assertEquals( rotateRight, rotateLeft );
		// Quad rotation
		rotateRight = Board.rotateRight( rotateRight );
		rotateRight = Board.rotateRight( rotateRight );
		rotateLeft = Board.rotateLeft( rotateLeft );
		rotateLeft = Board.rotateLeft( rotateLeft );
		assertEquals( origin, rotateRight );
		assertEquals( origin, rotateLeft );
	}
	
	@Test
    public void testMirrors() throws ParseException {
		Board origin = new Board(BOARD1);

		Board mirrorUpDown = Board.mirror( origin, Direction.UP_DOWN );		
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++ ) {
				assertEquals( origin.get( ROWCOL[rowi][coli] ), mirrorUpDown.get(ROWCOL[ROWS-rowi-1][coli]));
			}
		}
		Board mirrorRightLeft = Board.mirror( origin, Direction.RIGHT_LEFT );		
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++ ) {
				assertEquals( origin.get( ROWCOL[rowi][coli] ), mirrorRightLeft.get(ROWCOL[rowi][ COLS-coli-1]));
			}
		}
		// Double mirror
		mirrorUpDown = Board.mirror( mirrorUpDown, Direction.UP_DOWN );
		assertEquals( origin, mirrorUpDown );
		mirrorRightLeft = Board.mirror( mirrorRightLeft, Direction.RIGHT_LEFT );
		assertEquals( origin, mirrorRightLeft );
	}
	
	@Test
	public void testTaken() throws ParseException {
		// Tests rowTake, colTaken, blockTaken
		Board board3 = new Board( BOARD3 );

		// Taken
		assertTrue( board3.rowTaken( 0, 3));
		assertTrue( board3.colTaken( 4, 7));
		assertTrue( board3.boxTaken( 8, 9));

		// Not Taken
		assertFalse( board3.rowTaken( 0, 5));
		assertFalse( board3.colTaken( 4, 1));
		assertFalse( board3.boxTaken( 8, 8));		
	}
	
	@Test
    public void testLegalComplete() throws ParseException {
		Board complete = new Board( COMPLETE );
		// System.out.println( "Board=" + complete);
		assertTrue( complete.completed() );
		assertTrue( complete.legal() );
		
		complete.set( ROWCOL[8][8], 0 );
		// System.out.println( "Board=" + complete);
		assertFalse( complete.completed() );
		assertTrue( complete.legal() );

		complete.set( ROWCOL[8][8], 9 );
		// System.out.println( "Board=" + complete);
		assertTrue( complete.completed() );
		assertFalse( complete.legal() );
	}

}