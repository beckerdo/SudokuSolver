package info.danbecker.ss.rules;

import info.danbecker.ss.RowCol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static info.danbecker.ss.Board.ROWCOL;
import static java.lang.String.format;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import static info.danbecker.ss.Candidates.NOT_NAKED;
import static info.danbecker.ss.Candidates.FULL_COMBI_MATCH;
import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.util.List;

public class HiddenSubsetsTest {
	// Puzzle from https://www.sudokuoftheday.com/techniques/hidden-pairs-triples
	// Website has additional candidates knocked out.
// Row 2, combo [0, 2], has 2 hidden locations, 4 candidates with 2 extra
//    rowCol=2/5, candidates=[1, 0, 3, 4, 0, 0, 0, 0, 0]
//    rowCol=2/8, candidates=[1, 2, 3, 0, 0, 0, 0, 0, 0]
	public static String HIDDENPAIRSROW2 = """
8.1..6.94
3....9.8.
97..8.5..
547.62.3.
632....5.
198375246
.8362.915
.65198...
2195....8
""";

// Col 3, combo [7, 8], has 2 hiddens with 5 candidates
//    rowCol=3/3, candidates=[0, 0, 0, 0, 0, 0, 0, 8, 9]
//    rowCol=4/3, candidates=[0, 0, 0, 4, 0, 0, 0, 8, 9]
	public static String HIDDENPAIRSCOL4 = """
7..6..254
.465.213.
12534.9..
...8..74.
4..9....1
..24.639.
...2.4..9
..41.95..
298765413
""";

	@BeforeEach
	public void setup() {
	}

	@Test
	public void testBasics() throws ParseException {
		Board board = new Board(HIDDENPAIRSROW2);
		// Row 2, combo [0, 2], has 2 hidden locations, 4 candidates with 2 extra
		//	    rowCol=2/5, candidates=[1, 0, 3, 4, 0, 0, 0, 0, 0]
		//	    rowCol=2/8, candidates=[1, 2, 3, 0, 0, 0, 0, 0, 0]
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);

		int [] combo13 = new int [] {0, 2};
		// Row test
		List<RowCol> hiddenPairs = candidates.candidateComboRowLocations( 2, combo13, NOT_NAKED, 2 );
     	assertEquals(2, hiddenPairs.size());
  		assertEquals( ROWCOL[2][5], hiddenPairs.get( 0 ));
  		assertEquals( ROWCOL[2][8], hiddenPairs.get( 1 ));

		int [] combo12 = new int [] {0, 1};
		// Col test
		hiddenPairs = candidates.candidateComboColLocations( 8, combo12, NOT_NAKED, FULL_COMBI_MATCH );
     	assertEquals(2, hiddenPairs.size());
  		assertEquals( ROWCOL[1][8], hiddenPairs.get( 0 ));
  		assertEquals( ROWCOL[2][8], hiddenPairs.get( 1 ));

