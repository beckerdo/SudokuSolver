package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.*;

public class XYWingTest {

	@BeforeEach
	public void setup() {
	}

	// Puzzles from https://hodoku.sourceforge.net/en/tech_wings.php#xy
	public static String XYWING_PIVOT57 =
		"8..36.9....9.1.863.63.89..5924673158386951724571824396432196587698537......248639";
	// ....6........1.863..3..9...9.4......3.....7.457.82.........658.69...7.......4..3. // original soudoku
    // 857362941249715863163489275924673158386951724571824396432196587698537412715248639 // solution

	@Test
	public void testPivot57() throws ParseException {
		Board board = new Board(XYWING_PIVOT57);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertTrue(candidates.containsOnlyDigits( ROWCOL[0][2], new int[]{5,7})); // xy pivot
		assertTrue(candidates.containsOnlyDigits( ROWCOL[0][5], new int[]{2,5})); // xz pincer
		assertTrue(candidates.containsOnlyDigits( ROWCOL[1][0], new int[]{2,7})); // yz pincer
		assertTrue(candidates.isCandidate( ROWCOL[1][5], 2)); // removal candidate

		FindUpdateRule rule = new XYWing();
		List<int[]> encs = rule.find( board, candidates );
		assertNotNull( encs );
		assertEquals( 3, encs.size() );

		int[] enc = encs.get( 0 );
		assertNotNull( enc );
		assertEquals( 7, enc.length);
		assertEquals( 5, enc[0]);
		assertEquals( 7, enc[1]);
		assertEquals( 2, enc[2]);
		assertEquals( 13, enc[3]);
		assertEquals( 16, enc[4]);
		assertEquals( 21, enc[5]);
		assertEquals( 26, enc[6]);

		int updates = rule.update( board, null, candidates, encs );
		assertEquals( 1, updates );

		// Location to string
		String encString = rule.encodingToString( enc );
		assertTrue( encString.contains("xyz=572") );
		assertTrue( encString.contains("xyLoc=[0,2]") );
		assertTrue( encString.contains("xzLoc=[0,5]") );
		assertTrue( encString.contains("yzLoc=[1,0]") );
		assertTrue( encString.contains("locs=[1,5]") );
	}

	// Should rotate this one if you want to test col/box
	public static String XYWING_PIVOT16 =
			"714.6.5388..453..7356718429....24.854.....3.2285376941978631254.....7..6........3";
	//	".1....5.8...4.3....567.........2..8.4.....3.22..376..19.8...254.....7...........3"; // original sudoku
	@Test
	public void testPivot16() throws ParseException {
		Board board = new Board(XYWING_PIVOT16);
		board = Board.rotateRight( board );
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertTrue(candidates.containsOnlyDigits( ROWCOL[0][5], new int[]{1,6})); // xy pivot
		assertTrue(candidates.containsOnlyDigits( ROWCOL[3][5], new int[]{1,9})); // xz pincer
		assertTrue(candidates.containsOnlyDigits( ROWCOL[1][4], new int[]{6,9})); // yz pincer
		assertTrue(candidates.isCandidate( ROWCOL[1][5], 9)); // removal candidates
		assertTrue(candidates.isCandidate( ROWCOL[2][5], 9)); // removal candidates
		assertTrue(candidates.isCandidate( ROWCOL[3][4], 9)); // removal candidates
		assertTrue(candidates.isCandidate( ROWCOL[4][4], 9)); // removal candidates
		assertTrue(candidates.isCandidate( ROWCOL[5][4], 9)); // removal candidates

		FindUpdateRule rule = new XYWing();
		List<int[]> encs = rule.find( board, candidates );
		assertNotNull( encs );
		assertEquals( 1, encs.size() );

		int[] enc = encs.get( 0 );
		assertNotNull( enc );
		assertEquals( 11, enc.length);
		assertEquals( 1, enc[0]);
		assertEquals( 6, enc[1]);
		assertEquals( 9, enc[2]);
		assertEquals( 16, enc[3]);
		assertEquals( 46, enc[4]);
		assertEquals( 25, enc[5]);
		assertEquals( 26, enc[6]);
		assertEquals( 36, enc[7]);
		assertEquals( 45, enc[8]);
		assertEquals( 55, enc[9]);
		assertEquals( 65, enc[10]);

		int updates = rule.update( board, null, candidates, encs );
		assertEquals( 5, updates );

		// Location to string
		String encString = rule.encodingToString( enc );
		assertTrue( encString.contains("xyz=169") );
		assertTrue( encString.contains("xyLoc=[0,5]") );
		assertTrue( encString.contains("xzLoc=[3,5]") );
		assertTrue( encString.contains("yzLoc=[1,4]") );
		assertTrue( encString.contains("locs=[1,5],[2,5],[3,4],[4,4],[5,4]") );
	}

	@Test
	public void encode() {
		int[] xyzDigits = new int[]{1,5,8};
		RowCol xy = ROWCOL[0][2];
		RowCol xz = ROWCOL[0][5];
		RowCol yz = ROWCOL[1][0];
		RowCol[] locs  = new RowCol[]{ ROWCOL[1][5] };
		int[] enc = XYWing.encodeLocation( xyzDigits, xy, xz, yz, RowCol.toList(locs));

		assertNotNull( enc );
		assertEquals( 7, enc.length);
		assertEquals( xyzDigits[0], enc[0]);
		assertEquals( xyzDigits[1], enc[1]);
		assertEquals( xyzDigits[2], enc[2]);
		assertEquals( 13, enc[3]);
		assertEquals( 16, enc[4]);
		assertEquals( 21, enc[5]);
		assertEquals( 26, enc[6]);

		FindUpdateRule rule = new XYWing();
		String loc = rule.encodingToString( enc );
		// System.out.println( "enc=" + loc);
		assertTrue ( loc.contains( "xyz=" + xyzDigits[0] + xyzDigits[1] + xyzDigits[2]  ));
		assertTrue ( loc.contains( "xyLoc=[0,2]" ));
		assertTrue ( loc.contains( "xzLoc=[0,5]" ));
		assertTrue ( loc.contains( "yzLoc=[1,0]" ));
		assertTrue ( loc.contains( "z locs=[1,5]" ));
	}
}