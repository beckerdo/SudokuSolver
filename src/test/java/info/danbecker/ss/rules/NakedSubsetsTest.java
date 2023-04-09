package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Candidates.FULL_COMBI_MATCH;
import static info.danbecker.ss.Candidates.NAKED;
import static org.junit.jupiter.api.Assertions.*;

public class NakedSubsetsTest {
	// Puzzle from https://www.sudokuoftheday.com/techniques/naked-pairs-triples
	public static String NAKEDPAIRS = """
4..27.6..
798156234
.2.84...7
237468951
849531726
561792843
.82.15479
.7..243..
..4.87..2
""";
	public static String NAKEDPARTIALTRIPLE = """
6..8.2735
7.235694.
3..4.7.62
1..975.24
2..183.79
.79624..3
4..56.2.7
.6724.3..
92.7384.6
""";

	@BeforeEach
	public void setup() {
	}

	@Test
	public void testBasics() throws ParseException {
		Board board = new Board(NAKEDPAIRS);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);

		// Pre tests
		int [] combo15 = new int [] {0, 4};
		// Row test
		List<RowCol> nakedPairs = candidates.candidateComboRowLocations( 8, combo15, NAKED, FULL_COMBI_MATCH );
		assertEquals(2, nakedPairs.size());
		assertEquals( ROWCOL[8][1], nakedPairs.get( 0 ));
		assertEquals( ROWCOL[8][6], nakedPairs.get( 1 ));
		assertEquals( 6, candidates.candidateComboRowCount(8, combo15));

		// Col test
		nakedPairs = candidates.candidateComboColLocations( 1, combo15, NAKED, FULL_COMBI_MATCH );
		assertEquals(2, nakedPairs.size());
		assertEquals( ROWCOL[0][1], nakedPairs.get( 0 ));
		assertEquals( ROWCOL[8][1], nakedPairs.get( 1 ));
		assertEquals( 4, candidates.candidateComboColCount(1, combo15));

		// Run rule
		UpdateCandidatesRule rule = new NakedSubsets(2);
		assertEquals(rule.ruleName(), "NakedSubsets2" );

		// Locations test
		List<int[]> locations = rule.locations(board, candidates);
		assertNotNull( locations);
		assertEquals(1, locations.size());
		// int [] encoded = locations.get( 0 );
		// System.out.println(format("Rule %s reports %d find", rule.ruleName(), locations.size()));
        // System.out.println( "Encoded location=" + Arrays.toString(encoded)); // one based

        // Update test
        int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		rule.updateCandidates(board, null, candidates, locations);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertTrue( prevCandidates > candidates.getAllCandidateCount());

		assertTrue( prevCandidates > candidates.getAllCandidateCount());
		assertEquals( 4, candidates.candidateComboRowCount(8, combo15));		
	}
	
	@Test
	public void testNakedPartialTriple() throws ParseException {
		Board board = new Board(NAKEDPARTIALTRIPLE);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);

		// Pre tests
		int [] combo138 = new int [] {0,2,7};

		// Col test
		List<RowCol> nakedpartialTriples = candidates.candidateComboColLocations( 1, combo138, NAKED, 2 );
		assertEquals(3, nakedpartialTriples.size());
		assertEquals( ROWCOL[1][1], nakedpartialTriples.get( 0 ));
		assertEquals( ROWCOL[3][1], nakedpartialTriples.get( 1 ));
		assertEquals( ROWCOL[6][1], nakedpartialTriples.get( 2 ));

		// Run rule
		UpdateCandidatesRule rule = new NakedSubsets(3);
		assertEquals(rule.ruleName(), "NakedSubsets3" );

		// Locations test
		List<int[]> locations = rule.locations(board, candidates);
		assertNotNull(locations);
		assertEquals(1, locations.size());
		// System.out.println(format("Rule %s reports %d find", rule.ruleName(), locations.size()));
		// int [] encoded = locations.get( 0 );
        // System.out.println( "Encoded location=" + Arrays.toString(encoded)); // one based

        // Update test
        int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		rule.updateCandidates(board, null, candidates, locations);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertTrue( prevCandidates > candidates.getAllCandidateCount());
	}
}