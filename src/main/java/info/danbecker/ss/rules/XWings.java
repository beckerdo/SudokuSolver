package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;

import static info.danbecker.ss.Board.ROWCOL;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static info.danbecker.ss.Utils.ROWS;
import static info.danbecker.ss.Utils.COLS;
import static info.danbecker.ss.Utils.DIGITS;

/**
 * XWings occurs when a board has a candidate two lines, 
 * each having the same two positions for a number.
 * <p>
 * Info based on clues given at
 * https://www.sudokuoftheday.com/techniques/x-wings
 * <p>
 * Note that https://www.thonky.com/sudoku/unique-rectangle says
 * the corners of the double pair must be in two boxes, not four.
 * <p>
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class XWings implements FindUpdateRule {
	@Override
	// Location int [] index map
	// digit plus four rowCols A, B, C, D
	// digit at index 0 
	// first pair AB rowCols at indexes 1,2 and 3,4
	// second pair CD rowCols at indexes 5,6 and 7,8
	public int update(Board board, Board solution, Candidates candidates, List<int[]> encs) {
		int updates = 0;
		if ( null == encs) return updates;
		if (encs.size() > 0) {
			// Just correct 1 location (which might update multiple candidates.
			int[] loc = encs.get(0);
			// Encoding
			// int[] {digi,rowCol
			//  firstRow[0][0],firstRow[0][1],firstRow[1][0],firstRow[1][1],   // 2345
			// 	secondRow[0][0],secondRow[0][1],secondRow[1][0],secondRow[1][1]}; // 6789
					
			int digit = loc[ 0 ];
			int rowCol = loc[ 1 ];
			int rowA, rowB, colA, colB;
			if ( 0 == rowCol ) {
				rowA = loc[ 2 ]; rowB = loc[ 8 ];
				colA = loc[ 3 ]; colB = loc[ 9 ];				
			} else {
				rowA = loc[ 2 ]; rowB = loc[ 8 ];
				colA = loc[ 3 ]; colB = loc[ 9 ];				
			}
			
			// Perform only removal according to rowCol orientation
			if ( rowCol == 0 ) {
				// if rows match, remove col candidates not in these rows
				updates += candidates.removeColCandidatesNotIn(digit, colA, new RowCol[] {ROWCOL[rowA][colA], ROWCOL[rowB][colA] });
				updates += candidates.removeColCandidatesNotIn(digit, colB, new RowCol[] {ROWCOL[rowA][colB], ROWCOL[rowB][colB] });
				System.out.println( format("%s removed %d digit %d %s candidates not in rowCols [%d,%d] [%d,%d]", 
					ruleName(), updates, digit,"row",rowA, colA, rowB, colB ) );
			} else {
				// if cols match, remove row candidates not in these cols.
				updates += candidates.removeRowCandidatesNotIn(digit, rowA, new RowCol[] {ROWCOL[rowA][colA], ROWCOL[rowA][colB] });
				updates += candidates.removeRowCandidatesNotIn(digit, rowB, new RowCol[] {ROWCOL[rowB][colA], ROWCOL[rowB][colB] });
				System.out.println( format("%s removed %d digit %d %s candidates not in rowCols [%d,%d] [%d,%d]", 
						ruleName(), updates, digit,"col",rowA, colA, rowB, colB ) );
			}
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
	public List<int[]> find(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		List<int[]> matched = new ArrayList<>();
		for (int digi = 1; digi <= DIGITS; digi++) {
			if ( !board.digitCompleted( digi )) {
				// Go through all rows and pick out double candidate rows for this digit.
				// Be careful, there can be multiple matches such as
				// [1,2][1,6]/[7,2][7,6] and [3,3][3,5]/[6,3][6,5]
				LinkedList<int[][]> doubleRows = new LinkedList<>();
				for ( int rowi = 0; rowi < ROWS; rowi++) {
					int[] colLocs = candidates.candidateRowLocations(rowi,digi);
					if ( 2 == colLocs.length) {
					   doubleRows.add( new int[][]{ new int[]{rowi,colLocs[0]}, new int[]{rowi,colLocs[1]} });
					}
				}
				while ( doubleRows.size() > 0) {
					int[][] firstRow = doubleRows.removeFirst();
					int[][] secondRow;
					if ( null != (secondRow = matchingRow( firstRow, doubleRows ))) {
						// Check for additional matches
						int[][] thirdRow;
						if ( null != (thirdRow = matchingRow( firstRow, doubleRows ))) {
							System.out.println( format( "Warning: XWings found third row match for digit %d, row %d at rowcols [%d,%d],[%d,%d}",
							   digi, thirdRow[0][0], thirdRow[0][1], thirdRow[1][0], thirdRow[1][1]));							
						}

						// Check for col candidates not in these locations.						
						if ((candidates.candidateColCount( firstRow[0][1], digi) > 2 ) ||
							(candidates.candidateColCount( firstRow[1][1], digi) > 2 )) {
							int [] encoding = encode(digi, 0, firstRow, secondRow);
							// System.out.println( "XWings found " + encodingToString(encoding) );
							matched.add(encoding);
						}
					}
				}
				// Go through all cols and pick out double candidate cols for this digit.
				// Be careful, there can be multiple matches such as
				// [1,2][1,6]/[7,2][7,6] and [3,3][3,5]/[6,3][6,5]
				LinkedList<int[][]> doubleCols = new LinkedList<>();
				for ( int coli = 0; coli < COLS; coli++) {
					int[] rowLocs = candidates.candidateColLocations(coli,digi);
					if ( 2 == rowLocs.length) {
					   doubleCols.add( new int[][]{ new int[]{rowLocs[0],coli}, new int[]{rowLocs[1],coli} });
					}
				}
				while ( doubleCols.size() > 0) {
					int[][] firstCol = doubleCols.removeFirst();
					int[][] secondCol;
					if ( null != (secondCol = matchingCol( firstCol, doubleCols ))) {
						// Check for additional matches
						int[][] thirdCol;
						if ( null != (thirdCol = matchingCol( firstCol, doubleCols ))) {
							System.out.println( format( "Warning: XWings found third col match for digit %d, row %d at rowcols [%d,%d],[%d,%d}",
							   digi, thirdCol[0][0], thirdCol[0][1], thirdCol[1][0], thirdCol[1][1]));							
						}
						
						// Check for row candidates not in these locations.						
						if ((candidates.candidateRowCount( firstCol[0][0], digi) > 2 ) ||
							(candidates.candidateRowCount( firstCol[1][0], digi) > 2 )) {
							int [] encoding = encode(digi, 1, firstCol, secondCol);
							// System.out.println( "XWings found " + encodingToString(encoding) );
							matched.add(encoding);
						}
					}
				}
			}		
		}
		return matched;
	}

	/** 
	 * Finds and removes a matching row from the list.
	 * @return matching row or null
	 */	
	protected int[][] matchingRow( int[][] firstRow, LinkedList<int [][]> remainingRows ) {
		for ( int rowi = 0; rowi < remainingRows.size(); rowi++ ) {
			int [][] testRow = remainingRows.get(rowi);
			// Test cols for match
			if (firstRow[0][1] == testRow[0][1] && firstRow[1][1] == testRow[1][1]) {
				remainingRows.remove(rowi);
				return testRow;
			}			
		}
		return null;
	}
	
	/** 
	 * Finds and removes a matching col from the list.
	 * @return matching col or null
	 */	
	protected int[][] matchingCol( int[][] firstCol, LinkedList<int [][]> remainingCols ) {
		for ( int coli = 0; coli < remainingCols.size(); coli++ ) {
			int [][] testCol = remainingCols.get(coli);
			// Test rows for match
			if (firstCol[0][0] == testCol[0][0] && firstCol[1][0] == testCol[1][0]) {
				remainingCols.remove(coli);
				return testCol;
			}			
		}
		return null;
	}
	
	// Encode int [] index map
	// digit at index 0, 1-based
	// rowCol orientation at index 1, 0 == row, 1 == col
	// four rowCols A, B, C, D
	// first pair AB rowCols at indexes 2,3 and 4,5
	// second pair CD rowCols at indexes 6,7 and 8,9
	// Because XWings pairs form a box, there should be a match of
	//    - first pair rows, (A row == B row)
	//    - second pair rows (C row == D row)
	//    - first pair first col and second pair first col (A col == C col)
	//    - first pair second col and second pair second col (B col == D col)
	public static int [] encode( int digi, int rowCol, int[][] first, int[][] second ) {		
		if ( digi < 1 || digi > 9) 
			throw new IllegalArgumentException( "digit=" + digi);
		if ( rowCol < 0 || rowCol > 1) 
			throw new IllegalArgumentException( "rowCol=" + rowCol);
		if ( null == first || null == second) 
			throw new IllegalArgumentException( "first=" + first + ", second=" + second);
		
		String error = "";
		if (0 == rowCol) {
			// row orientation
			if (first[0][0] != first[1][0]) error += " first row mismatch";
			if (second[1][0] != second[1][0])	error += " second row mismatch";
			if (first[0][1] != second[0][1]) error += " first col mismatch";
			if (first[1][1] != second[1][1]) error += " second col mismatch";
		} else {
			if (first[0][1] != first[1][1]) error += " first col mismatch";
			if (second[1][1] != second[1][1])	error += " second col mismatch";
			if (first[0][0] != second[0][0]) error += " first row mismatch";
			if (first[1][0] != second[1][0]) error += " second row mismatch";
		}
		if (error.length() > 0)
			throw new IllegalArgumentException(error);
		
		return new int[] {digi,rowCol,
			first[0][0],first[0][1],first[1][0],first[1][1],
			second[0][0],second[0][1],second[1][0],second[1][1]};		
	}
	
	@Override
	public String encodingToString( int[] enc) {
		return format( "digit %d %s at rowCols=[%d,%d],[%d,%d] and rowCols=[%d,%d],[%d,%d]" ,
			enc[0],(enc[1]==0)?"row":"col", enc[2],enc[3],enc[4],enc[5],enc[6],enc[7],enc[8],enc[9]);
	}
	
	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}