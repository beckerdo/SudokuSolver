package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;
import info.danbecker.ss.graph.EdgePatternFinder;
import info.danbecker.ss.graph.GraphDisplay;
import info.danbecker.ss.graph.GraphUtils;
import info.danbecker.ss.graph.LabelEdge;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;

import java.util.*;

import static info.danbecker.ss.Board.ROWCOL;
import static java.lang.String.format;

/**
 * This BiLocCycleDigitRepeat rule is based on the research paper
 * "Nonrepetitive Paths and Cycles in Graphs
 * with Application to Sudoku" by David Eppstein.
 * <p>
 * That paper has a number of methods based on
 * bilocation (3.2) and bivalue (3.5)
 * This code implements the following methods:
 * <ul>
 *     <li>Bilocation cycle repetitive digit rule of 3.3 (type 1)</li>
 * </ul>
 * <p>
 * Use BiLocCycleDigitRepeat when there are many candidate pairs.
 * <p>
 * Note that Lemma 7 on page 11 states "Let C be a cycle in the bilocation graph,
 * in which exactly one pair of consecutive edges shares a repeated label."
 * Therefore cycles with repeats of XX and multiple XX, YY patterns are bad.
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class BiLocCycleDigitRepeat implements FindUpdateRule {
	public static final int BILOCCYCLE_DIGIT_REPEAT = 1;

	@Override
	// enc int []
	// type BILOCCYCLE_DIGIT_REPEAT = repeat single digit at location
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
			String typeStr = typeToString( enc[0] );
			int pathId = enc[1];
			int digit = enc[2];
			RowCol loc = ROWCOL[enc[3]][enc[4]];
			System.out.printf( "Rule %s, enc %d=%s%n", ruleName(), enci, encodingToString( enc ));

			// Validation if available
			if ( null != solution ) {
				int solutionDigit = solution.get(loc);
				if ( solutionDigit != digit ) {
					// System.out.println( "Board=\n" + solution.toString() );
					System.out.println( "Candidates=\n" + candidates.toStringBoxed() );
					String msg = format("Rule %s update error pathId %d digit %d at loc %s which has solution %d ***",
							ruleName(), pathId, digit, loc, solutionDigit);
					// System.out.println( msg );
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

		Graph<RowCol,LabelEdge> bilocGraph = GraphUtils.getBilocGraph( candidates );
		// DisplayGraph will cause test case to not exit. Use only for debugging.
		// new GraphDisplay( "BiLoc Graph ", 0, bilocGraph );

		List<GraphPath<RowCol,LabelEdge>> gpl = GraphUtils.getGraphCycles( bilocGraph);
		for( int gpi = 0; gpi < gpl.size(); gpi++ ) {
			GraphPath<RowCol, LabelEdge> gp = gpl.get(gpi);
			// String label = "Path " + gpi + "=" + GraphUtils.pathToString( gp, "-", false );
			// System.out.println( label );
			// new GraphDisplay( label, gpi, gp );

			List<int[]> found = findCycleRepeatDigit33( gpi, gp );
			// Repeats due to same digit, location, different path id
			Utils.addUniques( matched, found, DigitRowColComparator );
		}
		return matched;
	}

	/**
	 * Lists any location that has EXACTLY ONE pair of consecutive edges with a repeated label.
	 * This corresponds to the Repetitive Cycle Rule in 3.3 in Eppstein "Non Repetitive Paths and Cycles"
	 * <p>
	 * Note that Lemma 7 on page 11 states "Let C be a cycle in the bilocation graph,
	 * in which exactly one pair of consecutive edges shares a repeated label."
	 * Therefore cycles with repeats of XX, XXX, and multiple XX, YY patterns are bad.
	 * Do not use this rule for multi-digit labels (ignore those edges).
	 * Target location must have 2 candidates (strong link), not 3 or more (weak link).
	 *
	 * @param pathId
	 * @param gp
	 * @return
	 */
	public static List<int[]> findCycleRepeatDigit33( int pathId, final GraphPath<RowCol,LabelEdge> gp ) {
		List<int []> encs = new ArrayList<>();
		// For this path, find the single digit pattern XX.
		EdgePatternFinder patternFinder = new EdgePatternFinder(gp, EdgePatternFinder.XX_NAME);
		Map<String, List<RowCol>> matches = patternFinder.getMatches();

		// Only take paths with single pattern
		if (1 == matches.entrySet().size()) {
			// Encode and add the digit labels and vertices
			for (Map.Entry<String, List<RowCol>> entry : matches.entrySet()) {
				String pattern = entry.getKey();
				List<RowCol> locs = entry.getValue();
				// Only take paths with pattern, single location
				if (1 == locs.size()) {
					List<String> multiDigits = patternFinder.getMultiDigitLabels();
					// Only take paths with no multi-digit labels. (Failed 20221221 puzzle)
					if (0 == multiDigits.size()) {
						int digit = Integer.parseInt(pattern.substring(0, 1));
						System.out.printf("Pattern=%s, pathStr=%s, locs=%s%n",
								pattern, patternFinder.pathString(), RowCol.toString(locs));
						System.out.printf("Path %d=%s%n",
								pathId, GraphUtils.pathToString(gp, "-", false));
						for (RowCol loc : locs) {
							int[] enc = encode(BILOCCYCLE_DIGIT_REPEAT, pathId, digit, loc, loc, loc);
							int added = Utils.addUnique(encs, enc, DigitRowColComparator);
							// String addStr = (0 == added) ? "dup of" : "added";
							// System.out.printf("%s digit=%d. loc=%s%n", addStr, digit, loc);
						}
					} // no multidigits
				} // single location
			}
		} // single entry
		return encs;
	}

	/** Encode int []
	 * type BILOCCYCLE_DIGIT_REPEAT = repetitive cycle single repeat digit at location
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
		String typeStr = typeToString( enc[0] );
		return format("%s pathId %d, digit %d, orig loc %s->%s,%s", typeStr, enc[1], enc[2],
				ROWCOL[enc[3]][enc[4]], ROWCOL[enc[5]][enc[6]], ROWCOL[enc[7]][enc[8]]);
	}

	public static String typeToString( int type ) {
		return 1 == type ? "cycle repeat" : "unknown";
	}

	/**
	 * Compares an encoding just by one digit and rowCol.
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