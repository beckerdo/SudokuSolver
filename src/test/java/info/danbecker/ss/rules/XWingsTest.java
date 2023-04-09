package info.danbecker.ss.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import java.text.ParseException;
import java.util.List;

public class XWingsTest {
	// https://www.sudokuoftheday.com/techniques/x-wings
	// This puzzle appears to have additional candidates removed
	public static String XWINGS = 
	"9...5173.-1.73982.5-5...76.91-81.72435.-2..165..7-.75983.12-.21537...-758649123-39.81257.";

	@BeforeEach
	public void setup() {
	}

	@Test
	public void testEncode() {
		// Middle box double pair
		int digit = 1;	
		int row = 0;
		int[][] firstRow = new int[][] { new int []{3,3}, new int[]{3,5} };
		int[][] secondRow = new int[][] { new int []{5,3}, new int[] {5,5}};
		int[][] mismatch = new int[][] { new int []{2,2}, new int[]{7,7} };
		
		Exception e = assertThrows(IllegalArgumentException.class, ()->{
			XWings.encode( -1, row, null, secondRow);
		});
		assertTrue( e.getMessage().contains("digit=") );
		e = assertThrows(IllegalArgumentException.class, ()->{
			XWings.encode( digit, 2, null, secondRow);
		});
		assertTrue( e.getMessage().contains("rowCol=") );
		e = assertThrows(IllegalArgumentException.class, ()->{
			XWings.encode( digit, row, null, secondRow);
		});
		assertTrue( e.getMessage().contains("null") );
		e = assertThrows(IllegalArgumentException.class, ()->{
			XWings.encode( digit, row, firstRow, null);
		});
		assertTrue( e.getMessage().contains("null") );
		e = assertThrows(IllegalArgumentException.class, ()->{
			XWings.encode( digit, row, firstRow, mismatch);
		});
		assertTrue( e.getMessage().contains("mismatch") );
		e = assertThrows(IllegalArgumentException.class, ()->{
			XWings.encode( digit, row, mismatch, secondRow);
		});
		assertTrue( e.getMessage().contains("mismatch") );
		
		int [] encode = XWings.encode( digit, row, firstRow, secondRow);
		assertEquals( digit, encode[ 0 ] );
		assertEquals( row, encode[ 1 ] );

		assertEquals( firstRow[0][0], encode[ 2 ] );
		assertEquals( firstRow[0][1], encode[ 3 ] );
		assertEquals( firstRow[1][0], encode[ 4 ] );
		assertEquals( firstRow[1][1], encode[ 5 ] );

		assertEquals( secondRow[0][0], encode[ 6 ] );
		assertEquals( secondRow[0][1], encode[ 7 ] );
		assertEquals( secondRow[1][0], encode[ 8 ] );
		assertEquals( secondRow[1][1], encode[ 9 ] );
		
		int col = 1;
		int[][] firstCol = new int[][] { new int []{2,0}, new int[]{8,0} };
		int[][] secondCol = new int[][] { new int []{2,5}, new int[] {8,5}};
		
		e = assertThrows(IllegalArgumentException.class, ()->{
			XWings.encode( -1, col, null, secondCol);
		});
		assertTrue( e.getMessage().contains("digit=") );
		e = assertThrows(IllegalArgumentException.class, ()->{
			XWings.encode( digit, 2, null, secondCol);
		});
		assertTrue( e.getMessage().contains("rowCol=") );
		e = assertThrows(IllegalArgumentException.class, ()->{
			XWings.encode( digit, col, null, secondCol);
		});
		assertTrue( e.getMessage().contains("null") );
		e = assertThrows(IllegalArgumentException.class, ()->{
			XWings.encode( digit, col, firstCol, null);
		});
		assertTrue( e.getMessage().contains("null") );
		e = assertThrows(IllegalArgumentException.class, ()->{
			XWings.encode( digit, col, firstCol, mismatch);
		});
		assertTrue( e.getMessage().contains("mismatch") );
		e = assertThrows(IllegalArgumentException.class, ()->{
			XWings.encode( digit, col, mismatch, secondCol);
		});
		assertTrue( e.getMessage().contains("mismatch") );
		
		encode = XWings.encode( digit, col, firstCol, secondCol);
		assertEquals( digit, encode[ 0 ] );
		assertEquals( col, encode[ 1 ] );

		assertEquals( firstCol[0][0], encode[ 2 ] );
		assertEquals( firstCol[0][1], encode[ 3 ] );
		assertEquals( firstCol[1][0], encode[ 4 ] );
		assertEquals( firstCol[1][1], encode[ 5 ] );

		assertEquals( secondCol[0][0], encode[ 6 ] );
		assertEquals( secondCol[0][1], encode[ 7 ] );
		assertEquals( secondCol[1][0], encode[ 8 ] );
		assertEquals( secondCol[1][1], encode[ 9 ] );		
	}
	
