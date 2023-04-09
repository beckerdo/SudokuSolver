package info.danbecker.ss.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;

import java.text.ParseException;
import java.util.List;

public class DoublePairsTest {
	// https://www.sudokuoftheday.com/techniques/double-pairs
	// This puzzle appears to have additional candidates removed
	public static String DOUBLEPAIRS0 = """
934.6..5.
..6..4923
..89...46
8..546..7
6...1...5
5..39..62
36.4.127.
47.6..5..
.8....63.
""";

	// Not quite the same as https://www.sudokuoftheday.com/techniques/double-pairs
	// Puzzle features
	//	Rule DoublePairs found double pair for digit 2 at row/col=0/3,0/5 and row/col=4/3,4/5
	//	Rule DoublePairs found double pair for digit 3 at row/col=2/4,2/5 and row/col=7/4,7/5 with no more candidates
	//	Rule DoublePairs found double pair for digit 4 at row/col=4/1,4/6 and row/col=5/1,5/6 with no more candidates
	//	Rule DoublePairs found double pair for digit 5 at row/col=1/1,1/4 and row/col=2/1,2/4	
	public static String DOUBLEPAIRS = """
934.6..5.
..6..4923
2.89...46
8..546..7
6...1...5
5..39..62
36.4.127.
47.6..5..
.8....63.
""";

	@BeforeEach
	public void setup() {
	}

	@Test
	public void testLocationString() {
		// Middle box double pair
		int[] test = new int[] { 1, 3, 3, 3, 5, 5, 3, 5, 5 };
		assertEquals("null", DoublePairs.locationToString(null));
		assertTrue(DoublePairs.locationToString(new int[0]).contains("length"));
		assertTrue(DoublePairs.locationToString(new int[8]).contains("length"));
		String result = DoublePairs.locationToString(test);
		// System.out.println( "Location=" + result );
		assertTrue(result.contains("digit 1"));
		test[1] = 0;
		result = DoublePairs.locationToString(test);
		assertTrue(result.contains("row mismatch"));
		test[8] = 8;
		result = DoublePairs.locationToString(test);
		assertTrue(result.contains("col mismatch"));
	}
	
	@Test
	public void testBasics() throws ParseException {
		Board board = new Board(DOUBLEPAIRS);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		System.out.println( "Candidates=" + candidates.toStringCompact());

		UpdateCandidatesRule rule = new DoublePairs();
		
		// Locations test
		List<int[]> locations = rule.locations(board, candidates);
		assertNotNull(locations);
		assertEquals(2, locations.size());
		// double pair for digit 3 at row/col=2/4,2/5 and row/col=7/4,7/5
		// double pair for digit 4 at row/col=4/1,4/6 and row/col=5/1,5/6
		
        // Update test
        int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		rule.updateCandidates(board, null, candidates, locations);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertTrue( prevCandidates > candidates.getAllCandidateCount());
		// remove 4 digit 3 candidates not in rowCols 2,4 7,5
	}
}