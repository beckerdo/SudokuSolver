package info.danbecker.ss.rules;

import info.danbecker.ss.RowCol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SwordfishTest {
	// https://www.thonky.com/sudoku/sword-fish
	// Candidates match
	public static String SWORDFISH_TH_D8ROWS037 = """
.5..3.6.2
642895317
.37.2.8..
.235.47..
4.6...52.
571962483
214...9..
76.1.9234
3..24.17.
""";
	
	// Needs some extra steps to equal canidates
	public static String SWORDFISH_SOTD_D5COLS018 = """
238..9.7.
...23.9.8
69.8.5342
.62....9.
9.5.2.1.6
.839..42.
3145.2..9
8.6.932..
.296.....
""";

	@BeforeEach
	public void setup() {
	}

	@Test
	public void testRowMatch() throws ParseException {
		Board board = new Board(SWORDFISH_TH_D8ROWS037);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		Swordfish rule = new Swordfish();
		// Locations test
		// List<int[]> encodings = rule.locations(board, candidates, 8);
		List<int[]> encodings = rule.locations(board, candidates );
		assertTrue(null != encodings);
		assertEquals(1, encodings.size());
				
		// Swordfish found digit 8 rowa at locs=..., extra locs=...
		int[] enc = encodings.get(0);
		int digit = enc[ 0 ];
   		assertEquals(8, digit );
		int rowCol = enc[ 1 ];
   		assertEquals(0, rowCol);
   		
		List<RowCol> swLocs = new ArrayList<>();
		List<RowCol> exLocs = new ArrayList<>();
		
		Swordfish.decode(enc, swLocs, exLocs);
   		assertEquals(9, swLocs.size());
   		assertEquals(3, exLocs.size());
				
        // Update test
        int prevEntries = candidates.entryCount();
		int prevCandidates = candidates.candidateCount();
		rule.updateCandidates(board, null, candidates, encodings);
		assertEquals( prevEntries, candidates.entryCount());
		assertEquals( prevCandidates,candidates.candidateCount() + exLocs.size());		
	}
	
	@Test
	public void testColMatch() throws ParseException {
		Board board = new Board(SWORDFISH_TH_D8ROWS037);
		assertTrue(board.legal());
		// Same as testRowMatch with rotated bopard
		board = Board.rotateRight(board);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		Swordfish rule = new Swordfish();
		
		// Locations test
		List<int[]> encodings = rule.locations(board, candidates, 8);
		// List<int[]> encodings = rule.locations(board, candidates );
		assertTrue(null != encodings);
		assertEquals(1, encodings.size());
				
		// Swordfish found digit 8 rowa at locs=..., extra locs=...
		int[] enc = encodings.get(0);
		int digit = enc[ 0 ];
   		assertEquals(8, digit );
		int rowCol = enc[ 1 ];
   		assertEquals(1, rowCol);
   		
		List<RowCol> swLocs = new ArrayList<>();
		List<RowCol> exLocs = new ArrayList<>();
		
		Swordfish.decode(enc, swLocs, exLocs);
   		assertEquals(9, swLocs.size());
   		assertEquals(3, exLocs.size());
				
        // Update test
        int prevEntries = candidates.entryCount();
		int prevCandidates = candidates.candidateCount();
		rule.updateCandidates(board, null, candidates, encodings);
		assertEquals( prevEntries, candidates.entryCount());
		assertEquals( prevCandidates,candidates.candidateCount() + exLocs.size());		
	}
	
	@Test
	public void testEncodeDecode() {
		// Middle box double pair
		int digit = 1;	
		int rowCol = 0;
		
		List<RowCol> swLocs = new LinkedList<>();
		swLocs.add(ROWCOL[1][1]);
		swLocs.add(ROWCOL[1][4]);
		swLocs.add(ROWCOL[1][7]);
		swLocs.add(ROWCOL[4][1]);
		swLocs.add(ROWCOL[4][4]);
		swLocs.add(ROWCOL[4][7]);
		swLocs.add(ROWCOL[7][1]);
		swLocs.add(ROWCOL[7][4]);
		swLocs.add(ROWCOL[7][7]);
		List<RowCol> exLocs = new LinkedList<>();
		exLocs.add(ROWCOL[0][1]);
		exLocs.add(ROWCOL[8][7]);
		
		int [] enc = Swordfish.encode(digit, rowCol, swLocs, exLocs);
		
		int decDigit = enc[ 0 ];
   		assertEquals(digit, decDigit);
		int decRowCol = enc[ 1 ];
   		assertEquals(rowCol, decRowCol);
   		
		List<RowCol> decswLocs = new ArrayList<>();
		List<RowCol> decexLocs = new ArrayList<>();
		
		Swordfish.decode(enc, decswLocs, decexLocs);
   		assertEquals(swLocs.size(), decswLocs.size());
   		assertEquals(swLocs.get(0), decswLocs.get(0));
   		assertEquals(swLocs.get(swLocs.size()-1), decswLocs.get(decswLocs.size()-1));
   		assertEquals(exLocs.size(), decexLocs.size());
   		assertEquals(exLocs.get(0), decexLocs.get(0));
   		assertEquals(exLocs.get(exLocs.size()-1), decexLocs.get(decexLocs.size()-1));

   		// Test the string
   		String encString = Swordfish.encodingToString(enc);
   		// System.out.println( "Enc=" +encString);
   		assertTrue(null != encString);
   		assertTrue(encString.contains("digit " + digit));
   		assertTrue(encString.contains("rows"));
   		assertTrue(encString.contains("locs="));
   		assertTrue(encString.contains("size=" + exLocs.size()));
	}
	

}