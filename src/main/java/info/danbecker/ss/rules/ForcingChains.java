package info.danbecker.ss.rules;

import info.danbecker.ss.Board;

import info.danbecker.ss.Candidates;
import info.danbecker.ss.Candidates.Action;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils.Unit;
import info.danbecker.ss.tree.ChangeData;
import info.danbecker.ss.tree.HypoTreeData;
import info.danbecker.ss.tree.TreeNode;

import static info.danbecker.ss.Board.ROWCOL;
import static java.lang.String.format;

import java.util.LinkedList;
import java.util.List;

import static info.danbecker.ss.Candidates.ALL_DIGITS;

/**
 * ForcingChain is also known as a HypotheticalChain.
 *  
 * Unlike a ColorChain which looks at one digit,
 * the HypChain investigate hypothetical changes
 * to a Candidate tree, for instance when one
 * of two candidates in a pair is selected.
 * 
 * If the alternatives lead to a cell with the
 * same digit, that digit can be made a board/candidate entry.
 * 
 * SOTD has a bad example at
 * https://www.sudokuoftheday.com/techniques/forcing-chains
 * Thonky has ColoringChains examples at
 * https://www.thonky.com/sudoku/simple-coloring
 * 
 * Use ForcingChains when there are many candidate pairs. 
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class ForcingChains implements UpdateCandidatesRule {
	
	@Override
	// enc int []
	// 0,1 = origin rowCol,
	// 2 = digit
	// 3,4 = dest rowCol
	// 5,6 = tree depths
	public int updateCandidates(Board board, Board solution, Candidates candidates, List<int[]> encs) {
		int updates = 0;
		if ( null == encs) return updates;
		// Select encoding with the smallest tree depths.
		int minEnci = -1;
		int minDepth  = Integer.MAX_VALUE;		
		for ( int enci = 0; enci < encs.size(); enci++ ) {
			   int[] enc = encs.get( enci );
			   int td1 = enc[5];
			   int td2 = enc[6];
			   
			   if ( -1 == minEnci || minDepth > td1 + td2 ) {
				   minEnci = enci;
				   minDepth = td1 + td2;
			   }
		}
		// Update lowest depth encoding position
		if ( -1 != minEnci ) {
			   int[] enc = encs.get( minEnci );
			   int digit = enc[2];
			   int [] loc = new int [] { enc[3], enc[4] };
			   RowCol rowCol = ROWCOL[loc[0]][loc[1]];
			   String cStr = candidates.getCandidatesStringCompact( rowCol );
			   
			   // Validation, if available
			   if ( null != solution ) {
				   int cellStatus = solution.get(rowCol);
				   if ( cellStatus != digit ) {
			    	throw new IllegalArgumentException( format("Rule %s would like set digit %d (solution=%d) at loc %s.",
			    		ruleName(), digit, cellStatus, rowCol));
				   }
			   }
			    
			   board.set( rowCol, digit );
			   candidates.setOccupied( rowCol, digit );
			   updates += 1; 
	     	   System.out.println( format("%s occupied digit %d at rowCol %s, previous candidates %s", 
	     			 this.ruleName(), digit, rowCol, cStr));
		}
		return updates;
	}

	@Override
	/** 
     * Return any location where either digit leads to same output.
     * The starting digit location is given to help with testability. 
	 */
	public List<int[]> locations(Board board, Candidates candidates) {
		List<int[]> matched = new LinkedList<>();
		
		List<RowCol> digitPairs = candidates.getGroupLocations(Candidates.ALL_DIGITS, 2);
		// if (digitPairs.size() > 5) { // artificial threshhold, consider removing.	
		while ( 0 < digitPairs.size()) {
			// System.out.println( format("Digit pair count %d %s", digitPairs.size(), Utils.locationsString(digitPairs)));
		RowCol loc = digitPairs.get(0);
		boolean pairAdded = false;
		// int [] loc = new int [] { 0, 4 };
		// boolean verbose = (0 == loc[0] && 0 == loc[1]); 
		boolean verbose = false; // help debug location
		List<Integer> locCandidates = candidates.getCandidatesList(loc);
		if (2 == locCandidates.size()) {	
			if ( verbose ) {
				System.out.println( format( "%s testing rowCol %s, candidates %s",
				ruleName(),
			 	loc, candidates.getCandidatesStringCompact(loc)));
			}
			int firstDigit = locCandidates.get(0);
			TreeNode<HypoTreeData> firstChoice = new TreeNode<>( new HypoTreeData( firstDigit, loc, candidates ), 3 );
			fillNode( firstChoice );
			if ( verbose )
				firstChoice.printTree();

			int secondDigit = locCandidates.get(1);
			TreeNode<HypoTreeData> secondChoice = new TreeNode<>( new HypoTreeData( secondDigit, loc, candidates ), 3 );
			fillNode( secondChoice );
			if ( verbose )
				secondChoice.printTree();

			TreeNode<HypoTreeData> shorterTree;
			TreeNode<HypoTreeData> longerTree;
		
			if (firstChoice.size() < secondChoice.size()) {
				shorterTree = firstChoice;
				longerTree = secondChoice;
			} else {
				shorterTree = secondChoice;
				longerTree = firstChoice;				
			}
			// Look for nodes from shorter tree in larger one.
			for ( TreeNode<HypoTreeData> node : shorterTree ) {
				if (null != node.data) {
					TreeNode<HypoTreeData> foundNode = longerTree.findTreeNode(new RowColMatch(node.data.rowCol));
					List<TreeNode<HypoTreeData>> foundNodes = longerTree.findTreeNodes(new RowColMatch(node.data.rowCol));
					if (1 < foundNodes.size()) {
						System.out.println(format("Warning found %d nodes matching loc %s", foundNodes.size(), node.data.rowCol));
					}
					// If both trees have the location
					if (null != foundNode) {
						// Test if the two nodes produce the same outcome.
						if (node.data.digit == foundNode.data.digit) {
							System.out.println(format("%s rowCol %s both candidates %s lead to digit %d at loc %s, tree depths=%d,%d",
								ruleName(),
							 	loc, candidates.getCandidatesStringCompact(loc), foundNode.data.digit,
							 	foundNode.data.rowCol,node.getLevel(),foundNode.getLevel()));
							matched.add( encode( loc, foundNode.data.digit, foundNode.data.rowCol, node.getLevel(),foundNode.getLevel() ) );
							System.out.println( "Candidates=\n" + candidates.toStringBoxed());
							
							// Need to remove all tree locations from the pairs list.
							// int originalSize = digitPairs.size();
							digitPairs = removeLocations( digitPairs, shorterTree );
							digitPairs = removeLocations( digitPairs, longerTree );
							// System.out.println( format( "Removed %d pairs from search, %d remain.", originalSize - digitPairs.size(), digitPairs.size() ));
							pairAdded = true;
						}
					}
				}
			}	
			// If the current location was not a match, remove it and continue.
			if ( !pairAdded ) {
				// int originalSize = digitPairs.size();
				digitPairs.remove( 0 );				
				// System.out.println( format( "Removed %d pairs from search, %d remain.", originalSize - digitPairs.size(), digitPairs.size() ));
			}
		} // assure we are operating on a pair
		} // while more digit pairs
		// } // digit pair count met
		
		return matched;
	}

	// What to return, count of changes?
	public void fillNode( TreeNode<HypoTreeData> node ) {
		HypoTreeData nodeData = node.data;
		if ( null != nodeData ) {
			// Bring node info into scope.
			Candidates candidates = nodeData.candidates;
			if ( 0 == candidates.candidateCount()) return;
			int digit = nodeData.digit;
			RowCol rowCol = nodeData.rowCol;
			List<ChangeData> actions = nodeData.actions;
			// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
			if ( candidates.isCandidate(rowCol, digit)) {
				candidates.setOccupied(rowCol, digit);
				actions.add( new ChangeData( digit, rowCol, Action.OCCUPY, 1));
				int updates = candidates.removeCandidatesSameUnits(rowCol, digit);
				actions.add( new ChangeData( digit, rowCol, Action.REMOVE, updates));
				
				// System.out.println( format("%sDigit %d at [%d,%d], %d remaining",
				// 	Utils.createIndent(node.getLevel()), digit, rowCol[0], rowCol[1], candidates.candidateCount()));

				TreeNode<HypoTreeData> root = node.getRoot();
				for( Unit unit : Unit.values() ) {
					List<RowCol> sameUnit = candidates.getGroupSameUnitLocations( unit, ALL_DIGITS, 1, rowCol );
					for ( int uniti = 0; uniti < sameUnit.size(); uniti++ ) {
						RowCol candLoc = sameUnit.get(uniti);
						TreeNode<HypoTreeData> foundNode = root.findTreeNode(new RowColMatch( candLoc ));
						if ( null == foundNode ) {
							// Location does not exist in tree
							// System.out.println( format("%sDigit %d at [%d,%d] has new same %s single at [%d,%d] with digit %s",
							// 	Utils.createIndent(node.getLevel()+1), digit,rowCol[0],rowCol[1], unit.name(), 
							// 	candLoc[0],candLoc[1], candidates.getCandidatesStringCompact(candLoc[0],candLoc[1]) ));
							int candDigit = candidates.getCandidateDigit(candLoc);
							// Child node
							HypoTreeData childData = new HypoTreeData(candDigit,candLoc,candidates);
							TreeNode<HypoTreeData> child = node.setChild(childData, unit.ordinal());
							fillNode( child );
						// } else {
							// Location exists in tree.
							// No need to set children of existing node. Might lead to endless recursion.
						}						
					}
				}
				// System.out.println( "Candidates=\n" + candidates.toStringBoxed());				
			}			
		}
	}
	
	/** Returns a list of locations minus the locations in the given tree.
	 */
	public List<RowCol> removeLocations( List<RowCol> locs, TreeNode<HypoTreeData> nodes ) {
		List<RowCol> modified = new LinkedList<>();

		for ( RowCol loc: locs ) {
			TreeNode<HypoTreeData> node = nodes.findTreeNode( new RowColMatch(loc) );
			if ( null == node)
				modified.add( loc );
		}
		
		return modified;
	}
		
	// Encode int [] index map
	// - origin rowCol
	// - destination digit
	// - destination rowCol
	// - tree depths, first and second tree (might not equal candidate order)
	public static int [] encode( RowCol origRowCol, int destDigit, RowCol destRowCol, int td1, int td2 ){
		int [] enc = new int[ 7 ];
		enc[0] = origRowCol.row();
		enc[1] = origRowCol.col();
		enc[2] = destDigit;
		enc[3] = destRowCol.row();
		enc[4] = destRowCol.col();
		enc[5] = td1;
		enc[6] = td2;
		return enc;
	}
	
	public static String encodingToString( int[] enc) {
		return format("Orig loc [%d,%d], dest loc [%d,%d], digit=%d, tree depths [%d,%d]",
				enc[0], enc[1], enc[3], enc[4], enc[2], enc[5], enc[6]);
	}
	
	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}

	
	static class RowColMatch implements Comparable<HypoTreeData> {
		RowCol rowCol;
		
		public RowColMatch( RowCol rowCol ) {
			this.rowCol = rowCol;
		}

		@Override
		public int compareTo(HypoTreeData that) {
			if (null == that) return 1;
			return this.rowCol.compareTo(that.rowCol);
		}
	}
}