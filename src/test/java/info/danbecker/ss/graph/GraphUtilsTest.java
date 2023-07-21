package info.danbecker.ss.graph;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.rules.ForcingChains;
import info.danbecker.ss.rules.LegalCandidates;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Candidates.ALL_COUNTS;
import static info.danbecker.ss.Candidates.ALL_DIGITS;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * This test suite implements examples from
 * Epstein "NonRepetitive Paths and Cycles..." at https://arxiv.org/abs/cs/0507053
 * SOTD has an example at https://www.sudokuoftheday.com/techniques/forcing-chains
 * Thonky has ColoringChains examples at https://www.thonky.com/sudoku/simple-coloring
 * <p>
 * TODO
 * Replace setup, find, update, test pattern with a TestRunner
 */
public class GraphUtilsTest {
	@BeforeEach
	public void setup() {
	}

	// From David Eppstein "Nonrepetetive Paths..." https://arxiv.org/abs/cs/0507053
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
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		// System.out.println( "Candidates=\n" + candidates.toStringFocus( false, ALL_DIGITS, ALL_COUNTS ));

		Graph<RowCol,LabelEdge> bilocGraph = GraphUtils.getBilocGraph( candidates );
		// DisplayGraph will cause test case to not exit. Use only for debugging.
		// new GraphDisplay( "BiLoc Graph ", 0, bilocGraph );
		List<GraphPath<RowCol,LabelEdge>> gpl = GraphUtils.getGraphPaths( bilocGraph);
		for( int gpi = 0; gpi < gpl.size(); gpi++ ) {
			GraphPath<RowCol, LabelEdge> gp = gpl.get(gpi);
			// String label = "Path " + gpi + "=" + GraphUtils.pathToString( gp, "-", false );
			// System.out.println( label );
			// new GraphDisplay( label, gpi, gp );
		}
		// This thread will not exit when launching DisplayGraph. Use ExecutorService
		// Thread.currentThread().join(); // Wait for threads to exit
	}

	public static String EPP_BILOCCYCLE_NONREPEAT_FIG6 =
			".9..5342..134928...4..67.39..1524...92.318..4.5.9763121.924576...573.2...7.68....";
	public static String EPP_BILOCCYCLE_NONREPEAT_FIG6_SOLUTION =
			"796853421513492876248167539631524987927318654854976312189245763465731298372689145";
	@Test
	public void testBiLocFig6() throws ParseException, InterruptedException {
		// Set up test board and candidates
		Board board = new Board(EPP_BILOCCYCLE_NONREPEAT_FIG6);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		// System.out.println( "Candidates=\n" + candidates.toStringFocus( false, ALL_DIGITS, ALL_COUNTS ));

		Graph<RowCol,LabelEdge> bilocGraph = GraphUtils.getBilocGraph( candidates );
		List<GraphPath<RowCol,LabelEdge>> gpl = GraphUtils.getGraphPaths( bilocGraph);
		for( int gpi = 0; gpi < gpl.size(); gpi++ ) {
			GraphPath<RowCol, LabelEdge> gp = gpl.get(gpi);
			// String label = "Path " + gpi + "=" + GraphUtils.pathToString( gp, "-", false );
			// System.out.println( label );
		}
	}

	public static String EPP_BILOCCYCLE_REPEAT_FIG7 =
			"..318679559647231818795364297.321.64.1.86..3736.5.7.216.17.528.7..61845.85.23.176";
	public static String EPP_BILCOCYCLE_REPEAT_FIG7_SOLUTION =
			"423186795-596472318-187953642-975321864-214869537-368547921-641795283-732618459-859234176";
	@Test
	public void testBiLocFig7() throws ParseException, InterruptedException {
		// Set up test board and candidates
		Board board = new Board(EPP_BILOCCYCLE_REPEAT_FIG7);
		assertTrue(board.legal());
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		// System.out.println( "Candidates=\n" + candidates.toStringFocus( false, ALL_DIGITS, ALL_COUNTS ));

		Graph<RowCol, LabelEdge> bilocGraph = GraphUtils.getBilocGraph( candidates );
		// DisplayGraph will cause test case to not exit. Use only for debugging.
		// new GraphDisplay( "BiLoc Graph ", 0, bilocGraph );
		List<GraphPath<RowCol,LabelEdge>> gpl = GraphUtils.getGraphPaths( bilocGraph);
		for( int gpi = 0; gpi < gpl.size(); gpi++ ) {
			GraphPath<RowCol,LabelEdge> gp = gpl.get(gpi);
			// String label = "Path " + gpi + "=" + GraphUtils.pathToString( gp, "-", false );
			// System.out.println( label );
			// new GraphDisplay( label, gpi, gp );
			switch ( gpi ) {
				case 0: {
					assertEquals( 4, gp.getVertexList().size());
					assertEquals( 3, gp.getEdgeList().size());
					break;
				}
				case 1: {
					assertEquals(  gp.getStartVertex(), gp.getEndVertex());
					assertEquals( 4, gp.getLength());
					break;
				}
			}
		}
		// Thread will not exit when launching DisplayGraph. Use ExecutorService
		// Thread.currentThread().join(); // Wait for threads to exit
	}

	public static String EPP_BILOCCYCLE_ENDPOINTS_FIG8 =
			"53..76.2912639.57..7952..63263.5.9477..2.93569457632813.4685792697432..5.5291763.";

	public static String EPP_BILOCCYCLE_ENDPOINTS_FIG8_SOLUTION =
			"538176429-126394578-479528163-263851947-781249356-945763281-314685792-697432815-852917634";

	public static String EPP_BIVALUE_FIG9 =
			"53..76.2912639.57..7952..63263.5.9477..2.93569457632813.4685792697432..5.5291763.";

	public static String EPP_BIVALUE_FIG9_SOLUTION =
			"538176429-126394578-479528163-263851947-781249356-945763281-314685792-697432815-852917634";

	public static String EPP_DIFFICULT_FIG11 =
			"5....1..8......6......6257..9.2.51....4.1.3....83.9.2..7698......5......8..1....3";
	public static String EPP_DIFFICULT_FIG11_SOLUTION =
			"562731948741598632983462571397245186254816397618379425176983254435627819829154763";

	/**
	 * Runs a given test case.
	 * <p>
	 * The expected updates should be
	 * - placement of digit X at rowCol
	 * - removement of candidate Y at rowCol
	 *
	 * @param start
	 * @param solution
	 * @param expectedFinds
	 * @param expectedEncs
	 * @param expectedUpdates
	 * @param updates
	 * @return
	 * @throws ParseException
	 */
	public boolean testRunner( String start, String solution,
	    int expectedFinds, List<int[]> expectedEncs,
		int expectedUpdates, List<Object> updates ) throws ParseException {
		return false;
	}
}