		board = new Board(HIDDENPAIRSCOL4);
		// Col 3, combo [7, 8], has 2 hiddens with 5 candidates
		//    rowCol=3/3, candidates=[0, 0, 0, 0, 0, 0, 0, 8, 9]
		//    rowCol=4/3, candidates=[0, 0, 0, 4, 0, 0, 0, 8, 9]
		assertTrue(board.legal());
		candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);

		int [] combo25 = new int [] {1, 4};
		// Col test
		hiddenPairs = candidates.candidateComboColLocations( 4, combo25, NOT_NAKED, FULL_COMBI_MATCH );
     	assertEquals(2, hiddenPairs.size());
  		assertEquals( ROWCOL[3][4], hiddenPairs.get( 0 ));
  		assertEquals( ROWCOL[4][4], hiddenPairs.get( 1 ));
  		
		// Run rule
		board = new Board(HIDDENTRIPLE_347_COL4);
		// Col 4, combo [2,3,6], has 3 hidden locs, 8 candidates with 1 extra 0
		//	    rowCol=0/4, candidates=[1, 0, 3, 0, 0, 0, 7, 0, 0], subset 2, 1 extra 
		//	    rowCol=3/4, candidates=[0, 0, 3, 4, 0, 0, 7, 0, 0], subset 3, 0 extra
		//	    rowCol=5/4, candidates=[0, 0, 3, 4, 0, 0, 7, 0, 0], subset 3, 0 extra
		assertTrue(board.legal());
		candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=" + candidates.toStringCompact());

		int [] combo236 = new int [] {2, 3, 6};
		// Col test
		List<RowCol> hiddenTriples = candidates.candidateComboColLocations( 4, combo236, NOT_NAKED, 2 );
     	assertEquals(3, hiddenTriples.size());
  		assertEquals( ROWCOL[0][4], hiddenTriples.get( 0 ));
  		assertEquals( ROWCOL[3][4], hiddenTriples.get( 1 ));
  		assertEquals( ROWCOL[5][4], hiddenTriples.get( 2 ));
	}

  	@Test
  	public void testHiddenSubsets2() throws ParseException {
		// Run rule
		Board board = new Board(HIDDENPAIRSROW2);
//		8.1..6.94
//		3....9.8.
//		97..8.5..
//		547.62.3.
//		632....5.
//		198375246
//		.8362.915
//		.65198...
//		2195....8
		// Row 2, combo [0, 2], has 2 hiddens with 6 candidates
//	    rowCol=2/5, candidates=[1, 0, 3, 4, 0, 0, 0, 0, 0]
//	    rowCol=2/8, candidates=[1, 2, 3, 0, 0, 0, 0, 0, 0]
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		FindUpdateRule rule = new HiddenSubsets(2);
		assertEquals(rule.ruleName(), "HiddenSubsets2" );

		// Locations test
		List<int[]> locations = rule.find(board, candidates);
		assertNotNull( locations);
		// System.out.println(format("Rule %s reports %d finds", rule.ruleName(), locations.size()));
		// int [] encoded = locations.get( 0 );
		// Encoded location=[13, 36, 39]
        // System.out.println( "Encoded location=" + Arrays.toString(encoded)); // one based
		assertEquals(3, locations.size());

        // Update test
        int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		rule.update(board, null, candidates, locations);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertTrue( prevCandidates > candidates.getAllCandidateCount());
//		assertEquals( 4, candidates.candidateComboRowCount(8, combo15));
		System.out.println( format( "Candidate count prev/curr = %d/%d", prevCandidates, candidates.getAllCandidateCount()));
		// System.out.println( "Candidates=" + candidates.toStringCompact());		
	}
  	
 // Col 4, combo [2,3,6], has 3 hidden locs, 8 candidates with 1 extra 0
//  rowCol=0/4, candidates=[1, 0, 3, 0, 0, 0, 7, 0, 0], subset 2, 1 extra 
//  rowCol=3/4, candidates=[0, 0, 3, 4, 0, 0, 7, 0, 0], subset 3, 0 extra
//  rowCol=5/4, candidates=[0, 0, 3, 4, 0, 0, 7, 0, 0], subset 3, 0 extra
	public static String HIDDENTRIPLE_347_COL4 =
	"5286...49-13649..25-7942.563.-...1..2..-..78263..-..25.9.6.-24.3..976-8.97.2413-.7.9.4582";		
  	@Test
  	public void testHiddenSubsets3() throws ParseException {
		// Create test
		Board board = new Board(HIDDENTRIPLE_347_COL4);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// Run rule
		FindUpdateRule rule = new HiddenSubsets( 3 );
		assertEquals(rule.ruleName(), "HiddenSubsets3" );
		// Locations test
		List<int[]> locs = rule.find(board, candidates);
		assertNotNull( locs);
		assertEquals( 1, locs.size());
		// Update test
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		rule.update(board, null, candidates, locs);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertEquals( prevCandidates, candidates.getAllCandidateCount() + 1);
	}
 
	// Puzzle from https://www.sudokuoftheday.com/dailypuzzles/2023-01-03/diabolical/solver
  	// Hidden Pair 35 in locs [3,6] and [4,6]
	public static String HIDDENPAIRS_20230103= 
	"...2..6.3-...37685.-..6.1..4.-.6892....-21.....69-....612..-.9268.135-.51.32..6-6.3195.2.";
 	@Test
  	public void testHiddenSubsets2_20230103 () throws ParseException {
		// Create test
		Board board = new Board(HIDDENPAIRS_20230103);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// Run rule.
		FindUpdateRule rule = new HiddenSubsets( 2 );
		assertEquals(rule.ruleName(), "HiddenSubsets2" );
		// Locations test
		List<int[]> locs = rule.find(board, candidates);
		assertNotNull(locs);
		assertEquals( 2, locs.size());
		// Update test
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		rule.update(board, null, candidates, locs);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertEquals( prevCandidates, candidates.getAllCandidateCount() + 4);
 	}
}