package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;
import info.danbecker.ss.tree.DigitsData;
import info.danbecker.ss.tree.TreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.*;

public class RemotePairsTest {
	@BeforeEach
	public void setup() {
	}

	// This example from https://hodoku.sourceforge.net/en/tech_chains.php#rp
	// Website says there is equivalence between SimpleColorsand RemotePairs chain
	public static String REMOTEPAIR_1 =
			"7984523166.3781.92.12.3.87.37.265.4882.14376..6.897.2398..142371.7.28.5.2...7..81";

	public static String REMOTEPAIR_2 =
			"1786.9.5.93415.6.72567.3.1.79356..41641.3759.8259147365673.1...41..75.6.38.4.6175";

	@Test
	public void testValidTree() throws ParseException {
		// Test a tree with no internal color clashes
		Board board = new Board(REMOTEPAIR_1);
		assertTrue(board.legal());

		// Set up and validate candidates
		RemotePairs rule = new RemotePairs();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// Update r7c678 to match web site. Naked triple in r7.
		candidates.removeCandidates( ROWCOL[7][6], new int[]{3,5,7} );
		candidates.removeCandidates( ROWCOL[7][7], new int[]{3,5,7} );
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertEquals( 56, candidates.getAllCount());
		int[] pair45 = new int[]{4,5}; // one-based
		List<Integer> pair45List = Utils.arrayToList(pair45); // one-based
		int[] zbpair45 = new int[]{3,4}; // zero-based
		assertArrayEquals( pair45, candidates.getRemainingCandidates(ROWCOL[1][1]) );
		assertArrayEquals( pair45, candidates.getRemainingCandidates(ROWCOL[1][6]) );
		assertArrayEquals( pair45, candidates.getRemainingCandidates(ROWCOL[2][0]) );
		assertArrayEquals( pair45, candidates.getRemainingCandidates(ROWCOL[2][8]) );
		assertArrayEquals( pair45, candidates.getRemainingCandidates(ROWCOL[5][0]) );

		// Test tree (no clashes)
		RowCol rootLoc = ROWCOL[1][6];
		TreeNode<DigitsData> pairRoot = new TreeNode<>( new DigitsData(pair45List, rootLoc, 0 ),  3 );
		List<RowCol> pairLocs = candidates.candidateComboAllLocations( zbpair45, Candidates.FULL_COMBI_MATCH);
		assertEquals( 5, pairLocs.size());
        List<int[]> encs = rule.buildPairTree( candidates, pairRoot, pair45List, pairLocs );
		assertNotNull( encs );
		assertEquals( 0, encs.size());

		// Test tree contents
		// pairRoot.printTree();
		assertEquals( 5, pairRoot.size());
		assertEquals( 1, pairRoot.findTreeNodes(new DigitsData.RowColMatch( new DigitsData(pair45List,ROWCOL[1][6],0) )).size() );
		assertEquals( 1, pairRoot.findTreeNodes(new DigitsData.RowColMatch( new DigitsData(pair45List,ROWCOL[1][1],1) )).size() );
		assertEquals( 1, pairRoot.findTreeNodes(new DigitsData.RowColMatch( new DigitsData(pair45List,ROWCOL[2][0],0))).size() );
		assertEquals( 1, pairRoot.findTreeNodes(new DigitsData.RowColMatch( new DigitsData(pair45List,ROWCOL[2][8],1))).size() );
		assertEquals( 1, pairRoot.findTreeNodes(new DigitsData.RowColMatch( new DigitsData(pair45List,ROWCOL[5][0],1))).size() );
	}

	@Test
	public void testValidTree2() throws ParseException {
		// Test a tree with no internal color clashes
		Board board = new Board(REMOTEPAIR_2);
		assertTrue(board.legal());

		// Set up and validate candidates
		RemotePairs rule = new RemotePairs();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertEquals( 57, candidates.getAllCount());
		int[] pair28 = new int[]{2,8}; // one-based
		List<Integer> pair28List = Utils.arrayToList(pair28); // one-based
		int[] zbpair28 = new int[]{1,7}; // zero-based
		assertArrayEquals( pair28, candidates.getRemainingCandidates(ROWCOL[1][5]));
		assertArrayEquals( pair28, candidates.getRemainingCandidates(ROWCOL[1][7]));
		assertArrayEquals( pair28, candidates.getRemainingCandidates(ROWCOL[7][3]));
		assertArrayEquals( new int[]{2,3,4}, candidates.getRemainingCandidates(ROWCOL[0][8]));
		assertArrayEquals( new int[]{2,4,8,9}, candidates.getRemainingCandidates(ROWCOL[6][8]));
		assertArrayEquals( new int[]{2,3,8,9}, candidates.getRemainingCandidates(ROWCOL[7][8]));

		// Test tree (no clashes)
		RowCol rootLoc = ROWCOL[6][7];
		TreeNode<DigitsData> pairRoot = new TreeNode<>( new DigitsData(pair28List, rootLoc, 0 ),  3 );
		List<RowCol> pairLocs = candidates.candidateComboAllLocations( zbpair28, Candidates.FULL_COMBI_MATCH);
		assertEquals( 8, pairLocs.size());
		List<int[]> encs = rule.buildPairTree( candidates, pairRoot, pair28List, pairLocs );
		assertNotNull( encs );
		assertEquals( 0, encs.size());

		// Test tree contents
		// pairRoot.printTree();
		assertEquals( 8, pairRoot.size());
		DigitsData test1 = new DigitsData(pair28List,ROWCOL[6][7],0);
		DigitsData test2 = pairRoot.findTreeNodes(new DigitsData.RowColMatch( test1 )).get(0).data;
		assertEquals( test1, test2 );
		int count = 0;
		for (TreeNode<DigitsData> treeNode : pairRoot ) {
			switch ( count ) {
				case 0 -> { assertEquals( test1, treeNode.data ); }
				case 6 -> { assertEquals( new DigitsData(pair28List,ROWCOL[3][6],0), treeNode.data ); }
			}
			count++;
		}
	}

