package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;

import static info.danbecker.ss.Board.ROWCOL;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import static info.danbecker.ss.Utils.ROWS;
import static info.danbecker.ss.Utils.DIGITS;
import static info.danbecker.ss.Utils.contains;

import static info.danbecker.ss.Board.NOT_FOUND;

/**
 * DoublePairs occurs when a board has
 * a candidate digit twice in a rowCol and that rowCol is repeated on a different rowCol.
 * The candidate digits should be the only one in the cell.
 * If these form a rectangle, the other digits inside or out can be removed.
 * (Notice one rowCol direction will be a Candidate line, the other rowCol direction will be a Double Pair line.)
 * 
 * Info based on clues given at
 * https://www.sudokuoftheday.com/techniques/double-pairs
 * 
 * Note that https://www.thonky.com/sudoku/unique-rectangle says
 * the corners of the double pair must be in two boxes, not four.
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class DoublePairs implements UpdateCandidatesRule {
	
	@Override
	// Location int [] index map
	// digit plus four rowCols A, B, C, D
	// digit at index 0 
	// first pair AB rowCols at indexes 1,2 and 3,4
	// second pair CD rowCols at indexes 5,6 and 7,8
	public int updateCandidates(Board board, Board solution, Candidates candidates, List<int[]> locations) {
		int updates = 0;
		if ( null == locations) return updates;
		if (locations.size() > 0) {
			// Just correct 1 location (which might update multiple candidates.)
			int[] loc = locations.get(0);
			int digit = loc[ 0 ];
			int rowA = loc[ 1 ]; int rowB = loc[ 5 ];
			int colA = loc[ 2 ]; int colB = loc[ 8 ];
			// if rows match, remove row candidates not in these cols
			updates += candidates.removeRowCandidatesNotIn(digit, rowA, new RowCol[] {ROWCOL[rowA][colA], ROWCOL[rowA][colB] });
			updates += candidates.removeRowCandidatesNotIn(digit, rowB, new RowCol[] {ROWCOL[rowB][colA], ROWCOL[rowB][colB] });
			// if cols match, remove col candidates not in these rows.
			updates += candidates.removeColCandidatesNotIn(digit, colA, new RowCol[] {ROWCOL[rowA][colA], ROWCOL[rowB][colB] });
			updates += candidates.removeColCandidatesNotIn(digit, colB, new RowCol[] {ROWCOL[rowA][colA], ROWCOL[rowB][colB] });
			// CandidateLines does similar, but just for one row or col pair, not double pair box
			System.out.println( format("%s removed %d digit %d candidates not in rowCols [%d,%d],[%d,%d]", 
				ruleName(), updates, digit, rowA, colA, rowB, colB ) );
		}
		return updates;
	}

	@Override
	/** 
     * a candidate digit twice in a rowCol and that rowCol is repeated on a different rowCol.
	 * can knock out candidates in other boxes in the same row/col
	 * 
	 * Search for only two same candidates in each box,
  	 * see if row or col is the same, 
     * if row match, see if other candidates exist on same row outside of box
	 * if col match, see if other candidates exist on same col outside of box
	 */
	public List<int[]> locations(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		ArrayList<int[]> locations = new ArrayList<>();
		for (int digi = 1; digi <= DIGITS; digi++) {
			if ( !board.digitCompleted( digi )) {
				// If found, record int[]{ digit, firstRow, firstCol, secondRow, secondCol
				int [] firstRow = new int[ 5 ]; firstRow[0] = NOT_FOUND;
				int [] secondRow = new int[ 5 ]; secondRow[0] = NOT_FOUND;
				for( int rowi = 0; rowi < ROWS && secondRow[0] == NOT_FOUND; rowi++ ) {
				   int[] colLocs = candidates.candidateRowLocations(rowi, digi);
				   // Need a first row, or the second row contains the first row columns
				   if ((firstRow[ 0 ] == NOT_FOUND && 2 == colLocs.length ) || 
					   (firstRow[ 0 ] != NOT_FOUND && contains( colLocs, firstRow[2] ) && contains( colLocs, firstRow[ 4 ]))) {
					   // Box candidates for first row OR a second row with these candidates
				       int boxi = (firstRow[ 0 ] == NOT_FOUND) ?  ROWCOL[rowi][colLocs[0]].box() : ROWCOL[rowi][firstRow[2]].box() ;
				       if ( 2 == candidates.candidateBoxCount(boxi, digi)) {
				          if ( NOT_FOUND == firstRow[0] ) {
				             firstRow[0] = digi; 
				             firstRow[1] = rowi; firstRow[2] = colLocs[0]; 
				             firstRow[3] = rowi; firstRow[4] = colLocs[1]; 
				          } else {
			        		 secondRow[0] = digi; 
			        		 secondRow[1] = rowi; secondRow[2] = firstRow[2];
			        		 secondRow[3] = rowi; secondRow[4] = firstRow[4];
				          }
				       }					   
				   }
				}
				if ((NOT_FOUND != firstRow[0]) && (NOT_FOUND != secondRow[0])) {
         	        // We have a box. Should check for more candidates in this line
					boolean moreCandFirstRow = candidates.candidateRowCount(firstRow[1], digi) > 2;
					boolean moreCandSecondRow = candidates.candidateRowCount(secondRow[3], digi) > 2;
					boolean moreCandFirstCol = candidates.candidateRowCount(firstRow[2], digi) > 2;
					boolean moreCandSecondCol = candidates.candidateRowCount(secondRow[4], digi) > 2;
					int [] loc = new int[] {digi,firstRow[1],firstRow[2],firstRow[3],firstRow[4],
							secondRow[1],secondRow[2],secondRow[3],secondRow[4]};
					if ( moreCandFirstRow || moreCandSecondRow || moreCandFirstCol || moreCandSecondCol ) {
						locations.add( loc );
						// Found two double pair rows
						System.out.println( format( "Rule %s found double pair for %s" ,
							ruleName(), locationToString( loc )));
					} else {
						System.out.println( format( "Rule %s found double pair for %s with no more candidates" ,
								ruleName(), locationToString( loc )));						
					}
				}
			}		
		}
		return locations;
	}
	
	
	// Location int [] index map
	// digit plus four rowCols A, B, C, D
	// digit at index 0 
	// first pair AB rowCols at indexes 1,2 and 3,4
	// second pair CD rowCols at indexes 5,6 and 7,8
	// Because double pairs form a box, there should be a match of
	//    - first pair rows, (A row == B row)
	//    - second pair rows (C row == D row)
	//    - first pair first col and second pair first col (A col == C col)
	//    - first pair second col and second pair second col (B col == D col)
	public static String locationToString( int[] loc) {
		if ( null == loc) return "null";
		if ( 9 != loc.length) return "length of " + loc.length + "!= 9";
		
		String error = "";
		if ( loc[ 1 ] != loc [ 3 ] ) error += " first row mismatch";
		if ( loc[ 5 ] != loc [ 7 ] ) error += " second row mismatch";
		if ( loc[ 2 ] != loc [ 6 ] ) error += " first col mismatch";
		if ( loc[ 4 ] != loc [ 8 ] ) error += " second col mismatch";
		
		return format( "digit %d at [%d,%d],[%d,%d] and [%d,%d],[%d,%d]%s" ,
				 loc[0],loc[1],loc[2],loc[3],loc[4],loc[5],loc[6],loc[7],loc[8], error);		
	}
	
	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}