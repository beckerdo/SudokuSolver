package info.danbecker.ss.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.Utils;

import java.text.ParseException;
import java.util.List;

public class CandidateLinesTest {
	public static String LINES = """
1........
.........
.........
........1
.....1...
.........
.7.......
.8.......
.9.......
""";
	public static String LINES2 = """
1........
.........
.........
.7.......
.8.......
.9.......
........1
.....1...
.........
""";

	@BeforeEach
	public void setup() {
	}

	@Test
	public void testBasics() throws ParseException {
		Board board = new Board(LINES);
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);

		assertTrue(board.legal());

		UpdateCandidatesRule rule = new CandidateLines();
		assertEquals(rule.ruleName(), rule.getClass().getSimpleName());

		List<int[]> locations = rule.locations(board, candidates);
		assertTrue(null != locations);
		assertEquals(1, locations.size());

		System.out.print(format("Rule %s reports %d possible locations at row/col/block/digit ", rule.ruleName(), locations.size()));
		if (locations.size() > 0) {
			System.out.println( Utils.locationsString(locations));
		} else {
			System.out.println();
		}
		// System.out.print(candidates.candidateBlockLocationsString(locations.get(0)[2])); // blocki
		
		int[] loc = locations.get(0);
		int prevCount = candidates.candidateCount();
		candidates.removeCandidateNotInBox(loc[0],loc[1],loc[2],loc[3]);
		assertTrue( prevCount > candidates.candidateCount());
		System.out.print(format("Rule %s reduced candidates from %d to %d", rule.ruleName(), prevCount, candidates.candidateCount()));
	}
}