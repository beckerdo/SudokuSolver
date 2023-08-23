package info.danbecker.ss.graph;

import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This test suite tests finding edge patterns in paths of JGraphT.
 */
public class EdgePatternFinderTest {
	@BeforeEach
	public void setup() {
	}

	@Test
	public void testEdgePatternUtils() {
		assertEquals( "1", EdgePatternFinder.labelEncode( "1" ));
		assertEquals( "Abc", EdgePatternFinder.labelEncode( "123" ));

		assertIterableEquals( Collections.singleton( "9" ), EdgePatternFinder.labelDecode( "9" ));
		assertIterableEquals( Collections.singleton( "1" ), EdgePatternFinder.labelDecode( "A" ));
		assertIterableEquals( Collections.singleton( "23" ), EdgePatternFinder.labelDecode( "Bc" ));
		assertIterableEquals( Arrays.asList( "1", "233", "4"), EdgePatternFinder.labelDecode( "1Bcc4" ));
		assertIterableEquals( Arrays.asList( "12", "3", "4", "5", "677"), EdgePatternFinder.labelDecode( "Ab345Fgg" ));

		assertEquals( 0, Utils.compareTo( Arrays.asList( 0, 1, 2, 3 ), EdgePatternFinder.labelPosToEdgei("1234")));
		assertEquals( 0, Utils.compareTo( Arrays.asList( 0, 1, 1, 2 ), EdgePatternFinder.labelPosToEdgei("1Bc4")));
		assertEquals( 0, Utils.compareTo( Arrays.asList( 0, 0, 0, 1 ), EdgePatternFinder.labelPosToEdgei("Abc4")));
		assertEquals( 0, Utils.compareTo( Arrays.asList( 0, 1, 1, 1 ), EdgePatternFinder.labelPosToEdgei("1Bcd")));
	}

	@Test
	public void testEdgePatternFindXX() {
		// Test different graph patterns.
		String [] graphStrs = {
				"[8,0]-2-[2,0]-9-[1,0]-6-[1,8]-6-[8,8]-4-[8,0]", // no long labels
				"[8,0]-2-[2,0]-58-[1,0]-6-[1,8]-6-[8,8]-4-[8,0]", // some long labels
				"[8,0]-2-[2,0]-9-[1,0]-6-[1,8]-6-[8,8]-58-[8,0]", // various wrap arounds
				"[8,8]-4-[8,0]-2-[2,0]-58-[1,0]-6-[1,8]-6-[8,8]",
				"[1,8]-6-[8,8]-4-[8,0]-2-[2,0]-58-[1,0]-6-[1,8]",
				"[1,0]-6-[1,8]-6-[8,8]-4-[8,0]-2-[2,0]-58-[1,0]",
				"[1,0]-6-[1,8]-6-[8,8]-456-[8,0]-6-[2,0]-6-[4,0]-78-[1,0]", // pattern multiples
				"[1,0]-6-[1,8]-6-[8,8]-456-[8,0]-7-[2,0]-7-[4,0]-78-[1,0]", // multiple patterns
				"[1,0]-12-[1,8]-6-[8,8]-6-[8,0]-6-[2,0]-2-[1,0]", // do not find
				"[1,0]-6-[1,8]-6-[8,8]-23-[8,0]-1-[2,0]-6-[1,0]", // do not find
		};
		List<Map<String,List<RowCol>>> expected = Arrays.asList(
			new HashMap<>(){{ put( "66", Arrays.asList(ROWCOL[1][8])); }},
			new HashMap<>(){{ put( "66", Arrays.asList(ROWCOL[1][8])); }},
			new HashMap<>(){{ put( "66", Arrays.asList(ROWCOL[1][8])); }},
			new HashMap<>(){{ put( "66", Arrays.asList(ROWCOL[1][8])); }},
			new HashMap<>(){{ put( "66", Arrays.asList(ROWCOL[1][8])); }},
			new HashMap<>(){{ put( "66", Arrays.asList(ROWCOL[1][8])); }},
			new HashMap<>(){{ put( "66", Arrays.asList(ROWCOL[1][8],ROWCOL[2][0])); }},
			new HashMap<>(){{ put( "66", Arrays.asList(ROWCOL[1][8]));
				              put( "77", Arrays.asList(ROWCOL[2][0])); }},
			new HashMap<>(){},
			new HashMap<>(){}
		);

		for ( int testi = 0; testi < graphStrs.length; testi++) {
			String graphStr = graphStrs[ testi ];
			// System.out.println("GraphInp=" + graphStr);
			Graph<RowCol, LabelEdge> bilocGraph = GraphUtils.getBilocGraph(graphStr);
			// System.out.println( "GraphStr " + testi +"=" + GraphUtils.graphToStringE(bilocGraph, ","));

			List<GraphPath<RowCol, LabelEdge>> gpl = GraphUtils.getGraphCycles(bilocGraph);
			for (int gpi = 0; gpi < gpl.size(); gpi++) {
				GraphPath<RowCol, LabelEdge> gp = gpl.get(gpi);
				// System.out.println( "Cycle " + gpi + "=" + GraphUtils.pathToString(gp, "-", false));

				EdgePatternFinder patternFinder = new EdgePatternFinder(gp, EdgePatternFinder.XX_NAME);
				Map<String,List<RowCol>> finds= patternFinder.getMatches();
				assertEquals( expected.get(testi).entrySet(), finds.entrySet() );
				for( Map.Entry<String,List<RowCol>> entry : finds.entrySet()) {
					// System.out.printf( "Path=%s, pattern=%s, locs=%s%n",
					// 	patternFinder.pathString(),entry.getKey(),RowCol.toString( entry.getValue() ) );
					assertEquals( expected.get(testi).get( entry.getKey() ), entry.getValue() );
				}
			}
		} // for each graphStr
	}

