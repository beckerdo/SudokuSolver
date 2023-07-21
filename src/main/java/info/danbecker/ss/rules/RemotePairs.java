package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;
import info.danbecker.ss.tree.DigitsData;
import info.danbecker.ss.tree.TreeNode;

import java.util.*;

import static info.danbecker.ss.Board.*;
import static info.danbecker.ss.Candidates.*;
import static info.danbecker.ss.Utils.Unit;
import static java.lang.String.format;

/**
 * RemotePairs
 * <p>
 * Remote Pair is the simplest chaining technique. It considers only bivalue cells that contain the same two candidates.
 * Since the cells are bivalue, a strong link exists within every cell between the two candidates.
 * The links between the cells can therefore be weak (the cells have to see each other).
 * <p>
 * To eliminate something, the chain has to be at least four cells long. The Remote Pair ensures
 * that any cell within the chain has the opposite value of the cell before it.
 * <p>>
 * Any cell outside the Remote Pair that sees two cells with different values cannot have one of the Remote Pair digits set.
 * <p>
 * From https://hodoku.sourceforge.net/en/tech_chains.php#rp
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class RemotePairs implements FindUpdateRule {
	@Override
	// Location int [] index map
	// - 01 two one-based digits
	// - 2 type						0=ColorTrap,1=ColorWrap
	// - 34 tree root rowCol
	// - 56 cand rowCol
	// - 7 first color
	// - 89 first RowCol
	// - A second color
	// - BC second RowCOl
	public int update(Board board, Board solution, Candidates candidates, List<int[]> encs) {
		int updates = 0;
		if ( null == encs) return updates;
		for ( int enci = 0; enci < encs.size(); enci++ ) {
			int[] enc = encs.get(enci);
			int[] digits = new int[]{enc[ 0 ], enc[1]};
			int type = enc[ 2 ];
			String typeString = switch( type ) {
				case 0 -> "color trap";
				case 1 -> "color wrap";
				default -> "type " + type;
			};
			RowCol cLoc = ROWCOL[enc[5]][enc[6]];

			// Validation, if available
			if ( null != solution ) {
				int cellStatus = solution.get(cLoc);
				if ( cellStatus == digits[0] ) {
					throw new IllegalArgumentException( format("Rule %s would like to remove solution digit %d at loc %s.",
							ruleName(), digits[0], cLoc));
				}
				if ( cellStatus == digits[1] ) {
					throw new IllegalArgumentException( format("Rule %s would like to remove solution digit %d at loc %s.",
							ruleName(), digits[1], cLoc));
				}
			}

			String prev = candidates.getCompactStr( cLoc );
			for ( int digi = 0; digi < 2; digi++) {
				if (candidates.removeCandidate(cLoc, digits[digi])) {
					updates++;
					String cStr = candidates.getCompactStr(cLoc);
					System.out.println(format("%s %s removed digit %d from %s, remaining candidates %s",
							ruleName(), typeString, digits[digi], cLoc, cStr));
				}
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

		// Get sorted set of digit pairs
		List<RowCol> allPairLocs = candidates.getGroupLocations( ALL_DIGITS, 2 );
		allPairLocs.sort(RowCol.RowColComparator);
		Set<List<Integer>> pairDigits = new HashSet<>();
		for ( RowCol loc : allPairLocs ) {
			// 	public List<Integer> getCandidatesList( RowCol rowCol ) {
			pairDigits.add( candidates.getCandidatesList( loc ));
		}
		List<List<Integer>> sortedPairDigits = new ArrayList<>(pairDigits);
		Collections.sort(sortedPairDigits, Candidates.CandidatesComparator);
		// System.out.println( "Pair digits=" + sortedPairDigits.toString());

		for ( List<Integer> pair : sortedPairDigits) {
			int[] zeroBasedDigits = new int[]{ pair.get(0) - 1, pair.get(1) - 1};
			locs.addAll( find( board, candidates, zeroBasedDigits ));
		}
		return locs;
	}

	/**
	 * Strategy.
	 * -For a given pair of digits on the board,
	 *    -find the longest chain for each location.
	 *    -test for either digit, that can see two color PairNodes in the chain.
	 * Useful for testing
	 * @return a list of all locations that can see two colors.
	 */
	public List<int[]> find(Board board, Candidates candidates, int[] zeroBasedDigits ) {
		if (null == candidates)
			return null;
		if ( null == zeroBasedDigits )
			throw new NullPointerException();
		if ( zeroBasedDigits.length != 2 || zeroBasedDigits[0] < 0 || zeroBasedDigits[1] > 8 || zeroBasedDigits[1] < 0 || zeroBasedDigits[1] > 8)
			throw new IllegalArgumentException( "zb digits=" + Utils.digitsToString(zeroBasedDigits));
		List<int[]> locs = new ArrayList<>();

		List<Integer> obDigitPair = Utils.comboToDigits(zeroBasedDigits);
		List<RowCol> pairLocs = candidates.candidateComboAllLocations( zeroBasedDigits, Candidates.FULL_COMBI_MATCH);
		if ( 2 < pairLocs.size()) {
			// System.out.println("Digits=" + obDigitPair.toString() + ", locs=" + pairLocs.toString());
			// Make a long linear chain from the locations.
			// Count each location's visibility to determine an endpoint.
			int mini = TreeNode.pickMinSights( candidates, zeroBasedDigits, pairLocs);
			if ( -1 != mini ) {
				RowCol treeLoc = pairLocs.get( mini );
				// System.out.println( format( "   tree of digits %s starting at %s", obDigitPair, pairLocs.get(mini) ));
				TreeNode<DigitsData> tree = new TreeNode<>(new DigitsData(obDigitPair, treeLoc, 0), 3);
				List<int[]> treeClashes = buildPairTree(candidates, tree, obDigitPair, pairLocs );
				// System.out.println(format("Digit pair %s tree at %s has %d nodes and %d clashes",
				// 		obDigitPair.toString(), treeLoc, tree.size(), treeClashes.size()));
				// tree.printTree();
				// Add clashes to matched locations
				locs.addAll( treeClashes );

				// Now add in all non-pair candidates that can see two colors
				List<int[]> twoDifferent = cellSeesTwoDifferentType0Trap(candidates, tree, obDigitPair );
				locs.addAll( twoDifferent );
			}
		}
		return locs;
	}

	/**
     * Create or expand a tree from given node with these ones-based digits in these locations..
     * For example digit 4,5 in locations [11][16][20][50]
	 * The tree will start at a given location.
	 * Tree building should end when a node has multiple children because that will be shorter
	 * than a single child chain.
     */
	public List<int[]> buildPairTree(Candidates candidates, TreeNode<DigitsData> parentNode, List<Integer> digits, List<RowCol> pairLocs ) {
		if (null == parentNode )
			throw new NullPointerException( "parent node is null");
		if (null == parentNode.data )
			throw new NullPointerException( "parent node data is null");
		List<int[]> matched = new ArrayList<>();
		DigitsData pData = parentNode.data;
		TreeNode<DigitsData> root = parentNode.getRoot();

		// For each unit, find visible locations with this digit, group size.
		boolean [] childAdded = new boolean[] { false, false, false };
		for ( Unit unit : Unit.values() ) {
			TreeNode<DigitsData> child = parentNode.getChild( unit.ordinal() );
			if ( null != child ) {
				DigitsData cData = child.data;
				if ( null == cData) {
					// child data is null, let's look for children;
					// System.out.println( format("Rule %s, rowCol [%d,%d], digit %d, unit %s has child node, null child data.",
					// ruleName(), data.rowCol[0], data.rowCol[1], digi, unit.name() ));
					int uniti = switch (unit) {
						case ROW -> pData.rowCol.row();
						case COL -> pData.rowCol.col();
						case BOX -> pData.rowCol.box();
					};

					int[] zeroBased = Utils.digitsToCombo( candidates.getCandidatesList( pData.rowCol ) );
					List<RowCol> unitLocs = candidates.candidateComboUnitLocations( unit, uniti, zeroBased, NAKED, FULL_COMBI_MATCH );
					if (2 == unitLocs.size()) {
						// Only add conjugate pairs to color tree
						for (int loci = 0; loci < unitLocs.size(); loci++) {
							RowCol loc = unitLocs.get(loci);
							if ( !loc.equals(pData.rowCol)) {
								int nextColor = (0 == pData.color) ? 1 : 0;
								cData = new DigitsData(digits, loc, nextColor);
								TreeNode<DigitsData> foundNode = root.findTreeNode( new DigitsData.RowColMatch(cData));
								if (null == foundNode) {
									// unitNode not in tree
									// System.out.println( format("   Digit %d, parent %s, loc %s not in tree", digi, pData.rowCol, loc));
									// A child should not see another node in the tree that is the same color
									List<int[]> seesSameColor = childSeesSameType1Wrap( candidates, root, cData );
									if ( 0 == seesSameColor.size() ) {
										// Add as child. Recurse to child node.
										parentNode.setChild(cData, unit.ordinal());
										childAdded[unit.ordinal()] = true;
										// System.out.println( format("Rule %s, node %s added %s candidate at %s, seen by %d tree nodes (no color clash) %s",
										// 	ruleName(), pData.toString(), unit.name(), Utils.locationString(loc), sameUnitNodes.size(), sameUnitNodes ));
										// Depth first recursion
										List<int[]> colorClashes = buildPairTree(candidates,parentNode.getChild(unit.ordinal()), digits, pairLocs);
										matched.addAll(colorClashes);

									} else {
										matched.addAll(seesSameColor);
									}
								}
							}

						}
					}
				} else {
//					System.out.println( format("Rule %s, rowCol %s, digit %d, unit %s has child node with data %s",
//							ruleName(), pData.rowCol, digit, unit.name(), pData ));
				}
			} else {
				// By implementation detail, should not have null children, only null child data.
//				throw new IllegalArgumentException(format("Rule %s, rowCol %s, digit %d, unit %s has null child.",
//						ruleName(), pData.rowCol, digi, unit.name() ));
			}
		} // unit
		return matched;
	}

	/**
	 * Check for new child being able to see two colors of this tree.
	 * Color Wrap (type 1): A new child (inside tree) that sees cells of opposite colors.
	 *
	 * @param candidates
	 * @param root
	 * @param proposedChild
	 * @return int [] of candidates to remove
	 */
	public List<int[]> childSeesSameType1Wrap(Candidates candidates, TreeNode<DigitsData> root, DigitsData proposedChild ) {
		List<int[]> seesSame = new LinkedList<>();
		List<TreeNode<DigitsData>> sameUnitNodes = root.findTreeNodes(new DigitsData.AnyUnitMatch(proposedChild));
		for ( int nodei = 0; nodei < sameUnitNodes.size(); nodei++) {
			TreeNode<DigitsData> sameUnit = sameUnitNodes.get(nodei);
			if ( proposedChild.color == sameUnit.data.color ) {
				// System.out.println(format("Color wrap (type 1 int) proposed child %s sees same color tree node %s",
				// proposedChild, sameUnit.data ));
				// Just repeat sameUnit for second color and rowCol
				int[] enc = encode(Utils.listToArray(root.data.digits), 1, root.data.rowCol, proposedChild.rowCol,
						sameUnit.data.color, sameUnit.data.rowCol,
						sameUnit.data.color, sameUnit.data.rowCol );
				seesSame.add( enc );
			}
		}
		return seesSame;
	}

	/**
	 * Check for non tree candidates that can see two colors of this tree.
	 * Color Trap (type 0): An uncolored cell (outside tree) that sees cells of opposite colors.
	 *
	 * @param candidates
	 * @param tree
	 * @param digits
	 * @return int [] of candidates to remove
	 */
	public List<int[]> cellSeesTwoDifferentType0Trap(Candidates candidates, TreeNode<DigitsData> tree, List<Integer> digits ) {
		List<int[]> seesTwo = new LinkedList<>();
		for (int digi = 0; digi < digits.size(); digi++) {
			int digit = digits.get(digi);
			List<RowCol> locs = candidates.getGroupLocations(digit, ALL_COUNTS);
			for (int loci = 0; loci < locs.size(); loci++) {
				RowCol rowCol = locs.get(loci);
				DigitsData pData = new DigitsData( digits, rowCol, -1);
				if (null == tree.findTreeNode(new DigitsData.RowColMatch(pData))) {
					// RowCol is not in given tree
					List<TreeNode<DigitsData>> sameUnitNodes = tree.findTreeNodes(new DigitsData.AnyUnitMatch(pData));
					// Check is all nodes have same color.
					boolean colorClashAnyUnit = false;
					if (1 < sameUnitNodes.size()) {
						DigitsData firstColorData = null;
						for (int sameUniti = 0; sameUniti < sameUnitNodes.size(); sameUniti++) {
							TreeNode<DigitsData> sameUnitNode = sameUnitNodes.get(sameUniti);
							if (null == firstColorData) firstColorData = sameUnitNode.data;
							if (firstColorData.color != sameUnitNode.data.color) {
								// System.out.println( format( "Color Trap (type 0 ext) digit %d at %s, can see %s",
								// 		digit, rowCol, DigitsData.toString( sameUnitNodes )));
								// Add enc data.
								// Warning, two digits or one of the two digits might see same color.
								int[] enc = encode( Utils.listToArray(digits), 0, tree.data.rowCol, pData.rowCol,
										firstColorData.color, firstColorData.rowCol,
										sameUnitNode.data.color, sameUnitNode.data.rowCol);
								seesTwo.add(enc);
							}
						}
					}
				}
			}
		} // for each digit
		return seesTwo;
	}

	// Encode tree as int[]
	// - 01 two one-based digits
	// - 2 type						0=ColorTrap,1=ColorWrap
	// - 34 tree root rowCol
	// - 56 cand rowCol
	// - 7 first color
	// - 89 first RowCol
	// - A second color
	// - BC second RowCOl
	public static int [] encode( int[] digits, int type, RowCol root, RowCol cand,
								 int firstColor, RowCol first, int secondColor, RowCol second ) {
		if ( null == digits )
			throw new NullPointerException();
		if ( digits.length != 2 || digits[0] < 1 || digits[1] > 9 || digits[1] < 1 || digits[1] > 9)
			throw new IllegalArgumentException( "digits=" + Utils.digitsToString(digits));
		// perhaps more validation later
		return new int[] {digits[0],digits[1],
				type,
				root.row(), root.col(),
				cand.row(), cand.col(),
				firstColor, first.row(), first.col(),
				secondColor, second.row(), second.col()};
	}

	@Override
	public String encodingToString( int[] enc) {
		String typeString = (0 == enc[2]) ? "color trap" : "child sees same (wrap)";
		if ( 0 == enc[2] )
			return format( "digits %d,%d %s, root %s, cands at %s sees %d at %s and %d at %s" ,
					enc[0], enc[1], typeString,
					ROWCOL[enc[3]][enc[4]], ROWCOL[enc[5]][enc[6]],
					enc[7], ROWCOL[enc[8]][enc[9]],
					enc[10], ROWCOL[enc[11]][enc[12]]);
		else
			return format( "digits %d,%d %s, root %s, cand %s color %d sees %s color %d" ,
					enc[0], enc[1], typeString,
					ROWCOL[enc[3]][enc[4]], ROWCOL[enc[5]][enc[6]], enc[7], ROWCOL[enc[8]][enc[9]], enc[7] );
	}

	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}