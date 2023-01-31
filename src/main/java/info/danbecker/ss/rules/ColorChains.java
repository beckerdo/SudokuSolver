package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.Utils;
import info.danbecker.ss.tree.TreeNode;
import info.danbecker.ss.tree.ColorData;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static info.danbecker.ss.Utils.DIGITS;

import static info.danbecker.ss.Utils.Unit;

/**
 * ColorChains also known as candidate coloring
 * occurs when a candidate is colored hypothetically,
 * and that selection forces other selections.
 * 
 * The chain is implemented as a TreeNode. 
 * The data for each node is ColorData (digit, rowCol, color)
 * The children are matching row/col/box locations.
 * 
 * ColorChains is implemented for a single candidate digit.
 * Contrast this to ForcingChains which works on multiple digits. 
 * 
 * For example, a assume you count digit candidates
 * in all units. When there are 2 in a single unit
 * you can mark the 2 candidates red/green, u/n, or 0/1.
 * If adding a child causes
 *    -two colors on the same unit (bad choice)
 *    -mismatching a color of an element in the tree.
 *    -a color appearing twice in one unit (remove that color)
 *    -uncolored candidate can see two colors in a unit (remove them)
 *    
 * To reduce the tree branches, will take the strategy of 
 * creating a pair tree, then adding other non-pair candidates.
 * 
 * Info based on clues given at
 * https://www.sudokuoftheday.com/techniques/forcing-chains
 * https://www.thonky.com/sudoku/simple-coloring
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class ColorChains implements UpdateCandidatesRule {
	public static int NO_COLOR = -1;
	public static int ALL_COLORS = -2;
	
	@Override
	// Location int [] index map
	// digit plus rowCol
	public int updateCandidates(Board board, Board solution, Candidates candidates, List<int[]> locations) {
		int updates = 0;
		if ( null == locations) return updates;
		if (locations.size() > 0) {
			// Just correct 1 location (which might update multiple candidates.
			int[] loc = locations.get(0);
			// Encoding
			// - digit						0
			// - intended/cand rowCol		56
			int digit = loc[ 0 ];
			int cRow = loc[ 5 ];
			int cCol = loc[ 6 ];
			
		    // Validation, if available
		    if ( null != solution ) {
		    	int cellStatus = solution.get(cRow, cCol);
		    	if ( cellStatus == digit ) {
		    		throw new IllegalArgumentException( format("Rule %s would like to remove solution digit %d at loc %s.",
		    			ruleName(), digit, Utils.locationString(cRow,cCol)));		    		
		    	}
		    }

			// Perform only removal according to rowCol orientation
		    String prev = candidates.getCandidatesStringCompact( cRow,cCol );		    
			if ( candidates.removeCandidate( cRow, cCol, digit )) {
			   updates++;
			   String cStr = candidates.getCandidatesStringCompact( cRow,cCol );
			   System.out.println( format( "%s removed digit %d from %s %s, remaining candidates %s", 
				ruleName(), digit, Utils.locationString( cRow, cCol ), prev, cStr ));
			}
		}
		return updates;
	}

	@Override
	/**
	 * Strategy.
	 * -For each digit,
	 *    -count candidates in each unit
	 *    -if there are two 2 counts in a unit
	 *    	- create a chain with a starting location,
	 *      - a chain can connect up to 3 times (unit) to another candidate
	 *      - if color collision remove it.
	 */
	public List<int[]> locations(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		List<int[]> matched = new ArrayList<int[]>();
		for (int digi = 1; digi <= DIGITS; digi++) {
			if ( !board.digitCompleted( digi )) {
				matched.addAll( locations( board, candidates, digi ) );
			}		
		}
		return matched;
	}

	// Helps for testing purposes.
	public List<int[]> locations(Board board, Candidates candidates, int digit ) {
		if (null == candidates)
			throw new NullPointerException( "Candidates is null");
		List<int[]> matched = new ArrayList<int[]>();
		if (!board.digitCompleted(digit)) {
			// Start with pairs for this digit.
			int groupSize = 2;
			List<int[]> digitPairs = candidates.getGroupLocations(digit, groupSize);
			while ( 0 < digitPairs.size() ) {
				int [] pairLoc = digitPairs.get( 0 );
				TreeNode<ColorData> root = new TreeNode<>(new ColorData(digit, pairLoc, 0), 3);
				List<int[]> colorClash = buildColorTree( candidates, root, digit, groupSize );
				matched.addAll( colorClash );
				// root.printTree();
				
				// Now need to check all uncolored candidates if it can see two colors in unit.
				// If two colors in unit are seen, add the uncolored candidate to the list.
				// These locations will have uncolored candidates (color -1) that see other colors.
				List<int[]> allDigitLocs = candidates.getGroupLocations(digit, Candidates.ALL_COUNTS);
				for ( int loci = 0; loci < allDigitLocs.size(); loci++ ) {
					int [] loc = allDigitLocs.get( loci );
					// Skip pair locations
					if ( Board.NOT_FOUND == Utils.indexOf(digitPairs, loc) ) { // needs a deep match
						List<TreeNode<ColorData>> sameUnitNodes = root.findTreeNodes(new AnyUnitMatch( loc ));
						// System.out.println( format( "Loc [%d,%d] same unit %s", loc[0],loc[1], sameUnitNodes));
						if (1 < sameUnitNodes.size() ) {
							for ( int nodei1 = 0; nodei1 < sameUnitNodes.size() - 1; nodei1++) {							
							TreeNode<ColorData> node1 = sameUnitNodes.get( nodei1 );
							for ( int nodei2 = nodei1 + 1; nodei2 < sameUnitNodes.size(); nodei2++) {
								TreeNode<ColorData> node2 = sameUnitNodes.get(nodei2);
								// The uncolored node can see colors in different rows,
								// for example uncolored sees red in row and green not in row, but in box
								if ( node1.data.color != node2.data.color ) {
									int [] unitMatch = Utils.unitMatch( node1.data.rowCol, node2.data.rowCol);
									if ( Board.NOT_FOUND != unitMatch[0]) {
										// Same unit
										 List<Integer> rootCandidates = candidates.getCandidatesList( pairLoc[0], pairLoc[1] );
										 System.out.println( format( "%s digit %s root %s %s, %d candidates %s %s", 
											ruleName(), digit, 
											Utils.locationString(pairLoc), candidates.getCandidatesStringCompact(pairLoc),
											rootCandidates.size(),
											Utils.locationString(loc), candidates.getCandidatesStringCompact(loc)));									
										int [] enc = encode( digit, Unit.values()[unitMatch[0]], unitMatch[1], 
											root.data.rowCol,
											loc, -1, // uncolored node								
											node1.data.rowCol, node1.data.color, 
											node2.data.rowCol, node2.data.color ); 
										matched.add( enc );
										System.out.println( format( "%s, color clash %s", 
											ruleName(), encodingToString( enc ) ));										
									}
								}									
							}
							}

						}					
					}					
				} // for all candidate locations
				
				// Need to remove all tree locations from the pairs list.
			    String cStr = candidates.getCandidatesStringCompact( pairLoc );
				int originalSize = digitPairs.size();				
				digitPairs = removeLocations( digitPairs, root );
				System.out.println( format( "%s, loc %s, digit %d, cand %s, tree size %d, removed %d pairs from search, %d remain.", 
						ruleName(), Utils.locationString(pairLoc), digit, cStr, root.size(), originalSize - digitPairs.size(), digitPairs.size() ));				
			} // while digitPairs has members
		}
		return matched;
	}

	/** 
	 * Create or expand a tree from given node with this ones-based digit and given groupSize.
	 * For example digit 3 pairs has parameters digi == 3, groupSize == 2
	 * 
	 * @param root tree to build on or create
	 * @param digi
	 * @param groupSize
	 * 
	 * @return nodes not added because of color clash
	 */
	public List<int[]> buildColorTree( Candidates candidates, TreeNode<ColorData> parentNode, int digi, int groupSize ) {
		if (null == candidates)
			throw new NullPointerException( "Candidates is null");
		if (null == parentNode )
			throw new NullPointerException( "parent node is null");
		if (null == parentNode.data )
			throw new NullPointerException( "parent node data is null");
		if (digi != parentNode.data.digit )
			throw new IllegalArgumentException( "current node digit " + parentNode.data.digit + " not equal call digit " + digi );
		List<int[]> matched = new ArrayList<int[]>();
		ColorData pData = parentNode.data;
		TreeNode<ColorData> root = parentNode.getRoot();

		// For each unit, find visible locations with this digit, group size.
		boolean [] childAdded = new boolean[] { false, false, false };
		for ( Unit unit : Unit.values() ) {
			TreeNode<ColorData> child = parentNode.getChild( unit.ordinal() );
			if ( null != child ) {
				ColorData cData = child.data;				
				if ( null == cData) {
					// child data is null, let's look for children;
					// System.out.println( format("Rule %s, rowCol [%d,%d], digit %d, unit %s has child node, null child data.",
					// ruleName(), data.rowCol[0], data.rowCol[1], digi, unit.name() ));
					int uniti = switch (unit) {
					case ROW -> pData.rowCol[ 0 ]; 
					case COL -> pData.rowCol[ 1 ]; 
					case BOX -> Board.getBox(pData.rowCol[ 0 ],pData.rowCol[ 1 ]); 
					};

					List<int[]> unitLocs = candidates.candidateUnitGroupLocs( unit, uniti, digi, groupSize);
					for ( int loci = 0; loci < unitLocs.size(); loci++ ) {
						int[] loc = unitLocs.get(loci);
						TreeNode<ColorData> foundNode = root.findTreeNode(new RowColMatch( loc ));
						if ( null == foundNode ) {
							int nextColor = (0==pData.color)?1:0;
							cData = new ColorData( digi, loc, nextColor );

							// unitNode not in tree
							List<TreeNode<ColorData>> sameUnitNodes = root.findTreeNodes(new AnyUnitMatch( loc ));
							boolean colorClashAnyUnit = false;
							for ( int sameUniti = 0; sameUniti < sameUnitNodes.size(); sameUniti++) {
								TreeNode<ColorData> sameUnitNode = sameUnitNodes.get( sameUniti );
								if ( !Utils.locEquals( pData.rowCol, sameUnitNode.data.rowCol )) { // not parent rowCol									
									if ( cData.color == sameUnitNode.data.color) {
										// Induces color clash
										colorClashAnyUnit = true;
										// Add to locations data.
										int [] enc = encode( digi, unit, uniti, root.data.rowCol, 
											cData.rowCol, cData.color,
											pData.rowCol, pData.color, 
											sameUnitNode.data.rowCol, sameUnitNode.data.color); 
										matched.add( enc );
										System.out.println( format( "Rule %s, color clash %s", ruleName(), encodingToString( enc ) ));
									}
								}								
							}							
							if (!colorClashAnyUnit) {
								// Add as child
								parentNode.setChild(cData, unit.ordinal() );
								childAdded[ unit.ordinal() ] = true;										
								// System.out.println( format("Rule %s, node %s added %s candidate at %s, seen by %d tree nodes (no color clash) %s",
								// 	ruleName(), pData.toString(), unit.name(), Utils.locationString(loc), sameUnitNodes.size(), sameUnitNodes ));
								List<int[]> colorClashes = buildColorTree( candidates, parentNode.getChild(unit.ordinal()), digi, groupSize );
								matched.addAll(colorClashes);
							}
						} else {
							// unitNode already in tree
							// System.out.println( format("Rule %s, node %s can match %s candidates at %s (in tree)",
							// ruleName(), data.toString(), unit.name(), Utils.locationString(loc)));							
						}						
					}
				} else {
					System.out.println( format("Rule %s, rowCol [%d,%d], digit %d, unit %s has child node with data %s",
					    ruleName(), pData.rowCol[0], pData.rowCol[1],
						digi, unit.name(), cData.toString() ));					
				}

			} else {
				// By implementation detail, should not have null children, only null child data.
				throw new IllegalArgumentException(format("Rule %s, rowCol [%d,%d], digit %d, unit %s has null child.",
					ruleName(), pData.rowCol[0], pData.rowCol[1],	digi, unit.name() ));
			}			
		} // unit	
		return matched;
	}
	
	// Encode tree as int []
	// encodes as
	// - digit						0
	// - unit ordinal				1
	// - unit number				2
	// - tree root rowCol			34
	// - intended/cand rowCol		56
	// - intended/cand color		7
	// - parent rowCol				89
	// - parent color 				A
	// - sameunit rowCol			BC
	// - sameunit color				D
	public static int [] encode( int digi, Unit unit, int uniti, 
		int[] rootRowCol, int[] cRowCol, int cColor, int[] pRowCol, int pColor, int[] sRowCol, int sColor ) {		
		if ( digi < 1 || digi > 9) 
			throw new IllegalArgumentException( "digit=" + digi);
		// perhaps more validation later
		
		return new int[] {digi, unit.ordinal(), uniti, 
			rootRowCol[0], rootRowCol[1],
			cRowCol[0], cRowCol[1], cColor,
			pRowCol[0], pRowCol[1], pColor,
			sRowCol[0], sRowCol[1], sColor };
	}
	
	public String encodingToString( int[] loc) {
		Unit unit = Unit.values()[loc[1]];
		return format( "digit %d tree origin [%d,%d], candidate [%d,%d]-%d has %s %d clash with [%d,%d]-%d or [%d,%d]-%d" ,
			loc[0], loc[3], loc[4], loc[5],loc[6],loc[7], unit.name(),loc[2], loc[8],loc[9],loc[10], loc[11],loc[12],loc[13]);		
	}
	
	/** Returns a list of locations minus the locations in the given tree.
	 */
	public List<int[]> removeLocations( List<int[]> locs, TreeNode<ColorData> nodes ) {
		List<int[]> modified = new LinkedList<>();

		for ( int[] loc: locs ) {
			TreeNode<ColorData> node = nodes.findTreeNode( new RowColMatch(loc) );
			if ( null == node)
				modified.add( loc );
		}
		
		return modified;
	}
	
	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}