	@Test
	public void testRowMatch() throws ParseException {
		Board board = new Board(XWINGS);
		assertTrue(board.legal());

		UpdateCandidatesRule rule = new XWings();

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=" + candidates.toStringCompact());

		// Locations test
		List<int[]> encodings = rule.locations(board, candidates);
		assertTrue(null != encodings);
		assertEquals(2, encodings.size());
		// XWings found digit 6 row at rowCols=[3,2],[3,8] and rowCols=[8,2],[8,8]
		// XWings found digit 6 col at rowCols=[5,0],[6,0] and rowCols=[5,6],[6,6]
		int[] first = encodings.get(0);
		assertEquals(6, first[0]);
		assertEquals(0, first[1]);
		assertEquals(3, first[2]);
		assertEquals(2, first[3]);
		assertEquals(8, first[8]);
		assertEquals(8, first[9]);
		
        // Update test
        int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		// XWings removed 3 digit 6 row candidates not in rowCols [3,2] [8,8]
		rule.updateCandidates(board, null, candidates, encodings);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertTrue( prevCandidates > candidates.getAllCandidateCount());
		
		encodings = rule.locations(board, candidates);
		assertTrue(null != encodings);
		assertEquals(1, encodings.size());
		// XWings found digit 6 col at rowCols=[5,0],[6,0] and rowCols=[5,6],[6,6]
		first = encodings.get(0);
		assertEquals(6, first[0]);
		assertEquals(1, first[1]);
		assertEquals(5, first[2]);
		assertEquals(0, first[3]);
		assertEquals(6, first[8]);
		assertEquals(6, first[9]);
		
        // Update test
        prevEntries = candidates.getAllOccupiedCount();
		prevCandidates = candidates.getAllCandidateCount();
		// XWings removed 1 digit 6 col candidates not in rowCols [5,0] [6,6]
		rule.updateCandidates(board, null, candidates, encodings);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertTrue( prevCandidates > candidates.getAllCandidateCount());
	}
	
	@Test
	public void testColMatch() throws ParseException {
		Board board = new Board(XWINGS);
		assertTrue(board.legal());

		UpdateCandidatesRule rule = new XWings();
		
		// Try a column search
		board = Board.rotateRight(board);
		assertTrue(board.legal());
		
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=" + candidates.toStringCompact());

		List<int[]> encodings = rule.locations(board, candidates);
		assertTrue(null != encodings);
		assertEquals(2, encodings.size());
		// XWings found digit 6 row at rowCols=[0,2],[0,3] and rowCols=[6,2],[6,3]
		// XWings found digit 6 col at rowCols=[2,0],[8,0] and rowCols=[2,5],[8,5]
		int[] first = encodings.get(0);
   		assertEquals(6, first[0]);
   		assertEquals(0, first[1]);
   		assertEquals(0, first[2]);
  		assertEquals(2, first[3]);
        assertEquals(6, first[8]);
        assertEquals(3, first[9]);
		
        // Update test
        int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		// XWings removed 2 digit 6 row candidates not in rowCols [0,2] [6,3]
		rule.updateCandidates(board, null, candidates, encodings);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertTrue( prevCandidates > candidates.getAllCandidateCount());

		encodings = rule.locations(board, candidates);
		assertTrue(null != encodings);
		assertEquals(1, encodings.size());
		// XWings found digit 6 col at rowCols=[2,0],[8,0] and rowCols=[2,5],[8,5]
		first = encodings.get(0);
   		assertEquals(6, first[0]);
   		assertEquals(1, first[1]);
   		assertEquals(2, first[2]);
  		assertEquals(0, first[3]);
        assertEquals(8, first[8]);
        assertEquals(5, first[9]);
		
        // Update test
        prevEntries = candidates.getAllOccupiedCount();
		prevCandidates = candidates.getAllCandidateCount();
		rule.updateCandidates(board, null, candidates, encodings);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertTrue( prevCandidates > candidates.getAllCandidateCount());
	}

