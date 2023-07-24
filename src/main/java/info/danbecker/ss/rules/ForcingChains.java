package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;
import info.danbecker.ss.graph.GraphUtils;
import info.danbecker.ss.graph.LabelEdge;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;

import java.util.*;

import static info.danbecker.ss.Board.DIGITS;
import static info.danbecker.ss.Board.ROWCOL;
import static java.lang.String.format;

/**
 * This ForcingChain rule is based on the research paper
 * "Nonrepetitive Paths and Cycles in Graphs
 * with Application to Sudoku" by David Eppstein.
 * <p>
 * That paper has a number of methods based on
 * bilocation (3.2) and bivalue (3.5)
 * This code implements the following methods:
 * <ul>
 *     <li>Bilocation cycle repetitive digit rule of 3.3 (type 0)</li>
 * </ul>
 * <p>
 * Use ForcingChains when there are many candidate pairs.
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class ForcingChains implements FindUpdateRule {

	@Override
	// enc int []
	// type 0 = repeat digit at location
	// digit
	// remove loc
	// prev loc
	// next loc
	public int update(Board board, final Board solution, Candidates candidates, List<int[]> encs) {
		int updates = 0;
		if ( null == encs) return updates;
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed() );
		for( int enci = 0; enci < encs.size(); enci++ ) {
			int[] enc = encs.get(enci);
			int type = enc[0];
			String typeStr = "cycle repeat";
			int pathId = enc[1];
			int digit = enc[2];
			RowCol loc = ROWCOL[enc[3]][enc[4]];
			// System.out.printf( "Rule %s, enc %d=%s%n", ruleName(), enci, encodingToString( enc ));

			// Validation if available
			if ( null != solution ) {
				int solutionDigit = solution.get(loc);
				if ( solutionDigit != digit ) {
					// System.out.println( "Board=\n" + solution.toString() );
					System.out.println( "Candidates=\n" + candidates.toStringBoxed() );
					String msg = format("Rule %s update error pathId %d digit %d at loc %s which has solution %d ***",
							ruleName(), pathId, digit, loc, solutionDigit);
					/// System.out.println( msg );
					throw new IllegalArgumentException( msg);
				}
			}
			// Remove endDigit from sees location
			int prevPlay = board.getOccupiedCount();
			board.set(loc, digit); // put a digit in the board
			int currPlay = board.getOccupiedCount();
			int prevCand = candidates.getAllCount();
			candidates.setOccupied(loc, digit); // places entry, removes candidates
			int currCand = candidates.getAllCount();
			updates += currPlay - prevPlay + prevCand - currCand;
			if ( 0 < updates ) {
				// Repeats from same digit, same loc, different pathId
				System.out.printf("%s update %s played digit %d and removed %d candidates at loc %s%n",
						ruleName(), typeStr, digit, prevCand - currCand, loc);
			}
		}
		return updates;
	}

	/**
	 * Return any location where either digit leads to same output.
	 * The starting digit location is given to help with testability.
	 */
	@Override
	public List<int[]> find(final Board board, final Candidates candidates) {
		List<int[]> matched = new LinkedList<>();

		Graph<RowCol, LabelEdge> bilocGraph = GraphUtils.getBilocGraph( candidates );
		// DisplayGraph will cause test case to not exit. Use only for debugging.
		List<GraphPath<RowCol,LabelEdge>> gpl = GraphUtils.getGraphPaths( bilocGraph);
		for( int gpi = 0; gpi < gpl.size(); gpi++ ) {
			GraphPath<RowCol, LabelEdge> gp = gpl.get(gpi);
			// String label = "Path " + gpi + "=" + GraphUtils.pathToString( gp, "-", false );
			// System.out.println( label );
			List<int[]> found = findRepetitiveCycle33( candidates, gpi, gp );
			// Repeats due to same digit, location, different path id
			Utils.addUniques( matched, found, DigitRowColComparator );
		}
		return matched;
	}

	/**
	 * Lists any location that has EXACTLY ONE pair of consecutive edges with a repeated label.
	 * This corresponds to the Repetitive Cycle Rule in 3.3 in Eppstein "Non Repetitive Paths and Cycles"
	 * Do not use this rule for multi-digit labels (ignore those edges).
	 * Target location must have 2 candidates (strong link), not 3 or more (weak link).
	 * @param gp GraphPath with a cycle
	 * @return list of encodings
	 */
	public static List<int[]> findRepetitiveCycle33( final Candidates candidates, int pathId, final GraphPath<RowCol,LabelEdge> gp ) {
		List<int []> encs = new ArrayList<>();
		Graph<RowCol,LabelEdge> g = gp.getGraph();
		Iterator<RowCol> viterator = gp.getVertexList().iterator();

		// Count digit sequences as we travel the path
		int[] digitCounts = new int[]{0,0,0,0,0,0,0,0,0};
		// Preset elements from last edge (in case wraparound)
		List<LabelEdge> edges = gp.getEdgeList();
		LabelEdge prevEdge = edges.get( edges.size() - 1);
		RowCol prevLoc = g.getEdgeTarget(prevEdge);
		String prevLabel = prevEdge.getLabel();
		if ( 1 == prevLabel.length() ) {
			int digit = Integer.parseInt( prevLabel );
			digitCounts[digit - 1] = 1;
		}
		RowCol prevprevLoc = null;
		// Ready to iterate from vertex 0.
		RowCol loc = viterator.next();
		while (viterator.hasNext()) {
			RowCol nextLoc = viterator.next();
			LabelEdge edge = g.getEdge(loc, nextLoc);
			String label = edge.getLabel();
			boolean lastVertex = nextLoc.equals( gp.getEndVertex());

			// Just count single digit edges. Multi digits end counts
			Set<Integer> labelInts = LabelEdge.labelToInts( label );
			for (int digi = 0; digi < DIGITS; digi++) {
				if (1 == labelInts.size() && labelInts.contains(digi + 1)) {
					// Continue counting
					digitCounts[digi]++;
				} else {
					// Multi digit labels always end counting.
					// Last vertex always ends counting
					// Check if we have a repeat count of 2.
					// if (2 == digitCounts[digi] && 3 > candidates.candidateCellCount(prevLoc)) {
					// Test without cand count
					if (2 == digitCounts[digi] ) {
						// Check candidate count <= 2.
						// Encode and add
						// System.out.printf("   Label %s at %s-%s ends run of digit %d at %s-%s-%s. Adding enc.%n",
						//    label, loc, nextLoc, digi + 1, prevprevLoc, prevLoc, loc);
						int [] enc;
						if ( null == prevprevLoc )
							// Beginning wraparound case (count from end edge list)
							enc = encode(0, pathId, digi + 1, loc, prevLoc, nextLoc);
						else
							// Non wraparound case (count begin edge list)
						    enc = encode(0, pathId, digi + 1, prevLoc, prevprevLoc, loc);
						// Be aware if a different path caught this digit/loc as a single repeated edge.
						// This path must be discounted if it causes a double repeated edge
						// Utils.addUnique(encs, enc, DigitRowColComparator) )
						encs.add( enc );
					}
					digitCounts[digi] = 0;
				}
				// Check for last vertex
				if ( lastVertex && 2 == digitCounts[digi]) {
					int[] enc = encode(0, pathId, digi + 1, loc, prevLoc, nextLoc);
					// Utils.addUnique(encs, enc, DigitRowColComparator) )
					encs.add( enc );
				}
			} // for each digit

			prevprevLoc = prevLoc;
			prevLoc = loc;
			loc = nextLoc;
		} // for each vertex in path
		// If a pathId has repeats of more than one digit, it must be removed.
		List<int []> singleRepeats = new ArrayList<>();
		for ( int[] enc : encs) {
			List<int[]> pathIdEncs = encs.stream().filter(ele -> 0 == PathIdComparator.compare(ele, enc) ).toList();
			// System.out.printf( "Found enc=%s with count=%d%n", toString( enc ), pathIdEncs.size() );
			if ( 1 == pathIdEncs.size() ) {
				singleRepeats.add( Arrays.copyOf(enc, enc.length));
			}
		}
		// System.out.printf( "Encs found=%d, encs returned=%d%n", encs.size(), singleRepeats.size());
		encs.clear();
		return singleRepeats;
	}


	/** Encode int []
	 * type 0 = repetitive cycle single repeat digit at location
	 * path id
	 * digit
	 * remove loc
	 * prev loc
	 * next loc
	 */
	public static int [] encode( int type, int id, int digit, RowCol loc, RowCol prevLoc, RowCol nextLoc ){
		int[] enc = new int[] { type, id, digit,
				loc.row(), loc.col(), prevLoc.row(), prevLoc.col(), nextLoc.row(), nextLoc.col()};
		return enc;
	}

	@Override
	public String encodingToString(final int[] enc) {
		return toString( enc );
	}

	public static String toString( final int[] enc) {
		String typeStr =
				switch ( enc[0]) {
					case 0 -> "cycle repeat";
					default -> "unknown";
				};
		return format("%s pathId %d, digit %d, orig loc %s->%s,%s", typeStr, enc[1], enc[2],
				ROWCOL[enc[3]][enc[4]], ROWCOL[enc[5]][enc[6]], ROWCOL[enc[7]][enc[8]]);
	}

	/**
	 * Compares an encoding just by digit and rowCol.
	 * which are located at elements 2, 3, and 4.
	 * Useful for when you don't want duplicates which
	 * match digit and rowCol, but not other fields.
	 */
	public static final Comparator<int[]> DigitRowColComparator =
			new Utils.SubsetComparator(Arrays.asList( 2, 3, 4 ));
	/**
	 * Compares an encoding just by pathId.
	 */
	public static final Comparator<int[]> PathIdComparator =
			new Utils.SubsetComparator(Arrays.asList( 1 ));

	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}