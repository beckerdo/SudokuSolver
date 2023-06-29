package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Candidates.NAKED;
import static info.danbecker.ss.Utils.*;
import static java.lang.String.format;

/**
 * NakedSubsets
 * Also known as naked doubles, triples, quads, etc.,
 * this is when a pair such as {46} occurs in two rows/cols/boxes,
 * and therefore can be removed as candidates from other cells. 
 * <p>
 * Another common naked triple is {24}{47}{27} in exactly 3 cells.
 * You might often see this in puzzles with three cells containing 
 * just two values each, for instance {24} {47} {27}. Again, there's 
 * just three values shared between three cells, so you can remove 
 * 2,4 and 7 from any other cells in that area!
 * <p>
 * Notice that NakedPairs do not have this problem because {18}{1} 
 * or {18}{8} in two spots would become a single candidate.
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class NakedSubsets implements FindUpdateRule {

	protected int subsetSize;
	protected int partialCount;

	public NakedSubsets(int subsetSize) {
		if ( subsetSize < 2)
			throw new IllegalArgumentException(  "Subset size " + subsetSize + " was less than 2.");
		this.subsetSize = subsetSize;
		if ( 2 == subsetSize ) {
			partialCount = 2;
		} else {
			partialCount = subsetSize - 1;
		}
	}

	@Override
	public int update(Board board, Board solution, Candidates candidates, List<int[]> encs) {
		int updates = 0;
		if ( null == encs) return updates;
		for (int enci = 0; enci < encs.size(); enci++) {
			// Act on eacht find
			int[] enc = encs.get(enci);
			// Decode information
			int[] zbDigits = onebasedComboToZeroBasedInts(enc[0]); // converts 1-based to 0-based
			RowCol[] rowCols = encToRowCols(enc);

			List<RowCol> found = candidates.findDigitsNotInLocs(zbDigits, Arrays.asList(rowCols));
			System.out.printf("%s, digits {%d} at %s, will remove at %s%n",
					ruleName(), enc[0], RowCol.toString(rowCols), RowCol.toString(found));

			// Validation if available
			if (null != solution) {
				for (int digi = 0; digi < zbDigits.length; digi++) {
					int digit = zbDigits[digi] + 1;
					for (RowCol loc : found) {
						int cellStatus = solution.get(loc);
						if (cellStatus == digit) {
							System.out.println("Candidates=\n" + candidates.toStringBoxed());
							throw new IllegalArgumentException(format("Rule %s would like to remove solution digit %d at loc %s.%n",
									ruleName(), cellStatus, loc));
						}
					}
				}
			}

			updates += candidates.removeCandidatesNotInLocations(zbDigits, rowCols);
		}
		return updates;
	}

	/**
	 * A pair/triplet of candidates, if in the same row/col,
	 * can knock out candidates in other blocks in the same row/col
	 * <p>
	 * Search for only R same candidates in each block,
	 * see if row or col is the same,
     * if row match, see if other candidates exist on same row outside of block
	 * if col match, see if other candidates exist on same col outside of block
	 * <p>
	 * Each location is reported as: comboToInt, rowCol1ToInt, rowCol2ToInt, ..., rowColRToInt
	 */
	@Override
	public List<int[]> find(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		ArrayList<int[]> locations = new ArrayList<>();
		// Generate combinations of n elements (9 digits), r at a time.
		// Note that these combos are 0 based
		List<int[]> combinations = Utils.comboGenerate(DIGITS, subsetSize); // combos are 0 based
		for( int [] zbCombo : combinations ) {
			if (!board.comboCompleted(zbCombo)) {
				// Search row/col/block for this naked candidate
				for (Unit unit : Unit.values()) {
					for (int uniti = 0; uniti < UNITS; uniti++) {
						List<RowCol> unitFound = candidates.candidateComboUnitLocations( unit, uniti, zbCombo, NAKED, partialCount );
						if (unitFound.size() == subsetSize) {
							// Found this unit has exactly N of these combos. For example pair of combo {15}
							// Now need to see if there are combo digits in more locations.
							int comboDigitCount = candidates.candidateComboUnitCount( unit, uniti, zbCombo);
							int comboNakedCount = candidates.candidateComboLocCount(zbCombo, unitFound);
							if (comboDigitCount > comboNakedCount) {
								// For example combo {15}, row contains [15][15][156],
								// {15} combo digit count of 6, with naked pair count of 4.
								// System.out.printf( "%s %d, digits {%d} at %s, %d naked candidates, %d candidates in line%n",
								// 	unit, uniti, zerobasedIntsToOnebasedCombo(zbCombo), RowCol.toString(unitFound), comboNakedCount, comboDigitCount);
								int[] encoded = encode(zbCombo, unitFound);
								locations.add(encoded);
							}
						}
					} // each unit index
				} // each unit
			} // comboCompleted
		}
		return locations;
	}

	/**
	 * Given 0-based combo and 0-based locations, return 1-base combo,locations array
	 * 0 - one based combo digit for example int 19 == {08}
	 * 1* - ones encoded row,col for example int 13 == ROWCOL[0][2]
	 */
	public static int [] encode(int [] combo, List<RowCol> locs) {
		int [] enc = new int[1 + locs.size() ];
		enc[0] = zerobasedIntsToOnebasedCombo( combo ); // Converts 0-based digits to 1-based int
		for( int loci = 0; loci < locs.size(); loci++) {
			RowCol rowCol = locs.get( loci );
			enc[ loci+1 ] = zerobasedIntsToOnebasedCombo( new int[]{ rowCol.row(), rowCol.col() } ); // Converts 0-based int[] to 1-based int
		}
		return enc;
	}

	/** Decode just the locations part */
	public static RowCol[] encToRowCols( int[] enc ) {
		RowCol[] rowCols = new RowCol[ enc.length - 1];
		for( int loci = 1; loci < enc.length; loci++) {
			int[] rowCol = onebasedComboToZeroBasedInts( enc[ loci ] );
			rowCols[ loci - 1] = ROWCOL[rowCol[0]][rowCol[1]]; // // Converts 1-based int to 0-based int[]
		}
		return rowCols;
	}

	/**
	 * {15} row candidate at row/col [8,1],[8,6]
	 * @param enc  one-based locations
	 * @return String version of encoded location
	 */
	@Override
	public String encodingToString(int[] enc ) {
		if ( null == enc ) return "null";
		if ( enc.length != subsetSize + 1 )
			return "location length should be " + (subsetSize + 1) + "but was " + enc.length;
		int combo = enc[0];
		RowCol[] rowCols = encToRowCols( enc );

		// Check if rows/cols
		if ( RowCol.rowsMatch( rowCols )) {
			return format("digits {%d} have %d row locs at %s",
					combo, subsetSize, RowCol.toString(rowCols));
		} else if ( RowCol.colsMatch( rowCols )) {
			return format("digits {%d} have %d col locs at %s",
					combo, subsetSize, RowCol.toString(rowCols));
		} else if ( RowCol.boxesMatch( rowCols )) {
			return format("digits {%d} have %d box locs at %s",
					combo, subsetSize, RowCol.toString(rowCols));
		}
		return format("combo {%d} has %d locs with no matching units at %s",
				combo, subsetSize, RowCol.toString(rowCols));
	}


	@Override
	public String ruleName() {
		return this.getClass().getSimpleName() + subsetSize;
	}
}