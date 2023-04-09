package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Candidates.ALL_DIGITS;
import static info.danbecker.ss.Utils.*;
import static java.lang.String.format;

/**
 * XY-Wing
 * From https://hodoku.sourceforge.net/en/tech_wings.php#xy
 * <p>
 * An XY-Wing is really a short XY-Chain.
 * Look for bivalue cell (the pivot).
 * The possible candidates in that cell are called X and Y.
 * <p>
 * Now we try to find two other bivalue cells that see the pivot (the pincers).
 * One of those cells contains candidates X and Z (Z is an arbitrary candidate different from X and Y),
 * and the other candidates Y and Z.
 * <p>
 * Now Z can be eliminated from any cell that sees both pincers.
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class XYWing implements UpdateCandidatesRule {
	public XYWing() {
	}

	@Override
	public int updateCandidates(Board board, Board solution, Candidates candidates, List<int[]> locations) {
		int updates = 0;
		if ( null == locations) return updates;
		if (locations.size() > 0) {
			// Just act on first find
			int [] enc = locations.get(0);
			// int xDigit = enc[0];
			// int yDigit = enc[1];
			int zDigit = enc[2];
			// int [] rowCol = comboToInts(enc[3]);
			// RowCol xy = ROWCOL[rowCol[0]][rowCol[1]];
			// rowCol = comboToInts(enc[4]);
			// RowCol xz = ROWCOL[rowCol[0]][rowCol[1]];
			// rowCol = comboToInts(enc[5]);
			// RowCol yz = ROWCOL[rowCol[0]][rowCol[1]];
			RowCol[] locs = new RowCol[ enc.length - 6 ];
			for( int loci = 6; loci < enc.length; loci++) {
				int[] rowCol = comboToInts( enc[ loci ] ); // converts one-based to zero-based
				locs[ loci - 6] = ROWCOL[rowCol[0]][rowCol[1]];
			}

			for ( int loci = 0; loci < locs.length; loci++ ) {
				RowCol loc = locs[loci];
				if ( candidates.isCandidate( loc, zDigit )) {
					// Validation
					if ( null != solution ) {
						int cellStatus = solution.get(loc);
						if ( cellStatus == zDigit ) {
							throw new IllegalArgumentException( format("Rule %s would like to remove solution digit %d at loc %s.",
								ruleName(), zDigit, loc));
						}
					}
					// It validates, go ahead
					if (candidates.removeCandidate(loc, zDigit)) {
						updates++;
					}
				}
			}
			System.out.println( format( "%s removed digit %d from %d location%s %s",
					ruleName(), zDigit, updates,
					locs.length == 1 ? "" : "s", // pluralize location?
					RowCol.toString( locs )));
		}
		return updates;
	}

	@Override
	public List<int[]> locations(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		List<int[]> locs = new ArrayList<>();
		List<RowCol> xyPairs = candidates.getGroupLocations( ALL_DIGITS, 2 );
		for( int pairi = 0; pairi < xyPairs.size(); pairi++) {
			RowCol xyLoc = xyPairs.get(pairi);
			List<Integer> xyCandidates = candidates.getCandidatesList(xyLoc);
			Collections.sort( xyCandidates );
			if (2 != xyCandidates.size())
				throw new IllegalArgumentException("xy location " + xyLoc + " should be size 2, but found %s " + xyCandidates);
			int xDigit = xyCandidates.get(0);
			int yDigit = xyCandidates.get(1);

			// Orthogonal search directions are row/col, row/box, col/box
			Unit[][] searches = new Unit[][]{
					new Unit[]{Unit.ROW, Unit.COL}, new Unit[]{Unit.ROW, Unit.BOX}, new Unit[]{Unit.COL, Unit.BOX},
			};

			// Search for xz and yz pincers.
			for (int searchi = 0; searchi < searches.length; searchi++) {
				Unit firstUnit = searches[searchi][0];
				int firstUniti = switch (firstUnit) {
					case ROW -> xyLoc.row();
					case COL -> xyLoc.col();
					case BOX -> xyLoc.box();
				};
				List<RowCol> firstUnitFounds = candidates.candidateUnitGroupLocs(firstUnit, firstUniti, xDigit, 2);
				for (int xfirstfoundi = 0; xfirstfoundi < firstUnitFounds.size(); xfirstfoundi++) {
					RowCol xzLoc = firstUnitFounds.get(xfirstfoundi);
					if (!xyLoc.equals(xzLoc)) {
						int[] xz = candidates.getRemainingCandidates(xzLoc);
						int zDigit = xz[0] == xDigit ? xz[1] : xz[0];
						if (yDigit != zDigit) {
							Unit secondUnit = searches[searchi][1];
							int secondUniti = switch (secondUnit) {
								case ROW -> xyLoc.row();
								case COL -> xyLoc.col();
								case BOX -> xyLoc.box();
							};
							// int[] yz = new int[]{yDigit, zDigit};
							List<RowCol> secondUnitFounds = candidates.candidateUnitGroupLocs(secondUnit, secondUniti, zDigit, 2);
							for (int ysecondfoundi = 0; ysecondfoundi < secondUnitFounds.size(); ysecondfoundi++) {
								RowCol yzLoc = secondUnitFounds.get(ysecondfoundi);
								if (!xyLoc.equals(yzLoc)) {
									int[] yzFound = candidates.getRemainingCandidates(yzLoc);
									if (2 == yzFound.length &&
											((yzFound[0] == yDigit && yzFound[1] == zDigit) || (yzFound[1] == yDigit && yzFound[0] == zDigit)) ){
										// Assure locs are not all in same row or col.
										if  (!((xyLoc.row() == xzLoc.row() && xyLoc.row() == yzLoc.row()) ||
											   (xyLoc.col() == xzLoc.col() && xyLoc.col() == yzLoc.col()))) {
											List<RowCol> zLocs = findZLocs( candidates, zDigit, xyLoc, xzLoc, yzLoc );
											if ( 0 < zLocs.size()) {
												int[] xyz = new int[]{xDigit, yDigit, zDigit};
												int[] enc = encodeLocation(xyz, xyLoc, xzLoc, yzLoc, zLocs);
												System.out.println(format("%s found %s", ruleName(), locationToString(enc)));
												locs.add( enc );
											}
										}
									} // candidates match
								}  // found yz
							} // xDigit != zDigit
						} // second unit search
					} // found xz
				} // xzLocs
			} // unit searches
		} // pairs
		return locs;
	}

	/** Find all rowCols with Candidate zDigit that can see the pincers xz and yz (pivot xy is not included)
	 * @return list of Z locations that see xz and yz locations.
	 */
	public List<RowCol> findZLocs( Candidates candidates, int zDigit, RowCol xyLoc, RowCol xzLoc, RowCol yzLoc ) {
		List<RowCol> rowCols = new LinkedList<>();
		List<RowCol> candLocs = candidates.digitLocs(zDigit);
		for (RowCol rowCol : candLocs) {
			if ( !rowCol.equals(xyLoc) && !rowCol.equals(xzLoc) && !rowCol.equals(yzLoc)) {
				if (0 < RowCol.getMatchingUnits(rowCol, xzLoc).size() && 0 < RowCol.getMatchingUnits(rowCol, yzLoc).size())
					rowCols.add(rowCol);
			}
		}
		return rowCols;
	}

	/**
	 * Encoded as such
	 * 0 - rowCol of xyPivot (rc 1,5 => 27)
	 * 1 - rowCol of xzPincer (rc 0,8 => 19)
	 * 2 - rowCol of yzPincer (rc 1,2 => 29)
	 * 4+ - rowCols of z values to remove
	 */
	public static int [] encodeLocation( int[] xyzDigits, RowCol xyPivot, RowCol xzPincer, RowCol yzPincer, List<RowCol> locs) {
		if ( null == xyzDigits || 3 != xyzDigits.length )
			throw new IllegalArgumentException( "xyzDigits should be length 3" );
		for ( int digi = 0; digi < xyzDigits.length; digi++ ) {
			if ( 1 > xyzDigits[digi] || 9 < xyzDigits[ digi ])
				throw new IllegalArgumentException( "xyzDigits " + digi + " should be length one-base in the range 1..9" );
		}
		if ( null == xyPivot )
			throw new NullPointerException( "xyPivot=null" );
		if ( null == xzPincer )
			throw new NullPointerException( "xzPincer=null" );
		if ( null == yzPincer )
			throw new NullPointerException( "yzPincer=null" );
		if ( null == locs )
			throw new IllegalArgumentException( "locs empty" );

		int [] encoded = new int[6 + locs.size()];
		encoded[0] = xyzDigits[0];
		encoded[1] = xyzDigits[1];
		encoded[2] = xyzDigits[2];
		encoded[3] = intsToCombo( new int[]{ xyPivot.row(), xyPivot.col()} );
		encoded[4] = intsToCombo( new int[]{ xzPincer.row(), xzPincer.col()} );
		encoded[5] = intsToCombo( new int[]{ yzPincer.row(), yzPincer.col()} );
		int i = 6;
		for ( RowCol loc : locs ) {
			encoded[ i++ ] = intsToCombo( new int[] { loc.row(), loc.col() } );
		}
		return encoded;
	}

	/**
	 * @param loc one-based digit and combo encoded locations
	 * @return String version of encoded locations
	 */
	public static String locationToString( int [] loc ) {
		if ( null == loc ) return "null";
		int xDigit = loc[0];
		int yDigit = loc[1];
		int zDigit = loc[2];
		int [] rowCol = comboToInts(loc[3]);
		RowCol xy = ROWCOL[rowCol[0]][rowCol[1]];
		rowCol = comboToInts(loc[4]);
		RowCol xz = ROWCOL[rowCol[0]][rowCol[1]];
		rowCol = comboToInts(loc[5]);
		RowCol yz = ROWCOL[rowCol[0]][rowCol[1]];

		RowCol[] locs = new RowCol[ loc.length - 6 ];
		for( int loci = 6; loci < loc.length; loci++) {
			rowCol = comboToInts( loc[ loci ] ); // converts one-based to zero-based
			locs[ loci - 6] = ROWCOL[rowCol[0]][rowCol[1]];
		}
		return format("xyz=%d%d%d, xyLoc=%s, xzLoc=%s, yzLoc=%s, and z locs=%s",
			xDigit, yDigit, zDigit, xy, xz, yz, RowCol.toString(locs));
	}

	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}