package info.danbecker.ss.graph;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils.Unit;
import static info.danbecker.ss.Utils.Unit.BOX;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.cycle.PatonCycleBase;
import org.jgrapht.alg.interfaces.CycleBasisAlgorithm;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.*;

/**
 * Implements many Graph related functions.
 * Graphs are collections of vertices (locations
 * on the Sudoku puzzle) and edges, various
 * connections between the locations.
 * <p>
 * These graphs are used to implement the research paper
 * "Nonrepetitive Paths and Cycles in Graphs
 * with Application to Sudoku" by David Eppstein.
 * See the BiLocCycleDigitRepeat rule.
 * <p>
 * That paper has a number of methods based on
 * bilocation (3.2) and bivalue (3.5)
 * This code implements the following methods:
 * <ul>
 *     <li>Bilocation repetitive cycle rule of 3.3 (type 0)</li>
 * </ul>
 * <p>
 * Much of this data structure is based on
 * org.jgrapht.Graph
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class GraphUtils {
	/**
	 * Create a bilocation graph from the string that is delimited by
	 * rowCol end label delimiters, for example:
	 * [1,0]-6-[1,8]-5-[8,8]-37-[8,0]-2-[2,0]-5-[1,0]
	 * The edge label delimiters may be - or =.
	 *
	 * @param graphStr string  for example [1,0]-6-[1,8]-5-[8,8]-37-[8,0]-2-[2,0]-5-[1,0]
	 * @return a biloc graph with RowCol vertices and labeled edges
	 */
	public static Graph<RowCol,LabelEdge> getBilocGraph(String graphStr ) {
		// Create bilocGraph with unoccupied vertices
		Graph<RowCol,LabelEdge> graph = new SimpleGraph<>(LabelEdge.class);
		Scanner s = new Scanner(graphStr).useDelimiter("\\s*[=-]\\s*");
		// bilocGraph.addVertex( rowCol );
		// bilocGraph.addEdge(vertex, seeMe, new LabelEdge(Integer.toString(digit)));
		RowCol prev = RowCol.parse( s.next() );
		graph.addVertex( prev );
		while ( s.hasNext() ) {
			String label = s.next();
			RowCol next = RowCol.parse( s.next() );
			graph.addVertex( next );
			graph.addEdge(prev, next, new LabelEdge(label));
			prev = next;
		}
		return graph;
	}

	public static Graph<RowCol,LabelEdge> getBilocGraph(final Candidates candidates ) {
		List<RowCol> unoccupied = candidates.getUnoccupiedLocs();
		// System.out.println( "Unoccupied vertices=" + RowCol.toString(unoccupied));

		// Fill in counts [digit][unit][uniti]
		int[][][] counts = new int[ Board.DIGITS ][][];
		for ( int digi = 1; digi <= Board.DIGITS; digi++ ) {
			int digitCount = candidates.digitCount( digi );
			if ( 0 < digitCount && null == counts[ digi -1 ]) {
				counts[ digi-1 ] = candidates.candidateUnitCounts(digi);
				// Display counts
				// System.out.printf( "Digit %d%n", digi);
				// for ( Unit unit: Unit.values() ) {
				// 	System.out.printf( "%s %s%n", unit, Utils.digitsToString( counts[digi-1][unit.ordinal()]));
				// }
			}
		}

		// Create bilocGraph with unoccupied vertices
		Graph<RowCol,LabelEdge> bilocGraph = new SimpleGraph<>(LabelEdge.class);
		for( RowCol rowCol : unoccupied ) {
			bilocGraph.addVertex( rowCol );
		}
		addDigitEdges( bilocGraph, candidates );
		// System.out.println( "BiLocGraph=" + bilocGraph ); // verbose
		// System.out.println( LabelEdge.edgesString("BilocGraph edges=", bilocGraph, "" ));
		return bilocGraph;
	}

	/**
	 * Given a Bilocation graph, from the vertices,
	 * add and label edges based on the candidates.
	 * @param bilocGraph graph of vertices
	 * @param candidates state of game
	 */
	public static void addDigitEdges(Graph<RowCol,LabelEdge> bilocGraph, final Candidates candidates ) {
		for ( RowCol vertex : bilocGraph.vertexSet() ) {
			List<Integer> locCands = candidates.getCandidatesList( vertex );
			for( int digit : locCands ) {
				for ( Unit unit : Unit.values()) {
					int uniti = switch( unit ) {
						case ROW -> vertex.row();
						case COL -> vertex.col();
						case BOX -> vertex.box();
					};
					List<RowCol> unitLocs = candidates.getUnitDigitLocs(unit, uniti, digit);
					// Only place edges where the digit locs is 2.
					// "We connect two vertices by an edge, labeled with a digit x,
					// if the two cells lie in a single unit, and those two cells are the only
					// ones that can contain x."
					if ( 2 == unitLocs.size()) {
						for (RowCol seeMe : unitLocs) {
							if (!seeMe.equals(vertex)) {
								// Don't make a box edge if ROW or COL matches
								if ((BOX != unit && (vertex.row() == seeMe.row() || vertex.col() == seeMe.col())) ||
										(BOX == unit && (vertex.row() != seeMe.row() && vertex.col() != seeMe.col()))) {
									LabelEdge edge = bilocGraph.getEdge(vertex, seeMe);
									if (null == edge) {
										// New edge
										bilocGraph.addEdge(vertex, seeMe, new LabelEdge(Integer.toString(digit)));
									} else {
										// Expand edge label
										edge.addToLabel(digit);
									}
								} // avoid box repeats
							} // avoid self location
						} // for each next vertex
					} // for loc pairs
				} // for each unit
			} // for each cand digit
		} // for each vertex
	} // addEdges

	/**
	 * From the graph, produce a list of graph paths
	 * that form cycles in the graph.
	 *
	 * @param graph fully formed biloc or bival graph
	 * @return list of GraphPath cycles
	 */
	public static List<GraphPath<RowCol,LabelEdge>> getGraphCycles(final Graph<RowCol,LabelEdge> graph ) {
		PatonCycleBase<RowCol,LabelEdge> det = new  PatonCycleBase<>( graph );
		CycleBasisAlgorithm.CycleBasis<RowCol,LabelEdge> cy = det.getCycleBasis();
		Set<GraphPath<RowCol,LabelEdge>> gps = cy.getCyclesAsGraphPaths();
		return gps.stream().toList();
	}

	/**
	 * Show edges of graph with given prefix and delimiters
	 * Much more compact than Graph toString
	 * @param prefix one time prefix string
	 * @param graph graph with vertices and edges
	 * @param delim delimiter between edges
	 * @return compact string of graph edges
	 */
	public static String edgesString(String prefix, final Graph<RowCol,LabelEdge> graph, String delim) {
		StringBuilder sb = new StringBuilder(prefix);
		Set<LabelEdge> edges = graph.edgeSet();
		// To mutable list
		List<String> labels = edges.stream().map(LabelEdge::toStringVerbose).sorted().toList();
		for (String edgeStr : labels) {
			sb.append(edgeStr);
			sb.append(delim);
		}
		return sb.toString();
	}

	/**
	 * Shows all vertices and edges in graph.
	 * Very compact compared to Graph toString
	 * Uses DepthFirstIterator
	 * @param graph graph to print
	 * @param startLoc starting location, can be null
	 */
	public static String graphToString(final Graph<RowCol,LabelEdge> graph, final RowCol startLoc, final String delim ) {
		StringBuilder sb = new StringBuilder( "depth first, start=" + startLoc + ": " );
		// Warning, the vertex set might not show all edges in a cycle.
		// The last vertex does not show its edge back to the first.
		Iterator<RowCol> iterator = (null == startLoc) ? new DepthFirstIterator<>(graph) :
				new DepthFirstIterator<>(graph,startLoc);
		RowCol lastLoc = null;
		while (iterator.hasNext()) {
			RowCol loc = iterator.next();
			if ( null != lastLoc ) {
				// System.out.print(loc);
				// sb.append( "d" + graph.degreeOf( lastLoc ) + graph.degreeOf( loc ));
				LabelEdge edge = graph.getEdge( lastLoc, loc );
				if ( null != edge )
					sb.append( edge.toStringVerbose() + delim);
				else
					sb.append( lastLoc + "-null-" + loc + delim);
			}
			lastLoc = loc;
		}
		return sb.toString();
	}

	/**
	 * Shows all edges and vertices in graph.
	 * Very compact compared to Graph toString
	 * @param graph graph to print
	 * @param delim for edges that don't connect to previous vertex
	 */
	public static String graphToStringE(final Graph<RowCol,LabelEdge> graph, final String delim ) {
		StringBuilder sb = new StringBuilder();

		Iterator<LabelEdge> iterator = graph.edgeSet().iterator();
		Object lastLoc = null;
		int edgeCount = 0;
		while (iterator.hasNext()) {
			LabelEdge edge = iterator.next();
			if ( null != edge ) {
				Object target = edge.getTarget();
				Object source= edge.getSource();
				if ( source.equals( lastLoc )) {
					sb.append( "-" + edge.getLabel() + "-" + target);
				} else {
					if ( 0 < edgeCount ) sb.append( delim );
					sb.append( source + "-" + edge.getLabel() + "-" + target);
				}
				edgeCount++;
				lastLoc = target;
			}
		}
		return sb.toString();
	}

	/**
	 * From the path,
	 * list the vertices and edges in the path.
	 * @param gp
	 * @return
	 */
	public static String pathToString( final GraphPath<RowCol,LabelEdge> gp, String edgeLink, boolean unitMatch ) {
		StringBuilder sb = new StringBuilder();
		// System.out.println( "Cycle vertices=" + RowCol.toString(gp.getVertexList()));
		Graph<RowCol,LabelEdge> graph = gp.getGraph();
		RowCol lastLoc = null;
		Iterator<RowCol> viterator = gp.getVertexList().iterator();
		while (viterator.hasNext()) {
			RowCol loc = viterator.next();
			if ( null != lastLoc ) {
				// int degree = graph.degreeOf( loc );
				String unitStr = "";
				if ( unitMatch ) {
					Unit unit = RowCol.firstUnitMatch(loc, lastLoc);
					unitStr = null == unit ? "*" : unit.toString().substring(0, 1).toLowerCase();
				}
				LabelEdge edge = graph.getEdge( lastLoc, loc );
				if ( null != edge )
					sb.append(edgeLink).append(edge.getLabel()).append(unitStr).append(edgeLink).append(loc);
				else
					sb.append(lastLoc).append("null").append(loc); // should not see this
			} else {
				sb.append( loc ); // start
			}
			lastLoc = loc;
		}
		return sb.toString();
	}
}