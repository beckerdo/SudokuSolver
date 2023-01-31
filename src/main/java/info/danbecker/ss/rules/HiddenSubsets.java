package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.Utils;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static info.danbecker.ss.Utils.ROWS;
import static info.danbecker.ss.Utils.COLS;
import static info.danbecker.ss.Utils.BOXES;
import static info.danbecker.ss.Utils.DIGITS;
import static info.danbecker.ss.Utils.intToCombo;
import static info.danbecker.ss.Utils.comboToInt;

import static info.danbecker.ss.Candidates.NOT_NAKED;

/**
 * HiddenSubsets
 * Also known as hidden doubles, triples, quads, etc,
 * this is when a pair such as {13} occurs in just two rows/cols/boxes,
 * but the two boxes are poluted by other candidates: {13} {1345} -> {13}{13}
 * These extra candidates can be removed, thus turning this into naked subsets. 
 * 
 * Another common hidden triple is {24}{47}{27} in exactly 3 cells.
 * You might often see this in puzzles with three cells containing 
 * just two values each, for instance {24}{47}{27}. Again, there's 
 * just three values shared between three cells, so you can remove 
 * any other candidates from these cells: {245}{476}{278} -> {24}{47}{27}
 * 
 * Notice that Hidden do not have partial sets because something like
 * {18}{1} or {18}{8} wwould be discovered by the single candidate rule.
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class HiddenSubsets implements UpdateCandidatesRule {
	
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
	public int updateCandidates(Board board, Board solution, Candidates candidates, List<int[]> locations) {
		int updates = 0;
		if ( null == locations) return updates;
		if (locations.size() > 0) {
			// Just act on first find
			int [] encoded = locations.get(0);
			// Decode information
			int [] combo = intToCombo( encoded[0] ); // converts 1-based to 0-based
			int [][] rowCols = new int[ encoded.length - 1][]; 
			StringBuffer sb = new StringBuffer();
			for( int loci = 1; loci < encoded.length; loci++) {
				rowCols[ loci - 1] = intToCombo( encoded[ loci ] ); // converts 1-based to 0-based
				sb.append(Arrays.toString(rowCols[ loci - 1]) );
			}
			System.out.println( format( "%s will turn hidden combos {%d} to nakeds in locations %s", ruleName(),
				comboToInt(combo), // converts 0-based to 1-based
				sb.toString()));
			// Just correct first item
			updates += candidates.removeCandidatesNotInCombo(combo, rowCols);
		}
		return updates;
	}

	@Override
	/** 
	 * A pair/triplet of candidates, can knock out other candidates 
	 * in the same row/col/box: {245}{476}{278} -> {24}{47}{27}
	 * 
	 * Search for only R same candidates in each block,
  	 * see if row or col is the same, 
     * if row match, see if other candidates exist in same rowon same row outside of block
	 * if col match, see if other candidates exist on same col outside of block
	 * 
	 * Each location is reported as: comboToInt, rowCol1ToInt, rowCol2ToInt, ..., rowColRToInt 
	 */
	public List<int[]> locations(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		ArrayList<int[]> locations = new ArrayList<int[]>();
		// Generate combinations of n elements (9 digits), r at a time.
		// Note that these combos are 0 based
		ArrayList<int[]> combinations = Utils.comboGenerate(DIGITS, subsetSize); // combos are 0 based
		for( int combi = 0; combi < combinations.size(); combi++) {
			int [] combo = combinations.get(combi); // combo is 0 based
			if (!board.comboCompleted(combo)) {
				// Search row/col/block for this naked candidate
				for (int rowi = 0; rowi < ROWS; rowi++) {
					List<int[]> rowFound = candidates.candidateComboRowLocations(rowi, combo, NOT_NAKED, partialCount);
					if (rowFound.size() == subsetSize) {
						// Found this row has exactly N of these combos. For example {127}{12}
						// Need to ensure location cands are more than just partials (digitCount >
						// subsetSize)
						// Need to see if there are other candidates in these locations
						// (candidateLocCount > comboCount)
						int candidateLocCount = candidates.candidateRowColCount(rowFound);
						int comboCount = candidates.candidateComboLocCount(combo, rowFound);
						if ((candidates.digitLocCount(combo, rowFound) >= subsetSize)
								&& (candidateLocCount > comboCount)) {
							// Now check no more stray combo digits outside of locations, same row.
							int comboRowCount = candidates.candidateComboRowCount(rowi, combo);
							if (comboRowCount == comboCount) {
								System.out.println(format(
										"Row %d, combo %s, has %d locs, %d digits, %d candidates, %d combo candidates",
										rowi, Arrays.toString(combo), rowFound.size(),
										candidates.digitLocCount(combo, rowFound), candidateLocCount, comboCount));
								int[] encoded = encodeLocation(combo, rowFound);
								locations.add(encoded);
							}
						}
						// System.out.println( this.ruleName() + " " + locationToString( encoded ) );
					}
				}
				for (int coli = 0; coli < COLS; coli++) {
					List<int[]> colFound = candidates.candidateComboColLocations(coli, combo, NOT_NAKED, partialCount);
					if (colFound.size() == subsetSize) {
						// Found this col has exactly N of these combos. For example {127}{12}
						// Need to ensure location cands are more than just partials (digitCount >
						// subsetSize)
						// Need to see if there are other candidates in these locations
						// (candidateLocCount > comboCount)
						int candidateLocCount = candidates.candidateRowColCount(colFound);
						int comboCount = candidates.candidateComboLocCount(combo, colFound);
						if ((candidates.digitLocCount(combo, colFound) >= subsetSize)
								&& (candidateLocCount > comboCount)) {
							// Now check no more stray combo digits outside of locations, same col.
							int comboColCount = candidates.candidateComboColCount(coli, combo);
							if (comboColCount == comboCount) {
								System.out.println(format(
										"Col %d, combo %s, has %d locs, %d digits, %d candidates, %d combo candidates",
										coli, Arrays.toString(combo), colFound.size(),
										candidates.digitLocCount(combo, colFound), candidateLocCount, comboCount));
								int[] encoded = encodeLocation(combo, colFound);
								locations.add(encoded);
							}
						}
						// System.out.println( this.ruleName() + " " + locationToString( encoded ) );
					}
				}
				// Note, it is possible that a naked row or col subset lies in the same box.
				// Thus this loop can find locations found above.
				// However, it is possible that a naked box subset is not in the same row or
				// col.
				for (int boxi = 0; boxi < BOXES; boxi++) {
					List<int[]> boxFound = candidates.candidateComboBoxLocations(boxi, combo, NOT_NAKED, partialCount);
					if (boxFound.size() == subsetSize) {
						// Found this box has exactly N of these combos. For example {127}{12}
						// Need to ensure location cands are more than just partials (digitCount >
						// subsetSize)
						// Need to see if there are other candidates in these locations
						// (candidateLocCount > comboCount)
						int candidateLocCount = candidates.candidateRowColCount(boxFound);
						int comboCount = candidates.candidateComboLocCount(combo, boxFound);
						if ((candidates.digitLocCount(combo, boxFound) >= subsetSize)
								&& (candidateLocCount > comboCount)) {
							// Now check no more stray combo digits outside of locations, same col.
							int comboBoxCount = candidates.candidateComboBoxCount(boxi, combo);
							if (comboBoxCount == comboCount) {
								System.out.println(format(
										"Box %d, combo %s, has %d locs, %d digits, %d candidates, %d combo candidates",
										boxi, Arrays.toString(combo), boxFound.size(),
										candidates.digitLocCount(combo, boxFound), candidateLocCount, comboCount));
								int[] encoded = encodeLocation(combo, boxFound);
								locations.add(encoded);
							}
						}
						// System.out.println( this.ruleName() + " " + locationToString( encoded ) );
					}
				}
			}
		}
		return locations;
	}

	/** Given 0-based combo and 0-based locations, return 1-base combo,locations array */
	public int [] encodeLocation( int [] combo, List<int[]> locations) {
		int [] encoded = new int[1 + locations.size() ];
		encoded[0] = comboToInt( combo ); // Converts 0-based digits to 1-based int
		for( int loci = 0; loci < locations.size(); loci++) {
			int [] rowCol = locations.get( loci );
			encoded[ loci+1 ] = comboToInt( rowCol ); // Converts 0-based int[] to 1-based int 					
		}
		return encoded;
	}
	
	/**
	 * {15} row candidate at row/col [8,1],[8,6]
	 * @param one-based location
	 * @return
	 */
	public String locationToString( int [] location ) {
		if ( null == location ) return "null";
		if ( location.length != subsetSize + 1 )
			return "location length should be " + (subsetSize + 1) + "but was " + location.length;
		int combo = location[0];
		int [][] rowCols = new int[ location.length - 1][];
		for( int loci = 1; loci < location.length; loci++) {
			rowCols[ loci - 1] = intToCombo( location[ loci ] ); // // Converts 1-based int to 0-based int[]	
		}
		
		// Check if rows/cols
		if ( Utils.rowsMatch( rowCols )) {
			return format("combo {%d} has %d row locs at row/cols %s", 
					combo, subsetSize, Utils.locationsString(rowCols));
		}
		return format("combo {%d} has %d col locs at row/cols %s", 
				combo, subsetSize, Utils.locationsString(rowCols));
	}
	
	@Override
	public String ruleName() {
		return this.getClass().getSimpleName() + subsetSize;
	}
}