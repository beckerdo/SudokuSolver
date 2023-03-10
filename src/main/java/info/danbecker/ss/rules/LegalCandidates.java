package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;

import java.util.ArrayList;
import java.util.List;

import static info.danbecker.ss.Utils.ROWS;
import static info.danbecker.ss.Utils.COLS;
import static info.danbecker.ss.Utils.BOXES;
import static info.danbecker.ss.Board.NOT_FOUND;

/**
 * This validates that candidates are legal based
 * on current entries in the board.
 * Includes:
 *    - remove candidates from rows/cols/boxes with digit entries.
 *    - remove candidates from cell with digit entry.
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class LegalCandidates implements UpdateCandidatesRule {
	public static ArrayList<int[]> DUMMY = new ArrayList<int[]>();
	
	public LegalCandidates() {
	}

	@Override
	public int updateCandidates( Board board, Board solution, Candidates candidates, List<int[]> locations ) {
		int count = 0;
		
		// Remove candidates from  occupied boxes
		System.out.println( ruleName() + " will remove illegal candidates" );
		count += candidates.removeAllOccupiedCandidates();
		
		// Remove candidate digits in row/col/box
		for( int digit = 1; digit <= 9; digit++ ) {
			for( int item = 0; item < ROWS; item++) {
				int index;
				if ( NOT_FOUND != (index = board.rowLocation( item, digit ))) {
					for ( int coli = 0; coli < COLS; coli++) {
						if ( coli != index ) {
						   if (candidates.removeCandidate( item, coli, digit)) count++;
						}
					}						
				}
				if ( NOT_FOUND != (index = board.colLocation( item, digit ))) {
					for ( int rowi = 0; rowi < ROWS; rowi++) {
						if ( rowi != index ) {
						   if (candidates.removeCandidate( rowi, item, digit)) count++;
						}
					}						
				}
				int rowCol[];
				if ( null != (rowCol = board.boxLocation( item, digit ))) {	
					int [][] cells = Board.getBoxRowCols(item);
					for( int boxi = 0; boxi < BOXES; boxi++ ) {
						int rowi = cells[boxi][0];
						int coli = cells[boxi][1];
						if (( rowi != rowCol[ 0 ]) || coli != rowCol[ 1 ] ) {
						   if (candidates.removeCandidate( rowi, coli, digit)) count++;							
						}
					}
					
				}
			}
		}
		return count;
	}

	@Override
	public List<int[]> locations( Board board, Candidates candidates ){
		// Always reports location, but does not look til update phase.		
		return DUMMY;
	}

	@Override
    public String ruleName() {
		return this.getClass().getSimpleName();
	}
}
