package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;
import info.danbecker.ss.tree.DigitData;
import info.danbecker.ss.tree.TreeNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static info.danbecker.ss.Board.NOT_FOUND;
import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Candidates.ALL_COUNTS;
import static info.danbecker.ss.Utils.DIGITS;
import static info.danbecker.ss.Utils.Unit;
import static java.lang.String.format;

/**
 * X-Chain
 * <p>
 * X-Chains are chains that use one digit only.
 * The important thing with X-Chains is, that they have to start and end with a strong link.
 * This ensures that one of the endpoints actually contains the chain digit.
 * <p>>
 * That digit can be removed from any cell that sees BOTH ENDS OF THE CHAIN.
 * <p>
 * SimpleColors is similar to X-Chain. SimpleColors matches
 * conjugate pairs. X-Chain matches strong links.
 * <p>
 * From https://hodoku.sourceforge.net/en/tech_chains.php#x
 * <p>
 * As mentioned in https://www.sudopedia.org/wiki/X-Chain,
 * X-Chain is a single-digit solving technique which uses a chain consisting of links
 * that ALTERNATE between STRONG and WEAK links, with the start and end link being strong.
 * <p>
 * This is important because test case Digit6Bug shows if no alternation,
 * digit 6 in 0,8 will be excluded with chain 05=25=28-06=46=48.
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class XChain implements FindUpdateRule {
	@Override
	public int update(Board board, Board solution, Candidates candidates, List<int[]> encs) {
		// SimpleColors sameUpdateProcess = new SimpleColors();
		// return sameUpdateProcess.update( board, solution, candidates, encs );
		int updates = 0;
		if ( null == encs) return updates;
		for ( int enci = 0; enci < encs.size(); enci++ ) {
			int[] enc = encs.get(enci);
			int digit = enc[ 0 ];
			// int type = enc[ 1 ];
			RowCol cLoc = ROWCOL[enc[4]][enc[5]];

			// Validation, if available
			if ( null != solution ) {
				int cellStatus = solution.get(cLoc);
				if ( cellStatus == digit ) {
					throw new IllegalArgumentException( format("Rule %s would like to remove solution digit %d at loc %s.",
							ruleName(), digit, cLoc));
				}
			}

			// String prev = candidates.getCandidatesStringCompact( cLoc );
			if ( candidates.removeCandidate( cLoc, digit )) {
				updates++;
				// String cStr = candidates.getCandidatesStringCompact( cLoc );
				// System.out.println( format( "%s %s removed digit %d from %s, remaining candidates %s",
				// 		this.ruleName(), typeString, digit, cLoc, cStr ));
			}
		}
		return updates;
	}

	/**
	 * Strategy.
	 * -For each pair of digits on the board,
	 *    -find the longest chain for each location.
	 *    -test for either digit, that can see two color PairNodes in the chain.
	 * <p>
	 * Note that the list may have duplicate candidates when it  can see 3 other locs in chain
	 * @return a list of all locations that can see two colors.
	 */
	@Override
	public List<int[]> find(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		List<int[]> locs = new ArrayList<>();

		for (int digi = 1; digi <= DIGITS; digi++) {
			if ( !board.digitCompleted( digi )) {
				locs.addAll( find( board, candidates, digi ) );
			}
		}
		return locs;
	}

	/**
	 * Strategy.
	 * -For each digit
	 *    -enumerate strong link location pairs (units that have only two of that digit)
	 *    -attempt to connect link ends with other seen locations
	 *       -see if other candidate locations see endpoints
	 * <p>
	 * Useful for testing
	 * @return a list of all locations that can see two colors.
	 */
	public List<int[]> find(Board board, Candidates candidates, int digit ) {
		List<int[]> problemLocs = new LinkedList<>();
		List<RowCol> digitLocs = candidates.digitLocs(digit);
		int[][] unitCounts = candidates.candidateUnitCounts(digit);
		List<List<RowCol>> strongLinks = new LinkedList<>();
		// First find all strong links and locations
		for (Unit unit : Unit.values()) {
			for (int uniti = 0; uniti < Utils.UNITS; uniti++) {
				if (1 == unitCounts[unit.ordinal()][uniti]) {
					List<RowCol> single = candidates.candidateUnitGroupLocs(unit, uniti, digit, ALL_COUNTS);
					System.out.printf("Warning, %s found single digit %d in %s %d, locs=%s%n",
							ruleName(), digit, unit, uniti, RowCol.toString(single));
				} else if (2 == unitCounts[unit.ordinal()][uniti]) {
					List<RowCol> strongLink = candidates.candidateUnitGroupLocs(unit, uniti, digit, ALL_COUNTS);
					// Avoid BOX links that equal a ROW or COL link. Include if different.
					// Note that some strong links, might share end points.
					if (NOT_FOUND == RowCol.indexOf(strongLinks, strongLink)) {
						// System.out.println( format("Strong link digit %d in %s %d, locs=%s",
						// 		digit, unit, uniti, RowCol.toString( strongLink )));
						strongLinks.add(strongLink);
					}
				}
			}
		}
		// System.out.printf("Digit %d locs %s, strong links %s%n",
		// 		digit, RowCol.toString(digitLocs), RowCol.toString(strongLinks,"/") );

		// Method
		// for each strong link (A=B)
		//    for each endpoint (A,B)
		//       next strong link (C=D)
		//          A=B-C=D?
		//          A=B-D=C?
		//          B=A-C=D?
		//          B=A-D=C?
		//             for each, check for exclusions
        //             yes, add to problem list, end
		//             no, recurse
		List<int[]> foundLocs = buildXChain( candidates, digit,null, digitLocs, strongLinks );
		addUniques( problemLocs, foundLocs );

		return problemLocs;
	}

	/** Add non-duplicate encodings from potentials to the bigList
	 *
	 * @param bigList
	 * @param potentials
	 * @return number of new potentials added.
	 */
	public static int addUniques( List<int[]> bigList, List<int[]> potentials ) {
		int added = 0;
		for (int loci = 0; loci < potentials.size(); loci++) {
			int [] enc = potentials.get(loci);
			if (!bigList.contains( enc)) {
				bigList.add(enc);
				added++;
			}
		}
		return added;
	}

	public List<int[]> buildXChain(Candidates candidates, int digit, TreeNode<DigitData> pNode,
	   List<RowCol> digitLocs, List<List<RowCol>> strongLinks ) {
		List<int[]> problemLocs = new ArrayList<>();
		if ( 0 == digitLocs.size() ) return problemLocs;
		if ( 0 == strongLinks.size() ) return problemLocs;

		// For each strong link recurse X-Chain
		for (int linki = 0; linki < strongLinks.size(); linki++) {
			List<RowCol> strongLink = strongLinks.get( linki );

			// Remove link and locs from subset lists
			RowCol A = strongLink.get( 0 );
			RowCol B = strongLink.get( 1 );
			List<RowCol> digitSubset = new LinkedList<>(digitLocs); // deep copy of digit locs, for modifications
			digitSubset.remove( A );
			digitSubset.remove( B );
			List<List<RowCol>> strongLinksSubset = new LinkedList<>(strongLinks);
			strongLinksSubset.remove(linki);

			if ( null == pNode) {
				// make permutations of new trees A=B, B=A
				// A=B
				// System.out.printf("%sL%d-l%d Digit %d strong link %d perm A: %s\n",
				// 		Utils.indent(level), level, linki, digit, linki, RowCol.toString(Arrays.asList(A, B)));
				createStrongLinkTree(candidates, digit, A, B, digitSubset, strongLinksSubset, problemLocs);

				// B=A
				// System.out.printf("%sL%d-l%d Digit %d strong link %d perm B: %s\n",
				// 		Utils.indent(level), level, linki, digit, linki, RowCol.toString(Arrays.asList(B, A)));
				createStrongLinkTree(candidates, digit, B, A, digitSubset, strongLinksSubset, problemLocs);
			} else {
				// for new links, attempt to see parent node with a weak link from each strong link endpoint
				// A=B
				// System.out.printf("%sL%d-l%d Digit %d strong link %d perm A: %s\n",
				// 		Utils.indent(level), level, linki, digit, linki, RowCol.toString(Arrays.asList(A, B)));
				addStrongLink(candidates, digit, pNode, A, B, digitSubset, strongLinksSubset, problemLocs );
				// B=A
				// System.out.printf("%sL%d-l%d Digit %d strong link %d perm B: %s\n",
				// 		Utils.indent(level), level, linki, digit, linki, RowCol.toString(Arrays.asList(B, A)));
				addStrongLink(candidates, digit, pNode, B, A, digitSubset, strongLinksSubset, problemLocs );
			}
		} // for each strongLink
		return problemLocs;
	}

	/**
	 * Start a tree with two strong link endpoints.
	 * @param candidates
	 * @param digit
	 * @param a
	 * @param b
	 * @param digitSubset
	 * @param strongLinksSubset
	 * @param problemLocs problem encodings are added to this list
	 */
	public void createStrongLinkTree(Candidates candidates, int digit, RowCol a, RowCol b, List<RowCol> digitSubset, List<List<RowCol>> strongLinksSubset, List<int[]> problemLocs) {
		// Add strongLink as a unit to tree
		TreeNode<DigitData> rNode = new TreeNode<>(new DigitData(digit, a, 0), 3);
		DigitData bData = new DigitData(digit, b, 1);
		TreeNode<DigitData> bNode = rNode.setChild(bData, RowCol.firstUnitMatch(a, b).ordinal() ); // null not possible
		// Recurse from bNode
		List<int[]> moreProblems = buildXChain(candidates, digit, bNode,
				digitSubset, strongLinksSubset);
		addUniques(problemLocs, moreProblems );
	}

	/**
	 * Continue a tree with the endpoints if they can be weak-linked to pNode
	 *
	 * @param candidates
	 * @param digit
	 * @param pNode
	 * @param a
	 * @param b
	 * @param digitSubset
	 * @param strongLinksSubset
	 * @param problemLocs problem encodings are added tp this list
	 */
	public void addStrongLink(Candidates candidates, int digit, TreeNode<DigitData> pNode, RowCol a, RowCol b, List<RowCol> digitSubset, List<List<RowCol>> strongLinksSubset, List<int[]> problemLocs ) {
		if ( null == pNode)
			throw new NullPointerException( "pNode == null");
		// int level = pNode.getLevel(); // needed for debug logging
		TreeNode<DigitData> rNode = pNode.getRoot();
		DigitData aData = new DigitData(digit, a, (pNode.data.color + 1 ) % 2);
		DigitData bData = new DigitData(digit, b, (aData.color + 1 ) % 2);
		// Only add if both nodes are outside tree
		// No use in checking B for exclusions if it is already in the tree
		if ((null == rNode.findTreeNode(new DigitData.RowColMatch(aData))) &&
		    (null == rNode.findTreeNode(new DigitData.RowColMatch(bData)))) {
			// link endpoints not in tree
			// match first endpoint
			Unit unitMatch = RowCol.firstUnitMatch( pNode.data.rowCol, a);
			if ( null != unitMatch ) {
				int unitCount = candidates.candidateUnitCount( unitMatch,
						RowCol.firstUnitMatchIndex( pNode.data.rowCol, a), digit);
				if ( 2 < unitCount ) {
					// weak link connector to aNode
					TreeNode<DigitData> aNode = pNode.setChild(aData, unitMatch.ordinal() ); // null not possible
					TreeNode<DigitData> bNode = aNode.setChild(bData, RowCol.firstUnitMatch(a, b).ordinal() ); // null not possible

					// Check for outside candidate exlusions and add
					List<int[]> outsideSees = outsideSeesTwoDifferentEndpointsType0Trap(candidates, rNode, bNode, digitSubset);
					int newProbs = addUniques(problemLocs, outsideSees );
					// if ( 0 < newProbs ) {
						// Verbosity
						// rNode.printTree();
						// System.out.printf("Digit %d, weak %s link from %s to %s, check %s to see %s and %s, %d new probs.\n",
						// 		digit, unitMatch, pNode.data.rowCol, a, digitSubset, rNode, bNode, newProbs);
					// }

					// recurse
					List<int[]> moreProblems = buildXChain(candidates, digit, bNode, digitSubset, strongLinksSubset);
					addUniques(problemLocs, moreProblems );
				}
			}
		}
	}

	/**
	 * Check for non tree candidates that can see two color endpoints of this tree.
	 * Color Trap (type 0): An uncolored cell (outside tree) that sees cells of opposite colors.
	 *
	 * @param candidates
	 * @param tree one end of tree
	 * @param end another end of tree
	 * @param remainingLocs location with this digit that is not in the tree
	 * @return int [] of candidates to remove
	 */
	public static List<int[]> outsideSeesTwoDifferentEndpointsType0Trap(Candidates candidates, TreeNode<DigitData> tree, TreeNode<DigitData> end, List<RowCol> remainingLocs ) {
		List<int[]> seesTwo = new LinkedList<>();
		int digit = tree.data.digit;
		if (tree.data.color == end.data.color) {
			// System.out.printf( "   Digit %d tree begin/end same colors at %s and %s\n",
			//		digit, tree.data, end.data );
			return seesTwo;
		}
		for ( int loci = 0; loci < remainingLocs.size(); loci++) {
			RowCol rowCol = remainingLocs.get(loci);
			DigitData cData = new DigitData(digit,rowCol,-1);
			if ( null == tree.findTreeNode( new DigitData.RowColMatch(cData))) {
				// remainingLoc RowCol is not in given tree
                // Are both tree node and end node visible
				List<TreeNode<DigitData>> sameUnitNodes = tree.findTreeNodes(new DigitData.AnyUnitMatch(cData));
				if( sameUnitNodes.contains( tree ) && sameUnitNodes.contains( end )) {
					// Is last link of tree strong or weak?
					// May occur when we added strong loc C of C-D to AB and b---c is weak.
					// May also occur on AB when one AB unit is strong and another AB unit is weak
					TreeNode<DigitData> penultimate = end.getParent();
					Unit penUnit = RowCol.firstUnitMatch( penultimate.data.rowCol, end.data.rowCol );
					int penUnitCount = candidates.candidateUnitCount(penUnit,
							RowCol.firstUnitMatchIndex(penultimate.data.rowCol, end.data.rowCol), digit);
					if (2 == penUnitCount ) {
						// Last link is strong
						// Unit treeUnit = RowCol.firstUnitMatch( tree.data.rowCol, rowCol );
						// Unit endUnit = RowCol.firstUnitMatch( end.data.rowCol, rowCol );
						// tree.printTree();
						// System.out.printf( "Color Trap digit %d at %s, can see %s %s and %s %s\n",
						// 		digit, rowCol, treeUnit, tree, endUnit, end );
						// Add enc data.
						int[] enc = SimpleColors.encode(digit, 0,
								tree.data.rowCol,
								cData.rowCol,
								tree.data.color, tree.data.rowCol,
								end.data.color, end.data.rowCol );
						// Manually search for same int[] enc
						if ( NOT_FOUND == Utils.findFirst( seesTwo, enc) )
							seesTwo.add(enc);
					// } else {
						// Last link is weak
						//System.out.printf( "   Digit %d tree end at %s has weak %s link to %s\n",
						// 		digit, end.data, penUnit, penultimate.data );
					}
				}
			// } else {
				// RowCol is in given tree. It should be "outside" the tree
				// System.out.printf("Color Trap digit %d at %s should not be in tree starting at %s%n",
				//		digit, rowCol, tree.data );
			}
		}
		return seesTwo;
	}

	// Encode tree as int[] taken from SimpleColors
	@Override
	public String encodingToString( int[] enc) {
		String typeString = (0 == enc[1]) ? "color trap" : "color wrap";
		if ( 0 == enc[1] )
			return format( "digit %d %s, tree %s, cand %s sees %d at %s and %d at %s" ,
					enc[0], typeString,
					ROWCOL[enc[2]][enc[3]], ROWCOL[enc[4]][enc[5]],
					enc[6], ROWCOL[enc[7]][enc[8]],
					enc[9], ROWCOL[enc[10]][enc[11]]);
		else
			return format( "digit %d %s, tree %s, child %s color %d sees %s color %d" ,
					enc[0], typeString,
					ROWCOL[enc[2]][enc[3]], ROWCOL[enc[4]][enc[5]], enc[6], ROWCOL[enc[7]][enc[8]], enc[6] );
	}

	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}