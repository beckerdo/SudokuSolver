package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.tree.ColorData;
import info.danbecker.ss.tree.TreeNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Candidates.ALL_COUNTS;
import static info.danbecker.ss.SudokuSolver.runOnce;
import static org.junit.jupiter.api.Assertions.*;

public class SimpleColorsTest {
	@BeforeEach
	public void setup() {
	}

	// SimpleColors example 1 from
	//  * https://hodoku.sourceforge.net/en/tech_col.php
	// candidates in r7c678 go not match website. Naked triple in r7.
	public static String SIMPLECOLORS_DIGIT3CHAIN_DIGIT3GONE =
			"214..6.....79.2..4...4.7.....187..32..269.....48.21..642.7.9861..9168...18624...9";
	public static String SIMPLECOLORS_DIGIT3_SOLUTION =
			"214586397867932514935417628691875432372694185548321976423759861759168243186243759";

	// SimpleColors example 2 from
	//  * https://hodoku.sourceforge.net/en/tech_col.php
	public static String SIMPLECOLORS_DIGIT8CHAIN_DIGIT8GONE =
			"659...13...1.3.6252.3165.49.2..9631.36.7..59.91.3.4.6279.6..2535.6...9811.2...476";
	public static String SIMPLECOLORS_DIGIT8_SOLUTION =
			"659428137481937625273165849827596314364712598915384762798641253546273981132859476";

	@Test
	public void testValidColorTree() throws ParseException {
		// Test a tree with no internal color clashes
		Board board = new Board(SIMPLECOLORS_DIGIT3CHAIN_DIGIT3GONE);
		assertTrue(board.legal());

		SimpleColors rule = new SimpleColors();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// Update r7c678 to match web site. Naked triple in r7.
		candidates.removeCandidates(ROWCOL[7][6], new int[]{3, 5, 7});
		candidates.removeCandidates(ROWCOL[7][7], new int[]{3, 5, 7});
		candidates.removeCandidates(ROWCOL[7][7], new int[]{7});
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// No color clash in pairs tree
		int digit = 3;
		RowCol rootLoc = ROWCOL[0][3];
		TreeNode<ColorData> tree = new TreeNode<>(new ColorData(digit, rootLoc, 0), 3);
		List<int[]> colorClash = rule.buildColorTree(candidates, tree, digit, ALL_COUNTS);

		assertNotNull(colorClash);
		assertEquals(0, colorClash.size());

		// tree.printTree();
		assertEquals(10, tree.size());
		assertEquals(1, tree.findTreeNodes(new ColorData.RowColMatch(new ColorData(digit, ROWCOL[0][3], 0))).size());
		assertEquals(1, tree.findTreeNodes(new ColorData.RowColMatch(new ColorData(digit, ROWCOL[8][6], 0))).size());
		assertEquals(1, tree.findTreeNodes(new ColorData.RowColMatch(new ColorData(digit, ROWCOL[8][5], 1))).size());
		assertEquals(1, tree.findTreeNodes(new ColorData.RowColMatch(new ColorData(digit, ROWCOL[7][8], 1))).size());
	}

	@Test
	public void testColorClashTree() throws ParseException {
		// Test a tree with no internal color clashes
		Board board = new Board(SIMPLECOLORS_DIGIT8CHAIN_DIGIT8GONE);
		assertTrue(board.legal());

		SimpleColors rule = new SimpleColors();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// Update candidates to match web site.
		candidates.removeCandidates(ROWCOL[0][3], new int[]{8});
		candidates.removeCandidates(ROWCOL[0][4], new int[]{8});
		candidates.removeCandidates(ROWCOL[1][3], new int[]{4});
		candidates.removeCandidates(ROWCOL[1][5], new int[]{8});
		candidates.removeCandidates(ROWCOL[3][2], new int[]{4,8});
		candidates.removeCandidates(ROWCOL[3][8], new int[]{8});
		candidates.removeCandidates(ROWCOL[4][4], new int[]{8});
		candidates.removeCandidates(ROWCOL[4][5], new int[]{8});
		candidates.removeCandidates(ROWCOL[5][2], new int[]{8});
		candidates.removeCandidates(ROWCOL[6][4], new int[]{8});
		candidates.removeCandidates(ROWCOL[6][5], new int[]{4});
		candidates.removeCandidates(ROWCOL[7][4], new int[]{2});
		candidates.removeCandidates(ROWCOL[8][5], new int[]{8});
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// No color clash in pairs tree
		int digit = 8;
		int type = 1;
		RowCol rootLoc = ROWCOL[0][5];
		RowCol candLoc = ROWCOL[2][1];
		RowCol sameLoc = ROWCOL[8][1];
		TreeNode<ColorData> tree = new TreeNode<>(new ColorData(digit, rootLoc, 0), 3);
		List<int[]> colorClash = rule.buildColorTree(candidates, tree, digit, ALL_COUNTS);

		assertNotNull(colorClash);
		assertEquals(5, colorClash.size());
		// Test first and last
		int[] enc = colorClash.get(0);
		assertEquals(12, enc.length);
		assertEquals(digit, enc[0]);
		assertEquals(type, enc[1]);
		assertEquals(rootLoc, ROWCOL[enc[2]][enc[3]]);
		assertEquals(candLoc, ROWCOL[enc[4]][enc[5]]);
		assertEquals(sameLoc, ROWCOL[enc[7]][enc[8]]);

		candLoc = ROWCOL[1][3];
		sameLoc = ROWCOL[1][0];
		enc = colorClash.get(colorClash.size() - 1);
		assertEquals(12, enc.length);
		assertEquals(digit, enc[0]);
		assertEquals(type, enc[1]);
		assertEquals(rootLoc, ROWCOL[enc[2]][enc[3]]);
		assertEquals(candLoc, ROWCOL[enc[4]][enc[5]]);
		assertEquals(sameLoc, ROWCOL[enc[7]][enc[8]]);

		// Validate mishappen tree with color clashes? Just root.
		// tree.printTree();
		assertEquals(13, tree.size());
		assertEquals(1, tree.findTreeNodes(new ColorData.RowColMatch(new ColorData(digit, ROWCOL[0][5], 0))).size());
	}

