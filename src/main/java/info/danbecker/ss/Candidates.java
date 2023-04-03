package info.danbecker.ss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import info.danbecker.ss.Utils.Unit;
import info.danbecker.ss.tree.ChangeData;

import static info.danbecker.ss.Board.*;
import static info.danbecker.ss.Utils.ROWS;
import static info.danbecker.ss.Utils.COLS;
import static info.danbecker.ss.Utils.DIGITS;
import static info.danbecker.ss.Utils.BOXES;
import static java.lang.String.format;

/**
 * Sudoku board candidates
 * Can be initialized from a board and applying the LegalCandidates rule.
 * <p>
 * An empty board contains all digit candidates in each cell.
 * A filled-in occupied position contains a single negative digit.
 * A not-filled in position contains one or more candidates, which are positive.
 * An uncompleted board with 0 candidates in locations is considered an error condition.
 * In general, positions are 0-based, however digits are 1-based.
 * Can be modified by addin/setting and removing/unsetting candidates at rows,col
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class Candidates implements Comparable<Candidates> {
	// Various magical number names to make code more readable.
    public static boolean NAKED = true;	
    public static boolean NOT_NAKED = false;	
    public static int FULL_COMBI_MATCH = -1;	
    public static int NOT_CANDIDATE = 0;
    public static int ALL_DIGITS = -1;
    public static int ALL_COUNTS = -1;
    public static int ALL_UNITS = -1;

    /** 
     * Action verbs to describe a change made to a Candidates object.
     * -Occupy states that a loc is now occupied. Candidates should be removed from this loc.
     * -Unoccupy states that the play has been reversed. The occupant is now a candidate.  
     * -Add states that a candidate has been added to a loc (maybe used for undo or setting up a puzzle state).
     * -Remove states that a candidate has been removed from a loc, one step closer to solving.
     * <p>
     * Given two candidates states, one can determine the actions to go from one to the other.
     */
    public enum Action { OCCUPY, UNOCCUPY, ADD, REMOVE }
    
	// 9 rows, 9 cols, 9 digits.
	// Int is negative digit for filled in, 0 for not candidate, digit for candidate
	private int [][][] candidates;

	public Candidates( Board board )  {
		init( board );
	}
	
	public void init( Board board ) {
		if ( null == board ) throw new IllegalArgumentException("board cannot be null");
		
		candidates = new int[ROWS][COLS][DIGITS];
		
		for( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++) {
				if ( NOT_OCCUPIED == board.get(ROWCOL[rowi][coli])) {
					// Choice, set all as candidates, or check row/col/block (default rule)
					// Later we run the Validate rule which removes candidates from occupied.
					for( int digiti = 0; digiti < DIGITS; digiti++) {
						addCandidate( ROWCOL[rowi][coli], digiti + 1);
					}
				} else if ( NOT_OCCUPIED != board.get(ROWCOL[rowi][coli])) {
					setOccupied( ROWCOL[rowi][coli], board.get( ROWCOL[rowi][coli] ));
				}
			}
		}
	}

	public Candidates( Candidates that )  {
		if ( null == that ) throw new IllegalArgumentException("candidates cannot be null");		
		this.candidates = new int[ROWS][COLS][DIGITS];
		
		for( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++) {
				for ( int digi = 0; digi < DIGITS; digi++) {
					this.candidates[ rowi ][ coli ][digi ] = that.candidates[ rowi ][ coli ][digi ];
				}
			}
		}
	}
	
	/** Returns NOT_OCCUPIED if the box is empty, a positive digit if filled in. */
	public int getOccupied( RowCol rowCol ) {
	   int [] boxCandidates = getCandidates( rowCol );
	   for ( int digiti = 0; digiti < DIGITS; digiti++ ) {
		   if ( boxCandidates[ digiti ] < 0)
			   return -boxCandidates[ digiti ];
	   }
	   return NOT_OCCUPIED;
	}
	
	/** Returns false if the box is empty, true if filled in. */
	public boolean isOccupied( RowCol rowCol ) {
		return NOT_OCCUPIED != getOccupied( rowCol );
	}
	
	/** 
	 * Sets digit to an occupied board entry.
	 * Sets all other digits to NOT_CANDIDATE.
	 * @returns NOT_CANDIDATE if the box is empty, a digit if filled in. */
	public int setOccupied( RowCol rowCol, int digit ) {
	   int previous = NOT_CANDIDATE;
	   // Check for setting two occupied digits
	   int alreadyThere = getOccupied( rowCol );
	   if ( NOT_OCCUPIED != alreadyThere ) {
		   throw new IllegalArgumentException( format( "Attempting to set digit %d in rowCol=%s with previous digit %d already there.",
		       digit, rowCol, alreadyThere ));
	   }
	   for ( int digiti = 0; digiti < DIGITS; digiti++ ) {
		   if (  digiti == digit - 1 ) {	   
			  previous = candidates[rowCol.row()][rowCol.col()][ digiti ];
		      candidates[rowCol.row()][rowCol.col()][ digiti ] = -digit;
		   } else {
		      candidates[rowCol.row()][rowCol.col()][ digiti ] = NOT_CANDIDATE;
		   }
	   }
	   return previous;
	}

	/**
	 * Sets the occupied location to NOT_OCCUPIED.
	 * Other digits are left alone.
	 * Useful for undo or reset actions.
	 * Will need to perform an ADD action on the digit if you wish to make it a candidate.
	 * @returns 0 if the box is empty, a digit if filled in. */
	public int setUnoccupied( RowCol rowCol, int digit ) {
	   // Check for setting two occupied digits
	   int alreadyThere = getOccupied( rowCol );
	   if ( alreadyThere != digit ) {
		   throw new IllegalArgumentException( format( "Attempting to unoccupy digit %d in rowCol=%s which was already occupied by digit %d.",
		       digit, rowCol, alreadyThere ));
	   }
       candidates[rowCol.row()][rowCol.col()][ digit - 1 ] = NOT_OCCUPIED;
       return alreadyThere;
	}

	/** Returns number of occupied boxes for the entire board. */
	public int entryCount() {
	   if ( null == candidates) return 0;
	   int count = 0;
	      for( int rowi = 0; rowi < ROWS; rowi++ ) {
	   	   for ( int coli = 0; coli < COLS; coli++) {
	   		   if ( isOccupied( ROWCOL[rowi][coli] )) count++;
	   	   }    	   
	      }
	   return count;
	}

	/** Returns all candidates in the given box. */
	public int [] getCandidates( RowCol rowCol ) {
		return candidates[ rowCol.row() ][ rowCol.col() ];
	}

	/** Returns all candidates in the given box. */
	public List<Integer> getCandidatesList( RowCol rowCol ) {
		List<Integer> candList = new ArrayList<>();
		for( int digi = 0; digi < DIGITS; digi++) {
			if ( this.candidates[rowCol.row()][rowCol.col()][digi] > 0)
				candList.add( this.candidates[rowCol.row()][rowCol.col()][digi] );
		}
		return candList;
	}

	/** Returns all candidates in the given cell. */
	public int [] getRemainingCandidates( RowCol rowCol ) {
		int [] candidates = new int[ candidateCount( rowCol ) ];
		int candi = 0;
		for( int digi = 0; digi < DIGITS; digi++) {
			if ( this.candidates[rowCol.row()][rowCol.col()][digi] > 0)
				candidates[ candi++ ] =  this.candidates[rowCol.row()][rowCol.col()][digi];
		}
		return candidates;
	}

	/** Completely replace the candidate list.
	 * @return the current candidate list.
	 */
	public int [] setCandidates( RowCol rowCol, int [] vals ) {
		candidates[ rowCol.row() ][ rowCol.col() ] = vals;
		return candidates[ rowCol.row() ][ rowCol.col() ];
	}

	/** 
	 * Add a single candidate digit to this location.
	 * @return digit previously NOT_CANDIDATE, was something added?
	 */
	public boolean addCandidate( RowCol rowCol, int digit ) {
		int previous = candidates[rowCol.row()][rowCol.col()][ digit - 1 ];
		candidates[rowCol.row()][rowCol.col()][ digit - 1 ] = digit;
		return previous == NOT_CANDIDATE;
	}

	/** Remove a single one-based candidate digit from this location.
	 * Ignores deleting a negative item which is a placement.
	 * @return digit previously there, was something deleted?
	 */
	public boolean removeCandidate( RowCol rowCol, int digit ) {
		int previous = candidates[rowCol.row()][rowCol.col()][ digit - 1 ] ;
		if ( previous < 1) return false; // ignore placed items 
		candidates[rowCol.row()][rowCol.col()][ digit - 1 ] = 0;
		// if ( previous != NOT_CANDIDATE )
		//	System.out.println( format( "   Removed candidate %d from row/col %d/%d", digit, rowi, coli ));			
		return previous != NOT_CANDIDATE;
	}

	/** Remove one-based candidate digits from this location.
	 * @return count of digits removed
	 */
	public int removeCandidates( RowCol rowCol, int [] digits ) {
		int count = 0;
		for ( int digi=0; digi < digits.length; digi++) {
			if( removeCandidate( rowCol, digits[ digi ]))
				count++;
		}
		return count;
	}

	/** 
	 * Given a candidate digit at a location,
	 * remove other candidates not in this location
	 * in the given row, col, box.
	 * @return number of other candidates removed
	 */
	public int removeCandidatesSameUnit( RowCol rowCol, int digit, Unit unit) {
		int candCount = 0;
		switch (unit) {
		case ROW: {
			for (int coli = 0; coli < COLS; coli++) {
				// Ignore my location
				if (rowCol.col() != coli) {
					if (removeCandidate(ROWCOL[rowCol.row()][coli], digit))
						candCount++;
				}
			}
			break;
		}
		case COL: {
			for (int rowi = 0; rowi < ROWS; rowi++) {
				// Ignore my location
				if (rowCol.row() != rowi) {
					if (removeCandidate(ROWCOL[rowi][rowCol.col()], digit))
						candCount++;
				}
			}
			break;
		}
		case BOX: {
			RowCol[] boxLocs = Board.getBoxRowCols(rowCol.box());
			for (int loci = 0; loci < boxLocs.length; loci++) {
				// Ignore my location
				if ( rowCol != boxLocs[loci] ) {
					if (removeCandidate( rowCol, digit))
						candCount++;
				}
			}
			break;
		}
		} // switch
		return candCount;
	}

	/** 
	 * Given a candidate digit at a location,
	 * remove other candidates not in this location
	 * in all given row, col, box.
	 * @return number of other candidates removed
	 */
	public int removeCandidatesSameUnits(RowCol rowCol, int digit) {
		int candCount = 0;
		for ( Unit unit: Unit.values()) {
			candCount += removeCandidatesSameUnit( rowCol, digit, unit );
		}
		return candCount;
	}

	/** Remove row or col candidates, given digit, not in given box
	 * Rowi or coli might be -1 (multiple)
	 * @return count of candidates removed
	 */
	public int removeCandidateNotInBox(int rowi, int coli, int boxi, int digit) {
		int count = 0;
		if (rowi == -1) {
			// Row not specified
			for (rowi = 0; rowi < ROWS; rowi++) {
				if (boxi != ROWCOL[rowi][coli].box()) {
					if (removeCandidate(ROWCOL[rowi][coli], digit))
						count++;
				}
			}
		}
		if (coli == -1) {
			// Col not specified
			for (coli = 0; coli < COLS; coli++) {
				if (boxi != ROWCOL[rowi][coli].box()) {
					if (removeCandidate(ROWCOL[rowi][coli], digit))
						count++;
				}
			}
		}
		return count;
	}

	/** Remove row candidate digits in rowi if not in given locations
	 * @return count of candidates removed
	 */
	public int removeRowCandidatesNotIn(int digi, int rowi, RowCol[] rowCols) {
		int count = 0;
		// List of cols to ignore
		int[] ignoreCols = new int[rowCols.length];
		for (int ignorei = 0; ignorei < rowCols.length; ignorei++)
			ignoreCols[ignorei] = rowCols[ignorei].col();
		for (int coli = 0; coli < COLS; coli++) {
			// Ignore ignore cols
			if (!containsDigit(ignoreCols, coli)) {
				if (removeCandidate(ROWCOL[rowi][coli], digi))
					count++;
			}
		}
		return count;
	}

	/** Remove box candidates not in the given row.
	 * @return count of candidates removed
	 */
	public int removeBoxCandidatesNotInRow(int digi, int boxi, int rowi) {
		int count = 0;
		RowCol[] rowCols = Board.BOXR[boxi];
		for (int loci = 0; loci < rowCols.length; loci++) {
			// Ignore locations with given row
			if ( rowi != rowCols[loci].row()) {
				if (removeCandidate(rowCols[loci], digi))
					count++;
				
			}
		}
		return count;
	}

	/** Remove col candidate digits in coli if not in given locations
	 * @return count of candidates removed
	 */
	public int removeColCandidatesNotIn( int digi, int coli, RowCol[] rowCols) {
		int count = 0;
		// List of rows to ignore
		int[] ignoreRows = new int[rowCols.length];
		for (int ignorei = 0; ignorei < rowCols.length; ignorei++)
			ignoreRows[ignorei] = rowCols[ignorei].row();
		for (int rowi = 0; rowi < ROWS; rowi++) {
			// Ignore ignore rows 
			if (!containsDigit(ignoreRows, rowi)) {
				if (removeCandidate( ROWCOL[rowi][coli], digi))
					count++;
			}
		}
		return count;
	}
	
	/** Remove box candidates not in the given col.
	 * @return count of candidates removed
	 */
	public int removeBoxCandidatesNotInCol(int digi, int boxi, int coli) {
		int count = 0;
		RowCol [] rowCols = Board.BOXR[boxi];
		for (int loci = 0; loci < rowCols.length; loci++) {
			// Ignore locations with given col
			if ( coli != rowCols[loci].col() ) {
				if (removeCandidate(rowCols[loci], digi))
					count++;
			}
		}
		return count;
	}

	/** Remove numerals if not in given locations
	 * For example combo {04} locations [8, 1][8, 6]
	 * will remove 1s and 5s in row 8, except for columns 1 and 6.
	 * Just corrects items in one row or column
	 * @return count of candidates removed
	 */
	public int removeCandidatesNotInLocations( int [] combo, RowCol[] rowCols) {
		int count = 0;
		boolean rowsMatch = RowCol.rowsMatch(rowCols);
		boolean colsMatch = RowCol.colsMatch(rowCols);
		// boolean boxesMatch = Utils.colsMatch(rowCols);
		
		if ( rowsMatch ) {
			int rowi = rowCols[0].row();
			// List of cols to ignore
			int [] ignore = new int[ rowCols.length ];
			for( int ignorei = 0; ignorei < rowCols.length; ignorei++)
				ignore[ignorei] = rowCols[ignorei].col();
			for ( int coli = 0; coli < COLS; coli++ ) {
				// Ignore cols 1 and 6
				if ( !containsDigit( ignore, coli )) {
					for ( int digiti = 0; digiti < combo.length; digiti++) {
						// System.out.println( format("Removing digit %d from row rowCol=%d/%d", combo[ digiti ] + 1, rowi, coli));
						if (removeCandidate(ROWCOL[rowi][coli], combo[ digiti ] + 1))
							count++;									
					}
				}
			}
			
		} else if ( colsMatch ) {
			int coli = rowCols[0].col();
			// List of cols to ignore
			int [] ignore = new int[ rowCols.length ];
			for( int ignorei = 0; ignorei < rowCols.length; ignorei++)
				ignore[ignorei] = rowCols[ignorei].row();
			for ( int rowi = 0; rowi < COLS; rowi++ ) {
				// Ignore rows 3 and 7
				if ( !containsDigit( ignore, rowi )) {
					for ( int digiti = 0; digiti < combo.length; digiti++) {
						// System.out.println( format("Removing digit %d from col rowCol=%d/%d", combo[ digiti ] + 1, rowi, coli));
						if (removeCandidate(ROWCOL[rowi][coli], combo[ digiti ] + 1))
							count++;									
					}
				}
			}
		}
		return count;
	}

	/** Turns hidden pairs into naked pairs.
	 * For example combo {02} locations [8, 1][8, 6]
	 * will remove non combo digits in thes locations.
	 * @return count of candidates removed
	 */
	public int removeCandidatesNotInCombo( int [] combo, RowCol[] rowCols) {
		int count = 0;
		for ( int loci = 0; loci < rowCols.length; loci++) {
			for ( int digiti = 0; digiti < DIGITS; digiti++) {
				if ( !containsDigit( combo, digiti )) {
				   if (removeCandidate(rowCols[loci], digiti + 1 )) {
                      // System.out.println( format("Removed digit %d from row rowCol=%d/%d", digiti + 1, rowi, coli));
				      count++;
				   }
				}
			}
		}
		return count;
	}

	/** 
	 * Removes all candidates from all cells. 
	 * Does not touch occupied digit entries.
	 * @return count of removed candidates
	 */
	public int removeAllCandidates() {
	   if ( null == candidates) return 0;
	   int count = 0;
	      for( int rowi = 0; rowi < ROWS; rowi++ ) {
	   	   for ( int coli = 0; coli < COLS; coli++) {
	   		   count += removeCandidates( ROWCOL[rowi][coli] );
	   	   }    	   
	      }
	   return count;
	}

	/** 
	 * Removes candidates in this cell.
	 * Does not touch occupied digit entries.
	 * 
	 * @return count of removed candidates
	 */
	public int removeCandidates( RowCol rowCol ) {
	   int count = 0;
	   for ( int digiti = 0; digiti < DIGITS; digiti++ ) {
		   if ( candidates[rowCol.row()][rowCol.col()][ digiti ] > 0) {
		      candidates[rowCol.row()][rowCol.col()][ digiti ] = NOT_CANDIDATE;
		      count++;
		   }
	   }
	   return count;
	}
	
	/** 
	 * Removes all candidates from boxes with occupied entry digit. 
	 * Does not touch occupied digit entries.
	 * @return count of removed candidates
	 */
	public int removeAllOccupiedCandidates() {
		if (null == candidates)
			return 0;
		int count = 0;
		for (int rowi = 0; rowi < ROWS; rowi++) {
			for (int coli = 0; coli < COLS; coli++) {
				if (NOT_OCCUPIED != getOccupied(ROWCOL[rowi][coli])) {
					count += removeCandidates(ROWCOL[rowi][coli]);
				}
			}
		}
		return count;
	}

	/** Return the first non-zero candidate at the given row, col. 
	 * @return the digit for the first candidate, or NOT_CANDIDATE if no candidates.
	 */
	public int getCandidateDigit(RowCol rowCol) {
		for (int digi = 0; digi < DIGITS; digi++) {
			if (candidates[rowCol.row()][rowCol.col()][digi] > 0)
				return candidates[rowCol.row()][rowCol.col()][digi];
		}
		return NOT_CANDIDATE;
	}

	/** Returns if this ones-based digit is a candidate in this cell. */
	public boolean isCandidate( RowCol rowCol, int digi) {
		if ( candidates[rowCol.row()][rowCol.col()][digi - 1] == digi ) return true;
		return false;
	}
	
	/** Returns total of all candidates in all boxes. */
	public int candidateCount() {
	   if ( null == candidates) return 0;
	   int count = 0;
	      for( int rowi = 0; rowi < ROWS; rowi++ ) {
	   	   for ( int coli = 0; coli < COLS; coli++) {
	   		   count += candidateCount( ROWCOL[rowi][coli] );
	   	   }    	   
	      }
	   return count;
	}

	/** Returns the number of candidates in a single cell. */
	public int candidateCount(RowCol rowCol) {
	   int count = 0;
	   for( int digi = 0; digi < DIGITS; digi++) {
		   if ( candidates[rowCol.row()][rowCol.col()][digi] > 0) count++;
	   }
	   return count;
	}

	/** Returns total of all candidates in the given locations. */
	public int candidateLocationCount(List<RowCol> locs) {
		if (null == candidates)	return 0;
		if (null == locs) return 0;
		int count = 0;
		for (int loci = 0; loci < locs.size(); loci++) {
			RowCol rowCol = locs.get(loci);
			count += candidateCount(rowCol);
		}
		return count;
	}

	/** Returns a list of locations with no candidates 
	 * and not occupied, a sign of an error condition.
	 * @return
	 */
	public List<RowCol> emptyLocations() {
		List<RowCol> locs = new ArrayList<>();
		for (int rowi = 0; rowi < ROWS; rowi++) {
			for (int coli = 0; coli < ROWS; coli++) {
				if ( !isOccupied( ROWCOL[rowi][coli] ) && 0 == candidateCount( ROWCOL[rowi][coli] ) ) {
					locs.add( ROWCOL[rowi][coli] );
				}
			}
		}
		return locs;
	}
	
	/** Returns a count of this digit in the given locations. */
	public int candidateLocationCount(int digit, List<RowCol> locs) {
		if (null == candidates)	return 0;
		if (null == locs) return 0;
		int count = 0;
		for (int loci = 0; loci < locs.size(); loci++) {
			if ( isCandidate( locs.get(loci), digit )) count++;
		}
		return count;
	}
	
	/** Returns the number of candidates in multiple boxes for this row. */
	public int candidateRowLocCount( int rowi, int [] cols) {
	   int count = 0;
	   for ( int coli = 0; coli < cols.length; coli++ ) {
		   count += candidateCount( ROWCOL[rowi][cols[ coli ]]);
	   }
	   return count;
	}

	/** Returns the number of candidates in multiple box locations. */
	public int candidateRowColCount( List<RowCol> rowCols) {
	   if ( null == rowCols || 0 == rowCols.size())
		   return 0;
	   int count = 0;
	   for( int loci = 0; loci < rowCols.size(); loci++) {
		   RowCol rowCol = rowCols.get(loci);
		   count += candidateCount( rowCol );
	   }
	   return count;
	}
	
	/**
	 * Returns the digit count of the given 1-based digits in the given locations.
	 * Can be used for one or more digits in any row col pattern
	 *     - can have a row of 9, a col of 9, or box of 9 locations 
	 *     - can have a row or col of 3 in a given box
	 *     - can inefficiently have all digits in all boxes. 
	 * */
	public int candidateDigitRowColCount( int digi, RowCol[] rowCols) {
	   if ( null == rowCols || 0 == rowCols.length)
		   return 0;
	   int count = 0;
	   for( int loci = 0; loci < rowCols.length; loci++) {
		   RowCol rowCol = rowCols[loci];
		   if ( candidates[rowCol.row()][rowCol.col()][digi - 1] > 0) count++;
	   }
	   return count;
	}
	
	/** Returns the number of candidates in multiple boxes for this col. */
	public int candidateColLocCount( int coli, int [] rows ) {
	   int count = 0;
	   for ( int rowi = 0; rowi < rows.length; rowi++ ) {
		   count += candidateCount( ROWCOL[rows[ rowi ]][coli] );
	   }
	   return count;
	}
	
	/** Returns the number of candidates for this ones-based digit in this row. */
	public int candidateRowCount( int rowi, int digi) {
	   int count = 0;
	   for( int coli = 0; coli < COLS; coli++) {
		   if ( isCandidate( ROWCOL[rowi][coli], digi )) count++;
	   }
	   return count;
	}

	/** Returns the number of groups of this size with this candidate ones-based digit in this row. */
	public int candidateRowGroupCount( int rowi, int digi, int groupSize) {
	   int count = 0;
	   for( int coli = 0; coli < COLS; coli++) {
		   if ( isCandidate( ROWCOL[rowi][coli], digi ) && ( ALL_COUNTS == groupSize || candidateCount( ROWCOL[rowi][coli] ) == groupSize)) count++;
	   }
	   return count;
	}

	/** Returns the locations of groups of this size with this candidate ones-based digit in this row. */
	public List<RowCol> candidateRowGroupLocs( int rowi, int digi, int groupSize) {
	   List<RowCol> locations = new LinkedList<>();
	   for( int coli = 0; coli < COLS; coli++) {
		   if ( isCandidate( ROWCOL[rowi][coli], digi ) && ( ALL_COUNTS == groupSize || candidateCount( ROWCOL[rowi][coli] ) == groupSize))
			   locations.add(ROWCOL[rowi][coli] );
	   }
	   return locations;
	}

	/** Returns the locations of groups of this size with this candidate ones-based digit in this row. */
	public List<RowCol> candidateUnitGroupLocs( Unit unit, int uniti, int digi, int groupSize) {
		return switch ( unit ) {
		case ROW -> candidateRowGroupLocs( uniti, digi, groupSize);
		case COL -> candidateColGroupLocs( uniti, digi, groupSize);
		case BOX -> candidateBoxGroupLocs( uniti, digi, groupSize);
		};
	}

	/** Returns the nth group of this size with this candidate ones-based digit in this row. */
	public int candidateRowGroupFind( int rowi, int digi, int groupSize, int groupi) {
	   int count = 0;
	   for( int coli = 0; coli < COLS; coli++) {
		   if ( isCandidate( ROWCOL[rowi][coli], digi ) && (ALL_COUNTS == groupSize || candidateCount( ROWCOL[rowi][coli] ) == groupSize)) {
			   if ( count == groupi ) {
				   return coli;
			   }
			   count++;
		   }
	   }
	   return NOT_FOUND;
	}

	/** Returns the number of candidates for this digit in this unit. */
	public int candidateCount( Utils.Unit unit, int uniti, int digi) {
		switch (unit) {
			case ROW: return candidateRowCount( uniti, digi );
			case COL: return candidateColCount( uniti, digi );
			case BOX: return candidateBoxCount( uniti, digi );
		}
		return -1; // should never get here
	}

	/** Returns the number of candidates for this digit in this unit, that are in a group of this size.
	 *  For example a group of 2 will return a count of pairs in this unit with this candidate. */
	public int candidateGroupCount( Utils.Unit unit, int uniti, int digi, int groupSize) {
		switch (unit) {
			case ROW: return candidateRowGroupCount( uniti, digi, groupSize );
			case COL: return candidateColGroupCount( uniti, digi, groupSize );
			case BOX: return candidateBoxGroupCount( uniti, digi, groupSize );
		}
		return -1; // should never get here
	}
	
	
	/** Returns a 3 unit by 9 cell digit counts. */
	public int[][] candidateUnitCounts( int digi ) {
		// Create 3 unit by 9 cell counts.
		int [][] digitCounts = new int [][] {
			new int [] {0,0,0,0,0,0,0,0,0},
			new int [] {0,0,0,0,0,0,0,0,0},
			new int [] {0,0,0,0,0,0,0,0,0},
		};
		
		for (Unit unit : Unit.values()){
			for ( int celli = 0; celli < DIGITS; celli++) {
				digitCounts[unit.ordinal()][celli] = candidateCount( unit, celli, digi );
			}
		}

		return digitCounts;
	}

	/** Returns a 3 unit by 9 cell digit counts with this group size (e.g. groupSize 2 = pair.) */
	public int[][] candidateUnitGroupCounts( int digi, int groupSize) {
		// Create 3 unit by 9 cell counts.
		int [][] digitPairCounts = new int [][] {
			new int [] {0,0,0,0,0,0,0,0,0},
			new int [] {0,0,0,0,0,0,0,0,0},
			new int [] {0,0,0,0,0,0,0,0,0},
		};
		
		for (Unit unit : Unit.values()){
			for ( int celli = 0; celli < DIGITS; celli++) {
				digitPairCounts[unit.ordinal()][celli]= candidateGroupCount( unit, celli, digi, groupSize );
			}
		}
		
		return digitPairCounts;
	}
	
	/** Returns the nth group with this number of candidates for this digit in this unit.
	 *  For example a groupSize of 2 will return the nth count of pairs in this unit with this candidate. */
	public int candidateGroupFind( Utils.Unit unit, int uniti, int digi, int groupSize, int groupi) {
		switch (unit) {
			case ROW: return candidateRowGroupFind( uniti, digi, groupSize, groupi );
			case COL: return candidateColGroupFind( uniti, digi, groupSize, groupi );
			case BOX: return candidateBoxGroupFind( uniti, digi, groupSize, groupi );
		}
		return -1; // should never get here
	}

	/** Returns the first col number of the candidates digit in this row. */
	public int candidateRowLocation( int rowi, int digi) {
	   for( int coli = 0; coli < COLS; coli++) {
		   if ( candidates[rowi][coli][digi - 1] == digi) 
			   return coli;
	   }
	   return NOT_FOUND;
	}

	/** Returns an array with col indexes containing this one-based candidate digit in this row. */
	public int[] candidateRowLocations(int rowi, int digi) {
		int[] colLocations = new int[candidateRowCount(rowi, digi)];
		if (0 == colLocations.length)
			return colLocations;
		int index = 0;
		for (int coli = 0; coli < COLS; coli++) {
			if (candidates[rowi][coli][digi - 1] > 0)
				colLocations[index++] = coli;
		}
		return colLocations;
	}

	/** Returns the number of candidates for this digit in this col. */
	public int candidateColCount( int coli, int digi) {
	   int count = 0;
	   for( int rowi = 0; rowi < ROWS; rowi++) {
		   if ( isCandidate( ROWCOL[rowi][coli], digi )) count++;
	   }
	   return count;
	}
	
	/** Returns the number boxes with this group size for this one-based candidate digit in this col. */
	public int candidateColGroupCount( int coli, int digi, int groupSize) {
	   int count = 0;
	   for( int rowi = 0; rowi < ROWS; rowi++) {
		   if ( isCandidate( ROWCOL[rowi][coli], digi ) && (ALL_COUNTS == groupSize || candidateCount( ROWCOL[rowi][coli] ) == groupSize)) count++;
	   }
	   return count;
	}

	/** Returns the locations of groups of this size with this candidate ones-based digit in this row. */
	public List<RowCol> candidateColGroupLocs( int coli, int digi, int groupSize) {
	   List<RowCol> locations = new LinkedList<>();
	   for( int rowi = 0; rowi < ROWS; rowi++) {
		   if ( isCandidate( ROWCOL[rowi][coli], digi ) && ( ALL_COUNTS == groupSize || candidateCount( ROWCOL[rowi][coli] ) == groupSize))
			   locations.add(ROWCOL[rowi][coli] );
	   }
	   return locations;
	}

	/** Returns the nth group of this size with this candidate ones-based digit in this col. */
	public int candidateColGroupFind( int coli, int digi, int groupSize, int groupi) {
	   int count = 0;
	   for( int rowi = 0; rowi < ROWS; rowi++) {
		   if ( isCandidate( ROWCOL[coli][rowi], digi ) && (ALL_COUNTS == groupSize || candidateCount( ROWCOL[coli][rowi] ) == groupSize)) {
			   if ( count == groupi ) {
				   return rowi;
			   }
			   count++;
		   }
	   }
	   return NOT_FOUND;
	}

	/** Returns the first row number of the candidates digit in this col. */
	public int candidateColLocation(int coli, int digi) {
		for (int rowi = 0; rowi < ROWS; rowi++) {
			if (isCandidate( ROWCOL[rowi][coli], digi ))
				return rowi;
		}
		return NOT_FOUND;
	}

	/** Returns an array with row indexes containing this one-based candidate digit in this col. */
	public int[] candidateColLocations(int coli, int digi) {
		int[] rowLocations = new int[candidateColCount(coli, digi)];
		if (0 == rowLocations.length)
			return rowLocations;
		int index = 0;
		for (int rowi = 0; rowi < ROWS; rowi++) {
			if (candidates[rowi][coli][digi - 1] > 0)
				rowLocations[index++] = rowi;
		}
		return rowLocations;
	}

	/** Returns the number of candidates for this digit in this box. */
	public int candidateBoxCount( int boxi, int digi) {
	   int count = 0;
	   RowCol[] locs = Board.BOXR[ boxi ];
	   for( int loci = 0; loci < BOXES; loci++) {
		   RowCol rowCol = locs[ loci ];
		   if ( candidates[rowCol.row()][rowCol.col()][digi - 1] == digi) count++;
	   }
	   return count;
	}
	
	/** Returns the number of groups with this one-based candidate digit in this box. */
	public int candidateBoxGroupCount( int boxi, int digi, int groupSize) {
	   int count = 0;
	   RowCol[] locs = Board.BOXR[ boxi ];
	   for( int loci = 0; loci < BOXES; loci++) {
		   if (isCandidate(locs[loci],digi) && (ALL_COUNTS == groupSize || groupSize == candidateCount(locs[loci]))) {
			 count++;
		   }
	   }
	   return count;
	}

	/** Returns the number of groups with this one-based candidate digit in this box. */
	public List<RowCol>  candidateBoxGroupLocs( int boxi, int digi, int groupSize) {
	   List<RowCol> locations = new LinkedList<>();
	   RowCol[] locs = Board.BOXR[ boxi ];
	   for( int loci = 0; loci < BOXES; loci++) {
		   if (isCandidate(locs[loci],digi) && (ALL_COUNTS == groupSize || groupSize == candidateCount(locs[loci]))) {
			 locations.add( locs[loci] );
		   }
	   }
	   return locations;
	}

	/** Returns the nth group of this size with this candidate ones-based digit in this box. */
	public int candidateBoxGroupFind( int boxi, int digi, int groupSize, int groupi) {
	   int count = 0;
	   RowCol[] locs = Board.BOXR[ boxi ];
	   for( int loci = 0; loci < BOXES; loci++) {
		   if (isCandidate(locs[loci],digi - 1) && (ALL_COUNTS == groupSize || candidateCount(locs[loci]) == groupSize)) {
			   if ( count == groupi )
				   return boxi;
			   count++;
		   }
	   }
	   return NOT_FOUND;
	}

	/** Returns the first rowCol of the candidate digit in this box. */
	public RowCol candidateBoxLocation( int boxi, int digi) {
	   RowCol[] locs = Board.BOXR[ boxi ];
	   for( int loci = 0; loci < BOXES; loci++) {
		   RowCol rowCol = locs[loci];
		   if ( candidates[rowCol.row()][rowCol.col()][digi - 1] == digi)
			   return rowCol;
	   }
	   return null;
	}

	/** Returns the locations of candidates for this digit in this box. */
	public List<RowCol> candidateBoxLocations( int boxi, int digi) {
	   List<RowCol> locations = new ArrayList<>();
	   RowCol[] locs = Board.BOXR[ boxi ];
	   for( int loci = 0; loci < BOXES; loci++) {
		   RowCol rowCol = locs[ loci ];
		   if ( candidates[rowCol.row()][rowCol.col()][digi - 1] == digi) {
			   locations.add(rowCol);
		   }
	   }
	   return locations;
	}

	/** Returns a String showing all candidates for this box */
	public String candidateBoxLocationsString(int blocki) {
		StringBuilder sb = new StringBuilder("Candidates for block " + blocki + ":\n");
		RowCol[] locs = Board.BOXR[blocki];
		
		for (int loci = 0; loci < locs.length; loci = loci + 3) {
			RowCol rowcol1 = locs[loci];
			RowCol rowcol2 = locs[loci+1];
			RowCol rowcol3 = locs[loci+2];
			sb.append(format("Row/cols %d/%d,%d,%d: %s%s%s", 
				rowcol1.row(), rowcol1.col(),rowcol2.col(),rowcol3.col(),
				getCandidatesString(rowcol1),
				getCandidatesString(rowcol2),
				getCandidatesString(rowcol3)
				));
			if (loci < locs.length - 1) sb.append("\n");
		}
		return sb.toString();
	}

	/** Returns a list of locations having these particulars
	 * 
	 * @param digi - one-based digi or ALL_DIGITS 
	 * @param count - particular count or ALL_COUNTS
	 * @return
	 */
	public List<RowCol> getGroupLocations( int digi, int count ){
	   List<RowCol> locs = new LinkedList<>();
	   for ( int rowi = 0; rowi < ROWS; rowi++ ) {
		   for ( int coli = 0; coli < COLS; coli++ ) {
			   if (( ALL_DIGITS == digi ) || isCandidate(ROWCOL[rowi][coli],digi)) {
				   if (( ALL_COUNTS == count ) || (count == candidateCount( ROWCOL[rowi][coli] ))) {
					   locs.add( ROWCOL[rowi][coli] );
				   }					   
			   }			   
		   }		   
	   }
	   return locs;
	}
	
	/** Returns a list of locations in same unit having these particulars
	 * @param unit - Unit enum 
	 * @param digi - one-based digi or ALL_DIGITS 
	 * @param count - particular count or ALL_COUNTS
	 * @param firstRowCol - starting location
	 * @return
	 */
	public List<RowCol> getGroupSameUnitLocations( Unit unit, int digi, int count, RowCol firstRowCol ){
	   List<RowCol> locations = new ArrayList<>();
       int uniti = switch ( unit ) {
       		case ROW -> firstRowCol.row();
			case COL -> firstRowCol.col();
			case BOX -> firstRowCol.box();
	   };
	   
	   List<RowCol> groupLocations = getGroupLocations( unit, uniti, digi, count );
	   // Remove the first location
	   for( RowCol loc : groupLocations ) {
		   if ( loc.equals( firstRowCol) ) {
			   locations.add(loc);
		   }
	   }
	   return locations;
	}

	/** Returns a list of locations in ALL units having these particulars
	 * @param digi - one-based digi or ALL_DIGITS 
	 * @param count - particular count or ALL_COUNTS
	 * @param firstRowCol - starting location
	 * @return
	 */
	public List<RowCol> getGroupAllUnitLocations( int digi, int count, RowCol firstRowCol ){
	   List<RowCol> locations = new ArrayList<>();
	   for ( Unit unit : Unit.values()) {
		   locations.addAll( getGroupSameUnitLocations( unit, digi, count, firstRowCol ) );
	   }
	   return locations;
	}
	
	/** Returns a list of locations having these particulars
	 * 
	 * @param unit - this particular row/col/box
	 * @param uniti - index of given unit 
	 * @param digi - one-based digi or ALL_DIGITS 
	 * @param count - particular count or ALL_COUNTS
	 * @return
	 */
	public List<RowCol> getGroupLocations( Utils.Unit unit, int uniti, int digi, int count ){
	   List<RowCol> locations = new ArrayList<>();
	   if ( Utils.Unit.ROW == unit) {
		   for ( int coli = 0; coli < COLS; coli++ ) {			   
			   if (( ALL_DIGITS == digi ) || isCandidate( ROWCOL[uniti][coli],digi)) {
				   if (( ALL_COUNTS == count ) || (count == candidateCount( ROWCOL[uniti][coli] ))) {
					   locations.add( ROWCOL[uniti][coli] );
				   }					   
			   }			   
		   }		   
	   } else if ( Utils.Unit.COL == unit) {
		   for ( int rowi = 0; rowi < ROWS; rowi++ ) {			   
			   if (( ALL_DIGITS == digi ) || isCandidate(ROWCOL[rowi][uniti],digi)) {
				   if (( ALL_COUNTS == count ) || (count == candidateCount( ROWCOL[rowi][uniti] ))) {
					   locations.add( ROWCOL[rowi][uniti] );
				   }					   
			   }			   
		   }		   
	   } else if ( Utils.Unit.BOX == unit) {
		   RowCol[] locs = Board.getBoxRowCols(uniti);
		   for ( int loci = 0; loci < locs.length; loci++ ) {
			   RowCol rowCol = locs[ loci ];
			   if (( ALL_DIGITS == digi ) || isCandidate(rowCol,digi)) {
				   if (( ALL_COUNTS == count ) || (count == candidateCount( rowCol ))) {
					   locations.add( rowCol );
				   }					   
			   }			   			   
		   }
	   }
	   return locations;		
	}
	
	/** 
	 * Returns the locations of candidates exactly matching this row digit combo.
	 * Combo {12} matches [120000000]. If naked
	 * Combo {12} matches [123000000]. If not naked
	 * 
	 * @param combi array of zero based digits
	 * @param naked combo exact match required, no other candidates allowed
	 * @param partialCount allows match of fewer than all combi digits. FULL_COMBI_MATCH requires
	 * 	all combi digits must match, but a partialCount will allow fewer to match.
	 * @return zero based array of rowCol
	 */
	public boolean candidatesMatch(RowCol rowCol, int[] combi, boolean naked, int partialCount) {
		if (NOT_OCCUPIED == getOccupied(rowCol)) {
			int[] cellCandidates = candidates[rowCol.row()][rowCol.col()]; // 9 digits long
			int matchCount = 0;
			for (int candi = 0; candi < DIGITS; candi++) {
			    // Combi contains candidate digit?
				if (containsDigit( combi, candi )) {
					// Candidate digit in combo
					if ( cellCandidates[candi] > 0 ) {
					   // Candidate digit in cell.
						matchCount++;
					} else {
					   // Candidate digit not in cell.	
					   if (FULL_COMBI_MATCH == partialCount) {
						   return false;
					   }
					}					
				} else {
					// Candidate digit not in combo. 
					// No match if cell contains digit, and we specify naked.
					if (naked && cellCandidates[candi] > 0) {
						return false;						
					}
				}
			}
			if (( FULL_COMBI_MATCH == partialCount) && (matchCount == combi.length)) {
				return true;
			}
			if ( matchCount >= partialCount) {
				return true;
			}
		}
		return false;
	}

	/** 
	 * Returns the locations of candidates exactly matching this row digit combo.
	 * Combo {12} matches [120000000]. If naked
	 * Combo {12} matches [123000000]. If not naked
	 * 
	 * @param combi array of zero based digits
	 * @param naked combo exact match required. 
	 * @param partialCount allows match of fewer than all combi digits. FULL_COMBI_MATCH requires
	 * 	all combi digits must match, but a partialCount will allow fewer to match.
	 * @return zero based array of rowCol
	 */
	public List<RowCol> candidateComboRowLocations( int rowi, int [] combi, boolean naked, int partialCount ) {
	   List<RowCol> locations = new ArrayList<>();
		for (int coli = 0; coli < COLS; coli++) {
			if (candidatesMatch( ROWCOL[rowi][coli], combi, naked, partialCount )) {
				locations.add(ROWCOL[rowi][coli]);
			}
		}
		return locations;
	}
	
	/** Returns the count of these digits as candidates in this row.
	 * For example combo {12} returns 2 for both [120000000] and [123000000]
	 * @param combi array of zero based digits
	 * @return zero based array of rowCol
	 */
	public int candidateComboRowCount( int rowi, int [] combi ) {
	   int count = 0;
	   for( int coli = 0; coli < COLS; coli++) {
		   int[] cellCandidates = candidates[rowi][coli]; // 9 digits long
		   for ( int candi = 0; candi < DIGITS; candi++) {
			   if ( cellCandidates[ candi ] > 0 && containsDigit( combi, candi ))
				   count++;
		   }
	   }
	   return count;
	}
	
	/** Returns the locations of candidates matching this col digit combo.
	 * 
	 * Combo {12} matches [120000000]. If naked
	 * Combo {12} matches [123000000]. If not naked
	 *
	 * For combo {12}, matches [120000000] and [020000000]. If naked. Weird, single candidate
	 * For combo {138}, matches [103000080] and [100000080]. If naked. Cool finds  {13} {18} {38} triple.
	 * 
	 * @param naked combo exact match required.
	 * @param partialCount allows match of fewer than all combi digits. FULL_COMBI_MATCH requires
	 * 	all combi digits must match, but a partialCount will allow fewer to match.
	 * @return zero based array of rowCol
	 */
	public List<RowCol> candidateComboColLocations(int coli, int[] combi, boolean naked, int partialCount) {
		List<RowCol> locations = new ArrayList<>();
		for (int rowi = 0; rowi < ROWS; rowi++) {
			if (candidatesMatch(ROWCOL[rowi][coli], combi, naked, partialCount)) {
				locations.add(ROWCOL[rowi][coli]);
			}
		}
		return locations;
	}

	/** Returns the count of these digits as candidates in this col.
	 * For example combo {12} returns 2 for both [120000000] and [123000000]
	 * @param combi array of zero based digits
	 * @return zero based array of rowCol
	 */
	public int candidateComboColCount( int coli, int [] combi ) {
	   int count = 0;
	   for( int rowi = 0; rowi < ROWS; rowi++) {
		   int [] cellCandidates = candidates[rowi][coli]; // 9 digits long
		   for ( int candi = 0; candi < DIGITS; candi++) {
			   if ( cellCandidates[ candi ] > 0 && containsDigit( combi, candi ))
				   count++;
		   }
	   }
	   return count;
	}

	/** Returns the locations of candidates matching this box digit combo.
	 * 
	 * Combo {12} matches [120000000]. If naked
	 * Combo {12} matches [123000000]. If not naked
	 * 
	 * @param naked combo exact match required. 
	 * @param partialCount allows match of fewer than all combi digits. FULL_COMBI_MATCH requires
	 * 	all combi digits must match, but a partialCount will allow fewer to match.
	 * @return zero based array of rowCol
	 */
	public List<RowCol> candidateComboBoxLocations(int boxi, int[] combi, boolean naked, int partialCount) {
		List<RowCol> locations = new ArrayList<>();
		RowCol[] locs = Board.getBoxRowCols(boxi);
		for (int loci = 0; loci < locs.length; loci++) {
			RowCol rowCol = locs[ loci ];
			if (candidatesMatch(rowCol, combi, naked, partialCount)) {
				locations.add(rowCol);
			}
		}
		return locations;
	}

	/** Returns the count of these digits as candidates in this box.
	 * For example combo {12} returns 2 for both [120000000] and [123000000]
	 * @param combi array of zero based digits
	 * @return zero based array of rowCol
	 */
	public int candidateComboBoxCount( int boxi, int [] combi ) {
       return candidateComboLocCount( combi, Board.getBoxRowCols(boxi) );
	}

	/** 
	 * Returns the count of these combo digits as candidates in these locations.
	 * For example combo {12} returns 4 for loc sets {12}{123}
	 * @param combi array of zero based digits
	 * @return zero based array of rowCol
	 */
	public int candidateComboLocCount( int [] combi, RowCol[] locs ) {
	   int count = 0;
	   for( int loci = 0; loci < locs.length; loci++) {
		   RowCol rowCol = locs[ loci ];
		   int[] cellCandidates = candidates[rowCol.row()][rowCol.col()]; // 9 digits long
		   for ( int candi = 0; candi < DIGITS; candi++) {
			   if ( cellCandidates[ candi ] > 0 && containsDigit( combi, candi ))
				   count++;
		   }
	   }
	   return count;
	}
	
	/** 
	 * Returns the count of these combo digits as candidates in these locations.
	 * For example combo {12} returns 4 for loc sets {12}{123}
	 * @param combi array of zero based digits
	 * @return zero based array of rowCol
	 */
	public int candidateComboLocCount( int [] combi, List<RowCol> locs ) {
	   int count = 0;
	   for( RowCol rowCol : locs) {
		   int [] cellCandidates = candidates[rowCol.row()][rowCol.col()]; // 9 digits long
		   for ( int candi = 0; candi < DIGITS; candi++) {
			   if ( cellCandidates[ candi ] > 0 && containsDigit( combi, candi ))
				   count++;
		   }
	   }
	   return count;
	}

	/** 
	 * Returns the unique digit count as candidates in these locations.
	 * For example returns 2 for combo {128} and loc sets {12}{123}
	 * @param combi array of zero based digits
	 * @return zero based array of rowCol
	 */
	public int digitLocCount(int[] combi, RowCol[] locs ) {
	   int count = 0;
	   boolean [] counted = new boolean [] {false,false,false,false,false,false,false,false,false};
	   for( int loci = 0; loci < locs.length; loci++) {
		   RowCol rowCol = locs[ loci ];
		   int [] cellCandidates = candidates[rowCol.row()][rowCol.col()]; // 9 digits long
		   for ( int candi = 0; candi < DIGITS; candi++) {
			   if (!counted[candi] && cellCandidates[ candi ] > 0 && containsDigit( combi, candi )) {
				   count++;
				   counted[candi] = true;
			   }
		   }
	   }
	   return count;
	}
	
	/** 
	 * Returns the unique digit count as candidates in these locations.
	 * For example returns 2 for combo {128} and loc sets {12}{123}
	 * @param combi array of zero based digits
	 * @return zero based array of rowCol
	 */
	public int digitLocCount( int[] combi, List<RowCol> locs ) {
	   int count = 0;
	   boolean [] counted = new boolean [] {false,false,false,false,false,false,false,false,false};
	   for( int loci = 0; loci < locs.size(); loci++) {
		   RowCol rowCol = locs.get(loci);
		   int [] cellCandidates = candidates[rowCol.row()][rowCol.col()]; // 9 digits long
		   for ( int candi = 0; candi < DIGITS; candi++) {
			   if (!counted[candi] && cellCandidates[ candi ] > 0 && containsDigit( combi, candi )) {
				   count++;
				   counted[candi] = true;
			   }
		   }
	   }
	   return count;
	}
	
	/** Returns the locations of candidates matching this digit combo.
	 * @param combi array of zero based digits
	 * @param partialCount allows match of fewer than all combi digits. FULL_COMBI_MATCH requires
	 * 	all combi digits must match, but a partialCount will allow fewer to match.
	 * @return
	 */
	public List<RowCol> candidateComboAllLocations(int[] combi, int partialCount) {
		List<RowCol> locations = new ArrayList<>();

		// Only go through rows
		for (int rowi = 0; rowi < ROWS; rowi++) {
			List<RowCol> rowFinds = candidateComboRowLocations(rowi, combi, NAKED, partialCount );
			locations.addAll(rowFinds);
		}
		return locations;
	}
	
	/**
	 * Returns whether the digit is in the array
	 * @param combi is a 0-based set of int, for example [0,1,...8]
	 * @param digit is a 0-based int
	 * @return
	 */
	public static boolean containsDigit( int[] combi, int digit ) {
		for( int i = 0; i < combi.length; i++) {
			if ( combi[ i ] == digit ) return true;
		}
		return false;
	}

	/** Tests whether multiple locations can be conjugate pairs for a color/forcing chain. 
	 *     - not the same location
	 *     - locations contain the digit candidate.
	 *     - locations have the same group size or ALL_COUNTS
	 *     - locations contain the same candidates
	 *     	    // 3,7, 3,7 is a conjugate even with other 3,7s not in this unit. (NakedPair should knock these others out.)
	 *     - no additional candidate locations in each unit 
	 *		    // 3,7, 3,9 is not a conjugate pair if other 3s in this unit. It is a 3 conjugate if these are the only 3s.
	 *	   TODO - Consider the discussion of weak and strong links at Thonky XCycles - https://www.thonky.com/sudoku/x-cycles
	 *		    Thonky considers both of these conjugates, but more candidates in the units make a weak link, zero makes a strong link
	 */
	public boolean isConjugate( int digit, int groupSize, RowCol firstRowCol, RowCol secondRowCol ) {
		if ( firstRowCol.equals( secondRowCol ))
			return false; // same rowCols		
		if ( !isCandidate(firstRowCol, digit) || !isCandidate(secondRowCol, digit ))
			return false;
		if ( ALL_COUNTS != groupSize &&
			 (groupSize != candidateCount(firstRowCol ) ||
			  groupSize != candidateCount(secondRowCol )))
			return false;

		// 3,7, 3,7 is a conjugate even with other 3,7s not in this unit. (NakedPair should knock these others out.)
		// 3,7, 3,9 is not a conjugate pair if other 3s in this unit.		
		int [] firstCandidates = getRemainingCandidates(firstRowCol);
		int [] secondCandidates = getRemainingCandidates(secondRowCol);
		if ( Arrays.equals(firstCandidates, secondCandidates))
			return true;
		
		if (firstRowCol.row() == secondRowCol.row()) {
			// sameRow
			// check that unit has none of these candidates in other positions.
			if ( 2 < candidateRowCount( firstRowCol.row(), digit))
			   return false;
		}
		if (firstRowCol.col() == secondRowCol.col()) {
			// sameCol
			// check that unit has none of these candidates in other positions.
			if ( 2 < candidateColCount( firstRowCol.col(), digit))
			   return false;
		}
		if (firstRowCol.box() == secondRowCol.box()) {
			// sameBox
			// check that unit has none of these candidates in other positions.
			if ( 2 < candidateBoxCount( firstRowCol.box(), digit))
			   return false;
		}
		return true;
	}
	
	/** Tests whether multiple locations can be conjugate pairs for a color/forcing chain. 
	 *     - not the same location
	 *     - locations contain the digit candidate.
	 *     - locations have the same group size or ALL_COUNTS
	 *     - locations contain the same candidates
	 *     	    // 3,7, 3,7 is a conjugate even with other 3,7s not in this unit. (NakedPair should knock these others out.)
	 *     - no additional candidate locations in each unit 
	 *		    // 3,7, 3,9 is not a conjugate pair if other 3s in this unit. It is a 3 conjugate if these are the only 3s.
	 */
	public List<RowCol> isConjugateExtraLocations( int digit, int groupSize, RowCol firstRowCol, RowCol secondRowCol ) {
		List<RowCol> locs = new LinkedList<RowCol>();
		if ( firstRowCol.equals( secondRowCol ))
			return locs; // same rowCols		
		if ( !isCandidate(firstRowCol, digit) || !isCandidate(secondRowCol, digit ))
			return locs;
		if ( ALL_COUNTS != groupSize &&
			 (groupSize != candidateCount(firstRowCol ) ||
			  groupSize != candidateCount(secondRowCol )))
			return locs;

		// 3,7, 3,7 is a conjugate even with other 3,7s not in this unit. (NakedPair should knock these others out.)
		// 3,7, 3,9 is not a conjugate pair if other 3s in this unit.		
		int [] firstCandidates = getRemainingCandidates(firstRowCol);
		int [] secondCandidates = getRemainingCandidates(secondRowCol);
		if ( Arrays.equals(firstCandidates, secondCandidates))
			return locs;
		
		if (firstRowCol.row() == secondRowCol.row()) {
			// sameRow
			// list unit candidates
			// in other positions.
			List<RowCol> rowLocs = candidateRowGroupLocs(firstRowCol.row(), digit, ALL_COUNTS);
			if ( rowLocs.size() > 2 ) {
				for ( RowCol rowCol : rowLocs) {
					if ( !rowCol.equals( firstRowCol ) && !rowCol.equals( secondRowCol )) {
					  locs.add( rowCol );
					}
				}					
			}
		}
		if (firstRowCol.col() == secondRowCol.col()) {
			// sameCol
			// list unit candidates in other positions.
			List<RowCol> colLocs = candidateColGroupLocs(firstRowCol.col(), digit, ALL_COUNTS);
			if ( colLocs.size() > 2 ) {
				for ( RowCol rowCol : colLocs) {
					if ( !rowCol.equals( firstRowCol ) && !rowCol.equals( secondRowCol )) {
					  locs.add( rowCol );
					}
				}					
			}
		}
		if (firstRowCol.box() == secondRowCol.box()) {
			// sameBox
			// list unit candidates in other positions.
			List<RowCol> boxLocs = candidateBoxGroupLocs(Board.getBox(firstRowCol), digit, ALL_COUNTS);
			if ( boxLocs.size() > 2 ) {
				for ( RowCol rowCol : boxLocs) {
					if ( !rowCol.equals( firstRowCol ) && !rowCol.equals( secondRowCol )) {
					  locs.add( rowCol );
					}
				}					
			}			
		}
		return locs;
	}

	/**
	 * Lists the differences between one candidates set and another.
	 * @return list of ChangeData (actions/digit/locations)
	 */
	public static List<ChangeData> changes( Candidates cFrom, Candidates cTo ) {
		List<ChangeData> locs = new LinkedList<>();
		if (( null == cFrom ) || ( null == cTo ))
			return locs;
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++ ) {
				for ( int digi = 0; digi < DIGITS; digi++ ) {
					// Short is negative digit for filled in, 0 for not candidate, digit for candidate
					// candidates = new short[ROWS][COLS][DIGITS];
					if (cTo.candidates[rowi][coli][digi] < 0) {
						if ( cFrom.candidates[rowi][coli][digi] >= 0 ) {
							locs.add(new ChangeData(digi+1, ROWCOL[rowi][coli], Action.OCCUPY, 1 ));
						}
					} else if (cTo.candidates[rowi][coli][digi] > 0) {
						if ( cFrom.candidates[rowi][coli][digi] == 0 ) {
							locs.add(new ChangeData(digi+1, ROWCOL[rowi][coli], Action.ADD, 1 ));
						} else if ( cFrom.candidates[rowi][coli][digi] < 0 ) {
							locs.add(new ChangeData(digi+1, ROWCOL[rowi][coli], Action.UNOCCUPY, 1 ));
						}
					} else { // cTo.candidates[rowi][coli][digi] == 0
						if ( cFrom.candidates[rowi][coli][digi] > 0 ) {
							locs.add(new ChangeData(digi+1, ROWCOL[rowi][coli], Action.REMOVE, 1 ));
						} else if ( cFrom.candidates[rowi][coli][digi] < 0 ) {
							locs.add(new ChangeData(digi+1, ROWCOL[rowi][coli], Action.UNOCCUPY, 1 ));
						} 						
					}
				}			
			}			
		}
		return locs;
	}
	
	/**
	 * Transform from one candidate to another with a list of changes.
	 * @return new Candidates with changes
	 */
	public static Candidates changes( Candidates cFrom, List<ChangeData> changes ) {
		Candidates cTo = new Candidates( cFrom );
		if (null == changes) return cTo;
		for ( int ci=0; ci < changes.size(); ci++) {
			ChangeData change = changes.get(ci);
			switch( change.action ) {
			case OCCUPY: {
				cTo.setOccupied( change.rowCol, change.digit); break;
			}
			case UNOCCUPY: {
				cTo.candidates[ change.rowCol.row() ][ change.rowCol.col() ][ change.digit - 1 ] = NOT_OCCUPIED; break;
			}
			case ADD: {
				cTo.addCandidate( change.rowCol, change.digit ); break;
			}
			case REMOVE: {
				cTo.removeCandidate( change.rowCol, change.digit ); break;
			}
			} // switch
		}
		return cTo;
	}

	@Override
	public int compareTo(Candidates that) {
		if (null == that) return 1;
		if ((null != candidates) && (that.candidates == null)) return 1;
		if ((null == candidates) && (that.candidates != null)) return -1;

		for( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++) {
				if ( null != candidates[ rowi ][ coli ] && null == that.candidates[ rowi ][ coli ]) return 1;
				if ( null == candidates[ rowi ][ coli ] && null != that.candidates[ rowi ][ coli ]) return -1;
				RowCol rowCol = ROWCOL[rowi][coli];
				if ( this.getOccupied(rowCol) > 0 && that.getOccupied(rowCol) == 0 ) return 1;
				if ( this.getOccupied(rowCol) == 0 && that.getOccupied(rowCol) > 0 ) return -1;
				if ( this.getOccupied(rowCol) > 0 && that.getOccupied(rowCol) > 0 )
				   return this.getOccupied(rowCol) - that.getOccupied(rowCol);
			
				// Same length of candidates
				for ( int i = 0; i < candidates[ rowi ][ coli ].length; i++) {
					if ( candidates[ rowi ][ coli ][ i ] > that.candidates[ rowi ][ coli ][ i ]) return 1;
					if ( candidates[ rowi ][ coli ][ i ] < that.candidates[ rowi ][ coli ][ i ]) return -1;
				}
			}
		}		
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
        // Compare with self   
        if (obj == this) return true; 
  
        // Compare with class type
        if (!(obj instanceof Candidates)) return false; 

        // Cast to same type  
        Candidates that = (Candidates) obj; 
		return 0 == this.compareTo( that );
	}
	
	@Override
	public String toString() {
	   if ( null == candidates) return "null";
	   
	   StringBuilder sb = new StringBuilder();
       for( int rowi = 0; rowi < ROWS; rowi++ ) {
    	   if ( rowi > 0 ) sb.append( "\n" );
    	   for ( int coli = 0; coli < COLS; coli++) {
    		   sb.append( getCandidatesString(ROWCOL[rowi][coli]));
    	   }
    	   
       }
	   return sb.toString();
	}

	public String toStringCompact() {
		if (null == candidates)
			return "null";

		StringBuilder sb = new StringBuilder();
		for (int rowi = 0; rowi < ROWS; rowi++) {
			if (rowi > 0)
				sb.append("\n");
			for (int coli = 0; coli < COLS; coli++) {
				sb.append(getCandidatesStringCompact(ROWCOL[rowi][coli]));
			}

		}
		return sb.toString();
	}
		
	/** A string version that formats the boxes of 3 cells so they align visually. */
	public String toStringBoxed() {
		   if ( null == candidates) return "null";

		   // First figure the longest box col 0 and col 1
		   int [] longestBox = new int[] { 0, 0 };
	       for( int rowi = 0; rowi < ROWS; rowi++ ) {
	    	   int boxLength = 0;
	    	   for ( int coli = 0; coli < COLS - 3; coli++) {
	    		   if ( coli % 3 == 0 )
	    			   boxLength = 0;
	    		   String rcCandidates = getCandidatesStringCompact(ROWCOL[rowi][coli] );
	    		   boxLength += rcCandidates.length();
	    		   if ( boxLength > longestBox[coli / 3] ) {
	    			   longestBox[coli / 3] = boxLength;
	    		   }	    		   
	    	   }	    	   
	       }
	       // Add gutter space
	       longestBox[ 0 ] += 1;
	       longestBox[ 1 ] += longestBox[ 0 ] + 1;
		   
		   StringBuilder sb = new StringBuilder();
	       for( int rowi = 0; rowi < ROWS; rowi++ ) {
	    	   if ( rowi > 0 ) sb.append( "\n" );
	    	   int boxLength = 0;
	    	   for ( int coli = 0; coli < COLS; coli++) {
	    		   String rcCandidates = getCandidatesStringCompact(ROWCOL[rowi][coli] );
	    		   sb.append( rcCandidates );
	    		   boxLength += rcCandidates.length();
		    	   if ( coli % 3 == 2  && coli / 3 < 2) {
		    		   while ( boxLength < longestBox[ coli / 3 ] ) {
		    			   sb.append( " " ); // add box col space.
		    			   boxLength++;
		    		   }
		    	   }	    		   
	    	   }	    	   
	    	   if ( rowi % 3 == 2 && rowi / 3 < 2 ) sb.append( "\n" ); // add box row space.
	       }
		   return sb.toString();
		}
		
	/** Returns a compact list of candidates. 
	 * Like an Arrays.toString, but less space. */
	public String getCandidatesString( RowCol rowCol ) {
		StringBuilder compact = new StringBuilder();
		compact.append("{");
		for (int digi = 0; digi < DIGITS; digi++) {
			if (digi > 0) compact.append(",");
			compact.append(candidates[rowCol.row()][rowCol.col()][digi]);
		}
		compact.append("}");
		return compact.toString();
	}

	/** Returns a compact list of candidates. Ignores non-candidates. 
	 * For example, candidates {0,1,-2} returns {1,-2}.
	 * Like an Arrays.toString, but 0 entries are ignored. */
	public String getCandidatesStringCompact( RowCol rowCol ) {
 	   StringBuilder compact = new StringBuilder();
 	   compact.append( "{" );
 	   for ( int digi = 0; digi < DIGITS; digi++) {
 		   // if (( digi > 0 ) && (0 != candidates[rowi][coli][digi - 1]))
 		   // compact.append( ",");
 		   if ( 0 != candidates[rowCol.row()][rowCol.col()][ digi ]) {
		   		compact.append( candidates[rowCol.row()][rowCol.col()][ digi ]);
 		   }
 	   }
 	   compact.append( "}" );
       return compact.toString();    		   
	}
}