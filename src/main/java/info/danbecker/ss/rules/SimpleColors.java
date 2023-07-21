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

import static info.danbecker.ss.Board.*;
import static info.danbecker.ss.Candidates.ALL_COUNTS;
import static info.danbecker.ss.Utils.DIGITS;
import static info.danbecker.ss.Utils.Unit;
import static java.lang.String.format;

/**
 * SimpleColors also known as candidate coloring
 * occurs when a candidate is colored hypothetically,
 * and that selection forces other selections.
 * <p>
 * The chain is implemented as a TreeNode. 
 * The data for each node is DigitData (digit, rowCol, color)
 * The children are matching row/col/box locations.
 * <p>
 * SimpleColors is implemented for a single candidate digit.
 * The color chain startes with a pair and alternating
 * colors are applied to conjugate pairs (another cell
 * with this digit and there are only two candidate cells
 * in the unit.
 * <p>
 * SimpleColors is similar to X-Chain. SimpleColors matches
 * conjugate pairs. X-Chain matches strong links.
 * <ol>
 * <li> An uncolored cell that sees cells of opposite colors (<b>Color Trap</b>):
 * Since the cells with the same color are either all true or all false, one of the two colored cells has to be true, and the uncolored cell can never have the color candidate placed.
 * <li> Two cells with the same color seeing each other (<b>Color Wrap</b>):
 * The cells with that color are either all true or all false. All true is impossible (we would get the same digit twice in the same house), so they must all be false.
 * <p>>
 * This version of coloring is given in
 * https://hodoku.sourceforge.net/en/tech_col.php
 * An earlier version ColorChains also implemented coloring,
 * but it had bugs, was complicated, and this starts fresh.
 * https://www.sudokuoftheday.com/techniques/forcing-chains
 * https://www.thonky.com/sudoku/simple-coloring
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class SimpleColors implements FindUpdateRule {

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

			String prev = candidates.getCompactStr( cLoc );
			if ( candidates.removeCandidate( cLoc, digit )) {
				updates++;
				String cStr = candidates.getCompactStr( cLoc );
				System.out.printf( "%s %s removed digit %d from %s, remaining candidates %s\n",
						this.ruleName(), typeString, digit, cLoc, cStr );
			}
		}
		return updates;
	}

	/**
	 * Strategy.
	 * -For each digit,
	 *    -start with one cell of a conjugate pair (two cells only in row/col/box). Color it 1.
	 *    -leap to other members of conjugate pair (pair in row/col/unit) not in tree. Color it 2.
	 *    -if no more visibles
	 *       - test all remaining digit cells not in tree. Can it see two colors (Color Trap).
	 *       - test all nodes of tree. Any two colors in same unit. Tree is false (Color Wrap).
	 *    - move onto other conjugate pairs. Ignore if they are in prior trees.
	 * @return a list of all locations that can see two colors.
	 */
	@Override
	public List<int[]> find(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		List<int[]> matched = new ArrayList<>();
		for (int digi = 1; digi <= DIGITS; digi++) {
			if ( !board.digitCompleted( digi )) {
				matched.addAll( find( board, candidates, digi ) );
			}
		}
		// System.out.println( format( "%s found %d candidate locations", ruleName(), matched.size()));
		return matched;
	}

	// Helps for testing purposes.
	public List<int[]> find(Board board, Candidates candidates, int digit ) {
		List<int[]> matched = new LinkedList<>();
		List<TreeNode<DigitData>> trees = new LinkedList<>();
		List<RowCol> doNotSearch = new LinkedList<>();
		int[][] unitCounts = candidates.candidateUnitCounts( digit );
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++) {
				RowCol rowCol = ROWCOL[rowi][coli];
				if ( candidates.isCandidate( rowCol, digit ) && !doNotSearch.contains( rowCol )) {
					boolean rowPair = ( 2 == unitCounts[ Unit.ROW.ordinal() ][ rowi ] );
					boolean colPair = ( 2 == unitCounts[ Unit.COL.ordinal() ][ coli ] );
					boolean boxPair = ( 2 == unitCounts[ Unit.BOX.ordinal() ][ ROWCOL[rowi][coli].box()] );
					if ( rowPair || colPair || boxPair) {
						// Found a conjugate pair
						// Ensure this node does not exist in any trees (is a child that has been colored)
						int treeContains = -1;
						for ( int treei = 0; treei < trees.size() && -1 == treeContains; treei++) {
							TreeNode<DigitData> tree = trees.get( treei );
							if ( null != tree.findTreeNode( new DigitData.RowColMatch(new DigitData(0,rowCol,0)) )) {
								treeContains = treei;
							}
						}
						if ( -1 == treeContains ) {
							// System.out.println(format("Digit %d at %s has conjugate pair %b/%b/%b",
							//		digit, rowCol, rowPair, colPair, boxPair));
							TreeNode<DigitData> tree = new TreeNode<>(new DigitData(digit, rowCol, 0), 3);
							List<int[]> treeClashes = buildColorTree(candidates, tree, digit, ALL_COUNTS);
							// System.out.println(format("Digit %d tree at %s has %d nodes and %d clashes",
							//		digit, rowCol, tree.size(), treeClashes.size()));
							// Add clashes to matched locations
							matched.addAll( treeClashes );
							// These clashing rowCol locations, should not be the source of a new tree.
							doNotSearch( doNotSearch, treeClashes );

							// Color Trap. Check for non tree candidates that can see two colors of this tree.
							if ( 0 == treeClashes.size() ) {
								List<int[]> seesTwo = outsideSeesTwoDifferentType0Trap(candidates, tree, digit);
								matched.addAll(seesTwo);
								doNotSearch(doNotSearch, seesTwo);
							}

							// tree.printTree();
							trees.add(tree);
						}
					}
				}
			}
		}
		return matched;
	}

	/** Maintain a list of Color Trap and Color Wrap find locations so that
	 * digit searches do not attempt to make a color tree in these locations.
	 * @param doNotSearch
	 * @param encProblems
	 */
	public static void doNotSearch( List<RowCol> doNotSearch, List<int[]> encProblems ) {
		for ( int enci = 0; enci < encProblems.size(); enci++) {
			int[] enc = encProblems.get( enci );
			RowCol cLoc = ROWCOL[enc[4]][enc[5]];
			if ( !doNotSearch.contains( cLoc )) {
				doNotSearch.add( cLoc );
			}
		}
	}

	/**
     * Create or expand a tree from given node with this ones-based digit and given groupSize.
         * For example digit 3 pairs has parameters digi == 3, groupSize == 2
         *
         * @param digi
         * @param groupSize
         *
         * @return nodes not added because of color clash
         */
	public List<int[]> buildColorTree(Candidates candidates, TreeNode<DigitData> parentNode, int digi, int groupSize ) {
		if (null == parentNode )
			throw new NullPointerException( "parent node is null");
		if (null == parentNode.data )
			throw new NullPointerException( "parent node data is null");
		if (digi != parentNode.data.digit )
			throw new IllegalArgumentException( "current node digit " + parentNode.data.digit + " not equal call digit " + digi );
		List<int[]> matched = new ArrayList<>();
		DigitData pData = parentNode.data;
		TreeNode<DigitData> root = parentNode.getRoot();

		// For each unit, find visible locations with this digit, group size.
		boolean [] childAdded = new boolean[] { false, false, false };
		for ( Unit unit : Unit.values() ) {
			TreeNode<DigitData> child = parentNode.getChild( unit.ordinal() );
			if ( null != child ) {
				DigitData cData = child.data;
				if ( null == cData) {
					// child data is null, let's look for children;
					// System.out.println( format("Rule %s, rowCol [%d,%d], digit %d, unit %s has child node, null child data.",
					// ruleName(), data.rowCol[0], data.rowCol[1], digi, unit.name() ));
					int uniti = switch (unit) {
						case ROW -> pData.rowCol.row();
						case COL -> pData.rowCol.col();
						case BOX -> pData.rowCol.box();
					};

					List<RowCol> unitLocs = candidates.candidateUnitGroupLocs(unit, uniti, digi, groupSize);
					if (2 == unitLocs.size()) {
						// Only add conjugate pairs to color tree
						for (int loci = 0; loci < unitLocs.size(); loci++) {
							RowCol loc = unitLocs.get(loci);
							int nextColor = (0 == pData.color) ? 1 : 0;
							cData = new DigitData(digi, loc, nextColor);
							TreeNode<DigitData> foundNode = root.findTreeNode(new DigitData.RowColMatch(cData));
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
									List<int[]> colorClashes = buildColorTree(candidates,
											parentNode.getChild(unit.ordinal()), digi, groupSize);
									matched.addAll(colorClashes);
								} else {
									matched.addAll(seesSameColor);
								}
							}
						}
					}
				} else {
					System.out.printf( "Rule %s, rowCol %s, digit %d, unit %s has child node with data %s\n",
							ruleName(), pData.rowCol, digi, unit.name(), cData );
				}
			} else {
				// By implementation detail, should not have null children, only null child data.
				throw new IllegalArgumentException(format("Rule %s, rowCol %s, digit %d, unit %s has null child.",
						ruleName(), pData.rowCol, digi, unit.name() ));
			}
		} // unit
		return matched;
	}

	/**
	 * Check for non tree candidates that can see two colors of this tree.
	 * Color Trap (type 0): An uncolored cell (outside tree) that sees cells of opposite colors.
	 *
	 * @param candidates
	 * @param tree
	 * @param digit
	 * @return int [] of candidates to remove
	 */
	public static List<int[]> outsideSeesTwoDifferentType0Trap(Candidates candidates, TreeNode<DigitData> tree, int digit ) {
		List<int[]> seesTwo = new LinkedList<>();
		List<RowCol> locs = candidates.getGroupLocations(digit, ALL_COUNTS);
		for ( int loci = 0; loci < locs.size(); loci++) {
			RowCol rowCol = locs.get(loci);
			DigitData cData = new DigitData(digit,rowCol,-1);
			if ( null == tree.findTreeNode( new DigitData.RowColMatch(cData))) {
				// RowCol is not in given tree
				List<TreeNode<DigitData>> sameUnitNodes = tree.findTreeNodes(new DigitData.AnyUnitMatch(cData));
				// Check is all nodes have same color.
				boolean colorClashAnyUnit = false;
				if (1 < sameUnitNodes.size() ) {
					DigitData firstDigitData = null;
					for (int sameUniti = 0; sameUniti < sameUnitNodes.size(); sameUniti++) {
						TreeNode<DigitData> sameUnitNode = sameUnitNodes.get(sameUniti);
						if ( null == firstDigitData) firstDigitData = sameUnitNode.data;
						if ( firstDigitData.color != sameUnitNode.data.color ) {
							// System.out.println( format( "Color Trap digit %d at %s, can see %s",
							// 		digi, rowCol, DigitData.toString( sameUnitNodes )));
							// Add enc data.
							int[] enc = encode(digit, 0, tree.data.rowCol,cData.rowCol,
									firstDigitData.color, firstDigitData.rowCol,
									sameUnitNode.data.color, sameUnitNode.data.rowCol );
							// Manually search for same int[] enc
							if ( NOT_FOUND != Utils.findFirst( seesTwo, enc) )
								System.out.printf( "SeesTwo contains digit %d at %s\n", digit, cData.rowCol);
							else
								seesTwo.add(enc);
						}
					}
				}
			}
		}
		return seesTwo;
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
	public static List<int[]> childSeesSameType1Wrap(Candidates candidates, TreeNode<DigitData> root, DigitData proposedChild ) {
		List<int[]> seesSame = new LinkedList<>();
		List<TreeNode<DigitData>> sameUnitNodes = root.findTreeNodes(new DigitData.AnyUnitMatch(proposedChild));
		for ( int nodei = 0; nodei < sameUnitNodes.size(); nodei++) {
			TreeNode<DigitData> sameUnit = sameUnitNodes.get(nodei);
			if ( proposedChild.color == sameUnit.data.color ) {
				// System.out.println(format("Rule %s color wrap. Proposed child %s sees same color tree node %s",
				// ruleName(), proposedChild, sameUnit.data ));
				// Just repeat sameUnit for second color and rowCol
				int[] enc = encode( proposedChild.digit, 1, root.data.rowCol, proposedChild.rowCol,
						sameUnit.data.color, sameUnit.data.rowCol,
						sameUnit.data.color, sameUnit.data.rowCol );
				seesSame.add( enc );
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