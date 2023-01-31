package info.danbecker.ss;

import org.junit.jupiter.api.Test;

import info.danbecker.ss.Board.Direction;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.text.ParseException;

import static info.danbecker.ss.Utils.ROWS;
import static info.danbecker.ss.Utils.COLS;
import static info.danbecker.ss.Utils.BOXES;

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

		assertTrue( board1.compareTo( null ) == -1  );
		assertTrue( board1.compareTo( board2 ) == -1  );
		assertTrue( board2.compareTo( board1 ) == 1  );
		assertTrue( board1.compareTo( board1 ) == 0  );
		assertEquals( board2, board2  );

		assertEquals( BOARD3, board3.toString().replace( "0", "."));
		board3.set(8, 8, 0);
		assertTrue( board3.get( 8, 8 ) < board3.set( 8, 8, 1 ));
		
		Board board4 = new Board( NODELIMSTRING );
		assertTrue( null != board4 );
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
		
		assertFalse( board.comboCompleted( new int[] {0,8} ));
		
		board = new Board( COMPLETE );
		assertTrue( board.digitCompleted( 1 ));
		assertTrue( board.digitCompleted( 9 ));
		
		assertTrue( board.comboCompleted( new int[] {0,8} ));
}
	
	@Test
    public void testBlocks() {
		// Makes sure each block returns list of boxes, and row and col correctly returns block.
		for( int boxi = 0; boxi < BOXES; boxi++ ) {
			int [][] boxes = Board.getBoxRowCols(boxi);
			// System.out.println( format("Blocki=%d, row,cols=%s", blocki, Arrays.toString( boxes )) );
			assertEquals( BOXES, boxes.length );
			for( int celli = 0; celli < BOXES; celli++ ) {
				int rowi = boxes[celli][0];
				int coli = boxes[celli][1];
				// System.out.println( format("Blocki=%d, boxi=%d, row,col=[%d,%d]", blocki, boxi, rowi, coli ));
				assertEquals( boxi, Board.getBox(rowi, coli));				
			}
		}
		// Makes sure each block returns list of boxes, and row and col correctly returns block.
		for( int boxi = 0; boxi < BOXES; boxi++ ) {
			int [][] boxes = Board.getBoxRowColsC(boxi);
			// System.out.println( format("Blocki=%d, row,cols=%s", blocki, Arrays.toString( boxes )) );
			assertEquals( BOXES, boxes.length );
			for( int celli = 0; celli < BOXES; celli++ ) {
				int rowi = boxes[celli][0];
				int coli = boxes[celli][1];
				// System.out.println( java.lang.String.format("Blocki=%d, boxi=%d, row,col=[%d,%d]", blocki, boxi, rowi, coli ));
				assertEquals( boxi, Board.getBox(rowi, coli));				
			}
		}
		// Make sure every location (row sorted) is in location set (col sorted)
		for( int boxi = 0; boxi < BOXES; boxi++ ) {
			int [][] boxes = Board.getBoxRowCols(boxi);
			int [][] boxesC = Board.getBoxRowColsC(boxi);
			// System.out.println( format("Blocki=%d, row,cols=%s", blocki, Arrays.toString( boxes )) );
			assertEquals( BOXES, boxes.length );
			for( int celli = 0; celli < BOXES; celli++ ) {
				int rowi = boxes[celli][0];
				int coli = boxes[celli][1];
				// System.out.println( java.lang.String.format("Blocki=%d, boxi=%d, row,col=[%d,%d]", blocki, boxi, rowi, coli ));
				int rowiC = boxesC[celli][0];
				int coliC = boxesC[celli][1];
				// System.out.println( java.lang.String.format("Blocki=%d, boxi=%d, rowC,colC=[%d,%d]", blocki, boxi, rowiC, coliC ));								
				assertEquals( rowi + coli, rowiC + coliC );				
			}
		}
	}

	@Test
    public void testRotations() throws ParseException {
		Board origin = new Board(BOARD1);

		Board rotateRight = Board.rotateRight( origin );		
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++ ) {
				assertEquals( origin.get( rowi, coli ), rotateRight.get(coli, COLS-rowi-1));
			}
		}
		Board rotateLeft = Board.rotateLeft( origin );		
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++ ) {
				assertEquals( origin.get( rowi, coli ), rotateLeft.get(ROWS-coli-1, rowi));
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
				assertEquals( origin.get( rowi, coli ), mirrorUpDown.get(ROWS-rowi-1, coli));
			}
		}
		Board mirrorRightLeft = Board.mirror( origin, Direction.RIGHT_LEFT );		
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++ ) {
				assertEquals( origin.get( rowi, coli ), mirrorRightLeft.get(rowi, COLS-coli-1));
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
		
		complete.set( 8, 8, 0 ); 
		// System.out.println( "Board=" + complete);
		assertFalse( complete.completed() );
		assertTrue( complete.legal() );

		complete.set( 8, 8, 9 ); 
		// System.out.println( "Board=" + complete);
		assertTrue( complete.completed() );
		assertFalse( complete.legal() );
	}

}