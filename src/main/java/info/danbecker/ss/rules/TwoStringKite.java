package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;

import java.util.ArrayList;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Utils.*;
import static java.lang.String.format;

/**
 * Two-string Kite
 * From https://hodoku.sourceforge.net/en/tech_sdp.php#t2sk
 * Find a row and a column that have only two candidates left (the "strings").
 * One candidate from the row and one candidate from the column have to be in the same block.
 * The candidate that sees the two other cells can be eliminated.
 * <p>
 * The dual string kite is not detected with this rule, but running
 * the rule twice will uncover the second pair of strings and candidate.
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class TwoStringKite implements UpdateCandidatesRule {
	public TwoStringKite() {
	}

	@Override
	public int updateCandidates(Board board, Board solution, Candidates candidates, List<int[]> locations) {
		int updates = 0;
		if ( null == locations) return updates;
		if (locations.size() > 0) {
			// Just act on first find
			int [] enc = locations.get(0);
			// Decode information
			int digit = enc[0];
			RowCol[] rowCols = new RowCol[ enc.length - 1];
			for( int loci = 1; loci < enc.length; loci++) {
				int[] rowCol = comboToInts( enc[ loci ] ); // converts 1-based to 0-based
				rowCols[ loci - 1] = ROWCOL[rowCol[0]][rowCol[1]];
			}
			// Just correct first item
			for ( int loci = 0; loci < rowCols.length - 4; loci++ ) {
				// Skip over first four rowCols, base and roof of skyscraper.
				RowCol rowCol = rowCols[loci + 4];
				if ( candidates.isCandidate( rowCol, digit )) {
					// Validation
					if ( null != solution ) {
						int cellStatus = solution.get(rowCol);
						if ( cellStatus == digit ) {
							throw new IllegalArgumentException( format("Rule %s would like to remove solution digit %d at loc %s.",
									ruleName(), digit, rowCol));
						}
					}
					// It validates, go ahead
					if (candidates.removeCandidate(rowCol, digit)) {
						updates++;
					}
				}
			}
			System.out.println( format( "%s removed digit %d from %d location %s",
					ruleName(), digit, updates,
					RowCol.toString( rowCols )));
		}
		return updates;
	}

	@Override
	public List<int[]> locations(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		List<int[]> locs = new ArrayList<>();
		// Generate combinations of n elements (9 digits), r at a time.
		// Note that these combos are 0 based
		for( int digit = 1; digit <= DIGITS; digit++) {
			if (!board.digitCompleted(digit)) {
				// Find rows with only two candidates
				int[][] unitCounts = candidates.candidateUnitCounts( digit );
				for ( int rowi = 0; rowi < ROWS; rowi++) {
					if ( 2 == unitCounts[ Unit.ROW.ordinal() ][ rowi ]) {
						// Check each of two locations for a matching base unit
						List<RowCol> rowLocs = candidates.getRowDigitLocs( rowi, digit );
						RowCol [] hands = new RowCol[2];
						for ( int rloci = 0; rloci < rowLocs.size(); rloci++ ) {
							RowCol rowLoc = rowLocs.get( rloci );
							for ( int coli = 0; coli < COLS; coli++ ) {
								if ( 2 == unitCounts[ Unit.COL.ordinal() ][ coli ]) {
									RowCol [] strings = new RowCol[2];
									List<RowCol> colLocs = candidates.getColDigitLocs( coli, digit );
									for ( int cloci = 0; cloci < colLocs.size(); cloci++ ) {
										RowCol colLoc = colLocs.get( cloci );
										// Check that the rowLoc and colLoc share a box.
										if ( !rowLoc.equals(colLoc) && rowLoc.box() == colLoc.box() ) {
											hands[0] = rowLoc;
											hands[1] = colLoc;
											strings[0] = (0 == rloci) ? rowLocs.get(1) : rowLocs.get(0); // other rowLoc
											strings[1] = (0 == cloci) ? colLocs.get(1) : colLocs.get(0); // other colLoc
										}
									}
									if ( null != hands[0] && null != hands[1] && null != strings[0] && null != strings[1] ) {
										// System.out.println(format("Digit %d has hands at %s and strings at %s",
										// 		digit, RowCol.toString( hands ), RowCol.toString(strings ) ));
										// Now test if there are implications (candidates that see the strings)
										List<RowCol> candLocs = candidates.digitLocs(digit);
										for (RowCol rowCol : candLocs) {
											if ( !rowCol.equals(hands[0]) && !rowCol.equals(hands[1]) &&
												!rowCol.equals(strings[0]) && !rowCol.equals(strings[1]) ) {
												// Not one of the hands or strings locations
												if ( rowCol.row() == strings[0].row() && rowCol.col() == strings[1].col() ) {
													// System.out.println( format("Digit %s cand %s matches row and col of %s and %s",
													// 		digit, rowCol, strings[0], strings[1] ));
													locs.add(encodeLocation(digit, hands, strings, rowCol ));
												} else if ( rowCol.col() == strings[0].col() && rowCol.row() == strings[1].row() ) {
													// System.out.println( format("Digit %s cand %s matches col and row of %s and %s",
													// 		digit, rowCol, strings[0], strings[1] ));
													locs.add(encodeLocation(digit, hands, strings, rowCol ));
												}
											}

										}
									}
								}
							}
						}
					}
				}
				// Check that the four elements have matching row cols and one shares a box.

			} // digit not complete
		}
		return locs;
	}

	/**
	 * Encoded as such
	 * 0 - ones based digit
	 * 1,2 - rowCol 1 and 2 (base/hands of kite strings, combo encoded (rc 1,5 => 27))
	 * 3,4 - rowCol 3 and 4 (roof/ends of kit strings)
	 * 5 - rowCols that can see both ends (always one location for TwoStringKite)
	 * <p>
	 * FYI, Hodoku shows this as rule: digit in base (connected by roof) => loc<>digit
	 * "2-String Kite: 5 in r2c7,r8c4 (connected by r8c9,r9c7) => r2c4<>5"
	 */
	public static int [] encodeLocation( int digit, RowCol[] hands, RowCol[] strings, RowCol loc) {
		if ( null == hands || 2 != hands.length )
			throw new IllegalArgumentException( "hands=" + RowCol.toString(hands) );
		if ( null == hands[0] || null == hands[1] )
			throw new IllegalArgumentException( "hands0=" + hands[0] + ", hands1=" + hands[1] );
		if ( null == strings || 2 != strings.length )
			throw new IllegalArgumentException( "strings=" + RowCol.toString(strings) );
		if ( null == strings[0] || null == strings[1] )
			throw new IllegalArgumentException( "strings0=" + strings[0] + ", strings=" + strings[1] );
		if ( null == loc )
			throw new IllegalArgumentException( "locs null" );

		int [] encoded = new int[6 ];
		encoded[0] = digit;
		encoded[1] = intsToCombo( new int[]{ hands[0].row(), hands[0].col() } );
		encoded[2] = intsToCombo( new int[]{ hands[1].row(), hands[1].col() } );
		encoded[3] = intsToCombo( new int[]{ strings[0].row(), strings[0].col() } );
		encoded[4] = intsToCombo( new int[]{ strings[1].row(), strings[1].col() } );
		encoded[5] = intsToCombo( new int[]{ loc.row(), loc.col() } );
		return encoded;
	}

	/**
	 * @param location one-based digit and combo encoded locations
	 * @return String version of encoded locations
	 */
	public static String locationToString( int [] location ) {
		if ( null == location ) return "null";
		int digit = location[0];
		RowCol[] rowCols = new RowCol[ location.length - 1];
		for( int loci = 1; loci < location.length; loci++) {
			int[] rowCol = comboToInts( location[ loci ] ); // converts one-based to zero-based
			rowCols[ loci - 1] = ROWCOL[rowCol[0]][rowCol[1]];
		}
		// RowCol[] base = new RowCol[]{
		// 		ROWCOL[rowCols[0].row()][rowCols[0].col()],
		// 		ROWCOL[rowCols[1].row()][rowCols[1].col()] };
		return format("digit %d has kite at (hands,strings,loc) %s",
			digit, RowCol.toString(rowCols));
	}

	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}