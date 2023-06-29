package info.danbecker.ss.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;

import java.text.ParseException;
import java.util.List;

public class DoublePairsTest {
	// Not quite the same as https://www.sudokuoftheday.com/techniques/double-pairs
	// That puzzle has extra candidates knocked out
	// Rule DoublePairs found double pair for digit 3 at [2,4],[2,5] and [7,4],[7,5] with no more candidates
	// Rule DoublePairs found double pair for digit 4 at [4,1],[4,6] and [5,1],[5,6] with no more candidates
	public static String DOUBLEPAIRS =
			"934.6..5...6..4923..89...468..546..76...1...55..39..6236.4.127.47.6..5...8....634";
	public static String DOUBLEPAIRS_SOLUTION =
			"934162758156874923728935146892546317643217895517398462365481279479623581281759634";

	@BeforeEach
	public void setup() {
	}
	@Test
	public void testNegative() throws ParseException {
		Board board = new Board(DOUBLEPAIRS);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// Locations test
		FindUpdateRule rule = new DoublePairs();
		List<int[]> locations = rule.find(board, candidates);
		assertNotNull(locations);
		assertEquals(0, locations.size());

        // Update test
        int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, new Board(DOUBLEPAIRS_SOLUTION), candidates, locations);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertEquals( prevCandidates, candidates.getAllCount() + updates);
	}

	@Test
	public void testPositive() throws ParseException {
		Board board = new Board(DOUBLEPAIRS);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// Doctor the candidates to equal the website example.
		candidates.removeCandidates( ROWCOL[0][3], new int[]{8});
		candidates.removeCandidates( ROWCOL[0][5], new int[]{8});
		candidates.removeCandidates( ROWCOL[2][4], new int[]{2});
		candidates.removeCandidates( ROWCOL[2][5], new int[]{2});
		candidates.removeCandidates( ROWCOL[4][1], new int[]{2});
		candidates.removeCandidates( ROWCOL[4][2], new int[]{2});
		candidates.removeCandidates( ROWCOL[7][2], new int[]{1});
		candidates.removeCandidates( ROWCOL[7][7], new int[]{9});
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// Locations test
		FindUpdateRule rule = new DoublePairs();
		List<int[]> locations = rule.find(board, candidates);
		assertNotNull(locations);
		assertEquals(2, locations.size());

		// Update test
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, new Board(DOUBLEPAIRS_SOLUTION), candidates, locations);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertEquals( prevCandidates, candidates.getAllCount() + updates);
	}
	@Test
	public void testEncoding() {
		// Middle box double pair
		FindUpdateRule rule = new DoublePairs();
		int[] test = new int[] { 1, 0, 3, 3, 3, 5, 5, 3, 5, 5 };
		assertEquals("null", rule.encodingToString(null));
		assertTrue(rule.encodingToString(new int[0]).contains("length"));
		assertTrue(rule.encodingToString(new int[8]).contains("length"));

		String result = rule.encodingToString(test);
		// System.out.println( "Encoding=" + result );
		assertTrue(result.contains("digit 1"));
		assertTrue(result.contains("ROW pairs"));
		assertTrue(result.contains("at [3,3]"));
		assertTrue(result.contains("and [5,3]"));

		// Cause error
		test[2] = 0;
		result = rule.encodingToString(test);
		assertTrue(result.contains("row mismatch"));
		test[9] = 8;
		result = rule.encodingToString(test);
		assertTrue(result.contains("col mismatch"));
	}
}