class RowColMatch implements Comparable<ColorData> {
	int [] rowCol;
	
	public RowColMatch( int [] rowCol ) {
		this.rowCol = rowCol;
	}

	@Override
	public int compareTo(ColorData that) {
		if (null == that) return 1;
		if ( this.rowCol[0] < that.rowCol[0]) return -1;
		if ( this.rowCol[0] > that.rowCol[0]) return 1;
		if ( this.rowCol[1] < that.rowCol[1]) return -1;
		if ( this.rowCol[1] > that.rowCol[1]) return 1;
		return 0;
	}
};

class AnyUnitMatch implements Comparable<ColorData> {
	int [] rowCol;
	
	public AnyUnitMatch( int [] rowCol ) {
		this.rowCol = rowCol;
	}

	@Override
	public int compareTo(ColorData that) {
		if (null == that) return 1;
		if ( this.rowCol[0] == that.rowCol[0]) return 0;
		if ( this.rowCol[1] == that.rowCol[1]) return 0;		
		if ( Board.getBox( this.rowCol[0], this.rowCol[1] ) ==
			 Board.getBox( that.rowCol[0], that.rowCol[1] )) return 0;
		return -1;
	}
};

class UnitMatch implements Comparable<ColorData> {
	Utils.Unit unit;
	int [] rowCol;
	
	public UnitMatch( Utils.Unit unit, int [] rowCol ) {
		this.unit = unit;
		this.rowCol = rowCol;
	}

	@Override
	public int compareTo(ColorData that) {
		if (null == that) return 1;
		if (Utils.Unit.ROW == unit) {
			if ( this.rowCol[0] == that.rowCol[0]) return 0;
		} else
		if (Utils.Unit.COL == unit) {
			if ( this.rowCol[1] == that.rowCol[1]) return 0;
		} else
		if (Utils.Unit.BOX == unit) {
			if ( Board.getBox( this.rowCol[0], this.rowCol[1] ) ==
				Board.getBox( that.rowCol[0], that.rowCol[1] )) return 0;
		}
		return -1;
	}
};