	@Test
	public void testOutsideLocations() throws ParseException {
		// Valid tree, no internal clashes, remove outside canidates
		Board board = new Board(SIMPLECOLORS_DIGIT3CHAIN_DIGIT3GONE);
		assertTrue(board.legal());

		SimpleColors rule = new SimpleColors();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// Update r7c678 to match web site. Naked triple in r7.
		candidates.removeCandidates(ROWCOL[7][6], new int[]{3, 5, 7});
		candidates.removeCandidates(ROWCOL[7][7], new int[]{3, 5, 7});
		candidates.removeCandidates(ROWCOL[7][7], new int[]{7});
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// No color clash in pairs tree
		int digit = 3;
		int type = 0;
		RowCol root = ROWCOL[0][3];
		RowCol cLoc = ROWCOL[0][8];
		List<int[]> encs = rule.find(board, candidates, digit);
		assertNotNull(encs);
		// 4 locations, one dup because of row==box location
		assertEquals(4, encs.size());

		int[] enc = encs.get(0);
		// System.out.println( rule.encodingToString( enc ));

		assertEquals(12, enc.length);
		assertEquals(digit, enc[0]);
		assertEquals(type, enc[1]);
		assertEquals(root.row(), enc[2]);
		assertEquals(root.col(), enc[3]);
		assertEquals(cLoc.row(), enc[4]);
		assertEquals(cLoc.col(), enc[5]);

		// Test
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		int updates = rule.update(board, null, candidates, encs);
		// Entries same. Candidate loses 1.
		// Candidates loses 1.
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(3, updates);
		assertEquals(prevCandidates, candidates.getAllCandidateCount() + updates);
	}

	@Test
	public void testInsideLocations() throws ParseException {
		// Valid tree, no internal clashes, remove outside canidates
		Board board = new Board(SIMPLECOLORS_DIGIT8CHAIN_DIGIT8GONE);
		assertTrue(board.legal());

		SimpleColors rule = new SimpleColors();
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// Update candidates to match web site.
		candidates.removeCandidates(ROWCOL[0][3], new int[]{8});
		candidates.removeCandidates(ROWCOL[0][4], new int[]{8});
		candidates.removeCandidates(ROWCOL[1][3], new int[]{4});
		candidates.removeCandidates(ROWCOL[1][5], new int[]{8});
		candidates.removeCandidates(ROWCOL[3][2], new int[]{4, 8});
		candidates.removeCandidates(ROWCOL[3][8], new int[]{8});
		candidates.removeCandidates(ROWCOL[4][4], new int[]{8});
		candidates.removeCandidates(ROWCOL[4][5], new int[]{8});
		candidates.removeCandidates(ROWCOL[5][2], new int[]{8});
		candidates.removeCandidates(ROWCOL[6][4], new int[]{8});
		candidates.removeCandidates(ROWCOL[6][5], new int[]{4});
		candidates.removeCandidates(ROWCOL[7][4], new int[]{2});
		candidates.removeCandidates(ROWCOL[8][5], new int[]{8});
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// No color clash in pairs tree
		int digit = 8;
		int type = 1;
		RowCol root = ROWCOL[0][5];
		RowCol candLoc = ROWCOL[2][1];
		RowCol sameLoc = ROWCOL[8][1];
		List<int[]> encs = rule.find(board, candidates, digit);
		assertNotNull(encs);
		// 4 locations, one dup because of row==box location
		assertEquals(5, encs.size());

		int[] enc = encs.get(0);
		// System.out.println( rule.encodingToString( enc ));
		assertEquals(12, enc.length);
		assertEquals(digit, enc[0]);
		assertEquals(type, enc[1]);
		assertEquals(root, ROWCOL[enc[2]][enc[3]]);
		assertEquals(candLoc, ROWCOL[enc[4]][enc[5]]);
		assertEquals(sameLoc, ROWCOL[enc[7]][enc[8]]);

		// Test
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCandidateCount();
		int updates = rule.update(board, null, candidates, encs);
		// Entries same. Candidate loses 1.
		// Candidates loses 1.
		assertEquals(prevEntries, candidates.getAllOccupiedCount());
		assertEquals(3, updates);
		assertEquals(prevCandidates, candidates.getAllCandidateCount() + updates);
	}

