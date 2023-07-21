package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Candidates.ALL_COUNTS;
import static info.danbecker.ss.Candidates.ALL_DIGITS;
import static info.danbecker.ss.Utils.*;
import static java.lang.String.format;

/**
 * W-Wing
 * <p>
 * W-Wing consist of two bivalue cells with the same candidates,
 * that are connected by a strong link on one of the candidates.
 * The other candidate can be eliminated from all cells seeing both bivalue cells.
 * <p>
 * From https://hodoku.sourceforge.net/en/tech_wings.php#w
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class WWing implements FindUpdateRule {
	@Override
	// Encode info as int[]
	// - 01 two one-based digits
	// - 23 ep1 rowCol
	// - 45 ep2 rowCol
	// - 6 sl digit
	// - 78 sl1 rowCol
	// - 9A sl2 rowCol
	// - B non sl digit
	// - CD* rowCol sees eps
	public int update(Board board, Board solution, Candidates candidates, List<int[]> encs) {
		int updates = 0;
		if ( null == encs) return updates;
		for ( int enci = 0; enci < encs.size(); enci++ ) {
			int[] enc = encs.get(enci);

			int nslDigit = enc[11];
			for( int loci = 12; loci < enc.length; loci += 2) {
				RowCol loc = ROWCOL[enc[loci]][enc[loci + 1]];
				// Validation, if available
				if ( null != solution ) {
					int cellStatus = solution.get(loc);
					if ( cellStatus == nslDigit ) {
						throw new IllegalArgumentException( format("Rule %s would like to remove solution digit %d at loc %s.",
								ruleName(), nslDigit, loc));
					}
				}

				String prev = candidates.getCompactStr( loc );
				if (candidates.removeCandidate(loc, nslDigit)) {
					updates++;
					String cStr = candidates.getCompactStr(loc);
					System.out.printf("%s removed digit %d from %s, remaining candidates %s\n",
							ruleName(), nslDigit, loc, cStr);
				}
			}
		}
		return updates;
	}

	/**
	 * Strategy.
	 * -For each pair of digits on the board,
	 *    - if more than two instances
	 *       - permute over endpoints, for example digit pair {59} has locs 33,53,78
	 *            must find sightings of 33-53,33-78,53-78
	 *       - for each digit in pair
	 *          - find strong links
	 *             - if one endpoint sees one end, and
	 *               the other endpoint sees the other end
	 *                  - eliminate candidates that see both endpoints
	 * @return a list of all locations that can see two endpoints.
	 */
	@Override
	public List<int[]> find(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		List<int[]> encs = new ArrayList<>();

		// Get sorted set of digit pairs
		List<RowCol> allPairLocs = candidates.getGroupLocations( ALL_DIGITS, 2 );

		Map<List<Integer>, List<RowCol>> pairLocs = allPairLocs.stream()
				.collect( Collectors.groupingBy(candidates::getCandidatesList, Collectors.toList()));

		for ( List<Integer> pair : pairLocs.keySet()) {
			// For each pair with 2 or more locations
			if ( 1 < pairLocs.get( pair ).size()) {
				// System.out.printf( "Pair %s, locs %s\n", pair, RowCol.toString(pairLocs.get(pair)));
				// Look at each pair of endpoint
				List<int[]> endPointCombos = Utils.comboGenerate( pairLocs.get( pair ).size(),2 );
				for( int combi = 0; combi < endPointCombos.size(); combi++) {
					int[] endPointCombo = endPointCombos.get( combi );
					for ( int digiti = 0; digiti < pair.size(); digiti++) {
						addUniques( encs, find(board, candidates, pair, pair.get(digiti),
								pairLocs.get(pair).get(endPointCombo[0]), pairLocs.get(pair).get(endPointCombo[1]) ));
					}
				}
			}
			// int[] zeroBasedDigits = new int[]{ pair.get(0) - 1, pair.get(1) - 1};
			// locs.addAll( find( board, candidates, zeroBasedDigits ));
		}

		return encs;
	}

	/** Given a digit and a pair of endpoints
	 *  - find strong links
	 *  - if one endpoint sees one end, and  the other endpoint sees the other end
	 *  - eliminate candidates that see both endpoints
     */
	public List<int[]> find(Board board, Candidates candidates, List<Integer> digits, int slDigit, RowCol ep1, RowCol ep2 ) {
		List<int[]> encs = new ArrayList<>();
		// System.out.printf( "   Pair %s, digit %d, endpoints %s,%s\n", digits, slDigit, ep1, ep2 );

		// Find strong links
		int[][] unitCounts = candidates.candidateUnitCounts(slDigit);
		// First find all strong links and locations
		for (Unit unit : Unit.values()) {
			for (int uniti = 0; uniti < Utils.UNITS; uniti++) {
				if (2 == unitCounts[unit.ordinal()][uniti]) {
					List<RowCol> strongLink = candidates.candidateUnitGroupLocs(unit, uniti, slDigit, ALL_COUNTS);
					// System.out.printf( "      Digit %d, %s %d strong link at %s\n", slDigit, unit, uniti, RowCol.toString( strongLink ) );
					int nslDigit = digits.get(0) == slDigit ? digits.get(1) : digits.get(0);
					// if one endpoint sees one end, and  the other endpoint sees the other end
					// Note this is a one link hop from ep1 to ep1, there could be multiple link hops
					RowCol sl1 = strongLink.get(0);
					RowCol sl2 = strongLink.get(1);
					if( !ep1.equals(sl1) && !ep2.equals(sl2) && !ep1.equals(sl2) && !ep2.equals(sl1)) {
						List <RowCol> nslLocs = new ArrayList<>();

						Unit unit1 = RowCol.firstUnitMatch(ep1, sl1);
						Unit unit2 = RowCol.firstUnitMatch(ep2, sl2);
						if (null != unit1 && null != unit2) {
							// System.out.printf("         Digit %d, eps %s,%s %s,%s links to sls %s,%s.\n",
							// 		slDigit, ep1, ep2, unit1, unit2, sl1, sl2 );
							// Check for non strong links that see ep1/ep2
							RowCol.addUniques( nslLocs, cellsSeeEndpoints( candidates, nslDigit, ep1, ep2, sl1, sl2 ) );
						}
						unit1 = RowCol.firstUnitMatch(ep1, sl2);
						unit2 = RowCol.firstUnitMatch(ep2, sl1);
						if (null != unit1 && null != unit2) {
							// System.out.printf("         Digit %d, eps %s,%s %s,%s links to sls %s,%s.\n",
							// 		slDigit, ep1, ep2, unit1, unit2, sl2, sl1 );
							// Check for non strong links that see ep1/ep2
							RowCol.addUniques( nslLocs, cellsSeeEndpoints( candidates, nslDigit, ep1, ep2, sl1, sl2 ) );
						}

						// Add uniques to list
						for ( RowCol nslLoc : nslLocs ) {
							int [] enc = encode( Utils.listToArray(digits), ep1, ep2, slDigit, sl1, sl2,
									nslDigit, nslLocs );
							int added = addUnique( encs, enc );
							// if ( 1 == added ) {
							// 	  System.out.printf("   enc %s added.\n", encodingToString(enc) );
							// }
						}
					}
				}
			}
		}

		return encs;
	}

	public List<RowCol> cellsSeeEndpoints(Candidates candidates, int nslDigit, RowCol ep1, RowCol ep2, RowCol sl1, RowCol sl2 ) {
		List<RowCol> locs = new LinkedList<>();
		List<RowCol> digitLocs = candidates.digitLocs( nslDigit );
		for ( RowCol digitLoc : digitLocs ) {
			if (  !ep1.equals( digitLoc ) && !ep2.equals( digitLoc ) && !sl1.equals( digitLoc ) && !sl2.equals( digitLoc )) {
				// This loc is not an endpoint
				if ( null != RowCol.firstUnitMatch(ep1, digitLoc) &&
						null != RowCol.firstUnitMatch(ep2, digitLoc) ) {
					locs.add( digitLoc);
				}
			}
		}
		return locs;
	}

	// Encode info as int[]
	// - 01 two one-based digits
	// - 23 ep1 rowCol
	// - 45 ep2 rowCol
	// - 6 sl digit
	// - 78 sl1 rowCol
	// - 9A sl2 rowCol
	// - B non sl digit
	// - CD* rowCol sees eps
	public static int [] encode( int[] digits, RowCol ep1, RowCol ep2,
		 int slDigit, RowCol sl1, RowCol sl2,
		 int nslDigit, List<RowCol> nslRowCols ) {
		if ( null == digits )
			throw new NullPointerException();
		if ( digits.length != 2 || digits[0] < 1 || digits[0] > 9 || digits[1] < 1 || digits[1] > 9)
			throw new IllegalArgumentException( "digits=" + Utils.digitsToString(digits));
		// perhaps more validation later
		int fixedSize = 12;
		int[] enc = new int[fixedSize + nslRowCols.size() * 2 ];
		enc[0] = digits[0];
		enc[1] = digits[1];
		enc[2] = ep1.row();
		enc[3] = ep1.col();
		enc[4] = ep2.row();
		enc[5] = ep2.col();
		enc[6] = slDigit;
		enc[7] = sl1.row();
		enc[8] = sl1.col();
		enc[9] = sl2.row();
		enc[10] = sl2.col();
		enc[11] = nslDigit;
		for( int i = 0; i < nslRowCols.size(); i++) {
			enc[ fixedSize + i * 2] = nslRowCols.get(i).row();
			enc[ fixedSize + i * 2 + 1] = nslRowCols.get(i).col();
		}
		return enc;
	}

	@Override
	public String encodingToString( int[] enc) {
		int digit0 = enc[0];
		int digit1 = enc[1];
		RowCol ep1 = ROWCOL[enc[2]][enc[3]];
		RowCol ep2 = ROWCOL[enc[4]][enc[5]];
		int slDigit = enc[6];
		RowCol sl1 = ROWCOL[enc[7]][enc[8]];
		RowCol sl2 = ROWCOL[enc[9]][enc[10]];
		int nslDigit = enc[11];
		List<RowCol> nslRowCols = new ArrayList<>();
		int fixedSize = 12;
		for( int i = fixedSize; i < enc.length; i += 2) {
			nslRowCols.add( ROWCOL[enc[ i ]][enc[ i + 1]]);
		}
		return format( "Digits {%d%d} at %s%s, linked by digit %d at %s%s, sees %d at %s",
				digit0, digit1, ep1,ep2, slDigit, sl1,sl2, nslDigit, RowCol.toString(nslRowCols) );
	}

	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}