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

public class TwoStringKiteTest {

	@BeforeEach
	public void setup() {
	}

	// Puzzles from https://hodoku.sourceforge.net/en/tech_sdp.php#sk
	public static String TWOSTRINGKITE_R1C3_REMOVEOVER1C3 =
	".81.2.6...42.6..89.568..24.69314275842835791617568932451..3689223...846.86.2.....";

	public static String TWOSTRINGKITE_R5C6_REMOVEOVER5C6 =
	"3617..295842395671.5.2614831.8526.34625....18.341..5264..61.85258...2167216857349";

	public static String DUALTWOSTRINGKITE_R1C1_REMOVEOVER8C3R5C7 =
	"32.5479.6..6213.5..4569823.5..472.....79.1.25..28.57..214359678673184592.5.726143";

	@Test
	public void testRow() throws ParseException {
		Board board = new Board(TWOSTRINGKITE_R1C3_REMOVEOVER1C3);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertTrue(candidates.isCandidate( ROWCOL[1][3], 5));
		assertTrue(candidates.isCandidate( ROWCOL[1][6], 5));
		assertTrue(candidates.isCandidate( ROWCOL[7][3], 5));
		assertTrue(candidates.isCandidate( ROWCOL[7][8], 5));
		assertTrue(candidates.isCandidate( ROWCOL[8][6], 5));

		UpdateCandidatesRule rule = new TwoStringKite();
		List<int[]> encs = rule.locations( board, candidates );
		assertNotNull( encs );
		assertEquals( 1, encs.size() );

		int[] enc = encs.get( 0 );
		assertNotNull( enc );
		assertEquals( 6, enc.length);
		assertEquals( 5, enc[0]);
		assertEquals( 89, enc[1]);
		assertEquals( 97, enc[2]);
		assertEquals( 84, enc[3]);
		assertEquals( 27, enc[4]);
		assertEquals( 24, enc[5]);

		int updates = rule.updateCandidates( board, null, candidates, encs );
		assertEquals( 1, updates );

		// Location to string
		String encString = TwoStringKite.locationToString( enc );
		assertTrue( encString.contains("digit 5") );
		assertTrue( encString.contains("[1,3]") );
		assertTrue( encString.contains("[1,6]") );
		assertTrue( encString.contains("[7,3]") );
		assertTrue( encString.contains("[7,8]") );
		assertTrue( encString.contains("[8,6]") );
	}

	@Test
	public void testCol() throws ParseException {
		Board board = new Board(TWOSTRINGKITE_R5C6_REMOVEOVER5C6);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertTrue(candidates.isCandidate( ROWCOL[3][1], 9));
		assertTrue(candidates.isCandidate( ROWCOL[5][0], 9));
		assertTrue(candidates.isCandidate( ROWCOL[5][5], 9));
		assertTrue(candidates.isCandidate( ROWCOL[6][1], 9));
		assertTrue(candidates.isCandidate( ROWCOL[6][5], 9));

		UpdateCandidatesRule rule = new TwoStringKite();
		List<int[]> encs = rule.locations( board, candidates );
		assertNotNull( encs );
		assertEquals( 1, encs.size() );

		int[] enc = encs.get( 0 );
		assertNotNull( enc );
		assertEquals( 6, enc.length);
		assertEquals( 9, enc[0]);
		assertEquals( 61, enc[1]);
		assertEquals( 42, enc[2]);
		assertEquals( 66, enc[3]);
		assertEquals( 72, enc[4]);
		assertEquals( 76, enc[5]);

		int updates = rule.updateCandidates( board, null, candidates, encs );
		assertEquals( 1, updates );
	}

	@Test
	public void testDualString() throws ParseException {
		Board board = new Board(DUALTWOSTRINGKITE_R1C1_REMOVEOVER8C3R5C7);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertTrue(candidates.isCandidate( ROWCOL[0][2], 1));
		assertTrue(candidates.isCandidate( ROWCOL[0][7], 1));
		assertTrue(candidates.isCandidate( ROWCOL[2][0], 1));
		assertTrue(candidates.isCandidate( ROWCOL[2][8], 1));
		assertTrue(candidates.isCandidate( ROWCOL[3][2], 1));
		assertTrue(candidates.isCandidate( ROWCOL[3][8], 1));
		assertTrue(candidates.isCandidate( ROWCOL[5][0], 1));
		assertTrue(candidates.isCandidate( ROWCOL[5][7], 1));

		UpdateCandidatesRule rule = new TwoStringKite();
		List<int[]> encs = rule.locations( board, candidates );
		assertNotNull( encs );
		assertEquals( 2, encs.size() );

		int[] enc = encs.get( 0 );
		assertNotNull( enc );
		assertEquals( 6, enc.length);
		assertEquals( 1, enc[0]);
		assertEquals( 13, enc[1]);
		assertEquals( 31, enc[2]);
		assertEquals( 18, enc[3]);
		assertEquals( 61, enc[4]);
		assertEquals( 68, enc[5]);

		int updates = rule.updateCandidates( board, null, candidates, encs );
		assertEquals( 1, updates );

		encs = rule.locations( board, candidates );
		assertNotNull( encs );
		assertEquals( 1, encs.size() );

		enc = encs.get( 0 );
		assertNotNull( enc );
		assertEquals( 6, enc.length);
		assertEquals( 1, enc[0]);
		assertEquals( 31, enc[1]);
		assertEquals( 13, enc[2]);
		assertEquals( 39, enc[3]);
		assertEquals( 43, enc[4]);
		assertEquals( 49, enc[5]);

		updates = rule.updateCandidates( board, null, candidates, encs );
		assertEquals( 1, updates );
	}

	@Test
	public void encode() {
		int digit = 5;
		RowCol[] base = new RowCol[]{ ROWCOL[8][6], ROWCOL[7][8] };
		RowCol[] roof = new RowCol[]{ ROWCOL[1][6], ROWCOL[7][3] };
		RowCol rem = ROWCOL[1][3];
		int[] enc = TwoStringKite.encodeLocation( digit, base, roof, rem );

		assertNotNull( enc );
		assertEquals( 6, enc.length);
		assertEquals( 5, enc[0]);
		assertEquals( 97, enc[1]);
		assertEquals( 89, enc[2]);
		assertEquals( 27, enc[3]);
		assertEquals( 84, enc[4]);
		assertEquals( 24, enc[5]);

		String loc = Skyscraper.locationToString( enc );
		// System.out.println( "Loc=" + loc);
		assertTrue ( loc.contains( "digit " + digit ));
		assertTrue ( loc.contains( "box " ));
		assertTrue ( loc.contains( "[8,6]" ));
		assertTrue ( loc.contains( "[1,3]" ));
	}
}