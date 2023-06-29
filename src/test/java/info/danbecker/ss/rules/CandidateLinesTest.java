package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CandidateLinesTest {
	@BeforeEach
	public void setup() {
	}

	public static String MULTIPLELINES_D4C7 =
			"..1957.63...8.6.7.76913.8.5..726135.312495786.56378...1.86.95.7.9.71.6.8674583...";
	public static String MULTIPLELINES_D4C7_SOLUTION =
			"281957463435826971769134825847261359312495786956378214128649537593712648674583192";

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
		assertEquals( -1, loc[0] ); // rowi
		assertEquals( 7, loc[1] ); // coli
		assertEquals( 8, loc[2] ); // boxi
		assertEquals( 4, loc[3] ); // digi

		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, new Board(MULTIPLELINES_D4C7_SOLUTION), candidates, locations);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertEquals( prevCandidates, candidates.getAllCount() + updates);
	}

	@Test
	public void testEncoding() {
		// encoding of form
		//	 int[] enc = new int[]{rowi, -1, boxi, digi};
	    //  int[] enc = new int[]{-1, coli, boxi, digi};
		FindUpdateRule rule = new CandidateLines();

		int[] enc = new int[]{ 1, -1, 3, 8};
		String encStr = rule.encodingToString( enc );
		// System.out.println( "enc=" + encStr );
		assertTrue( encStr.contains( "digit 8" ));
		assertTrue( encStr.contains( "box 3" ));
		assertTrue( encStr.contains( "row 1" ));

		enc = new int[]{ -1, 2, 5, 7};
		encStr = rule.encodingToString( enc );
		// System.out.println( "enc=" + encStr );
		assertTrue( encStr.contains( "digit 7" ));
		assertTrue( encStr.contains( "box 5" ));
		assertTrue( encStr.contains( "col 2" ));
	}
}