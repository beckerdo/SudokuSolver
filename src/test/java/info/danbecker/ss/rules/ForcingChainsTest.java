package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.graph.GraphDisplay;
import info.danbecker.ss.graph.GraphUtils;
import info.danbecker.ss.graph.LabelEdge;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.graph.GraphUtilsTest.EPP_BILCOCYCLE_REPEAT_FIG7_SOLUTION;
import static info.danbecker.ss.graph.GraphUtilsTest.EPP_BILOCCYCLE_REPEAT_FIG7;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test suite validates many examples from
 * Epstein "NonRepetitive Paths and Cycles..." at https://arxiv.org/abs/cs/0507053
 * SOTD has an example at https://www.sudokuoftheday.com/techniques/forcing-chains
 * Thonky has ColoringChains examples at https://www.thonky.com/sudoku/simple-coloring
 * <p>
 * See GraphUtils for testing the graph algorithms rather than
 * the ForcingChains find encode update functions.
 */
public class ForcingChainsTest {
	@BeforeEach
	public void setup() {
	}

	@Test
	public void testBiLocFig7() throws ParseException {
		Board board = new Board(EPP_BILOCCYCLE_REPEAT_FIG7);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringFocus( false, ALL_DIGITS, ALL_COUNTS ));

		ForcingChains rule = new ForcingChains();
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		assertEquals(2, encs.size());
		for( int enci = 0; enci < encs.size(); enci++ ){
			int [] enc = encs.get( enci );
			// Repeats from same digit, same loc, different pathId
			System.out.printf( "%s found %s%n", rule.ruleName(),  rule.encodingToString( enc ) );
			switch( enci ) {
				case 0: {
					assertEquals( 9, enc[2]);
					assertEquals( ROWCOL[5][6], ROWCOL[enc[3]][enc[4]]);
					break; }
				case 1: {
					assertEquals( 4, enc[2]);
					assertEquals( ROWCOL[6][1], ROWCOL[enc[3]][enc[4]]);
					break; }
			}
		}