	@Test
	public void testFindUpdate45() throws ParseException {
		// Valid tree, no internal clashes, remove outside canidates
		Board board = new Board(REMOTEPAIR_1);
		assertTrue(board.legal());

		// Set up and validate candidates
		RemotePairs rule = new RemotePairs();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertEquals( 56, candidates.getAllCount());
		int[] pair45 = new int[]{4,5}; // one-based
		int[] zbpair45 = new int[]{3,4}; // zero-based
		assertArrayEquals( pair45, candidates.getRemainingCandidates(ROWCOL[1][1]) );
		assertArrayEquals( pair45, candidates.getRemainingCandidates(ROWCOL[1][6]) );
		assertArrayEquals( pair45, candidates.getRemainingCandidates(ROWCOL[2][0]) );
		assertArrayEquals( pair45, candidates.getRemainingCandidates(ROWCOL[2][8]) );
		assertArrayEquals( pair45, candidates.getRemainingCandidates(ROWCOL[5][0]) );

		// Test find with specific digits (no clashes)
		int type = 0;
		List<int[]> encs = rule.find(board, candidates, zbpair45);
		assertNotNull(encs);
		assertEquals(1, encs.size());
		int[] enc = encs.get(0);
		// System.out.println( rule.encodingToString( enc ));
		RowCol rootLoc = ROWCOL[5][0];
		RowCol candLoc = ROWCOL[5][6];
		assertEquals(13, enc.length);
		assertEquals( pair45[0], enc[0]);
		assertEquals( pair45[1], enc[1]);
		assertEquals(type, enc[2]);
		assertEquals(rootLoc.row(), enc[3]);
		assertEquals(rootLoc.col(), enc[4]);
		assertEquals(candLoc.row(), enc[5]);
		assertEquals(candLoc.col(), enc[6]);

		// Test update
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, null, candidates, encs);
		// Entries same. Candidate loses 1.
		// Candidates loses 1.
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(1, updates);
		assertEquals(prevCandidates, candidates.getAllCount() + updates);
	}

	@Test
	public void testFindUpdate() throws ParseException {
		// Valid tree, no internal clashes, remove outside canidates
		Board board = new Board(REMOTEPAIR_1);
		assertTrue(board.legal());

		// Set up and validate candidates
		RemotePairs rule = new RemotePairs();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertEquals( 56, candidates.getAllCount());
		int[] pair45 = new int[]{4,5}; // one-based
		assertArrayEquals( pair45, candidates.getRemainingCandidates(ROWCOL[1][6]) );
		assertArrayEquals( pair45, candidates.getRemainingCandidates(ROWCOL[5][0]) );

		// Test find with all digits
		// Finds 45 digits with color trap, 69 digits with no clash
		List<int[]> encs = rule.find(board, candidates );
		assertNotNull(encs);
		assertEquals(1, encs.size());

		// Test update
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, null, candidates, encs);
		// Entries same. Candidate loses 1.
		// Candidates loses 1.
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(1, updates);
		assertEquals(prevCandidates, candidates.getAllCount() + updates);
	}

	@Test
	public void testFindUpdate28() throws ParseException {
		// Valid tree, no internal clashes, remove outside candidates
		Board board = new Board(REMOTEPAIR_2);
		assertTrue(board.legal());

		// Set up and validate candidates
		RemotePairs rule = new RemotePairs();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		assertEquals( 57, candidates.getAllCount());
		int[] pair28 = new int[]{2,8}; // one-based
		int[] zbpair28 = new int[]{1,7}; // zero-based
		assertArrayEquals( pair28, candidates.getRemainingCandidates(ROWCOL[1][5]));
		assertArrayEquals( pair28, candidates.getRemainingCandidates(ROWCOL[1][7]));
		assertArrayEquals( pair28, candidates.getRemainingCandidates(ROWCOL[7][3]));
		assertArrayEquals( new int[]{2,3,4}, candidates.getRemainingCandidates(ROWCOL[0][8]));
		assertArrayEquals( new int[]{2,4,8,9}, candidates.getRemainingCandidates(ROWCOL[6][8]));
		assertArrayEquals( new int[]{2,3,8,9}, candidates.getRemainingCandidates(ROWCOL[7][8]));

		// Test find clash in pairs tree
		int type = 0;
		List<int[]> encs = rule.find(board, candidates, zbpair28);
		assertNotNull(encs);
		assertEquals(12, encs.size());

		int[] enc = encs.get(0);
		assertEquals( 2, enc[0]); // digit0
		assertEquals( 8, enc[1]); // digit1
		assertEquals( 0, enc[2]); // type
		assertEquals( 0, enc[5]); // row
		assertEquals( 6, enc[6]); // col
		enc = encs.get(1);
		assertEquals( 2, enc[0]); // digit0
		assertEquals( 8, enc[1]); // digit1
		assertEquals( 0, enc[2]); // type
		assertEquals( 6, enc[5]); // row
		assertEquals( 4, enc[6]); // col
		enc = encs.get(11);
		assertEquals( 2, enc[0]); // digit0
		assertEquals( 8, enc[1]); // digit1
		assertEquals( 0, enc[2]); // type
		assertEquals( 7, enc[5]); // row
		assertEquals( 8, enc[6]); // col

		// Test update
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, null, candidates, encs);
		// Entries same. Candidate loses 1.
		// Candidates loses 1.
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals( 10, updates);
		assertEquals(prevCandidates, candidates.getAllCount() + updates);
	}
	@Test
	public void testFindUpdate2() throws ParseException {
		// Valid tree, no internal clashes, remove outside candidates
		Board board = new Board(REMOTEPAIR_2);
		assertTrue(board.legal());

		// Set up and validate candidates
		RemotePairs rule = new RemotePairs();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		assertEquals( 57, candidates.getAllCount());
		int[] pair28 = new int[]{2,8}; // one-based
		assertArrayEquals( pair28, candidates.getRemainingCandidates(ROWCOL[1][5]));
		assertArrayEquals( pair28, candidates.getRemainingCandidates(ROWCOL[1][7]));
		assertArrayEquals( pair28, candidates.getRemainingCandidates(ROWCOL[7][3]));
		assertArrayEquals( new int[]{2,3,4}, candidates.getRemainingCandidates(ROWCOL[0][8]));
		assertArrayEquals( new int[]{2,4,8,9}, candidates.getRemainingCandidates(ROWCOL[6][8]));
		assertArrayEquals( new int[]{2,3,8,9}, candidates.getRemainingCandidates(ROWCOL[7][8]));

		// Test find with all digits
		// Finds 57 digits with color trap, XX digits with no clash
		List<int[]> encs = rule.find(board, candidates );
		assertNotNull(encs);
		assertEquals(12, encs.size());

		// Test update
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, null, candidates, encs);
		// Entries same. Candidate loses 1.
		// Candidates loses 1.
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(10, updates);
		assertEquals(prevCandidates, candidates.getAllCount() + updates);
	}

	@Test
	public void testEncode()  {
		// Encode tree as int []
		int[] digits = new int[]{5,6};
		int type = 0;
		RowCol root = ROWCOL[1][1];
		RowCol cLoc = ROWCOL[1][8];
		int fColor = 0;
		RowCol fLoc = ROWCOL[2][2];
		int sColor = 1;
		RowCol sLoc = ROWCOL[7][8];

		int[] enc = RemotePairs.encode(digits, type,	root, cLoc, fColor, fLoc, sColor, sLoc );

		assertEquals( 13, enc.length);
		assertEquals( digits[0], enc[0]);
		assertEquals( digits[1], enc[1]);
		assertEquals( type, enc[2]);
		assertEquals( root.row(), enc[3]);
		assertEquals( root.row(), enc[4]);
		assertEquals( cLoc.row(), enc[5]);
		assertEquals( cLoc.col(), enc[6]);
		assertEquals( fColor, enc[7]);
		assertEquals( fLoc.row(), enc[8]);
		assertEquals( fLoc.col(), enc[9]);
		assertEquals( sColor, enc[10]);
		assertEquals( sLoc.row(), enc[11]);
		assertEquals( sLoc.col(), enc[12]);

		FindUpdateRule rule = new RemotePairs();
		String encString = rule.encodingToString( enc );
		System.out.println( "Enc=" + encString);
		assertTrue( encString.contains( "digits " + digits[0] + "," + digits[1]));
		assertTrue( encString.contains( "color trap"));
		assertTrue( encString.contains( "root " + root));
		assertTrue( encString.contains( "cands at " + cLoc));
		assertTrue( encString.contains( "sees " + fColor));
		assertTrue( encString.contains( "at " + fLoc));
		assertTrue( encString.contains( "and " + sColor));
		assertTrue( encString.contains( "at " + sLoc));
	}
}