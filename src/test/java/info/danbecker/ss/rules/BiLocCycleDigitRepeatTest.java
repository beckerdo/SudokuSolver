package info.danbecker.ss.rules;

import info.danbecker.ss.*;
import info.danbecker.ss.graph.GraphUtils;
import info.danbecker.ss.graph.LabelEdge;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.rules.BiLocCycleDigitRepeat.BILOCCYCLE_DIGIT_REPEAT;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test suite validates many examples from
 * Epstein "NonRepetitive Paths and Cycles..." at https://arxiv.org/abs/cs/0507053
 * SOTD has an example at https://www.sudokuoftheday.com/techniques/forcing-chains
 * Thonky has ColoringChains examples at https://www.thonky.com/sudoku/simple-coloring
 * <p>
 * See GraphUtils for testing the graph algorithms rather than
 * the BiLocCycleDigitRepeat find encode update functions.
 */
public class BiLocCycleDigitRepeatTest {
	@BeforeEach
	public void setup() {
	}

	@Test
	public void testEncodeDecode() {
		// Middle box double pair
		int digit = 5;
		int pathId = 13;
		RowCol loc = ROWCOL[0][1];
		RowCol prev = ROWCOL[2][2];
		RowCol next = ROWCOL[8][1];

		int [] enc = BiLocCycleDigitRepeat.encode(BILOCCYCLE_DIGIT_REPEAT, pathId, digit, loc, prev, next );
		assertEquals(9, enc.length );
		assertEquals(BILOCCYCLE_DIGIT_REPEAT, enc[0]);
		assertEquals(13, enc[1]);
		assertEquals(5, enc[2]);
		assertEquals( loc.row(), enc[3]);
		assertEquals( loc.col(), enc[4]);
		assertEquals( prev.row(), enc[5]);
		assertEquals( prev.col(), enc[6]);
		assertEquals( next.row(), enc[7]);
		assertEquals( next.col(), enc[8]);

		// Test the string
		FindUpdateRule rule = new BiLocCycleDigitRepeat();
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

	// From David Eppstein "Nonrepetetive Paths..." https://arxiv.org/abs/cs/0507053
	public static String EPP_BILOCCYCLE_REPEAT_FIG7 =
			"..318679559647231818795364297.321.64.1.86..3736.5.7.216.17.528.7..61845.85.23.176";
	public static String EPP_BILCOCYCLE_REPEAT_FIG7_SOLUTION =
			"423186795-596472318-187953642-975321864-214869537-368547921-641795283-732618459-859234176";

	@Test
	public void testBiLocFig7() throws ParseException, InterruptedException {
		Board board = new Board(EPP_BILOCCYCLE_REPEAT_FIG7);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed() );
		// System.out.println( "Candidates=\n" + candidates.toStringFocus( false, ALL_DIGITS, ALL_COUNTS ));

		// Graph<RowCol,LabelEdge> bilocGraph = GraphUtils.getBilocGraph( candidates );
		// System.out.println( "Biloc graph, " + GraphUtils.graphToString( bilocGraph, null, "\n" ) );
		// DisplayGraph will cause test case to not exit. Use only for debugging.
		// new GraphDisplay( "BiLoc Graph ", 0, bilocGraph );
		// List<GraphPath<RowCol,LabelEdge>> gpl = GraphUtils.getGraphCycles( bilocGraph);
		// for( int gpi = 0; gpi < gpl.size(); gpi++ ) {
			// GraphPath<RowCol, LabelEdge> gp = gpl.get(gpi);
			// String label = "Path " + gpi + "=" + GraphUtils.pathToString( gp, "-", false );
			// System.out.println( label );
			// new GraphDisplay( label, gpi, gp );
		//}
		// This thread will not exit when launching DisplayGraph. Use ExecutorService
		// Thread.currentThread().join(); // Wait for threads to exit

		BiLocCycleDigitRepeat rule = new BiLocCycleDigitRepeat();
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		int expectedEncs = 0; // Disallowed cycle with multiDigit label.
		if (expectedEncs != encs.size() ) {
			for ( int i = 0; i < encs.size(); i++ )
				System.out.println( "Enc " + i + "=" + rule.encodingToString(encs.get(i)));
		}
		assertEquals(expectedEncs, encs.size());
		for( int enci = 0; enci < encs.size(); enci++ ){
			int [] enc = encs.get( enci );
			// Repeats from same digit, same loc, different pathId
			// System.out.printf( "%s found %s%n", rule.ruleName(),  rule.encodingToString( enc ) );
			switch( enci ) {
				case 0 -> {
					assertEquals( 9, enc[2]);
					assertEquals( ROWCOL[5][6], ROWCOL[enc[3]][enc[4]]);
					}
				case 1 -> {
					assertEquals( 4, enc[2]);
					assertEquals( ROWCOL[6][1], ROWCOL[enc[3]][enc[4]]);
					}
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
	public void testBiLocFig7Runner() throws ParseException {
		assertTrue( TestUtils.testRunner(
			new BiLocCycleDigitRepeat(),
			EPP_BILOCCYCLE_REPEAT_FIG7,
			EPP_BILCOCYCLE_REPEAT_FIG7_SOLUTION,
			null, // candStr
			false, // displayCands
		    0, // expectedFinds,
			false, // displayEncs
//			Arrays.asList(
//				new int[] { 0, 0, 9, 5, 6 },
//				new int[] { 0, 0, 4, 6, 1 }
//			),
			new ArrayList<>() {},
			new Utils.SubsetComparator(Arrays.asList( 2, 3, 4 )), // encComparator
			0, // expectedOccs,
		    0, // expectedCands,
		    0 // expectedUpdates
//			2, // expectedOccs,
//			4, // expectedCands,
//			6 // expectedUpdates
		));
	}

	public static String EPP_BILOC_FIG5=
		"53..76.2912639.57..7952..63263.5.9477..2.93569457632813.4685792697432..5.5291763.";
	public static String EPP_BILOC_FIG5_SOLUTION =
		"538176429126394578479528163263851947781249356945763281314685792697432815852917634";
	@Test
	public void testBiLocFig5() throws ParseException, InterruptedException {
		// Set up test board and candidates
		// FIG5 is not a good puzzle. It can be solved with 4 SingleCandidates calls.
		// The graph paths do not equal what is shown in figure 5.
		Board board = new Board(EPP_BILOC_FIG5);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringFocus( false, ALL_DIGITS, ALL_COUNTS ));

		Graph<RowCol,LabelEdge> bilocGraph = GraphUtils.getBilocGraph( candidates );
		// DisplayGraph will cause test case to not exit. Use only for debugging.
		// new GraphDisplay( "BiLoc Graph ", 0, bilocGraph );
		List<GraphPath<RowCol,LabelEdge>> gpl = GraphUtils.getGraphCycles( bilocGraph);
		for( int gpi = 0; gpi < gpl.size(); gpi++ ) {
			GraphPath<RowCol, LabelEdge> gp = gpl.get(gpi);
			// System.out.println( "Path " + gpi + "=" + GraphUtils.pathToString( gp, "-", false ) );
			// new GraphDisplay( label, gpi, gp );
		}
		assertEquals(2, gpl.size());
		// This thread will not exit when launching DisplayGraph. Use ExecutorService
		// Thread.currentThread().join(); // Wait for threads to exit
	}

	// Waiting for the implementation of 3.4 conflicting path rule
	public static String EPP_BILOCCYCLE_ENDPOINTS_FIG8 =
			"53..76.2912639.57..7952..63263.5.9477..2.93569457632813.4685792697432..5.5291763.";

	public static String EPP_BILOCCYCLE_ENDPOINTS_FIG8_SOLUTION =
			"538176429-126394578-479528163-263851947-781249356-945763281-314685792-697432815-852917634";

	// Waiting for the implementation of 3.5 bivalue graph
	public static String EPP_BIVALUE_FIG9 =
			"53..76.2912639.57..7952..63263.5.9477..2.93569457632813.4685792697432..5.5291763.";

	public static String EPP_BIVALUE_FIG9_SOLUTION =
			"538176429-126394578-479528163-263851947-781249356-945763281-314685792-697432815-852917634";

	public static String EPP_DIFFICULT_FIG11 =
			"5....1..8......6......6257..9.2.51....4.1.3....83.9.2..7698......5......8..1....3";
	public static String EPP_DIFFICULT_FIG11_SOLUTION =
			"562731948741598632983462571397245186254816397618379425176983254435627819829154763";


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
		List<GraphPath<RowCol,LabelEdge>> gpl = GraphUtils.getGraphCycles( bilocGraph);
		for( int gpi = 0; gpi < gpl.size(); gpi++ ) {
			boolean includeAll = true;
			if (includeAll || Arrays.asList(12).contains(gpi)) { // Interesting or error
				GraphPath<RowCol, LabelEdge> gp = gpl.get(gpi);
				// System.out.println( "Path " + gpi + "=" + GraphUtils.pathToString(gp, "-", false) );
				// new GraphDisplay(label, gpi, gp);
				int finalGpi = gpi;
				assertDoesNotThrow( ()-> BiLocCycleDigitRepeat.findCycleRepeatDigit33(finalGpi, gp) );
			}
		}

		BiLocCycleDigitRepeat rule = new BiLocCycleDigitRepeat();
		// Locations test
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		int expectedEncs = 0;
		if (expectedEncs != encs.size() ) {
			for ( int i = 0; i < encs.size(); i++ )
				System.out.println( "Enc " + i + "=" + rule.encodingToString(encs.get(i)));
		}
		assertEquals(expectedEncs, encs.size());

		// Thread will not exit when launching DisplayGraph. Use ExecutorService
		// Thread.currentThread().join(); // Wait for threads to exit

		// Update test
		int prevOccs = board.getOccupiedCount();
		int prevCands = candidates.getAllCount();
		int expectedOccs = 0;
		int expectedCands = 0;
		int expectedUpdates = 0;
		final int[] updates = { 0 };
		assertDoesNotThrow( ()-> updates[0] = rule.update(board, new Board(P20230103_TH_SOLUTION), candidates, encs));
		assertEquals( expectedOccs, board.getOccupiedCount() - prevOccs );
		assertEquals( expectedCands, prevCands - candidates.getAllCount());

		// Thread will not exit when launching DisplayGraph. Use ExecutorService
		// Thread.currentThread().join(); // Wait for threads to exit
	}

	public static String P20230108_FORCINGCHAIN_17700 =
		"75.8.....-8...7.2.5-..39.54..-..8547..6-..5.817..-1..39.5..-5824.93..-..1.5.824-.....8.59";
	public static String P20230108_FORCINGCHAIN_17700_SOLUTION =
		"754832691-869174235-213965478-328547916-695281743-147396582-582419367-971653824-436728159";

	// @Test
	public void test20230108() throws ParseException {
		Board board = new Board(P20230108_FORCINGCHAIN_17700);
		assertTrue(board.legal());

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		System.out.println( "Candidates=\n" + candidates.toStringBoxed());
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
		System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		BiLocCycleDigitRepeat rule = new BiLocCycleDigitRepeat();
		// Locations test
		List<int[]> encs = rule.find(board, candidates);
		assertNotNull(encs);
		assertEquals(7, encs.size());

        // Update test
        int prevEntries = candidates.getAllOccupiedCount();
		int prevCandidates = candidates.getAllCount();
		int updates = rule.update(board, null, candidates, encs);
		// Destination gains entry, loses 2 candidates.
		// Will likely knock out more same-unit candidates after LegalCandidate update.
		assertEquals( prevEntries, candidates.getAllOccupiedCount() - updates);
		assertEquals( prevCandidates, candidates.getAllCount() + 2);
	}
}