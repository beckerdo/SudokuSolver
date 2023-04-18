package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;
import info.danbecker.ss.tree.ColorData;
import info.danbecker.ss.tree.PairData;
import info.danbecker.ss.tree.TreeNode;

import java.util.*;

import static info.danbecker.ss.Board.*;
import static info.danbecker.ss.Candidates.*;
import static info.danbecker.ss.Utils.DIGITS;
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
	// digit plus rowCol
	public int update(Board board, Board solution, Candidates candidates, List<int[]> encs) {
		int updates = 0;
		if ( null == encs) return updates;
		for ( int enci = 0; enci < encs.size(); enci++ ) {
			int[] enc = encs.get(enci);
			int digit = enc[ 0 ];
			int type = enc[ 1 ];
			String typeString = switch( type ) {
				case 0 -> "color trap";
				case 1 -> "color wrap";
				default -> "type " + type;
			};
			RowCol cLoc = ROWCOL[enc[4]][enc[5]];

			// Validation, if available
			if ( null != solution ) {
				int cellStatus = solution.get(cLoc);
				if ( cellStatus == digit ) {
					throw new IllegalArgumentException( format("Rule %s would like to remove solution digit %d at loc %s.",
							ruleName(), digit, cLoc));
				}
			}

			String prev = candidates.getCandidatesStringCompact( cLoc );
			if ( candidates.removeCandidate( cLoc, digit )) {
				updates++;
				String cStr = candidates.getCandidatesStringCompact( cLoc );
				System.out.println( format( "%s %s removed digit %d from %s, remaining candidates %s",
						ruleName(), typeString, digit, cLoc, cStr ));
			}
		}
		return updates;
	}

	@Override
	/**
	 * Strategy.
	 * -For each pair of digits on the board,
	 *    -find the longest chain for each location.
	 *    -test for either digit, that can see two color PairNodes in the chain.
	 * @return a list of all locations that can see two colors.
	 */
	public List<int[]> find(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		List<int[]> locs = new ArrayList<>();
		List<TreeNode<PairData>> trees = new LinkedList<>();

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
			List<RowCol> pairLocs = candidates.candidateComboAllLocations( zeroBasedDigits, Candidates.FULL_COMBI_MATCH);
			if ( 2 < pairLocs.size()) {
				System.out.println("Digits=" + pair.toString() + ", locs=" + pairLocs.toString());
				// Make a long linear chain from the locations.
				// Count each location's visibility to determine an endpoint.
				int mini = pickMinSights( candidates, zeroBasedDigits, pairLocs);
				if ( -1 != mini ) {

					RowCol treeLoc = pairLocs.get( mini );
					System.out.println( format( "   tree of digits %s starting at %s",
  					pair, pairLocs.get(mini) ));
					TreeNode<PairData> tree = new TreeNode<>(new PairData(pair, treeLoc, 0), 3);
					List<int[]> treeClashes = buildPairTree(candidates, tree, pair, pairLocs );
					System.out.println(format("Digit pair %s tree at %s has %d nodes and %d clashes",
						pair.toString(), treeLoc, tree.size(), treeClashes.size()));
					tree.printTree();
					// Add clashes to matched locations
					locs.addAll( treeClashes );
				}
			}
		}
		return locs;
	}

	/**
	 * Looks for a good endpoint to a pair chain (which sees only one other pair).
	 * Returns the pairLocs index, or -1 if one does not match (a loop).
	 * @param candidates
	 * @param zeroBasedDigits
	 * @param pairLocs
	 * @return index in pairLocs which sees one other digits pair, -1 otherwise
	 */
	public int pickMinSights( Candidates candidates, int[] zeroBasedDigits, List<RowCol> pairLocs  ) {
		List<Integer> sees = new LinkedList<>();
		for ( int loci = 0; loci < pairLocs.size(); loci++) {
			RowCol loc = pairLocs.get( loci );
			int locSightCount =
					candidates.candidateComboRowLocations( loc.row(), zeroBasedDigits, NAKED, FULL_COMBI_MATCH ).size() - 1; // subtract self loc
			locSightCount +=
					candidates.candidateComboColLocations( loc.col(), zeroBasedDigits, NAKED, FULL_COMBI_MATCH ).size() - 1; // subtract self loc
			// do not count box if unit was counted in same row or same col (
			List<RowCol> boxLocs = candidates.candidateComboBoxLocations( loc.box(), zeroBasedDigits, NAKED, FULL_COMBI_MATCH );
			for ( RowCol boxLoc : boxLocs) {
				if ( !loc.equals( boxLoc ) && loc.row() != boxLoc.row() && loc.col() != boxLoc.col())  {
					locSightCount++;
				}
			}
			sees.add( locSightCount );
			// System.out.println("   loc=" + pairLocs.get( loci ) + ", sees=" + RowCol.toString( seeLocs ));
			// System.out.println("   loc=" + pairLocs.get( loci ) + ", sees=" + locSightCount );
		}
		int min = Integer.MAX_VALUE;
		int mini = -1;
		// Find mininum sees count and index of this count.
		for ( int seei = 0; seei < sees.size(); seei++) {
			int units = sees.get( seei );
			if ( units < min ) {
				min = units;
				mini = seei;
			}

		}
		if ( -1 != mini ) {
			if ( 1 == min) {
				// System.out.println("   tree end min loc=" + pairLocs.get(mini) + ", sees=" + sees.get(mini));
				return mini;
			} else {
				// System.out.println("   no end min loc=" + pairLocs.get(mini) + ", sees=" + sees.get(mini));
				return -1;
			}
		}
		return -1;
	}

	/**
     * Create or expand a tree from given node with these ones-based digits in these locations..
     * For example digit 4,5 in locations [11][16][20][50]
	 * The tree will start at a given location.
	 * Tree building should end when a node has multiple children because that will be shorter
	 * than a single child chain.
     */
	public List<int[]> buildPairTree( Candidates candidates, TreeNode<PairData> parentNode, List<Integer> digits, List<RowCol> pairLocs ) {
		if (null == parentNode )
			throw new NullPointerException( "parent node is null");
		if (null == parentNode.data )
			throw new NullPointerException( "parent node data is null");
		List<int[]> matched = new ArrayList<>();
		PairData pData = parentNode.data;
		TreeNode<PairData> root = parentNode.getRoot();

		// For each unit, find visible locations with this digit, group size.
		boolean [] childAdded = new boolean[] { false, false, false };
		for ( Unit unit : Unit.values() ) {
			TreeNode<PairData> child = parentNode.getChild( unit.ordinal() );
			if ( null != child ) {
				PairData cData = child.data;
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
							int nextColor = (0 == pData.color) ? 1 : 0;
							cData = new PairData(digits, loc, nextColor);
							TreeNode<PairData> foundNode = root.findTreeNode( new PairData.RowColMatch(cData));
							if (null == foundNode) {
								// unitNode not in tree
								// System.out.println( format("   Digit %d, parent %s, loc %s not in tree", digi, pData.rowCol, loc));
								// A child should not see another node in the tree that is the same color
								List<int[]> seesSameColor = childSeesSame( candidates, root, pData );
								if ( 0 == seesSameColor.size() ) {
									// Add as child. Recurse to child node.
									parentNode.setChild(pData, unit.ordinal());
									childAdded[unit.ordinal()] = true;
									// System.out.println( format("Rule %s, node %s added %s candidate at %s, seen by %d tree nodes (no color clash) %s",
									// 	ruleName(), pData.toString(), unit.name(), Utils.locationString(loc), sameUnitNodes.size(), sameUnitNodes ));
									// Depth first recursion
									// List<int[]> colorClashes = buildPairTree(candidates,parentNode.getChild(unit.ordinal()), digits, 2);
									// matched.addAll(colorClashes);
								} else {
									matched.addAll(seesSameColor);
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
	 * Check for non tree candidates that can see two colors of this tree.
	 *
	 * @param candidates
	 * @param tree
	 * @param digits
	 * @return int [] of candidates to remove
	 */
	public List<int[]> seesTwoDifferent(Candidates candidates, TreeNode<PairData> tree, List<Integer> digits ) {
		List<int[]> seesTwo = new LinkedList<>();
		for (int digi = 0; digi < digits.size(); digi++) {
			int digit = digits.get(digi);
			List<RowCol> locs = candidates.getGroupLocations(digit, ALL_COUNTS);
			for (int loci = 0; loci < locs.size(); loci++) {
				RowCol rowCol = locs.get(loci);
				PairData pData = new PairData( digits, rowCol, -1);
				if (null == tree.findTreeNode(new PairData.RowColMatch(pData))) {
					// RowCol is not in given tree
					List<TreeNode<PairData>> sameUnitNodes = tree.findTreeNodes(new PairData.AnyUnitMatch(pData));
					// Check is all nodes have same color.
					boolean colorClashAnyUnit = false;
					if (1 < sameUnitNodes.size()) {
						PairData firstColorData = null;
						for (int sameUniti = 0; sameUniti < sameUnitNodes.size(); sameUniti++) {
							TreeNode<PairData> sameUnitNode = sameUnitNodes.get(sameUniti);
							if (null == firstColorData) firstColorData = sameUnitNode.data;
							if (firstColorData.color != sameUnitNode.data.color) {
								// System.out.println( format( "Color Trap digit %d at %s, can see %s",
								// 		digi, rowCol, ColorData.toString( sameUnitNodes )));
								// Add enc data.
								int[] enc = encode(digi, 0, tree.data.rowCol, pData.rowCol,
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

	/**
	 * Color Trap
	 * Check for new child being able to see the same color of this tree.
	 *
	 * @param candidates
	 * @param root
	 * @param proposedChild
	 * @return int [] of candidates to remove
	 */
	public List<int[]> childSeesSame(Candidates candidates, TreeNode<PairData> root, PairData proposedChild ) {
		List<int[]> seesSame = new LinkedList<>();
		List<TreeNode<PairData>> sameUnitNodes = root.findTreeNodes(new PairData.AnyUnitMatch(proposedChild));
		for ( int nodei = 0; nodei < sameUnitNodes.size(); nodei++) {
			TreeNode<PairData> sameUnit = sameUnitNodes.get(nodei);
			if ( proposedChild.color == sameUnit.data.color ) {
				// System.out.println(format("Rule %s color wrap. Proposed child %s sees same color tree node %s",
				// ruleName(), proposedChild, sameUnit.data ));
				// Just repeat sameUnit for second color and rowCol
//				int[] enc = encode( proposedChild.digit, 1, root.data.rowCol, proposedChild.rowCol,
//						sameUnit.data.color, sameUnit.data.rowCol,
//						sameUnit.data.color, sameUnit.data.rowCol );
//				seesSame.add( enc );
			}
		}
		return seesSame;
	}

	// Encode tree as int[]
	// - 0 digit
	// - 1 type						0=ColorTrap,1=ColorWrap
	// - 23 tree root rowCol
	// - 45 cand rowCol
	// - 6 first color
	// - 78 first RowCol
	// - 9 second color
	// - AB second RowCOl
	public static int [] encode( int digi, int type, RowCol root, RowCol cand,
								 int firstColor, RowCol first, int secondColor, RowCol second ) {
		if ( digi < 1 || digi > 9)
			throw new IllegalArgumentException( "digit=" + digi);
		// perhaps more validation later
		return new int[] {digi, type,
				root.row(), root.col(),
				cand.row(), cand.col(),
				firstColor, first.row(), first.col(),
				secondColor, second.row(), second.col()};
	}

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