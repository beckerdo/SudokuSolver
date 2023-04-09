package info.danbecker.ss.rules;

import info.danbecker.ss.RowCol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.*;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;

import java.text.ParseException;
import java.util.List;

public class ForcingChainsTest {
	@BeforeEach
	public void setup() {
	}

	// https://www.sudokuoftheday.com/dailypuzzles/2023-01-03/diabolical/solver
	// Either candidate for cell (1,5) forces digit 3 into cell (5,7).
	public static String FORCINGCHAINS_20230103=
	"...2..6.3-...37685.-..6.1..4.-.6892....-21.....69-....612..-.9268.135-.51.32..6-6.3195.2.";
	@Test
	public void testLocs() throws ParseException {
		Board board = new Board(FORCINGCHAINS_20230103);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);	
		// Need to update candidates to get it into the proper state
		candidates.removeCandidates( ROWCOL[0][0], new int[]{4,9});
		candidates.removeCandidates( ROWCOL[0][1], new int[]{4});
		candidates.removeCandidates( ROWCOL[0][2], new int[]{4,9});
		candidates.removeCandidates( ROWCOL[3][0], new int[]{3});
		candidates.removeCandidates( ROWCOL[3][6], new int[]{4,7});
		candidates.removeCandidates( ROWCOL[4][6], new int[]{4,7});
		candidates.removeCandidates( ROWCOL[5][3], new int[]{8});
		candidates.removeCandidates( ROWCOL[8][8], new int[]{4});
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		
		ForcingChains rule = new ForcingChains();
		// Locations test
		List<int[]> encs = rule.locations(board, candidates);
		assertNotNull( encs);
		assertEquals(2, encs.size());

        // Update test
        int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		int updates = rule.updateCandidates(board, null, candidates, encs);
		// Destination gains entry, loses 2 candidates.
		// Will likely knock out more same-unit candidates after LegalCandidate update.
		assertEquals( prevEntries, candidates.getAllOccupiedCount() - updates);
		assertEquals( prevCandidates,candidates.getAllCandidateCount() + 2);
	}
	
	@Test
	public void testEncodeDecode() {
		// Middle box double pair
		int digit = 5;	
		RowCol origRowCol = ROWCOL[0][1];
		RowCol destRowCol = ROWCOL[7][8];
		
		int [] enc = ForcingChains.encode( origRowCol, digit, destRowCol, 4, 5 );

		assertEquals(0, enc[0]);
		assertEquals(1, enc[1]);
		assertEquals(5, enc[2]);
		assertEquals(7, enc[3]);
		assertEquals(8, enc[4]);
		assertEquals(4, enc[5]);
		assertEquals(5, enc[6]);

   		// Test the string
   		String encString = ForcingChains.encodingToString(enc);
   		// System.out.println( "Enc=" +encString);
   		assertNotNull(encString);
   		assertTrue(encString.contains("digit=" + digit));
   		assertTrue(encString.contains( origRowCol.toString() ));
   		assertTrue(encString.contains( destRowCol.toString() ));
	}

	public static String P20230108_FORCINGCHAIN_51797 =
	"75.8.....-8...7.2.5-..39.54..-..8547..6-..5.817..-1..39.5..-5824.93..-..1.5.824-.....8.59";			
	@Test
	public void test20230108() throws ParseException {
		Board board = new Board(P20230108_FORCINGCHAIN_51797);
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
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);	
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		
		ForcingChains rule = new ForcingChains();
		// Locations test
		List<int[]> encs = rule.locations(board, candidates);
		assertNotNull(encs);
		assertEquals(6, encs.size());

        // Update test
        int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		int updates = rule.updateCandidates(board, null, candidates, encs);
		// Destination gains entry, loses 2 candidates.
		// Will likely knock out more same-unit candidates after LegalCandidate update.
		assertEquals( prevEntries, candidates.getAllOccupiedCount() - updates);
		assertEquals( prevCandidates, candidates.getAllCandidateCount() + 2);
	}
	
	public static String P20230103 =
	"...2..6.3-...37.85.-..6.1..4.-..89.....-21.....69-.....12..-.9..8.1..-.51.32...-6.3..5...";		
	public static String P20230103_PARTIAL_INVALID = 
	".8.2496.3-...37685.-3.651894.-.689235..-21.85.369-.3..6128.-.9268.135-851.32.96-6.3195.28"; // invalid (first row)
	public static String P20230103_PARTIAL = 
	"1872..693924376851536.1..42768923514215..83693.95612874926871358514329.6673195428";
	public static String P20230103_SOLUTION = 
	"187254693924376851536819742768923514215748369349561287492687135851432976673195428";

	@Test
	public void testBug20230103() throws ParseException {
		Board board = new Board(P20230103_PARTIAL);
		assertTrue(board.legal());
		Board solution = new Board(P20230103_SOLUTION);
		assertTrue(solution.legal());

		ForcingChains rule = new ForcingChains();

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);	
		System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// Need to run a few other rules to get to the test place
//		i38.12 Board entries=57, candidates=49
//		ForcingChains rowCol [0,0] both candidates {15} lead to digit 7 at [6,0], tree depths=11,18
//		Rule ForcingChains reports 1 possible locations: [0,0]
//		Exception in thread "main" java.lang.IllegalArgumentException: Rule ForcingChains would like set digit 7 (solution=4) at loc [6,0].
//			at info.danbecker.ss.rules.ForcingChains.updateCandidates(ForcingChains.java:75)
//			at info.danbecker.ss.SudokuSolver.solve(SudokuSolver.java:238)
//			at info.danbecker.ss.SudokuSolver.main(SudokuSolver.java:131)
		
		List<int[]> encs = rule.locations(board, candidates );		
		assertEquals( 3, encs.size());
	}
}