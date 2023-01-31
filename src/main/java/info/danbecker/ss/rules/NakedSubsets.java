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

import static info.danbecker.ss.Candidates.NAKED;

/**
 * NakedSubsets
 * Also known as naked doubles, triples, quads, etc,
 * this is when a pair such as {46} occurs in two rows/cols/boxes,
 * and therefore can be removed as candidates from other cells. 
 * 
 * Another common naked triple is {24}{47}{27} in exactly 3 cells.
 * You might often see this in puzzles with three cells containing 
 * just two values each, for instance {24} {47} {27}. Again, there's 
 * just three values shared between three cells, so you can remove 
 * 2,4 and 7 from any other cells in that area!
 * 
 * Notice that NakedPairs do not have this problem because {18}{1} 
 * or {18}{8} in two spots would become a single candidate.
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class NakedSubsets implements UpdateCandidatesRule {
	
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

			System.out.println( format( "%s will remove candidates {%d} not in locations %s", ruleName(),
			   comboToInt(combo), // converts 0-based to 1-based
			   sb.toString()));
			// Just correct first item
			updates += candidates.removeCandidatesNotInLocations(combo, rowCols);
		}
		return updates;
	}

	@Override
	/** 
	 * A pair/triplet of candidates, if in the same row/col, 
	 * can knock out candidates in other blocks in the same row/col
	 * 
	 * Search for only R same candidates in each block,
  	 * see if row or col is the same, 
     * if row match, see if other candidates exist on same row outside of block
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
					List<int[]> rowFound = candidates.candidateComboRowLocations(rowi, combo, NAKED, partialCount);
					if (rowFound.size() == subsetSize) {
						// Found this row has exactly N of these combos. For example pair of combo {15}
						// Now need to see if there are combo digits in more locations.
						int comboDigitCount = candidates.candidateComboRowCount(rowi, combo);
						int comboNakedCount = candidates.candidateComboLocCount(combo, rowFound);
						if (comboDigitCount > comboNakedCount) {
							// For example combo {15}, row contains [15][15][156],
							// {15} combo digit count of 6, with with naked pair count of 4.
							System.out.println(format(
									"Row %d, combo %s, has %d naked locs, %d naked candidates, %d candidates in line",
									rowi, Arrays.toString(combo), rowFound.size(), comboNakedCount, comboDigitCount));
							int[] encoded = encodeLocation(combo, rowFound);
							locations.add(encoded);
						}
						// System.out.println( this.ruleName() + " " + locationToString( encoded ) );
					}
				}
				for (int coli = 0; coli < COLS; coli++) {
					List<int[]> colFound = candidates.candidateComboColLocations(coli, combo, NAKED, partialCount);
					if (colFound.size() == subsetSize) {
						// Found this col has exactly N of these combos. For example pair of combo {15}
						// Now need to see if there are combo digits in more locations.
						int comboDigitCount = candidates.candidateComboColCount(coli, combo);
						int comboNakedCount = candidates.candidateComboLocCount(combo, colFound);
						if (comboDigitCount > comboNakedCount) {
							// For example combo {15}, col contains [15][15][156],
							// {15} combo digit count of 6, with with naked pair count of 4.
							System.out.println(format(
									"Col %d, combo %s, has %d naked locs, %d naked candidates, %d candidates in line",
									coli, Arrays.toString(combo), colFound.size(), comboNakedCount, comboDigitCount));
							// System.out.println( "Remaining candidates=\n" + candidates.toStringCompact()
							// );
							int[] encoded = encodeLocation(combo, colFound);
							locations.add(encoded);
						}
						// System.out.println( this.ruleName() + " " + locationToString( encoded ) );
					}
				}
				// Note, it is possible that a naked row or col subset lies in the same box.
				// Thus this loop can find locations found above.
				// However, it is possible that a naked box subset is not in the same row or
				// col.
				for (int boxi = 0; boxi < BOXES; boxi++) {
					List<int[]> boxFound = candidates.candidateComboBoxLocations(boxi, combo, NAKED, partialCount);
					if (boxFound.size() == subsetSize) {
						// Found this col has exactly N of these combos. For example pair of combo {15}
						// Now need to see if there are combo digits in more locations.
						int comboDigitCount = candidates.candidateComboBoxCount(boxi, combo);
						int comboNakedCount = candidates.candidateComboLocCount(combo, boxFound);
						if (comboDigitCount > comboNakedCount) {
							// For example combo {15}, col contains [15][15][156],
							// {15} combo digit count of 6, with with naked pair count of 4.
							System.out.println(format(
									"Box %d, combo %s, has %d naked locs, %d naked candidates, %d candidates in line",
									boxi, Arrays.toString(combo), boxFound.size(), comboNakedCount, comboDigitCount));
							int[] encoded = encodeLocation(combo, boxFound);
							locations.add(encoded);
						}
						// System.out.println( this.ruleName() + " " + locationToString( encoded ) );
					}
				}
			} // comboCompleted
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