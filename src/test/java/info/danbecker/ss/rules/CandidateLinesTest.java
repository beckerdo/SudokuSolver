package info.danbecker.ss.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;

import java.text.ParseException;
import java.util.List;

public class CandidateLinesTest {
	public static String MULTIPLELINES_D4C7 =
		"..1957.63-...8.6.7.-76913.8.5-..726135.-312495786-.56378...-1.86.95.7-.9.71.6.8-674583...";
	@BeforeEach
	public void setup() {
	}

	@Test
	public void testBasics() throws ParseException {
		Board board = new Board(MULTIPLELINES_D4C7);
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertTrue(board.legal());

		FindUpdateRule rule = new CandidateLines();
		assertEquals(rule.ruleName(), rule.getClass().getSimpleName());

		List<int[]> locations = rule.find(board, candidates);
		assertNotNull(locations);
		assertEquals(1, locations.size());
		int[] loc = locations.get(0);
		assertEquals( 4, loc.length );

		// System.out.println(format("Rule %s reports %d possible locations at row/col/block/digit=%d/%d/%d/%d ",
		// 	rule.ruleName(), locations.size(), loc[0],loc[1],loc[2],loc[3]));
		assertEquals( -1, loc[0] ); // rowi
		assertEquals( 7, loc[1] ); // coli
		assertEquals( 8, loc[2] ); // boxi
		assertEquals( 4, loc[3] ); // digi

		int prevCount = candidates.getAllCandidateCount();
		candidates.removeCandidateNotInBox(loc[0],loc[1],loc[2],loc[3]);
		assertTrue( prevCount > candidates.getAllCandidateCount());
		// System.out.print(format("Rule %s reduced candidates from %d to %d", rule.ruleName(), prevCount, candidates.getAllCandidateCount()));
	}
}