	// https://www.sudokuoftheday.com/dailypuzzles/2023-01-03/diabolical/solver
	public static String XWINGS_20230103=
	"18.2..6.3-924376851-..6.1..42-.6892351.-21....369-..9.6128.-.9268.135-851.32..6-6.3195.28";
	@Test
	public void test20230103() throws ParseException {
		// Set up
		Board board = new Board(XWINGS_20230103);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// Test rule
		UpdateCandidatesRule rule = new XWings();
		assertEquals(rule.ruleName(), "XWings" );
		// Locations test
		List<int[]> locs = rule.locations(board, candidates);
		assertTrue(null != locs);
		assertEquals( 2, locs.size());
		// System.out.println( "Locs=" + Utils.locationsString(locs));
		// Update test
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		rule.updateCandidates(board, null, candidates, locs);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertEquals( prevCandidates, candidates.getAllCandidateCount() + 1);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
	}

	@Test
	public void test20230103rotatedLeft() throws ParseException {
		// Set up
		Board board = new Board(XWINGS_20230103);
		assertTrue(board.legal());
		board = Board.rotateLeft(board);
		assertTrue(board.legal());	
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// Test rule
		UpdateCandidatesRule rule = new XWings();
		assertEquals(rule.ruleName(), "XWings" );
		// Locations test
		List<int[]> locs = rule.locations(board, candidates);
		assertTrue(null != locs);
		assertEquals( 2, locs.size());
		// System.out.println( "Locs=" + Utils.locationsString(locs));
		// Update test
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		rule.updateCandidates(board, null, candidates, locs);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertEquals( prevCandidates, candidates.getAllCandidateCount() + 1);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
	}
	
	public static String XWINGS_20230108_DIGIT7_ROWS26_COLS78 =
	"75.8.....-8...7.2.5-..39.54..-..8547..6-..5.81...-1..39.5..-5824.93..-..1.5.824-.....8.59";			
	@Test
	public void test20230108() throws ParseException {
		Board board = new Board(XWINGS_20230108_DIGIT7_ROWS26_COLS78);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);	
		// Need to update candidates to get it into the proper state
   	    candidates.removeCandidates( ROWCOL[0][4], new int[]{1,6});
   	    candidates.removeCandidates( ROWCOL[0][5], new int[]{6});
   	    candidates.removeCandidates( ROWCOL[1][1], new int[]{9});
   	    candidates.removeCandidates( ROWCOL[1][5], new int[]{6});
   	    candidates.removeCandidates( ROWCOL[1][7], new int[]{1});
   	    candidates.removeCandidates( ROWCOL[2][4], new int[]{2});
   	    candidates.removeCandidates( ROWCOL[2][7], new int[]{1,6});
   	    candidates.removeCandidates( ROWCOL[2][8], new int[]{1});
   	    candidates.removeCandidates( ROWCOL[4][0], new int[]{2});
   	    candidates.removeCandidates( ROWCOL[4][1], new int[]{2});
   	    candidates.removeCandidates( ROWCOL[5][1], new int[]{2});
   	    candidates.removeCandidates( ROWCOL[8][4], new int[]{1,6});
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		
		XWings rule = new XWings();
		// Locations test
		List<int[]> encs = rule.locations(board, candidates);
		assertTrue(null != encs);
		assertEquals(1, encs.size());

        // Update test
        int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		int updates = rule.updateCandidates(board, null, candidates, encs);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertEquals( prevCandidates,candidates.getAllCandidateCount() + updates);
		System.out.println( "Candidates=\n" + candidates.toStringBoxed());
	}
	
}