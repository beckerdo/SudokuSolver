package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

public class WWingTest {
	@BeforeEach
	public void setup() {
	}

	// This example from https://hodoku.sourceforge.net/en/tech_wings.php#w
	public static String WWING_1 =
			"9251346878..65943243672895164..1.8..15.48..6.3.8.6..145..276348263841...784395126";
	public static String WWING_1_SOLUTION =
			"925134687817659432436728951642513879159487263378962514591276348263841795784395126";
	@Test
	public void testFindUpdate1() throws ParseException {
		Board board = new Board(WWING_1);
		assertTrue(board.legal());

		// Set up and validate candidates
		WWing rule = new WWing();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertEquals( 46, candidates.getAllCandidateCount());
		int[] pair59 = new int[]{5,9}; // one-based
		assertArrayEquals( pair59, candidates.getRemainingCandidates(ROWCOL[3][3]) );
		assertArrayEquals( pair59, candidates.getRemainingCandidates(ROWCOL[5][3]) );
		assertArrayEquals( pair59, candidates.getRemainingCandidates(ROWCOL[7][8]) );

		// Test find with specific digits (no clashes)
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		assertEquals(3, encs.size());
		int[] enc = encs.get( encs.size() - 1);
		System.out.println( rule.encodingToString( enc ));
		int nslDigit = enc[11];
		assertEquals(5, nslDigit);
		assertEquals(ROWCOL[3][8], ROWCOL[enc[12]][enc[13]]);

		// Test update
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		int updates = rule.update(board, new Board( WWING_1_SOLUTION ), candidates, encs);
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(3, updates);
		assertEquals(prevCandidates, candidates.getAllCandidateCount() + updates);
	}

	// This puzzle from https://hodoku.sourceforge.net/en/tech_wings.php#w
	public static String WWING_2 =
			"6..95..7...9.2.....58.31...164389752...175946597246..8925417683...562.....6893...";
	public static String WWING_2_SOLUTION =
			"612958374379624815458731269164389752283175946597246138925417683831562497746893521";
	@Test
	public void testFindUpdate2() throws ParseException {
		Board board = new Board(WWING_2);
		assertTrue(board.legal());

		// Set up and validate candidates
		WWing rule = new WWing();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertEquals( 94, candidates.getAllCandidateCount());
		int[] pair14 = new int[]{1,4}; // one-based
		assertArrayEquals( pair14, candidates.getRemainingCandidates(ROWCOL[0][8]) );
		assertArrayEquals( pair14, candidates.getRemainingCandidates(ROWCOL[7][6]) );

		// Test find with specific digits (no clashes)
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		assertEquals(1, encs.size());
		int[] enc = encs.get( encs.size() - 1);
		System.out.println( rule.encodingToString( enc ));
		int nslDigit = enc[11];
		assertEquals(4, nslDigit);
		assertEquals(ROWCOL[0][6], ROWCOL[enc[12]][enc[13]]);
		assertEquals(ROWCOL[1][6], ROWCOL[enc[14]][enc[15]]);
		assertEquals(ROWCOL[2][6], ROWCOL[enc[16]][enc[17]]);
		assertEquals(ROWCOL[7][8], ROWCOL[enc[18]][enc[19]]);
		assertEquals(ROWCOL[8][8], ROWCOL[enc[20]][enc[21]]);

		// Test update
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		int updates = rule.update(board, new Board( WWING_2_SOLUTION ), candidates, encs);
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(5, updates);
		assertEquals(prevCandidates, candidates.getAllCandidateCount() + updates);
	}

	// This example from https://www.sudopedia.org/wiki/W-Wing
	public static String WWING_3 =
			"8..14..6..6358974...4.62...6..43..2......6....8..51..6..861.95.591...63..46.95..7";
	public static String WWING_3_SOLUTION =
			"859147263263589741174362895617438529935726418482951376728613954591874632346295187";
	@Test
	public void testFindUpdate3() throws ParseException {
		Board board = new Board(WWING_3);
		assertTrue(board.legal());

		// Set up and validate candidates
		WWing rule = new WWing();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		candidates.removeCandidates(ROWCOL[5][2], new int[]{9} );
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertEquals( 137, candidates.getAllCandidateCount());
		int[] pair27 = new int[]{2,7}; // one-based
		assertArrayEquals( pair27, candidates.getRemainingCandidates(ROWCOL[4][4]) );
		assertArrayEquals( pair27, candidates.getRemainingCandidates(ROWCOL[5][2]) );

		// Test find with specific digits (no clashes)
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		assertEquals(1, encs.size());
		int[] enc = encs.get( encs.size() - 1);
		System.out.println( rule.encodingToString( enc ));
		int nslDigit = enc[11];
		assertEquals(2, nslDigit);
		assertEquals(ROWCOL[4][0], ROWCOL[enc[12]][enc[13]]);
		assertEquals(ROWCOL[4][1], ROWCOL[enc[14]][enc[15]]);
		assertEquals(ROWCOL[4][2], ROWCOL[enc[16]][enc[17]]);
		assertEquals(ROWCOL[5][3], ROWCOL[enc[18]][enc[19]]);

		// Test update
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		int updates = rule.update(board, new Board( WWING_3_SOLUTION ), candidates, encs);
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(4, updates);
		assertEquals(prevCandidates, candidates.getAllCandidateCount() + updates);
	}

	@Test
	public void testEncode()  {
		// Encode tree as int []
		int[] digits = new int[]{5,9};
		RowCol ep1 = ROWCOL[3][3];
		RowCol ep2 = ROWCOL[7][8];
		int slDigit = 9;
		RowCol sl1 = ROWCOL[3][7];
		RowCol sl2 = ROWCOL[7][7];
		int nslDigit = 5;
		List<RowCol> nslRowCols = new ArrayList<>();
		nslRowCols.add(ROWCOL[3][8]);
		int[] enc = WWing.encode(digits, ep1,ep2, slDigit, sl1,sl2, nslDigit, nslRowCols );

		assertEquals( digits[0], enc[0]);
		assertEquals( digits[1], enc[1]);
		assertEquals(ep1,ROWCOL[enc[2]][enc[3]]);
		assertEquals(ep2,ROWCOL[enc[4]][enc[5]]);
		assertEquals(slDigit,enc[6]);
		assertEquals(sl1,ROWCOL[enc[7]][enc[8]]);
		assertEquals(sl2,ROWCOL[enc[9]][enc[10]]);
		assertEquals(nslDigit,enc[11]);
		RowCol nslLoc = ROWCOL[3][8];
		assertEquals(nslLoc,ROWCOL[enc[12]][enc[13]]);

		FindUpdateRule rule = new WWing();
		String encString = rule.encodingToString( enc );
		System.out.println( "Enc=" + encString);
		assertTrue( encString.contains( "Digits {" + digits[0] + digits[1] + "}"));
		assertTrue( encString.contains( format("at %s%s",ep1, ep2)));
		assertTrue( encString.contains( format("digit %d at %s%s", slDigit, sl1,sl2)));
		assertTrue( encString.contains( format("sees %d at %s", nslDigit, nslLoc )));
	}
}