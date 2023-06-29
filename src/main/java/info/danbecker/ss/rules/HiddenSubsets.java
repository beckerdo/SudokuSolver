package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Utils.*;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static info.danbecker.ss.Candidates.NOT_NAKED;

/**
 * HiddenSubsets
 * Also known as hidden doubles, triples, quads, etc.,
 * this is when a pair such as {13} occurs in just two rows/cols/boxes,
 * but the two boxes are poluted by other candidates: {13} {1345} -> {13}{13}
 * These extra candidates can be removed, thus turning this into naked subsets. 
 * <p>
 * Another common hidden triple is {24}{47}{27} in exactly 3 cells.
 * You might often see this in puzzles with three cells containing 
 * just two values each, for instance {24}{47}{27}. Again, there's 
 * just three values shared between three cells, so you can remove 
 * any other candidates from these cells: {245}{476}{278} -> {24}{47}{27}
 * <p>
 * Notice that Hidden do not have partial sets because something like
 * {18}{1} or {18}{8} wwould be discovered by the single candidate rule.
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class HiddenSubsets implements FindUpdateRule {

	protected int subsetSize;
	protected int partialCount;

	public HiddenSubsets(int subsetSize) {
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
		for ( int enci = 0; enci < encs.size(); enci++) {
			// Act on all finds
			int [] enc = encs.get(enci);
			// Decode information
			int [] zbDigits = onebasedComboToZeroBasedInts( enc[0] ); // converts 1-based to 0-based
			RowCol[] locs = encToRowCols( enc );

			int[][] digitsNotInCombo = candidates.digitsNotInCombo( zbDigits, locs );
			System.out.printf( "%s hidden %s pair will remove %s digits %s from %s%n",
					ruleName(),
					digitsToString(zbToobIntsInPlace(zbDigits)), // converts 0-based to 1-based
					RowCol.firstUnitMatch( locs[0], locs[1]),
					digitListsToString( digitsNotInCombo),
					RowCol.toString( locs ));

			// Validation if available
			if (null != solution) {
				// Test if digit candidate removal, removes a solution digit
				for (int loci = 0; loci < digitsNotInCombo.length; loci++) {
					int[] zbRemovalDigits =  digitsNotInCombo[ loci ];
					RowCol loc = locs[ loci ];
					for (int digi = 0; digi < zbRemovalDigits.length; digi++) {
						int digit = zbRemovalDigits[digi] + 1;
						int cellSolution = solution.get(loc);
						if (cellSolution == digit) {
							System.out.println("Candidates=\n" + candidates.toStringBoxed());
							throw new IllegalArgumentException(format("Rule %s wants to remove solution digit %d at loc %s.%n",
									ruleName(), cellSolution, loc));
						}
					}
				}
			}

			updates += candidates.removeCandidatesNotInCombo(zbDigits, locs);
		}
		return updates;
	}

	public static String digitListsToString( int[][] digitLists ) {
		StringBuffer sb = new StringBuffer();
		for ( int listi = 0; listi < digitLists.length; listi++) {
			if ( 0 < listi ) sb.append("");
			// Convert zb to ob
			sb.append( Utils.digitsToString( zbToobIntsCopy(digitLists[listi] )));
		}
		return sb.toString();
	}

	/**
	 * A pair/triplet of candidates, can knock out other candidates
	 * in the same row/col/box: {245}{476}{278} -> {24}{47}{27}
	 * <p>
	 * Search for only R same candidates in each block,
	 * see if row or col is the same,
     * if row match, see if other candidates exist in same rowon same row outside of block
	 * if col match, see if other candidates exist on same col outside of block
	 * <p>
	 * Each location is reported as: comboToInt, rowCol1ToInt, rowCol2ToInt, ..., rowColRToInt
	 */
	@Override
	public List<int[]> find(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		ArrayList<int[]> encs = new ArrayList<>();
		// Generate combinations of n elements (9 digits), r at a time.
		// Note that these combos are 0 based
		List<int[]> combinations = Utils.comboGenerate(DIGITS, subsetSize); // combos are 0 based
		for( int combi = 0; combi < combinations.size(); combi++) {
			int [] zbCombo = combinations.get(combi); // combo is 0 based
			if (!board.comboCompleted(zbCombo)) {
				// Search row/col/block for this combo
				for (Unit unit : Unit.values()) {
					for (int uniti = 0; uniti < UNITS; uniti++) {
						List<RowCol> foundLocs = candidates.candidateComboUnitLocations(unit, uniti, zbCombo, NOT_NAKED, partialCount);
						if (foundLocs.size() == subsetSize) {
							// Found this unit has exactly N of these combos. For example {127}{12}
							// Need to ensure location cands are more than just partials (digitCount > subsetSize)
							// Need to see if there are other candidates in these locations
							// (candidateLocCount > comboCount)
							int candidateLocCount = candidates.candidateRowColCount(foundLocs);
							int comboCount = candidates.candidateComboLocCount(zbCombo, foundLocs);
							if ((candidates.digitLocCount(zbCombo, foundLocs) >= subsetSize)
									&& (candidateLocCount > comboCount)) {
								// Now check no more stray combo digits outside of locations, same row.
								int comboUnitCount = candidates.candidateComboUnitCount(unit, uniti, zbCombo);
								if (comboUnitCount == comboCount) {
									// System.out.println(format(
									// 		"%s %d digits %s have %d locs, %d digits, %d candidates, %d combo candidates",
									// 		unit, uniti,
									// 		Utils.digitsToString(zbToobIntsCopy(zbCombo)), foundLocs.size(),
									// 		candidates.digitLocCount(zbCombo, foundLocs), candidateLocCount, comboCount));
									int[] enc = encode(zbCombo, foundLocs);
									// Note that boxes SOMETIMES duplicates row or col
									// Yes: [1,1][1,2] [1,1][2,1]   No: [1,1][2,2]
									addUnique( encs, enc );
								}
							} // more candidates in unit
						} // location count == subset size
					} // each unit row/col/box
				} // each unit
			} // combo incomplete
		} // for each combo
		return encs;
	}

	/**
	 * Given 0-based combo and 0-based locations, return 1-base combo,locations array
	 * <p>
	 * For example combo [0,3] and locations [1,0] and [8,0]
	 * is converted to
	 * int[] {14,21,91}
	 */
	public static int [] encode(int [] combo, List<RowCol> locations) {
		int [] encoded = new int[1 + locations.size() ];
		encoded[0] = zerobasedIntsToOnebasedCombo( combo ); // Converts 0-based digits to 1-based int
		for( int loci = 0; loci < locations.size(); loci++) {
			RowCol rowCol = locations.get( loci );
			encoded[ loci+1 ] = zerobasedIntsToOnebasedCombo( new int[] { rowCol.row(), rowCol.col() } ); // Converts 0-based int[] to 1-based int
		}
		return encoded;
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
	 * @param enc one-based location
	 * @return String version of encoded locations
	 */
	@Override
	public String encodingToString( int [] enc ) {
		if ( null == enc ) return "null";
		if ( enc.length != subsetSize + 1 )
			return "location length should be " + (subsetSize + 1) + "but was " + enc.length;
		int combo = enc[0];
		RowCol[] rowCols = new RowCol[ enc.length - 1];
		for( int loci = 1; loci < enc.length; loci++) {
			int[] rowCol = onebasedComboToZeroBasedInts( enc[ loci ] );
			rowCols[ loci - 1] = ROWCOL[rowCol[0]][rowCol[1]]; // // Converts 1-based int to 0-based int[]
		}
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