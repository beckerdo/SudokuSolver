package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.*;

public class XYChainTest {
	@BeforeEach
	public void setup() {
	}

	// This example from https://hodoku.sourceforge.net/en/tech_chains.php#xyc
	public static String XYCHAIN_1 =
			"361749528584...79.792.....4923574.8.416...357857631249678...4121452879..239416875";
	public static String XYCHAIN_1_SOLUTION =
			"361749528584162793792853164923574681416928357857631249678395412145287936239416875";
	@Test
	public void testFindUpdate1() throws ParseException {
		Board board = new Board(XYCHAIN_1);
		assertTrue(board.legal());

		// Set up and validate candidates
		XYChain rule = new XYChain();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertEquals( 41, candidates.getAllCount());
		int[] pair39 = new int[]{3,9}; // one-based
		assertArrayEquals( pair39, candidates.getRemainingCandidates(ROWCOL[6][3]) );
		int[] pair23 = new int[]{2,3}; // one-based
		assertArrayEquals( pair23, candidates.getRemainingCandidates(ROWCOL[1][5]) );

		// Test find with specific digits (no clashes)
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		assertEquals(10, encs.size());
		int[] enc = encs.get(0);
		// digits {89}, end digit 9, chain [4,3],[4,5],[1,5],[6,5],[6,4], sees [4,4],[6,3]
		String encString=  rule.encodingToString( enc );
		assertTrue( encString.contains( "digits {89}" ));
		assertTrue( encString.contains( "end digit 9" ));
		assertTrue( encString.contains( "chain " + RowCol.toString( Arrays.asList( ROWCOL[4][3],ROWCOL[4][5],ROWCOL[1][5],ROWCOL[6][5],ROWCOL[6][4] ) )));
		assertTrue( encString.contains( "sees " + RowCol.toString( Arrays.asList( ROWCOL[4][4],ROWCOL[6][3] ) )));
		enc = encs.get(encs.size() - 1);
		// digits {29}, end digit 2, chain [4,4],[6,4],[6,5],[1,5], sees [1,4],[4,5]
		encString=  rule.encodingToString( enc );
		assertTrue( encString.contains( "digits {29}" ));
		assertTrue( encString.contains( "end digit 2" ));
		assertTrue( encString.contains( "chain " + RowCol.toString( Arrays.asList( ROWCOL[4][4],ROWCOL[6][4],ROWCOL[6][5],ROWCOL[1][5] ) )));
		assertTrue( encString.contains( "sees " + RowCol.toString( Arrays.asList( ROWCOL[1][4],ROWCOL[4][5] ) )));

		// Test update
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, new Board( XYCHAIN_1_SOLUTION ), candidates, encs);
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(11, updates);
		assertEquals(prevCandidates, candidates.getAllCount() + updates);
	}

	// This example from https://hodoku.sourceforge.net/en/tech_chains.php#xyc
	public static String XYCHAIN_2 =
			"57.4.169.9482761536.....74...9...3.44...935.63.5...9.1254367819......265196528437";
	public static String XYCHAIN_2_SOLUTION =
			"572431698948276153613859742769185324421793586385642971254367819837914265196528437";
	@Test
	public void testFindUpdate2() throws ParseException {
		Board board = new Board(XYCHAIN_2);
		assertTrue(board.legal());

		// Set up and validate candidates
		XYChain rule = new XYChain();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertEquals( 76, candidates.getAllCount());
		int[] pair48 = new int[]{4,8}; // one-based
		assertArrayEquals( pair48, candidates.getRemainingCandidates(ROWCOL[5][4]) );
		int[] pair89 = new int[]{8,9}; // one-based
		assertArrayEquals( pair89, candidates.getRemainingCandidates(ROWCOL[2][3]) );

		// Test find with specific digits (no clashes)
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		assertEquals(4, encs.size());
		int[] enc = encs.get(0);
		String encString=  rule.encodingToString( enc );
		// System.out.println( encString );
		// digits {89}, end digit 8, chain [2,3],[2,5],[3,5],[5,5],[5,4], sees [0,4],[2,4],[3,3],[4,3],[5,3]
		assertTrue( encString.contains( "digits {89}" ));
		assertTrue( encString.contains( "end digit 8" ));
		assertTrue( encString.contains( "chain " + RowCol.toString( Arrays.asList( ROWCOL[2][3],ROWCOL[2][5],ROWCOL[3][5],ROWCOL[5][5],ROWCOL[5][4] ) )));
		assertTrue( encString.contains( "sees " + RowCol.toString( Arrays.asList( ROWCOL[0][4],ROWCOL[2][4],ROWCOL[3][3],ROWCOL[4][3],ROWCOL[5][3] ) )));
		enc = encs.get(encs.size() - 1);
		encString=  rule.encodingToString( enc );
		// System.out.println( encString );
		// digits {28}, end digit 2, chain [2,8],[2,3],[7,3],[7,4],[5,4],[0,4],[0,2], sees [0,8],[2,1],[2,2]
		assertTrue( encString.contains( "digits {28}" ));
		assertTrue( encString.contains( "end digit 2" ));
		assertTrue( encString.contains( "chain " + RowCol.toString( Arrays.asList( ROWCOL[2][8],ROWCOL[2][3],ROWCOL[7][3],ROWCOL[7][4],ROWCOL[5][4],ROWCOL[0][4],ROWCOL[0][2] ) )));
		assertTrue( encString.contains( "sees " + RowCol.toString( Arrays.asList( ROWCOL[0][8],ROWCOL[2][1],ROWCOL[2][2] ) )));

		// Test update
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, new Board( XYCHAIN_2_SOLUTION ), candidates, encs);
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(8, updates);
		assertEquals(prevCandidates, candidates.getAllCount() + updates);
	}

	// This example from https://www.sudopedia.org/wiki/XY-Chain
	public static String XYCHAIN_3 =
			"32...648..684.1.35.4.8.369.854267913.3.185.4.217..456847...2859.82..9374593748126";
	public static String XYCHAIN_3_SOLUTION =
			"321596487968471235745823691854267913639185742217934568476312859182659374593748126";
	@Test
	public void testFindUpdate3() throws ParseException {
		Board board = new Board(XYCHAIN_3);
		assertTrue(board.legal());

		// Set up and validate candidates
		XYChain rule = new XYChain();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertEquals( 51, candidates.getAllCount());
		int[] pairR = new int[]{1,5}; // one-based
		assertArrayEquals( pairR, candidates.getRemainingCandidates(ROWCOL[2][2]) );
		int[] pairE = new int[]{1,5}; // one-based
		assertArrayEquals( pairE, candidates.getRemainingCandidates(ROWCOL[7][4]) );

		// Test find with specific digits (no clashes)
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		assertEquals(10, encs.size());
		int[] enc = encs.get(0);
		String encString=  rule.encodingToString( enc );
		// System.out.println( encString );
		// digits {79}, end digit 9, chain [1,0],[2,0],[7,0],[7,3],[7,4],[6,4],[5,4], sees [1,4]
		assertTrue( encString.contains( "digits {79}" ));
		assertTrue( encString.contains( "end digit 9" ));
		assertTrue( encString.contains( "chain " + RowCol.toString( Arrays.asList( ROWCOL[1][0],ROWCOL[2][0],ROWCOL[7][0],ROWCOL[7][3],ROWCOL[7][4],ROWCOL[6][4],ROWCOL[5][4] ) )));
		assertTrue( encString.contains( "sees " + RowCol.toString( Collections.singletonList(ROWCOL[1][4]))));
		enc = encs.get(encs.size() - 1);
		encString=  rule.encodingToString( enc );
		// digits {39}, end digit 9, chain [5,4],[6,4],[6,2],[7,0],[2,0],[1,0], sees [1,4]
		// System.out.println( encString );
		assertTrue( encString.contains( "digits {39}" ));
		assertTrue( encString.contains( "end digit 9" ));
		assertTrue( encString.contains( "chain " + RowCol.toString( Arrays.asList( ROWCOL[5][4],ROWCOL[6][4],ROWCOL[6][2],ROWCOL[7][0],ROWCOL[2][0],ROWCOL[1][0] ) )));
		assertTrue( encString.contains( "sees " + RowCol.toString( Collections.singletonList(ROWCOL[1][4]) )));

		// Test update
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, new Board( XYCHAIN_3_SOLUTION ), candidates, encs);
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(4, updates);
		assertEquals(prevCandidates, candidates.getAllCount() + updates);
	}

	@Test
	public void testEncode()  {
		// Encode tree as int []
		List<Integer> digits = Arrays.asList( 3, 9 );
		int epDigit = 3;
		List<RowCol> chainList = Arrays.asList( ROWCOL[6][3], ROWCOL[4][3], ROWCOL[4][5], ROWCOL[1][5] );
		List<RowCol> seesList = Arrays.asList( ROWCOL[1][3], ROWCOL[2][3], ROWCOL[6][5] );
		int[] enc = XYChain.encode(digits, epDigit, chainList, seesList );

		assertEquals( digits.size() + 2 + chainList.size() *2 + seesList.size() * 2, enc.length);
		assertEquals( digits.get(0), enc[0]);
		assertEquals( digits.get(1), enc[1]);
		assertEquals( epDigit, enc[2]);
		assertEquals( chainList.size(), enc[3]);
		assertEquals( chainList.get(0).row(), enc[4]);
		assertEquals( chainList.get(0).col(), enc[5]);
		assertEquals( chainList.get(3).row(), enc[10]);
		assertEquals( chainList.get(3).col(), enc[11]);
		assertEquals( seesList.get(0).row(), enc[12]);
		assertEquals( seesList.get(0).col(), enc[13]);
		assertEquals( seesList.get(2).row(), enc[16]);
		assertEquals( seesList.get(2).col(), enc[17]);

		FindUpdateRule rule = new XYChain();
		String encString = rule.encodingToString( enc );
		// System.out.println( "Enc=" + encString);
		assertTrue( encString.contains( "digits {" + digits.get(0) + digits.get(1) + "}" ));
		assertTrue( encString.contains( "digit " + epDigit + "," ));
		assertTrue( encString.contains( "chain " + RowCol.toString( chainList ) ));
		assertTrue( encString.contains( "sees " + RowCol.toString( seesList ) ));
	}
}