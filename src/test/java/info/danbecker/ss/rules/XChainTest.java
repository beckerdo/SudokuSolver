package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.*;

public class XChainTest {
	@BeforeEach
	public void setup() {
	}

	// This example from https://hodoku.sourceforge.net/en/tech_chains.php#rp
	// Website says there is equivalence between SimpleColorsand RemotePairs chain
	public static String XCHAIN_1 =
			"3.452..8...6.9.....5..7.3.....689.23...734....631527...1.96......9.4..6.6.8217..5";
	public static String XCHAIN_1_SOLUTION =
			"374521986186493572952876314547689123291734658863152749415968237729345861638217495";

	public static String XCHAIN_2 =
			"...35178.8576293411..874..25.91628.4681.4.2.......816.718....2.....1...8.6..8..17";
	public static String XCHAIN_2_SOLUTION =
			"246351789857629341193874652539162874681547293472938165718493526925716438364285917";

	@Test
	public void testFindUpdate() throws ParseException {
		// Test a tree
		Board board = new Board(XCHAIN_1);
		assertTrue(board.legal());
		// Set up and validate candidates
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		// Update to match web site.
		// candidates.removeCandidates( ROWCOL[7][6], new int[]{3,5,7} );
		assertEquals( 143, candidates.getAllCount());
		assertArrayEquals( new int[]{7,9}, candidates.getRemainingCandidates(ROWCOL[0][1]) );
		assertArrayEquals( new int[]{1,6,7,9}, candidates.getRemainingCandidates(ROWCOL[0][8]) );
		assertArrayEquals( new int[]{1,4,5,7}, candidates.getRemainingCandidates(ROWCOL[1][7]) );
		assertArrayEquals( new int[]{3,4,7}, candidates.getRemainingCandidates(ROWCOL[6][7]) );
		assertArrayEquals( new int[]{2,5,7}, candidates.getRemainingCandidates(ROWCOL[6][2]) );
		assertArrayEquals( new int[]{1,5,7}, candidates.getRemainingCandidates(ROWCOL[3][2]) );

		// Test find with one digit
		XChain rule = new XChain();
		int testDigit = 7;
		// Note that different trees have different coloring
		// and can lead to the same rowCol candidate flagged as a problem.
		List<int[]> encs = rule.find( board, candidates, testDigit );
		assertNotNull(encs);
		assertEquals(2, encs.size());

		// Test find with all digits
		encs = rule.find( board, candidates );
		assertNotNull(encs);
		assertEquals(2, encs.size());

		// Test update
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, new Board(XCHAIN_1_SOLUTION), candidates, encs);
		// Entries same. Candidate loses 1.
		// Candidates loses 1.
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(1, updates);
		assertEquals(prevCandidates, candidates.getAllCount() + updates);
	}
	@Test
	public void testUpdateDigit6Bug() throws ParseException {
		// Test a tree
		Board board = new Board(XCHAIN_1);
		assertTrue(board.legal());
		// Set up and validate candidates
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		// Update to match web site.
		// candidates.removeCandidates( ROWCOL[7][6], new int[]{3,5,7} );
		assertEquals( 143, candidates.getAllCount());
		assertArrayEquals( new int[]{7,9}, candidates.getRemainingCandidates(ROWCOL[0][1]) );
		assertArrayEquals( new int[]{1,6,7,9}, candidates.getRemainingCandidates(ROWCOL[0][8]) );
		assertArrayEquals( new int[]{1,4,5,7}, candidates.getRemainingCandidates(ROWCOL[1][7]) );
		assertArrayEquals( new int[]{3,4,7}, candidates.getRemainingCandidates(ROWCOL[6][7]) );
		assertArrayEquals( new int[]{2,5,7}, candidates.getRemainingCandidates(ROWCOL[6][2]) );
		assertArrayEquals( new int[]{1,5,7}, candidates.getRemainingCandidates(ROWCOL[3][2]) );

		// Test find with one digit
		XChain rule = new XChain();
		int testDigit = 6;
		// Note that different trees have different coloring
		// and can lead to the same rowCol candidate flagged as a problem.
		List<int[]> encs = rule.find(board, candidates, testDigit );
		assertNotNull(encs);
		assertEquals(0, encs.size());

		// Test update
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, new Board(XCHAIN_1_SOLUTION), candidates, encs);
		// Entries same. Candidate loses 1.
		// Candidates loses 1.
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(0, updates);
		assertEquals(prevCandidates, candidates.getAllCount() + updates);
	}


	@Test
	public void testFindUpdate2() throws ParseException {
		// Test a tree
		Board board = new Board(XCHAIN_2);
		assertTrue(board.legal());
		// Set up and validate candidates
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		// Update to match web site.
		// candidates.removeCandidates( ROWCOL[7][6], new int[]{3,5,7} );
		assertEquals( 116, candidates.getAllCount());
		assertArrayEquals( new int[]{3,7}, candidates.getRemainingCandidates(ROWCOL[3][1]) );
		assertArrayEquals( new int[]{3,7}, candidates.getRemainingCandidates(ROWCOL[3][7]) );
		assertArrayEquals( new int[]{3,5,9}, candidates.getRemainingCandidates(ROWCOL[7][7]) );
		assertArrayEquals( new int[]{3,5,6,9}, candidates.getRemainingCandidates(ROWCOL[6][8]) );
		assertArrayEquals( new int[]{3,9}, candidates.getRemainingCandidates(ROWCOL[5][4]) );
		assertArrayEquals( new int[]{3,9}, candidates.getRemainingCandidates(ROWCOL[6][4]) );

		// Test find with one digit
		XChain rule = new XChain();
		int testDigit = 3;
		List<int[]> encs = rule.find(board, candidates, testDigit );
		assertNotNull(encs);
		assertEquals(8, encs.size());

		// Test find with all digits
		encs = rule.find(board, candidates );
		assertNotNull(encs);
		assertEquals(8, encs.size());

		// Test update
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, new Board(XCHAIN_2_SOLUTION), candidates, encs);
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(3, updates);
		assertEquals(prevCandidates, candidates.getAllCount() + updates);
	}

	@Test
	public void testEncode()  {
		// Encode tree as int []
		int digit = 3;
		int type = 0;
		RowCol root = ROWCOL[1][1];
		RowCol cLoc = ROWCOL[1][8];
		int fColor = 0;
		RowCol fLoc = ROWCOL[2][2];
		int sColor = 1;
		RowCol sLoc = ROWCOL[7][8];

		int[] enc = SimpleColors.encode(digit, type,
				root, cLoc, fColor, fLoc, sColor, sLoc);

		assertEquals(12, enc.length);
		assertEquals(digit, enc[0]);
		assertEquals(type, enc[1]);
		assertEquals(root.row(), enc[2]);
		assertEquals(root.row(), enc[3]);
		assertEquals(cLoc.row(), enc[4]);
		assertEquals(cLoc.col(), enc[5]);
		assertEquals(fColor, enc[6]);
		assertEquals(fLoc.row(), enc[7]);
		assertEquals(fLoc.col(), enc[8]);
		assertEquals(sColor, enc[9]);
		assertEquals(sLoc.row(), enc[10]);
		assertEquals(sLoc.col(), enc[11]);

		FindUpdateRule rule = new XChain();
		String encString = rule.encodingToString(enc);
		// System.out.println("Enc=" + encString);
		assertTrue(encString.contains("digit " + digit));
		assertTrue(encString.contains("color trap"));
		assertTrue(encString.contains("tree " + root));
		assertTrue(encString.contains("cand " + cLoc));
		assertTrue(encString.contains("sees " + fColor));
		assertTrue(encString.contains("at " + fLoc));
		assertTrue(encString.contains("and " + sColor));
		assertTrue(encString.contains("at " + sLoc));
	}
}