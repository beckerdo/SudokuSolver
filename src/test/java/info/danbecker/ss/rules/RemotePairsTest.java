package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.tree.ColorData;
import info.danbecker.ss.tree.TreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Candidates.ALL_COUNTS;
import static info.danbecker.ss.SudokuSolver.runOnce;
import static org.junit.jupiter.api.Assertions.*;

public class RemotePairsTest {
	@BeforeEach
	public void setup() {
	}
	// This example from https://hodoku.sourceforge.net/en/tech_chains.php#rp
	// Website says there is equivalence between SimpleColorsand RemotePaits chain
	public static String REMOTEPAIR_1 =
		"7984523166.3781.92.12.3.87.37.265.4882.14376..6.897.2398..142371.7.28.5.2...7..81";

	@Test
	public void testValidTree() throws ParseException {
		// Test a tree with no internal color clashes
		Board board = new Board(REMOTEPAIR_1);
		assertTrue(board.legal());

		SimpleColors rule = new SimpleColors();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// Update r7c678 to match web site. Naked triple in r7.
		candidates.removeCandidates( ROWCOL[7][6], new int[]{3,5,7} );
		candidates.removeCandidates( ROWCOL[7][7], new int[]{3,5,7} );
		candidates.removeCandidates( ROWCOL[7][7], new int[]{7} );
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// No color clash in pairs tree
		int digit = 3;
		RowCol rootLoc = ROWCOL[0][3];
		TreeNode<ColorData> tree = new TreeNode<>(new ColorData(digit, rootLoc, 0), 3);
		List<int[]> colorClash = rule.buildColorTree(candidates, tree, digit, ALL_COUNTS);

		assertNotNull( colorClash );
		assertEquals( 0, colorClash.size());

		// tree.printTree();
		assertEquals( 10, tree.size());
		assertEquals( 1, tree.findTreeNodes(new ColorData.RowColMatch( new ColorData(digit,ROWCOL[0][3],0) )).size() );
		assertEquals( 1, tree.findTreeNodes(new ColorData.RowColMatch( new ColorData(digit,ROWCOL[8][6],0) )).size() );
		assertEquals( 1, tree.findTreeNodes(new ColorData.RowColMatch( new ColorData(digit,ROWCOL[8][5],1))).size() );
		assertEquals( 1, tree.findTreeNodes(new ColorData.RowColMatch( new ColorData(digit,ROWCOL[7][8],1))).size() );
	}

	@Test
	public void testFindUpdate() throws ParseException {
		// Valid tree, no internal clashes, remove outside canidates
		Board board = new Board(REMOTEPAIR_1);
		assertTrue(board.legal());

		FindUpdateRule rule = new RemotePairs();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// No color clash in pairs tree
//		int digit = 5;
//		int type = 0;
//		RowCol root = ROWCOL[1][1];
//		RowCol cLoc = ROWCOL[5][6];
		List<int[]> encs = rule.find(board, candidates );
		assertNotNull( encs );
		assertEquals( 2, encs.size());
//		int[] enc = encs.get(0);
//		System.out.println( rule.encodingToString( enc ));
//		System.out.println( rule.encodingToString( encs.get(1) ));
//
//		assertEquals( 12, enc.length);
//		assertEquals( digit, enc[0]);
//		assertEquals( type, enc[1]);
//		assertEquals( root.row(), enc[2]);
//		assertEquals( root.col(), enc[3]);
//		assertEquals( cLoc.row(), enc[4]);
//		assertEquals( cLoc.col(), enc[5]);

		// Test
//		int prevEntries = candidates.getAllOccupiedCount();
//		int prevCandidates = candidates.getAllCandidateCount();
//		int updates = rule.update(board, null, candidates, encs);
//		// Entries same. Candidate loses 1.
//		// Candidates loses 1.
//		assertEquals( prevEntries, candidates.getAllOccupiedCount() );
//		assertEquals( 1, updates);
//		assertEquals( prevCandidates, candidates.getAllCandidateCount() + updates);
	}
	@Test
	public void testEncode()  {
		// Encode tree as int []
		int digit = 3;
		int type = 0;
		RowCol root = ROWCOL[1][1];
		RowCol cLoc = ROWCOL[1][8];
		int fColor = 0;
		RowCol fLoc = ROWCOL[2][2];
		int sColor = 1;
		RowCol sLoc = ROWCOL[7][8];

		int[] enc = SimpleColors.encode(digit, type,
				root, cLoc, fColor, fLoc, sColor, sLoc );

		assertEquals( 12, enc.length);
		assertEquals( digit, enc[0]);
		assertEquals( type, enc[1]);
		assertEquals( root.row(), enc[2]);
		assertEquals( root.row(), enc[3]);
		assertEquals( cLoc.row(), enc[4]);
		assertEquals( cLoc.col(), enc[5]);
		assertEquals( fColor, enc[6]);
		assertEquals( fLoc.row(), enc[7]);
		assertEquals( fLoc.col(), enc[8]);
		assertEquals( sColor, enc[9]);
		assertEquals( sLoc.row(), enc[10]);
		assertEquals( sLoc.col(), enc[11]);

		FindUpdateRule rule = new SimpleColors();
		String encString = rule.encodingToString( enc );
		System.out.println( "Enc=" + encString);
		assertTrue( encString.contains( "digit " + digit));
		assertTrue( encString.contains( "color trap"));
		assertTrue( encString.contains( "tree " + root));
		assertTrue( encString.contains( "cand " + cLoc));
		assertTrue( encString.contains( "sees " + fColor));
		assertTrue( encString.contains( "at " + fLoc));
		assertTrue( encString.contains( "and " + sColor));
		assertTrue( encString.contains( "at " + sLoc));
	}

}
