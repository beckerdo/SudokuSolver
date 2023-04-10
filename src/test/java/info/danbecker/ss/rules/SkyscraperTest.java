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

public class SkyscraperTest {

	@BeforeEach
	public void setup() {
	}

	// Puzzles from https://hodoku.sourceforge.net/en/tech_sdp.php#sk
	public static String SKYSCRAPERROW_BASER4C58_ROOFR0R2_REMOVER0C67R2C34 =
		"697.....2..1972.63..3..679.912...6.737426.95.8657.9.241486932757.9.24..6..68.7..9";


	public static String SKYSCRAPERCOL_BASER17C0_ROOFC4C3_REMOVER0C3 =
		"..1.28759-.879.5132-952173486-.2.7..34.-...5..27.-714832695-....9.817-.78.51963-19..87524";
	@Test
	public void testRow() throws ParseException {
		Board board = new Board(SKYSCRAPERROW_BASER4C58_ROOFR0R2_REMOVER0C67R2C34);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertTrue(candidates.isCandidate( ROWCOL[4][5], 1));
		assertTrue(candidates.isCandidate( ROWCOL[4][8], 1));
		assertTrue(candidates.isCandidate( ROWCOL[0][5], 1));
		assertTrue(candidates.isCandidate( ROWCOL[2][8], 1));

		FindUpdateRule rule = new Skyscraper();
		List<int[]> encs = rule.find( board, candidates );
		assertNotNull( encs );
		assertEquals( 1, encs.size() );

		int[] enc = encs.get( 0 );
		assertNotNull( enc );
		assertEquals( 9, enc.length);
		assertEquals( 1, enc[0]);
		assertEquals( 56, enc[1]);
		assertEquals( 59, enc[2]);
		assertEquals( 16, enc[3]);
		assertEquals( 39, enc[4]);
		assertEquals( 17, enc[5]);
		assertEquals( 18, enc[6]);
		assertEquals( 34, enc[7]);
		assertEquals( 35, enc[8]);

		int updates = rule.update( board, null, candidates, encs );
		assertEquals( 4, updates );
	}

	@Test
	public void testCol() throws ParseException {
		Board board = new Board(SKYSCRAPERCOL_BASER17C0_ROOFC4C3_REMOVER0C3);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertTrue(candidates.isCandidate( ROWCOL[1][0], 4));
		assertTrue(candidates.isCandidate( ROWCOL[7][0], 4));
		assertTrue(candidates.isCandidate( ROWCOL[1][4], 4));
		assertTrue(candidates.isCandidate( ROWCOL[7][3], 4));

		FindUpdateRule rule = new Skyscraper();
		List<int[]> encs = rule.find( board, candidates );
		assertNotNull( encs );
		assertEquals( 1, encs.size() );

		int[] enc = encs.get( 0 );
		assertNotNull( enc );
		assertEquals( 6, enc.length);
		assertEquals( 4, enc[0]);
		assertEquals( 21, enc[1]);
		assertEquals( 81, enc[2]);
		assertEquals( 25, enc[3]);
		assertEquals( 84, enc[4]);
		assertEquals( 14, enc[5]);

		int updates = rule.update( board, null, candidates, encs );
		assertEquals( 1, updates );
	}

	@Test
	public void encode() {
		int digit = 1;
		RowCol[] base = new RowCol[]{ ROWCOL[4][5], ROWCOL[4][8] };
		RowCol[] roof = new RowCol[]{ ROWCOL[0][5], ROWCOL[2][8] };
		RowCol[] locs = new RowCol[]{ ROWCOL[2][3], ROWCOL[2][4], ROWCOL[0][6],ROWCOL[0][7]};
		int[] enc = Skyscraper.encodeLocation( digit, base, roof, RowCol.toList(locs) );

		assertNotNull( enc );
		assertEquals( 9, enc.length);
		assertEquals( 1, enc[0]);
		assertEquals( 56, enc[1]);
		assertEquals( 59, enc[2]);
		assertEquals( 16, enc[3]);
		assertEquals( 39, enc[4]);
		assertEquals( 34, enc[5]);
		assertEquals( 35, enc[6]);
		assertEquals( 17, enc[7]);
		assertEquals( 18, enc[8]);

		FindUpdateRule rule = new Skyscraper();
		String loc = rule.encodingToString( enc );
		// System.out.println( "Loc=" + loc);
		assertTrue ( loc.contains( "digit " + digit ));
		assertTrue ( loc.contains( "row " ));
		assertTrue ( loc.contains( "[4,5]" ));
		assertTrue ( loc.contains( "[0,7]" ));
	}
}