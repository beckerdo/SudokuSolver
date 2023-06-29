package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Candidates.FULL_COMBI_MATCH;
import static info.danbecker.ss.Candidates.NAKED;
import static org.junit.jupiter.api.Assertions.*;

public class NakedSubsetsTest {
	@BeforeEach
	public void setup() {
	}

	// Puzzle from https://www.sudokuoftheday.com/techniques/naked-pairs-triples
	public static String NAKEDPAIRS =
			"4..27.6..798156234.2.84...7237468951849531726561792843.82.15479.7..243....4.87..2";
	public static String NAKEDPAIRS_SOLUTION =
			"415273698798156234623849517237468951849531726561792843382615479176924385954387162";

	@Test
	public void testNakedPairs() throws ParseException {
		Board board = new Board(NAKEDPAIRS);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);

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
		FindUpdateRule rule = new NakedSubsets(2);
		assertEquals(rule.ruleName(), "NakedSubsets2" );

		// Locations test
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull( encs);
		assertEquals(1, encs.size());
		// int [] encoded = locations.get( 0 );
		// System.out.println(format("Rule %s reports %d find", rule.ruleName(), locations.size()));
        // System.out.println( "Encoded location=" + Arrays.toString(encoded)); // one based

        // Update test
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, new Board( NAKEDPAIRS_SOLUTION ), candidates, encs);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertEquals( 2, updates );
		assertEquals( prevCandidates - 2, candidates.getAllCount());
	}

	public static String NAKEDPARTIALTRIPLE =
			"6..8.27357.235694.3..4.7.621..975.242..183.79.79624..34..56.2.7.6724.3..92.7384.6";
	public static String NAKEDPARTIALTRIPLE_SOLUTION =
			"694812735712356948358497162136975824245183679879624513483561297567249381921738456";

	@Test
	public void testNakedPartialTriple() throws ParseException {
		Board board = new Board(NAKEDPARTIALTRIPLE);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// Pre tests
		// Col test
		int [] combo138 = new int [] {0,2,7};
		List<RowCol> nakedpartialTriples = candidates.candidateComboColLocations( 1, combo138, NAKED, 2 );
		assertEquals(3, nakedpartialTriples.size());
		assertEquals( ROWCOL[1][1], nakedpartialTriples.get( 0 ));
		assertEquals( ROWCOL[3][1], nakedpartialTriples.get( 1 ));
		assertEquals( ROWCOL[6][1], nakedpartialTriples.get( 2 ));

		// Run rule
		FindUpdateRule rule = new NakedSubsets(3);
		assertEquals(rule.ruleName(), "NakedSubsets3" );

		// Locations test
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		assertEquals(1, encs.size());

        // Update test
        int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, new Board( NAKEDPARTIALTRIPLE_SOLUTION ), candidates, encs);
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertEquals( 3, updates );
		assertEquals( prevCandidates - 3, candidates.getAllCount());
	}

	@Test
	public void testEncode() {
		// Given 0-based combo and 0-based locations, return 1-base combo,locations array
		//  0 - one based combo digit for example int 19 == {08}
		//  1* - ones encoded row,col for example int 13 == ROWCOL[0][2]
		int combo = 19;
		List<RowCol> locs = Arrays.asList( ROWCOL[0][2], ROWCOL[0][8]);
		int[] enc = NakedSubsets.encode(Utils.onebasedComboToZeroBasedInts(combo), locs);
		assertNotNull(enc);
		assertEquals(3, enc.length);
		assertEquals( combo, enc[0]);
		assertEquals( 13, enc[1]);
		assertEquals( 19, enc[2]);

		NakedSubsets rule = new NakedSubsets(2);
		String encStr = rule.encodingToString(enc);
		// System.out.println( "enc=" + encStr );
		assertNotNull( encStr );
		assertTrue( encStr.contains( "digits {19}" ));
		assertTrue( encStr.contains( "2 row" ));
		assertTrue( encStr.contains( "locs at [0,2],[0,8]" ));
	}
}