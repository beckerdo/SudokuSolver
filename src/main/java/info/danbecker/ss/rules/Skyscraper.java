package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Utils.*;
import static java.lang.String.format;

/**
 * Skyscraper
 * A Skyscaper is a form of Turbot fish (chain). When a single digit
 * has two rows (or columns) with only two candidates, then any
 * other candidates of that digit that can see both roofs can be deleted.
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class Skyscraper implements FindUpdateRule {
	@Override
	public int update(Board board, Board solution, Candidates candidates, List<int[]> encs) {
		int updates = 0;
		if ( null == encs) return updates;
		if (encs.size() > 0) {
			// Just act on first find
			int [] enc = encs.get(0);
			// Decode information
			int digit = enc[0];
			RowCol[] rowCols = new RowCol[ enc.length - 1];
			for( int loci = 1; loci < enc.length; loci++) {
				int[] rowCol = onebasedComboToZeroBasedInts( enc[ loci ] ); // converts 1-based to 0-based
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
			System.out.printf( "%s removed digit %d from %d locations %s%n",
					ruleName(), digit, updates,
					RowCol.toString( rowCols ));
		}
		return updates;
	}

	public static RowCol[] getBase( Unit unit, Candidates candidates, int digit) {
		RowCol[] base = null;
		for (int uniti = 0; uniti < ROWS && null == base; uniti++) {
			List<RowCol> rowCols = switch ( unit ) {
				case ROW -> candidates.candidateRowGroupLocs( uniti, digit, 2 );
				case COL -> candidates.candidateColGroupLocs( uniti, digit, 2 );
				default -> throw new IllegalArgumentException( "method does not handle unit " + unit );
			};
			if ( 2 == rowCols.size() )  {
				base = new RowCol[2];
				base[ 0 ] = rowCols.get(0);
				base[ 1 ] = rowCols.get(1);
			}
		}
		return base;
	}
	public static RowCol[] getRoof( Unit unit, Candidates candidates, int digit,  RowCol[] base ) {
		RowCol[] roof = null;
		// Check for one roof per base
		if ( Unit.ROW == unit ) {
			if (2 == candidates.getColCount(base[0].col(), digit) &&
					2 == candidates.getColCount(base[1].col(), digit)) {
				// Two bases have one roof each
				// Put roofs on top of bases.
				roof = new RowCol[2];
				int coli = base[0].col();
				int[] rows = candidates.candidateColLocations(coli, digit);
				if (2 == rows.length) {
					if (base[0].equals(ROWCOL[rows[0]][coli])) roof[0] = ROWCOL[rows[1]][coli];
					else if (base[0].equals(ROWCOL[rows[1]][coli])) roof[0] = ROWCOL[rows[0]][coli];
					else throw new IllegalStateException(format("digit %d base should have a roof at coli %d, rows %s",
								digit, coli, Arrays.toString(rows)));
				}
				coli = base[1].col();
				rows = candidates.candidateColLocations(coli, digit);
				if (2 == rows.length) {
					if (base[1].equals(ROWCOL[rows[0]][coli])) roof[1] = ROWCOL[rows[1]][coli];
					else if (base[1].equals(ROWCOL[rows[1]][coli])) roof[1] = ROWCOL[rows[0]][coli];
					else throw new IllegalStateException(format("digit %d base should have a roof at coli %d, rows %s",
								digit, coli, Arrays.toString(rows)));
				}
			}
		} else if ( Unit.COL == unit ) {
			if (2 == candidates.getRowCount(base[0].row(), digit) &&
					2 == candidates.getRowCount(base[1].row(), digit)) {
				// Two bases have one roof each
				// Put roofs on top of bases.
				roof = new RowCol[2];
				int rowi = base[0].row();
				int[] cols = candidates.candidateRowLocations(rowi, digit);
				if (2 == cols.length) {
					if (base[0].equals(ROWCOL[rowi][cols[0]])) roof[0] = ROWCOL[rowi][cols[1]];
					else if (base[0].equals(ROWCOL[rowi][cols[1]])) roof[0] = ROWCOL[rowi][cols[0]];
					else throw new IllegalStateException(format("digit %d base should have a roof at row %d, cols %s",
								digit, rowi, Arrays.toString(cols)));
				}
				rowi = base[1].row();
				cols = candidates.candidateRowLocations(rowi, digit);
				if (2 == cols.length) {
					if (base[1].equals(ROWCOL[rowi][cols[0]])) roof[1] = ROWCOL[rowi][cols[1]];
					else if (base[1].equals(ROWCOL[rowi][cols[1]])) roof[1] = ROWCOL[rowi][cols[0]];
					else throw new IllegalStateException(format("digit %d base should have a roof at row %d, cols %s",
								digit, rowi, Arrays.toString(cols)));
				}
			}
		}

		return roof;
	}

	/**
	 * A Skyscaper is a form of Turbot fish (chain). When a single digit
	 * has two rows (or columns) with only two candidates, then any
	 * other candidates of that digit that can see both roofs can be deleted.
	 */
	@Override
	public List<int[]> find(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		List<int[]> locs = new ArrayList<>();
		// Generate combinations of n elements (9 digits), r at a time.
		// Note that these combos are 0 based
		for( int digit = 1; digit <= DIGITS; digit++) {
			if (!board.digitCompleted(digit)) {
				for ( Unit unit : new Unit[]{ Unit.ROW, Unit.COL } ) {
				// Search for base
				RowCol[] base = getBase( unit, candidates, digit );
				if ( null != base ) {
					RowCol[] roof = getRoof( unit, candidates, digit, base );

					// Search for other rowCols with same digit that can see both.
					if ((null != base && 2 == base.length && null != base[0] && null != base[1]) &&
						(null != roof && 2 == roof.length && null != roof[0] && null != roof[1])) {
						// Have a valid base and roof. Check for other locations that can see both roofs
						List<RowCol> removeMe = new ArrayList<>();
						List<RowCol> candLocs = candidates.digitLocs(digit);
						for (RowCol rowCol : candLocs) {
							// Check if candidate location is part of the skyscraper
							if (!base[0].equals(rowCol) && !base[1].equals(rowCol) &&
									!roof[0].equals(rowCol) && !roof[1].equals(rowCol)) {
								// Not part of the skyscraper
								switch ( unit ) {
									case ROW -> {
										if ((rowCol.row() == roof[0].row() && rowCol.box() == roof[1].box()) ||
												(rowCol.box() == roof[0].box() && rowCol.row() == roof[1].row())) {
											removeMe.add( rowCol );
										}
									}
									case COL -> {
										if ((rowCol.col() == roof[0].col() && rowCol.box() == roof[1].box()) ||
												(rowCol.box() == roof[0].box() && rowCol.col() == roof[1].col())) {
											removeMe.add( rowCol );
										}
									}
								}
							}
						}
						if ( 0 < removeMe.size() ) {
							System.out.printf("%s found a digit %d %s base at %s and roof at %s. These locs see both: %s%n",
								ruleName(), digit, unit,
								RowCol.toString(base), RowCol.toString(roof), RowCol.toString( removeMe ));
							locs.add( encodeLocation( digit, base, roof, removeMe ));
						}
					}
				} // null != base
				} // Unit in Unit.ROW, Unit.COL
			} // digit not complete
		}
		return locs;
	}

	/**
	 * Encoded as such
	 * 0 - ones based digit
	 * 1,2 - rowCol 1 and 2 (base of skyscraper, combo encoded (rc 1,5 => 27))
	 * 3,4 - rowCol 3 and 4 (roof of skyscraper)
	 * (5)+ - rowCols that can see both roofs
	 */
	public static int [] encodeLocation( int digit, RowCol[] base, RowCol[] roof, List<RowCol> locs) {
		if ( null == base || 2 != base.length )
			throw new IllegalArgumentException( "base=" + base );
		if ( null == base[0] || null == base[1] )
			throw new IllegalArgumentException( "base0=" + base[0] + ", base1=" + base[1] );
		if ( null == roof || 2 != roof.length )
			throw new IllegalArgumentException( "roof=" + roof );
		if ( null == roof[0] || null == roof[1] )
			throw new IllegalArgumentException( "roof=" + roof[0] + ", roof=" + roof[1] );
		if ( null == locs || 0 == locs.size() )
			throw new IllegalArgumentException( "locs=" + locs );

		int [] encoded = new int[5 + locs.size() ];
		encoded[0] = digit;
		encoded[1] = zerobasedIntsToOnebasedCombo( new int[]{ base[0].row(), base[0].col() } );
		encoded[2] = zerobasedIntsToOnebasedCombo( new int[]{ base[1].row(), base[1].col() } );
		encoded[3] = zerobasedIntsToOnebasedCombo( new int[]{ roof[0].row(), roof[0].col() } );
		encoded[4] = zerobasedIntsToOnebasedCombo( new int[]{ roof[1].row(), roof[1].col() } );
		for( int loci = 0; loci < locs.size(); loci++) {
			RowCol rowCol = locs.get( loci );
			encoded[ loci+5 ] = zerobasedIntsToOnebasedCombo( new int[] { rowCol.row(), rowCol.col() } );
		}
		return encoded;
	}

	/**
	 * @param enc one-based digit and combo encoded locations
	 * @return String version of encoded locations
	 */
	@Override
	public String encodingToString(int [] enc) {
		if ( null == enc ) return "null";
		int digit = enc[0];
		RowCol[] rowCols = new RowCol[ enc.length - 1];
		for( int loci = 1; loci < enc.length; loci++) {
			int[] rowCol = onebasedComboToZeroBasedInts( enc[ loci ] );
			rowCols[ loci - 1] = ROWCOL[rowCol[0]][rowCol[1]]; // // Converts 1-based int to 0-based int[]
		}
		RowCol[] base = new RowCol[]{
				ROWCOL[rowCols[0].row()][rowCols[0].col()],
				ROWCOL[rowCols[1].row()][rowCols[1].col()] };
		// Check if rows/cols
		if ( RowCol.rowsMatch( base )) {
			return format("digit %d has %d row locs at %s",
					digit, rowCols.length, RowCol.toString(rowCols));
		} else if ( RowCol.colsMatch( base )) {
			return format("digit %d has %d col locs at %s",
					digit, rowCols.length, RowCol.toString(rowCols));
		} else if ( RowCol.boxesMatch( base )) {
			return format("digit %d has %d box locs at %s",
					digit, rowCols.length, RowCol.toString(rowCols));
		}
		return format("digit %d has %d locs with no matching units at %s",
				digit, rowCols.length, RowCol.toString(rowCols));
	}

	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}