        // Update test
        int prevEntries = board.getOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		final int[] updates = {0};
		assertDoesNotThrow( ()-> updates[0] = rule.update(board, new Board(EPP_BILCOCYCLE_REPEAT_FIG7_SOLUTION), candidates, encs));
		assertEquals( updates[0], board.getOccupiedCount() - prevEntries + prevCandidates - candidates.getAllCount());
	}

	@Test
	public void testEncodeDecode() {
		// Middle box double pair
		int digit = 5;
		int pathId = 13;
		RowCol loc = ROWCOL[0][1];
		RowCol prev = ROWCOL[2][2];
		RowCol next = ROWCOL[8][1];

		int [] enc = ForcingChains.encode( 0, pathId, digit, loc, prev, next );
		assertEquals(9, enc.length );
		assertEquals(0, enc[0]);
		assertEquals(13, enc[1]);
		assertEquals(5, enc[2]);
		assertEquals( loc.row(), enc[3]);
		assertEquals( loc.col(), enc[4]);
		assertEquals( prev.row(), enc[5]);
		assertEquals( prev.col(), enc[6]);
		assertEquals( next.row(), enc[7]);
		assertEquals( next.col(), enc[8]);

		// Test the string
		FindUpdateRule rule = new ForcingChains();
		String encStr = rule.encodingToString(enc);
		// System.out.println( "Enc=" +encString);
		assertNotNull(encStr);
		assertTrue(encStr.contains("cycle"));
		assertTrue(encStr.contains("repeat"));
		assertTrue(encStr.contains("pathId " + pathId));
		assertTrue(encStr.contains("digit " + digit));
		assertTrue(encStr.contains( loc.toString() ));
		assertTrue(encStr.contains( prev.toString() ));
		assertTrue(encStr.contains( next.toString() ));
	}

	// Issue taken from 20230103-diabolical-24250.json
	public static String P20230103_TH_ORIG =
			"...2..6.3-...37.85.-..6.1..4.-..89.....-21.....69-.....12..-.9..8.1..-.51.32...-6.3..5...";
	// Error state
	public static String P20230103_TH =
			"...2..6.3-...37685.-..6.1..4.-.6892....-21.....69-....612..-.9268.135-.51.32..6-6.3195.28";
	public static String P20230103_TH_SOLUTION =
			"187254693-924376851-536819742-768923514-215748369-349561287-492687135-851432976-673195428";
	public static final String P20230103_CANDSTR = """
		{1578}{78}{57}     {-2}{45}{489}    {-6}{179}{-3}
		{149}{24}{49}      {-3}{-7}{-6}     {-8}{-5}{12}
		{3578}{2378}{-6}   {58}{-1}{89}     {79}{-4}{27}

		{457}{-6}{-8}      {-9}{-2}{347}    {35}{17}{147}
		{-2}{-1}{457}      {4578}{45}{3478} {35}{-6}{-9}
		{34579}{347}{4579} {457}{-6}{-1}    {-2}{78}{478}

		{47}{-9}{-2}       {-6}{-8}{47}     {-1}{-3}{-5}
		{478}{-5}{-1}      {47}{-3}{-2}     {479}{789}{-6}
		{-6}{478}{-3}      {-1}{-9}{-5}     {47}{-2}{78}""";

	@Test
	public void testP20230103() throws ParseException, InterruptedException {
		Board board = new Board(P20230103_TH);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(P20230103_CANDSTR);
		// Need to specify candidates specifically. Many steps from running 20230103-diabolical-24250.json
		// System.out.println( "Candidates=\n" + candidates.toStringFocus( false, ALL_DIGITS, ALL_COUNTS ));

		Graph<RowCol, LabelEdge> bilocGraph = GraphUtils.getBilocGraph( candidates );
		// DisplayGraph will cause test case to not exit. Use only for debugging.
		// new GraphDisplay( "BiLoc Graph ", 0, bilocGraph );
		List<GraphPath<RowCol,LabelEdge>> gpl = GraphUtils.getGraphPaths( bilocGraph);
		for( int gpi = 0; gpi < gpl.size(); gpi++ ) {
			boolean includeAll = true;
			if (includeAll || Arrays.asList(12).contains(gpi)) { // Interesting or error
				GraphPath<RowCol, LabelEdge> gp = gpl.get(gpi);
				// String label = "Path " + gpi + "=" + GraphUtils.pathToString(gp, "-", false);
				// System.out.println(label);
				// new GraphDisplay(label, gpi, gp);
				int finalGpi = gpi;
				assertDoesNotThrow( ()->ForcingChains.findRepetitiveCycle33(candidates, finalGpi, gp) );
			}
		}

		ForcingChains rule = new ForcingChains();
		// Locations test
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		assertEquals(0, encs.size());
		for( int enci = 0; enci < encs.size(); enci++ ){
			int [] enc = encs.get( enci );
			System.out.printf( "%s found %s%n", rule.ruleName(),  rule.encodingToString( enc ) );
		}

		// Thread will not exit when launching DisplayGraph. Use ExecutorService
		// Thread.currentThread().join(); // Wait for threads to exit

		// Update test
		int prevEntries = board.getOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		final int[] updates = {0};
		assertDoesNotThrow( ()-> updates[0] = rule.update(board, new Board(P20230103_TH_SOLUTION), candidates, encs));
		assertEquals( updates[0], board.getOccupiedCount() - prevEntries + prevCandidates - candidates.getAllCount());

		// Thread will not exit when launching DisplayGraph. Use ExecutorService
		// Thread.currentThread().join(); // Wait for threads to exit
	}

	public static String P20230108_FORCINGCHAIN_51797 =
			"75.8.....-8...7.2.5-..39.54..-..8547..6-..5.817..-1..39.5..-5824.93..-..1.5.824-.....8.59";

	// @Test
	public void test20230108() throws ParseException {
		Board board = new Board(P20230108_FORCINGCHAIN_51797);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// Need to update candidates to get it into the proper state
		candidates.removeCandidates( ROWCOL[0][4], new int[]{1,6});
		candidates.removeCandidates( ROWCOL[0][5], new int[]{6});
		candidates.removeCandidates( ROWCOL[1][1], new int[]{9});
		candidates.removeCandidates( ROWCOL[1][5], new int[]{6});
		candidates.removeCandidates( ROWCOL[1][7], new int[]{1});
		candidates.removeCandidates( ROWCOL[2][4], new int[]{2});
		candidates.removeCandidates( ROWCOL[2][7], new int[]{1,6});
		candidates.removeCandidates( ROWCOL[2][8], new int[]{1});
		candidates.removeCandidates( ROWCOL[4][0], new int[]{2});
		candidates.removeCandidates( ROWCOL[4][1], new int[]{2});
		candidates.removeCandidates( ROWCOL[5][1], new int[]{2});
		candidates.removeCandidates( ROWCOL[8][4], new int[]{1,6});
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		ForcingChains rule = new ForcingChains();
		// Locations test
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		assertEquals(6, encs.size());

        // Update test
        int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, null, candidates, encs);
		// Destination gains entry, loses 2 candidates.
		// Will likely knock out more same-unit candidates after LegalCandidate update.
		assertEquals( prevEntries, candidates.getAllOccupiedCount() - updates);
		assertEquals( prevCandidates, candidates.getAllCount() + 2);
	}

	public static String P20230103 =
			"...2..6.3-...37.85.-..6.1..4.-..89.....-21.....69-.....12..-.9..8.1..-.51.32...-6.3..5...";
	public static String P20230103_PARTIAL_INVALID =
			".8.2496.3-...37685.-3.651894.-.689235..-21.85.369-.3..6128.-.9268.135-851.32.96-6.3195.28"; // invalid (first row)
	public static String P20230103_PARTIAL =
			"1872..693924376851536.1..42768923514215..83693.95612874926871358514329.6673195428";
	public static String P20230103_SOLUTION =
			"187254693924376851536819742768923514215748369349561287492687135851432976673195428";

	// @Test
	public void testBug20230103() throws ParseException {
		Board board = new Board(P20230103_PARTIAL);
		assertTrue(board.legal());
		Board solution = new Board(P20230103_SOLUTION);
		assertTrue(solution.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// Need to run a few other rules to get to the test place
//		i38.12 Board entries=57, candidates=49
//		ForcingChains rowCol [0,0] both candidates {15} lead to digit 7 at [6,0], tree depths=11,18
//		Rule ForcingChains reports 1 possible locations: [0,0]
//		Exception in thread "main" java.lang.IllegalArgumentException: Rule ForcingChains would like set digit 7 (solution=4) at loc [6,0].
//			at info.danbecker.ss.rules.ForcingChains.updateCandidates(ForcingChains.java:75)
//			at info.danbecker.ss.SudokuSolver.solve(SudokuSolver.java:238)
//			at info.danbecker.ss.SudokuSolver.main(SudokuSolver.java:131)

		ForcingChains rule = new ForcingChains();
		// Locations test
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		assertEquals(6, encs.size());

		// Update test
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		final int[] updates = {0};
		assertDoesNotThrow( ()-> updates[0] = rule.update(board, new Board(P20230103_SOLUTION), candidates, encs));
		assertEquals( updates[0], board.getOccupiedCount() - prevEntries + prevCandidates - candidates.getAllCount());
	}
}