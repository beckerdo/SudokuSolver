package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;
import info.danbecker.ss.tree.DigitsData;
import info.danbecker.ss.tree.TreeNode;

import java.util.*;
import java.util.stream.Collectors;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Candidates.*;
import static info.danbecker.ss.Utils.Unit;
import static info.danbecker.ss.Utils.addUniques;
import static java.lang.String.format;

/**
 * XYChain
 * <p>
 * From https://hodoku.sourceforge.net/en/tech_chains.php#xyc
 * <p>
 * An XY-Chain is a chain that uses only bivalue cells (similar to Remote Pairs), but the cells can have
 * arbitrary candidates. The only restriction (besides the obvious necessities for the links) is, that
 * the XY-Chain starts and ends with a strong link on the same digit.
 * <p>
 * As with Remote Pairs all strong links are within the cells, all weak links are between the cells.
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class XYChain implements FindUpdateRule {
	@Override
	public int update(Board board, Board solution, Candidates candidates, List<int[]> encs) {
		int updates = 0;
		if ( null == encs) return updates;
		for ( int enci = 0; enci < encs.size(); enci++ ) {
			int[] enc = encs.get(enci);
			// Decode
			// List<Integer> digits = Arrays.asList( enc[0], enc[1]);
			int endDigit = enc[2];
			int chainStart = 4;
			int chainLength = enc[ 3 ];
			int seesStart = chainStart + chainLength * 2;
			List<RowCol> seesLocs = new ArrayList<>();
			for ( int i = seesStart; i < enc.length; i += 2 ) {
				seesLocs.add( ROWCOL[enc[i]][enc[i+1]]);
			}

			for( int loci = 0; loci < seesLocs.size(); loci++) {
				RowCol sLoc = seesLocs.get( loci );
				// Validation if available
				if ( null != solution ) {
					int cellStatus = solution.get(sLoc);
					if ( cellStatus == endDigit ) {
						System.out.println( "Candidates=\n" + candidates.toStringBoxed() );
						throw new IllegalArgumentException( format("Rule %s would like to remove solution digit %d at loc %s.%nenc=%s%n",
								ruleName(), cellStatus, sLoc, encodingToString( enc )));
					}
				}
				// Remove endDigit from sees location
			    // String prev = candidates.getCandidatesStringCompact( sLoc );
				if (candidates.removeCandidate(sLoc, endDigit)) {
					updates++;
					String cStr = candidates.getCompactStr(sLoc);
					System.out.printf("%s removed digit %d from %s, remaining candidates %s%n",
							ruleName(), endDigit, sLoc, cStr );
				}
			}
		}
		return updates;
	}

	/**
	 * Strategy.
	 * -For each pair of digits on the board,
	 *    -For each combo, is each digit on more than one location?
	 *       -Start a pair tree
	 *       -Add another pair if the last node sees it, and it shares a digit
	 *       -Does new end link have one of the starting digits?
	 *          -If yes, add to list
	 *          -If no, proceed to next link.
	 * @return a list of all locations that can see two colors.
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
		Map<Integer, List<RowCol>> digitLocs = digitLocs( pairLocs );

		for ( List<Integer> pair : pairLocs.keySet()) {
			// System.out.printf( "Pair %s, locs %s\n", pair, RowCol.toString(pairLocs.get(pair)));
			for ( int digi = 0; digi < pair.size(); digi++ ) {
				int pDigit = pair.get( digi );
				int eDigit = pair.get(0) == pDigit ? pair.get(1) : pair.get(0);
				List<RowCol> eLocs = digitLocs.get( eDigit );
				// Make sure at least 2 locs (root and end) for the eDigit.
				if ( 1 < eLocs.size() ) {
					for ( int starti = 0; starti < pairLocs.get(pair).size(); starti++ ) {
						RowCol startLoc = pairLocs.get(pair).get( starti );

						DigitsData rData = new DigitsData(pair,startLoc,0 );
						TreeNode<DigitsData> rNode = new TreeNode<>(rData, 3 );
						addUniques( encs, buildPairTree(candidates, rNode, pDigit, eDigit ));
					}
				}
			}
		}
		return encs;
	}

	/** Take map of pair locations and break into map of digit locations.
	 * @param pairLocs a map containing digits to locations
	 * @return a map containing digit to locations
	 */
	public Map<Integer,List<RowCol>> digitLocs( Map<List<Integer>,List<RowCol>> pairLocs ) {
		Map<Integer, List<RowCol>> digitLocs = new HashMap<>();
		for ( List<Integer> pair : pairLocs.keySet()) {
			for ( int pairi = 0; pairi < pair.size(); pairi++ ) {
				int digit = pair.get( pairi );
				List<RowCol> dLocs = digitLocs.computeIfAbsent(digit, k -> new ArrayList<>());
				List<RowCol> pLocs = pairLocs.get(pair);
				for ( RowCol pLoc : pLocs ) {
					if (!dLocs.contains( pLoc)) {
						dLocs.add( pLoc );
					}
				}
			}
		}
		return digitLocs;
	}

	/**
     * Create or expand a tree from given node with these ones-based digits in these locations.
     * For example digit 4,5 in locations [11][16][20][50]
	 * The tree will start at a given location.
	 * Tree building should end when a node has multiple children because that will be shorter
	 * than a single child chain.
     */
	public List<int[]> buildPairTree(Candidates candidates, TreeNode<DigitsData> pNode, int pDigit, int eDigit  ) {
		List<int[]> matched = new ArrayList<>();
		DigitsData pData = pNode.data;
		TreeNode<DigitsData> root = pNode.getRoot();

		// For each unit, find visible locations with this digit.
		for ( Unit unit : Unit.values() ) {
			// Get pairs in this unit with this digit.
			List<RowCol> hopLocs = candidates.candidateUnitGroupLocs( unit, pData.rowCol.unitIndex( unit ), pDigit, 2);
			for ( int loci = 0; loci < hopLocs.size(); loci++ ) {
				RowCol hopLoc = hopLocs.get( loci );
				if ( null == root.findTreeNode( new DigitsData.RowColMatch( new DigitsData( null, hopLoc, -1 )))) {
					// tree does not contain loc
					List<Integer> cDigits = candidates.getCandidatesList( hopLoc );
					// Ensure candidates contains parent digit or else how did we get here?
					if ( !cDigits.contains( pDigit ))
						throw new IllegalStateException( format( "Loc %s should contain digit %d. Candidates=%s\n",
								hopLoc, pDigit, Utils.digitListToString(cDigits)));

					TreeNode<DigitsData> cNode = pNode.setChild( new DigitsData( cDigits, hopLoc, (pData.color + 1 ) % 2 ), unit.ordinal());
					// System.out.printf( "%sParent digit %d at %s (end digit %d) seen by %s digits %s at %s.\n",
					// 		Utils.indent(pNode.getLevel()), pDigit, pData.rowCol, eDigit, unit, Utils.digitListToString(cDigits), hopLoc );

					// Does the child contain the end digit? Must be true to be endpoints of chain.
					if ( cDigits.contains( eDigit )) {
						// Does the child contain the ending digit eDigit? Search for non loc
						List<RowCol> outsideLocs = cellsSeeEndpoints( candidates, root, eDigit, root.data.rowCol, hopLoc );
						if ( 0 < outsideLocs.size() ) {
							// Make encodings, add to list
							// Would be great to express the chain as 3- r7c4 -9- r5c4 -8- r5c6 -2- r2c6 -3
							// System.out.printf("*%sEnd digit %d at root %s and end %s are seen by locs %s.\n",
							// 		Utils.indent(pNode.getLevel()+1), eDigit, root.data.rowCol, cNode.data.rowCol, RowCol.toString(outsideLocs));
							int rDigit = root.data.digits.get(0) == eDigit ? root.data.digits.get(1) : root.data.digits.get(0);
							int smallerD = Math.min( rDigit, eDigit );
							int largerD =Math.max( rDigit, eDigit );
							List<RowCol> treePath = treePath( cNode );
							Utils.addUnique( matched, encode( Arrays.asList( smallerD, largerD ), eDigit, treePath, outsideLocs ));
						} else {
							// Remove cNode from tree because it might be an outside node as the rest of the tree is built.
							pNode.setChild( null, unit.ordinal());
							// System.out.printf(" %sNode %s has 0 outside locs. Pruned.\n",
							// 		Utils.indent(pNode.getLevel()+1), cNode.data);
						}
					} else {
						// Else Recurse
						int nextDigit = cDigits.get( 0 ) == pDigit ? cDigits.get( 1 ) : cDigits.get( 0 );
						addUniques( matched, buildPairTree( candidates, cNode, nextDigit, eDigit ));
					}
				// } else {	// tree contains loc
				}
			}
		} // unit
		return matched;
	}

	/**
	 * Given a child node, return a list that shows the rowCol path from root to child.
	 * @param tNode a node in a tree
	 * @return a list that shows the rowCol path from root to tNode.
	 */
	public static List<RowCol> treePath( TreeNode<DigitsData> tNode ) {
		List<RowCol> path = new ArrayList<>();
		while( null != tNode) {
			path.add( tNode.data.rowCol );
			tNode = tNode.parent;
		}
		Collections.reverse( path );
		return path;
	}

	/**
	 * Check for non tree candidates that can see two endpoints of this tree.
	 * @return List<RowCol> of this digit, not in tree, can see endpoints
	 */
	public List<RowCol> cellsSeeEndpoints(Candidates candidates, TreeNode<DigitsData> rNode, int eDigit, RowCol ep1, RowCol ep2  ) {
		List<RowCol> locs = new LinkedList<>();
		List<RowCol> digitLocs = candidates.digitLocs( eDigit );
		for ( RowCol digitLoc : digitLocs ) {
			if ( null == rNode.findTreeNode( new DigitsData.RowColMatch( new DigitsData( null, digitLoc, -1 )))) {
				// Location not in tree
				if (!ep1.equals(digitLoc) && !ep2.equals(digitLoc)) {
					// This loc is not an endpoint
					if (null != RowCol.firstUnitMatch(ep1, digitLoc) &&
							null != RowCol.firstUnitMatch(ep2, digitLoc)) {
						// This loc shares units with ep1 and ep2
						// Check that root and ep contain the end digit.
						// This is redundant as the caller buildPairTree has validated this.
						List<Integer> ep1Cands = candidates.getCandidatesList( ep1 );
						List<Integer> ep2Cands = candidates.getCandidatesList( ep2 );
						if ( ep1Cands.contains( eDigit ) && ep2Cands.contains( eDigit )) {
							// Ep1 and Ep2 contain the end digit, not just some stepping stone in the chain.
							locs.add(digitLoc);
						}
					}
				}
			}
		}
		return locs;
	}

	// Encode tree as int[]
	// - 01 digits
	// - 2 ep digit
	// - 3 N = number of chain links
	// - 45 rowCol  |  repeat N times
	// - 4 + N*3 row of outside epDigit			| repeat til length
	// - 4 + N*3 + 1 Col of outside epDigit     |
	public static int [] encode( List<Integer> digits, int epDigit, List<RowCol> chainLocs, List<RowCol> epSeesLocs ) {
		if ( digits.size() != 2 || digits.get(0) < 1 || digits.get(0) > 9 || digits.get(1) < 1 || digits.get(1) > 9)
			throw new IllegalArgumentException( "digits=" + Utils.digitListToString(digits));
		if ( 2 >  chainLocs.size())
			throw new IllegalArgumentException( "chain is length " + chainLocs.size() );
		if ( 0 == epSeesLocs.size())
			throw new IllegalArgumentException( "sees list is length 0" );
		int chainStart = digits.size() + 2;
		int seesStart = chainStart + chainLocs.size() * 2;
		int[] enc = new int[ seesStart + epSeesLocs.size() * 2 ];
		for ( int i = 0; i < digits.size(); i++ ) {
			enc[ i ] = digits.get( i );
		}
		enc[ digits.size() ] = epDigit;
		enc[ digits.size() + 1 ] = chainLocs.size();
		for ( int i = 0; i < chainLocs.size(); i++ ) {
			RowCol loc = chainLocs.get(i);
			int loci = chainStart + i * 2;
			enc[ loci ] = loc.row();
			enc[ loci + 1 ] = loc.col();
		}
		for ( int i = 0; i < epSeesLocs.size(); i++ ) {
			RowCol loc = epSeesLocs.get(i);
			int loci = seesStart + i * 2;
			enc[ loci ] = loc.row();
			enc[ loci + 1 ] = loc.col();
		}
		return enc;
	}

	// Encode tree as int[]
	// - 01 digits
	// - 2 ep digit
	// - 3 N = number of chain links
	// - 45 rowCol  |  repeat N times
	// - 4 + N*3 row of outside epDigit			| repeat til length
	// - 4 + N*3 + 1 Col of outside epDigit     |
	@Override
	public String encodingToString( int[] enc) {
		int chainStart = 4;
		int chainLength = enc[ 3 ];
		int seesStart = chainStart + chainLength * 2;
		List<RowCol> chainLocs = new ArrayList<>();
		for ( int i = 0; i < chainLength; i++ ) {
			int encLoc = chainStart + i*2;
			chainLocs.add( ROWCOL[enc[encLoc]][enc[encLoc+1]]);
		}
		List<RowCol> seesLocs = new ArrayList<>();
		for ( int i = seesStart; i < enc.length; i += 2 ) {
			seesLocs.add( ROWCOL[enc[i]][enc[i+1]]);
		}

		return format( "digits %s, end digit %d, chain %s, sees %s" ,
				Utils.digitListToString( Arrays.asList( enc[0], enc[1])),
				enc[2],
				RowCol.toString( chainLocs),
				RowCol.toString( seesLocs)
		);
	}

	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}