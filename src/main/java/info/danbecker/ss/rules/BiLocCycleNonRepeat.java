package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;
import info.danbecker.ss.graph.EdgePatternFinder;
import info.danbecker.ss.graph.GraphUtils;
import info.danbecker.ss.graph.LabelEdge;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;

import java.util.*;

import static info.danbecker.ss.Board.ROWCOL;
import static java.lang.String.format;

/**
 * This BiLocCycleNonRepeat rule is based on the research paper
 * "Nonrepetitive Paths and Cycles in Graphs
 * with Application to Sudoku" by David Eppstein.
 * <p>
 * That paper has a number of methods based on
 * bilocation (3.2) and bivalue (3.5)
 * This code implements the following methods:
 * <ul>
 *     <li>Bilocation cycle non-repetitive digit rule of 3.2 (type 0)</li>
 * </ul>
 * <p>
 * Use BiLocCycleDigitRepeat when there are many candidate pairs.
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class BiLocCycleNonRepeat implements FindUpdateRule {
	public static final int BILOCCYCLE_NONREPEAT = 0;

	@Override
	// enc int []
	// type BILOCCYCLE_NON_REPEAT = cycle label does not match candidates at location
	// path id
	// x digit
	// y digit
	// 1st location
	// 2nd location
	public int update(Board board, final Board solution, Candidates candidates, List<int[]> encs) {
		int updates = 0;
		if ( null == encs) return updates;
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed() );
		for( int enci = 0; enci < encs.size(); enci++ ) {
			int[] enc = encs.get(enci);
			// String typeStr = typeToString( enc[0] );
			int pathId = enc[1];
			int xDigit = enc[2];
			int yDigit = enc[3];
			RowCol[] locs = {ROWCOL[enc[4]][enc[5]],ROWCOL[enc[6]][enc[7]]};
			System.out.printf( "Rule %s, enc %d=%s%n", ruleName(), enci, encodingToString( enc ));

			// Validation if available
			if ( null != solution ) {
				for ( RowCol loc : locs ) {
					// Remove non xy cands from locs
					int solutionDigit = solution.get(loc);
					List<Integer> cands = candidates.getCandidatesList(loc);
					for ( int cand : cands ) {
						if ( cand != xDigit && cand != yDigit && cand == solutionDigit) {
							// System.out.println( "Board=\n" + solution.toString() );
							System.out.println("Candidates=\n" + candidates.toStringBoxed());
							String msg = format("Rule %s update error pathId %d wants to remove digit %d at loc %s which has solution %d ***",
									ruleName(), pathId, cand, loc, solutionDigit);
							/// System.out.println( msg );
							throw new IllegalArgumentException(msg);
						}
					}
				}
			}
			// Remove endDigit from sees location
			int prevPlay = board.getOccupiedCount();
			int prevCand = candidates.getAllCount();
			for ( RowCol loc : locs ) {
				// Remove non xy cands from locs
				List<Integer> cands = candidates.getCandidatesList(loc);
				for (int cand : cands) {
					if (cand != xDigit && cand != yDigit ) {
						if ( candidates.removeCandidate( loc, cand ))
							updates++;
					}
				}
			}
			int currPlay = board.getOccupiedCount();
			int currCand = candidates.getAllCount();
		} // for encs
		return updates;
	}

	/**
	 * Return any locations in a non-repeating cycle that can eliminate
	 * candidates. This implements Eppstein's bilocation non repeating
	 * cycle of section 3.2.
	 */
	@Override
	public List<int[]> find(final Board board, final Candidates candidates) {
		List<int[]> matched = new LinkedList<>();

		Graph<RowCol, LabelEdge> bilocGraph = GraphUtils.getBilocGraph( candidates );
		// DisplayGraph will cause test case to not exit. Use only for debugging.
		List<GraphPath<RowCol,LabelEdge>> gpl = GraphUtils.getGraphCycles( bilocGraph);
		for( int gpi = 0; gpi < gpl.size(); gpi++ ) {
			GraphPath<RowCol, LabelEdge> gp = gpl.get(gpi);
			// System.out.println( "Path " + gpi + "=" + GraphUtils.pathToString( gp, "-", false ) );
			List<int[]> found = findCycleNonRepeat32( gpi, gp );
			Utils.addUniques( matched, found, DigitRowColComparator );
		}
		return matched;
	}

	/**
	 * Lists any locations that follow the pattern of xyx where the
	 * edge labels of x and y are in that order. The nodes between the
	 * edges can remove candidates that are not x or y.
	 * This implements Eppstein's bilocation non repeating cycle of section 3.2.
	 * @param gp GraphPath with a cycle
	 * @return list of encodings
	 */
	public static List<int[]> findCycleNonRepeat32( int pathId, final GraphPath<RowCol,LabelEdge> gp ) {
		List<int []> encs = new ArrayList<>();
		// For this path, find the single digit pattern XYX.
		EdgePatternFinder patternFinder = new EdgePatternFinder(gp, EdgePatternFinder.XYX_NAME);
		Map<String, List<RowCol>> matches = patternFinder.getMatches();

		// Encode and add the digit labels and vertices
		for (Map.Entry<String, List<RowCol>> entry : matches.entrySet()) {
			String pattern = entry.getKey();
			List<RowCol> locs = entry.getValue();
			// System.out.printf("Pattern=%s, locs=%s%n", pattern, RowCol.toString(locs));
			for (RowCol loc : locs) {
				int[] enc = encode( BILOCCYCLE_NONREPEAT, pathId,
						Integer.parseInt( pattern.substring(0,1) ),
						Integer.parseInt( pattern.substring(1,2) ),
						locs.get( 0 ), locs.get( 1 )
					);
				int added = Utils.addUnique(encs, enc, DigitRowColComparator);
				// String addStr = (0 == added) ? "dup of" : "added";
				// System.out.printf("%s digit=%d. loc=%s%n", addStr, digit, loc);
			}
		}
		return encs;
	}

	/** Encode int []
	 * type BILOCCYCLE_NON_REPEAT = cycle label does not match candidates at location
	 * path id
	 * x digit
	 * y digit
	 * 1st location
	 * 2nd location
	 */
	public static int [] encode( int type, int id, int xDigit, int yDigit, RowCol loc1, RowCol loc2 ){
		int[] enc = new int[] { type, id, xDigit, yDigit,
				loc1.row(), loc1.col(), loc2.row(), loc2.col() };
		return enc;
	}

	@Override
	public String encodingToString(final int[] enc) {
		return toString( enc );
	}

	public static String toString( final int[] enc) {
		String typeStr = typeToString( enc[0] );
		return format("%s pathId %d, x digit %d, y digit %d, loc1 %s, loc2 %s", typeStr, enc[1], enc[2], enc[3],
				ROWCOL[enc[4]][enc[5]], ROWCOL[enc[6]][enc[7]]);
	}

	public static String typeToString( int type ) {
		return 0 == type ? "cycle non repeat" : "unknown";
	}

	/**
	 * Compares an encoding just by the two digits and the two rowCols.
	 * which are located at elements 2, 3, and 4.
	 * Useful for when you don't want duplicates which
	 * match digit and rowCol, but not other fields.
	 */
	public static final Comparator<int[]> DigitRowColComparator =
			new Utils.SubsetComparator(Arrays.asList( 0, 2, 3, 4, 5, 6, 7 ));
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