	@Test
	public void testEdgePatternFindXYX() {
		// Test different permutations of graph for wrap around problems.
		String [] graphStrs = {
				"[8,0]-2-[2,0]-5-[1,0]-6-[1,8]-5-[8,8]-378-[8,0]", // test cycle boundaries
				"[2,0]-5-[1,0]-6-[1,8]-5-[8,8]-378-[8,0]-2-[2,0]",
				"[1,0]-6-[1,8]-5-[8,8]-378-[8,0]-2-[2,0]-5-[1,0]",
				"[1,8]-5-[8,8]-378-[8,0]-2-[2,0]-5-[1,0]-6-[1,8]",
				"[8,8]-378-[8,0]-2-[2,0]-5-[1,0]-6-[1,8]-5-[8,8]",
				"[8,0]-12-[2,0]-5-[1,0]-6-[1,8]-5-[8,8]-37-[5,0]-5-[4,0]-6-[4,8]-5-[8,0]", // pattern multiples
				"[8,0]-12-[2,0]-5-[1,0]-6-[1,8]-5-[8,8]-37-[5,0]-7-[4,0]-8-[4,8]-7-[8,0]", // multiple patterns
				"[1,0]-1-[1,8]-6-[8,8]-6-[8,0]-6-[2,0]-2-[1,0]", // do not find
				"[1,0]-6-[1,8]-6-[8,8]-2-[8,0]-1-[2,0]-6-[1,0]", // do not find
		};
		List<Map<String,List<RowCol>>> expected = Arrays.asList(
				new HashMap<>(){{ put( "565", Arrays.asList(ROWCOL[1][0],ROWCOL[1][8])); }},
				new HashMap<>(){{ put( "565", Arrays.asList(ROWCOL[1][0],ROWCOL[1][8])); }},
				new HashMap<>(){{ put( "565", Arrays.asList(ROWCOL[1][0],ROWCOL[1][8])); }},
				new HashMap<>(){{ put( "565", Arrays.asList(ROWCOL[1][0],ROWCOL[1][8])); }},
				new HashMap<>(){{ put( "565", Arrays.asList(ROWCOL[1][0],ROWCOL[1][8])); }},
				new HashMap<>(){{ put( "565", Arrays.asList(ROWCOL[1][0],ROWCOL[1][8],
															ROWCOL[4][0],ROWCOL[4][8])); }},
				new HashMap<>(){{ put( "565", Arrays.asList(ROWCOL[1][0],ROWCOL[1][8]));
					              put( "787", Arrays.asList(ROWCOL[4][0],ROWCOL[4][8])); }},
				new HashMap<>(){},
				new HashMap<>(){}
		);
		for ( int testi = 0; testi < graphStrs.length; testi++) {
			String graphStr = graphStrs[ testi ];
			// System.out.println("GraphInp=" + graphStr);
			Graph<RowCol, LabelEdge> bilocGraph = GraphUtils.getBilocGraph(graphStr);
			// System.out.println( "GraphStr " + testi +"=" + GraphUtils.graphToStringE(bilocGraph, ","));

			List<GraphPath<RowCol, LabelEdge>> gpl = GraphUtils.getGraphCycles(bilocGraph);
			for (int gpi = 0; gpi < gpl.size(); gpi++) {
				GraphPath<RowCol, LabelEdge> gp = gpl.get(gpi);
				// System.out.println( "Cycle " + gpi + "=" + GraphUtils.pathToString(gp, "-", false));

				EdgePatternFinder patternFinder = new EdgePatternFinder(gp, EdgePatternFinder.XYX_NAME);
				Map<String,List<RowCol>> finds= patternFinder.getMatches();
				assertEquals( expected.get(testi).entrySet(), finds.entrySet() );
				for( Map.Entry<String,List<RowCol>> entry : finds.entrySet()) {
					// System.out.printf( "Path=%s, pattern=%s, locs=%s%n",
					//  	patternFinder.pathString(),entry.getKey(),RowCol.toString( entry.getValue() ) );
					assertEquals( expected.get(testi).get( entry.getKey() ), entry.getValue() );
				}
			}
		} // for each graphStr
	}
}