	@Test
	public void testEncode() {
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
				root, cLoc, fColor, fLoc, sColor, sLoc);

		assertEquals(12, enc.length);
		assertEquals(digit, enc[0]);
		assertEquals(type, enc[1]);
		assertEquals(root.row(), enc[2]);
		assertEquals(root.row(), enc[3]);
		assertEquals(cLoc.row(), enc[4]);
		assertEquals(cLoc.col(), enc[5]);
		assertEquals(fColor, enc[6]);
		assertEquals(fLoc.row(), enc[7]);
		assertEquals(fLoc.col(), enc[8]);
		assertEquals(sColor, enc[9]);
		assertEquals(sLoc.row(), enc[10]);
		assertEquals(sLoc.col(), enc[11]);

		FindUpdateRule rule = new SimpleColors();
		String encString = rule.encodingToString(enc);
		System.out.println("Enc=" + encString);
		assertTrue(encString.contains("digit " + digit));
		assertTrue(encString.contains("color trap"));
		assertTrue(encString.contains("tree " + root));
		assertTrue(encString.contains("cand " + cLoc));
		assertTrue(encString.contains("sees " + fColor));
		assertTrue(encString.contains("at " + fLoc));
		assertTrue(encString.contains("and " + sColor));
		assertTrue(encString.contains("at " + sLoc));
	}

	// https://www.sudokuoftheday.com/techniques/x-wings
	// This puzzle appears to have additional candidates removed
	public static String COLOR_CHAIN_SOTD =
			"...5...39-.39.41257-..5.3968.-.9.32..6.-..895674.-....1892.-.461.53..-.87.9.51.-.5.......";
	public static String COLOR_CHAIN_SOTD_SOLUTION =
			"871562439639841257425739681594327168218956743763418925946185372387294516152673894";

	// https://www.thonky.com/sudoku/simple-coloring
	// Opposite Colors in the Same Unit
	// Digit 4 chain
	public static String COLOR_CHAIN_TH2 =
			"..85.21.3-35...12.8-.21.3..5.-56324.7.1-4821.753.-179.53..2-.3..2581.-8.731..25-215.843..";

	// https://www.thonky.com/sudoku/simple-coloring
	// Color appears twice in Same Unit
	// Digit 2 chain starting from [3,0], clash at [3,3]
	public static String COLOR_CHAIN_TH1 =
			"123...587-..5817239-987...164-.51..8473-39.75.618-7.81..925-.76...891-53..81746-81..7.352";
	public static String COLOR_CHAIN_TH1_SOLUTION =
			"123496587465817239987532164651928473392754618748163925276345891539281746814679352";

	// https://www.thonky.com/sudoku/simple-coloring
	// Seeing two opposite colors
	// Digit 2 chain
	public static String COLOR_CHAIN_TH3 =
			"..463.5..-6.54.1..3-37..5964.-938.6.154-457198362-216345987-.435.6.19-.6.9.34.5-5.9.14.36";
	public static String COLOR_CHAIN_TH3_SOLUTION =
			"194632578685471293372859641938267154457198362216345987843526719761983425529714836";

	@Test
	public void testSuite() throws ParseException {
		String[][] suites = new String[][]{
				// new String[]{ "COLOR_CHAIN_SOTD", COLOR_CHAIN_SOTD, COLOR_CHAIN_SOTD_SOLUTION, "0", "2" },
				new String[]{"COLOR_CHAIN_TH3", COLOR_CHAIN_TH3, COLOR_CHAIN_TH3_SOLUTION, "6", "4"},
				new String[]{"COLOR_CHAIN_TH1", COLOR_CHAIN_TH1, COLOR_CHAIN_TH1_SOLUTION, "3", "3"}
		};

		for (int suitei = 0; suitei < suites.length; suitei++) {
			String[] suite = suites[suitei];
			runSuite(suite);
		}
	}

	public void runSuite(String[] suite) throws ParseException {
		String name = suite[0];
		Board board = new Board(suite[1]);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, new Board(suite[2]), candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		int[] results = runOnce(board, candidates, suite[2], new SimpleColors());
		assertEquals(Integer.parseInt(suite[3]), results[0]); // find count
		assertEquals(Integer.parseInt(suite[4]), results[1]); // update count
	}

}
