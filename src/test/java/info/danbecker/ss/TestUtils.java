package info.danbecker.ss;

import info.danbecker.ss.rules.FindUpdateRule;
import info.danbecker.ss.rules.LegalCandidates;

import java.text.ParseException;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Utilities that can be shared by all unit tests.
 */
public class TestUtils {
	/**
	 * Runs a given test case.
	 *
	 * @return true if all expecteds pass, assert failure otherwise
	 * @throws ParseException if board or cand strings are illegal
	 */
	public static boolean testRunner (
			FindUpdateRule rule,
			String boardStr,
			String solutionString,
			String optCandString,
			boolean displayCands,
			int expectedFinds,
			boolean displayEncs,
			List<int[]> expectedEncs,
			Comparator<int[]> encComparator,
			int expectedOccs,
			int expectedCands,
 		    int expectedUpdates ) throws ParseException {

		Board board = new Board(boardStr);
		assertTrue(board.legal());
		Candidates candidates;
		if ( null == optCandString ) {
			candidates = new Candidates(board);
			(new LegalCandidates()).update(board, null, candidates, null);
		} else {
			candidates = new Candidates(optCandString);
		}
		if ( displayCands )
			System.out.println( "Candidates=\n" + candidates.toStringFocus( false,
				Candidates.ALL_DIGITS, Candidates.ALL_COUNTS ));

		// Locations test
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		if ( expectedFinds != encs.size() || displayEncs ) {
			for( int enci = 0; enci < encs.size(); enci++ ) {
				int[] enc = encs.get(enci);
				System.out.printf("%s found %s%n", rule.ruleName(), rule.encodingToString(enc));
			}
		}
		assertEquals(expectedFinds, encs.size());

		// Test encs
		for( int enci = 0; enci < expectedEncs.size(); enci++ ){
			int [] enc = encs.get( enci );
			int [] expectedEnc = expectedEncs.get( enci );
			// assertArrayEquals( enc, expectedEnc);
			assertEquals( 0, encComparator.compare( expectedEnc, enc) );
		}

		// Update test
		int prevOccs = board.getOccupiedCount();
		int prevCands = candidates.getAllCount();
		final int[] updateCount = { -1 };
		final Board solution = null == solutionString ? null : new Board( solutionString ) ;
		assertDoesNotThrow( ()-> updateCount[0] = rule.update(board, solution, candidates, encs));
		assertEquals( expectedOccs, board.getOccupiedCount() - prevOccs );
		assertEquals( expectedCands, prevCands - candidates.getAllCount());
		assertEquals( expectedUpdates, updateCount[0]);
		return true;
	}
}