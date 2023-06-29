package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static info.danbecker.ss.Board.NOT_FOUND;
import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Utils.*;
import static java.lang.String.format;

/**
 * DoublePairs occurs when a board has
 * a candidate digit twice in a rowCol and that rowCol is repeated on a different rowCol.
 * The candidate digits should be the only one in the cell.
 * If these form a rectangle, the other digits inside or out can be removed.
 * (Notice one rowCol direction will be a Candidate line, the other rowCol direction will be a Double Pair line.)
 * <p>
 * Info based on clues given at
 * https://www.sudokuoftheday.com/techniques/double-pairs
 * <p>
 * Note that https://www.thonky.com/sudoku/unique-rectangle says
 * the corners of the double pair must be in two boxes, not four.
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class DoublePairs implements FindUpdateRule {
	
	@Override
	// Location int [] index map
	// digit plus four rowCols A, B, C, D
	// digit at index 0 
	// first pair AB rowCols at indexes 1,2 and 3,4
	// second pair CD rowCols at indexes 5,6 and 7,8
	public int update(Board board, Board solution, Candidates candidates, List<int[]> encs) {
		int updates = 0;
		if ( null == encs) return updates;
		for ( int enci = 0; enci < encs.size(); enci++ ) {
			// Just correct 1 location (which might update multiple candidates.)
			int[] enc = encs.get(enci);
			int digit = enc[ 0 ];
			int[] zbDigit = { digit - 1 };
			Unit unit  = Unit.values()[enc[ 1 ] ];
			Unit otherUnit  = (unit == Unit.ROW) ? Unit.COL: Unit.ROW;
			RowCol ul = ROWCOL[enc[2]][enc[3]];
			RowCol ur = ROWCOL[enc[4]][enc[5]];
			RowCol ll = ROWCOL[enc[6]][enc[7]];
			RowCol lr = ROWCOL[enc[8]][enc[9]];
			// System.out.println( ruleName() + " update enc=" + encodingToString( enc ));

			switch( unit ) {
				case ROW: {
					List<RowCol> rowCol1OtherLocs = candidates.findUnitDigitsNotInLocs( otherUnit, ul.col(), zbDigit, Arrays.asList( ul, ll ));
					checkRemove( ruleName(), solution, candidates, zbDigit, rowCol1OtherLocs );
					List<RowCol> rowCol2OtherLocs = candidates.findUnitDigitsNotInLocs( otherUnit, lr.col(), zbDigit, Arrays.asList( ur, lr ));
					checkRemove( ruleName(), solution, candidates, zbDigit, rowCol2OtherLocs );

					int count = candidates.removeColCandidatesNotIn(digit, ul.col(), new RowCol[] {ul, ll});
					count += candidates.removeColCandidatesNotIn(digit, lr.col(), new RowCol[] {ur, lr});
					updates += count;

					System.out.printf( "%s removed digit %d of double pair %s,%s,%s,%s in %ss at %s%s, count %d%n",
							ruleName(), digit, ul, ur, ll, lr, otherUnit,
							RowCol.toString(rowCol1OtherLocs), RowCol.toString(rowCol2OtherLocs), count);
					break;
				}
				case COL: {
					// Strange naming of ul,ur,ll,lr for columns
					List<RowCol> colRow1OtherLocs = candidates.findUnitDigitsNotInLocs( otherUnit, ul.row(), zbDigit, Arrays.asList( ul, ll ));
					checkRemove( ruleName(), solution, candidates, zbDigit, colRow1OtherLocs );
					List<RowCol> colRow2OtherLocs = candidates.findUnitDigitsNotInLocs( otherUnit, lr.row(), zbDigit, Arrays.asList( ur, lr ));
					checkRemove( ruleName(), solution, candidates, zbDigit, colRow2OtherLocs );

					int count = candidates.removeRowCandidatesNotIn(digit, ul.row(), new RowCol[] {ul, ll});
					count += candidates.removeRowCandidatesNotIn(digit, lr.row(), new RowCol[] {ur, lr});
					updates += count;

					System.out.printf( "%s removed digit %d of double pair %s,%s,%s,%s in %ss at %s%s, count %d%n",
							ruleName(), digit, ul, ur, ll, lr, otherUnit,
							RowCol.toString(colRow1OtherLocs), RowCol.toString(colRow2OtherLocs), count);
					break;
				}
			}
		}
		return updates;
	}

	public static void checkRemove( String ruleName, Board solution, Candidates candidates, int[] zbDigits, List<RowCol> locs ) {
		if ( null == solution ) return;
		// Test if digit candidate removal, removes a solution digit
		for (int loci = 0; loci < locs.size(); loci++) {
			RowCol loc = locs.get( loci );
			for (int digi = 0; digi < zbDigits.length; digi++) {
				int digit = zbDigits[digi] + 1;
				int cellSolution = solution.get(loc);
				if (cellSolution == digit) {
					System.out.println("Candidates=\n" + candidates.toStringBoxed());
					throw new IllegalArgumentException(format("Rule %s wants to remove solution digit %d at loc %s.%n",
							ruleName, cellSolution, loc));
				}
			}
		}

	}

	/**
     * a candidate digit twice in a rowCol and that rowCol is repeated on a different rowCol.
	 * can knock out candidates in other boxes in the same row/col
	 * 
	 * Search for only two same candidates in each box,
  	 * see if row or col is the same, 
     * if row match, see if other candidates exist on same row outside of box
	 * if col match, see if other candidates exist on same col outside of box
	 */
	@Override
	public List<int[]> find(Board board, Candidates candidates) {
		ArrayList<int[]> encs = new ArrayList<>();
		for (int digi = 1; digi <= DIGITS; digi++) {
			if ( !board.digitCompleted( digi )) {
				// Refactored
				// If there are two rows with count 2, AND the cols match, knock out extra col candidates
				// If there are two cols with count 2, AND the rows match, knock out extra row candidates
				int[][] digitCounts = candidates.candidateUnitCounts( digi ); // [unit][uniti]
				// Unit unit = Unit.ROW; {
				for ( Unit unit : Unit.values()) {
					if ( Unit.BOX != unit ) {
						Unit otherUnit = (unit == Unit.ROW) ? Unit.COL : Unit.ROW;
						int firstUniti = NOT_FOUND;
						int secondUniti = NOT_FOUND;
						for ( int uniti = 0; uniti < UNITS; uniti++) {
							if ( 2 == digitCounts[unit.ordinal()][uniti] ) {
								if ( NOT_FOUND == firstUniti ) {
									// Warning, might not find pairs where first unit is non-matched row X
									// and the matched pairs Y and Z come later. Must validate.
									firstUniti = uniti;
								} else if (( NOT_FOUND == secondUniti ) && nonUnitMatch(candidates, digi, unit, firstUniti, uniti, otherUnit )) {
									secondUniti = uniti;
								}
							} // a pair
						} // for each uniti

						if ( NOT_FOUND != firstUniti && NOT_FOUND != secondUniti ) { // first units match, second units match
							// We have a double pair. Check other-unit counts > 2, (excess candidates.
							List<RowCol> firstLocs = candidates.getUnitDigitLocs( unit, firstUniti, digi );
							List<RowCol> secondLocs = candidates.getUnitDigitLocs( unit, secondUniti, digi );
							List<RowCol> moreDigitsFirst = candidates.getUnitDigitLocs( otherUnit, firstLocs.get(0).unitIndex( otherUnit ), digi );
							List<RowCol> moreDigitsSecond = candidates.getUnitDigitLocs( otherUnit, firstLocs.get(1).unitIndex( otherUnit ), digi );
							// We have a box. Check for more candidates on other unit
							if ( 2 < moreDigitsFirst.size() || 2 < moreDigitsSecond.size()) {
								int [] enc = new int[] { digi,unit.ordinal(),
										firstLocs.get(0).row(),firstLocs.get(0).col(),firstLocs.get(1).row(),firstLocs.get(1).col(),
										secondLocs.get(0).row(),secondLocs.get(0).col(),secondLocs.get(1).row(),secondLocs.get(1).col()};

								addUnique( encs, enc );
								// 	System.out.printf( "Rule %s added enc=%s%n" , ruleName(), encodingToString( enc ));
								// Found two double pair rows
								// System.out.printf( "Rule %s found digit %d via %s pairs at %s%s%s%s: counts row1 %d, row2 %d, col1 %d, col2 %d%n" ,
								//		ruleName(), digi, unit,
								//		firstLocs.get(0), firstLocs.get(1),
								//		secondLocs.get(0), secondLocs.get(1),
								//		candidates.getRowCount(firstLocs.get(0).row(), digi), 	candidates.getRowCount(secondLocs.get(0).row(), digi ),
								//		candidates.getColCount(secondLocs.get(0).col(), digi), candidates.getColCount(secondLocs.get(1).col(), digi));
 							}  // excess candidates
						} // first and second units with matching indexes
					} // not unit BOX
				} // unit values

			} // digit not complete
		} // each digit
		return encs;
	}

	/** We have two units with a count of 2 on the units.
	 * See if the other unit matches indexes on the other unit
	 * For instance, if digit 1 has 2 counts on rows 2 and 6,
	 * check that the columns match on the 2 columns
	 * @param candidates
	 * @param digit
	 * @param unit
	 * @param firstUniti
	 * @param secondUniti
	 * @param otherUnit
	 * @return whether the two sets of row/cols share the same col/rows
	 */
	public static boolean nonUnitMatch( Candidates candidates, int digit, Unit unit, int firstUniti, int secondUniti, Unit otherUnit ) {
		List<RowCol> firstLocs = candidates.getUnitDigitLocs( unit, firstUniti, digit );
		List<RowCol> secondLocs = candidates.getUnitDigitLocs( unit, secondUniti, digit );

		int firstMatchi = RowCol.unitMatch(otherUnit, firstLocs.get(0), secondLocs.get(0));
		int secondMatchi = RowCol.unitMatch(otherUnit, firstLocs.get(1), secondLocs.get(1));
		// Other units match abnd different?
		if ( NOT_FOUND != firstMatchi && NOT_FOUND != secondMatchi && firstUniti != secondUniti) {
			return true;
		}
		return false;
	}

	// Encoding int [] index map
	// digit plus four rowCols A, B, C, D
	// digit at index 0 
	// digit at index 0
	// first pair AB rowCols at indexes 1,2 and 3,4
	// second pair CD rowCols at indexes 5,6 and 7,8
	// Because double pairs form a box, there should be a match of
	//    - first pair rows, (A row == B row)
	//    - second pair rows (C row == D row)
	//    - first element cols (A col == C col)
	//    - second element cols (B col == D col)
	@Override
	public String encodingToString( int[] enc) {
		if ( null == enc) return "null";
		if ( 10 != enc.length) return "length of " + enc.length + "!= 10";
		
		String error = "";
		int digit = enc[0];
		Unit unit = Unit.values()[enc[1]];
		RowCol ul = ROWCOL[enc[2]][enc[3]];
		RowCol ur = ROWCOL[enc[4]][enc[5]];
		RowCol ll = ROWCOL[enc[6]][enc[7]];
		RowCol lr = ROWCOL[enc[8]][enc[9]];
		// Validate
		switch ( unit ) {
			case ROW: {
				if ( ul.row() != ur.row() ) error += " upper row mismatch";
				if ( ll.row() != lr.row() ) error += " lower row mismatch";
				if ( ul.col() != ll.col() ) error += " left col mismatch";
				if ( ur.col() != lr.col() ) error += " right col mismatch";
				break;
			}
			case COL: {
				if ( ul.col() != ur.col() ) error += " upper col mismatch";
				if ( ll.col() != lr.col() ) error += " lower col mismatch";
				if ( ul.row() != ll.row() ) error += " left row mismatch";
				if ( ur.row() != lr.row() ) error += " right row mismatch";
				break;
			}
			default: throw new IllegalArgumentException( "provided unit value index of " + enc[1]);
		}

		return format( "digit %d %s pairs at [%d,%d],[%d,%d] and [%d,%d],[%d,%d]%s" ,
			 digit,unit,enc[2],enc[3],enc[4],enc[5],enc[6],enc[7],enc[8],enc[9], error);
	}
	
	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}