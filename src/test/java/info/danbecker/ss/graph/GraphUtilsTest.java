package info.danbecker.ss.graph;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.rules.LegalCandidates;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.List;

import static info.danbecker.ss.rules.BiLocCycleDigitRepeatTest.EPP_BILOCCYCLE_REPEAT_FIG7;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test suite tests the generic graph abilities of JGraphT.
 * Some example are from the Eppstein paper
 * "NonRepetitive Paths and Cycles..." at https://arxiv.org/abs/cs/0507053
 */
public class GraphUtilsTest {
	@BeforeEach
	public void setup() {
	}

	@Test
	public void testGraphUtils() {
		String graphInp = "[1,0]-6-[1,8]-5-[8,8]-37-[8,0]-2-[2,0]-5-[1,0]";
		// Cycle 0=[1,8]-5-[8,8]-37-[8,0]-2-[2,0]-5-[1,0]-6-[1,8]
		System.out.println( "GraphInp="+ graphInp );
		Graph<RowCol, LabelEdge> bilocGraph = GraphUtils.getBilocGraph(graphInp);

		String graphStr = GraphUtils.graphToStringE(bilocGraph, ",");
		// System.out.println("GraphStr=" + graphStr);
		assertEquals( graphInp, graphStr);

		List<GraphPath<RowCol, LabelEdge>> gpl = GraphUtils.getGraphCycles(bilocGraph);
		for (int gpi = 0; gpi < gpl.size(); gpi++) {
			GraphPath<RowCol, LabelEdge> gp = gpl.get(gpi);
			String path = GraphUtils.pathToString(gp, "-", false);
			// System.out.println("Cycle " + gpi + "=" + path);
			switch( gpi) {
				case 0 -> assertEquals( "[1,8]-5-[8,8]-37-[8,0]-2-[2,0]-5-[1,0]-6-[1,8]", path );
			}
		}
	}

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
		List<GraphPath<RowCol,LabelEdge>> gpl = GraphUtils.getGraphCycles( bilocGraph);
		for( int gpi = 0; gpi < gpl.size(); gpi++ ) {
			GraphPath<RowCol,LabelEdge> gp = gpl.get(gpi);
			// String label = "Path " + gpi + "=" + GraphUtils.pathToString( gp, "-", false );
			// System.out.println( label );
			// new GraphDisplay( label, gpi, gp );
			switch (gpi) {
				case 0 -> {
					assertEquals(4, gp.getVertexList().size());
					assertEquals(3, gp.getEdgeList().size());
				}
				case 1 -> {
					assertEquals(gp.getStartVertex(), gp.getEndVertex());
					assertEquals(4, gp.getLength());
				}
			}
		}
		// Thread will not exit when launching DisplayGraph. Use ExecutorService
		// Thread.currentThread().join(); // Wait for threads to exit
	}
}