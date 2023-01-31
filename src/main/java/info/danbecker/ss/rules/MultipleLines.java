package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.Utils;

import java.util.ArrayList;
import java.util.List;

import static info.danbecker.ss.Utils.DIGITS;
import static info.danbecker.ss.Utils.BOXES;

/**
 * MultipleLines - like CandidateLines but requires 2 lines 
 * CandidateLines - A line of digits in box 0 can knock out candidates one line in boxes 1, 2
 * MultipleLines - Two lines of digits in boxes 0,1 can remove candidates on to lines in box 2
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class MultipleLines implements UpdateCandidatesRule {

	public MultipleLines() {
	}

	@Override
	public int updateCandidates(Board board, Board solution, Candidates candidates, List<int[]> locations) {
		int updates = 0;
		if ( null == locations) return updates;
		if (locations.size() > 0) {
			int [] enc = locations.get(0);
			// Just correct item 1.
			// Encoding [] = digit, row 0|col 1, boxML0, boxML1, savebox, ml0, ml1, keepline
			int digi = enc[0];
			boolean rowEncoding = (enc[1] == 0);
			int keepRowCol = enc[7];
			int candBox = enc[4];			
			if ( rowEncoding ) {
				updates += candidates.removeBoxCandidatesNotInRow(digi, candBox, keepRowCol );
			} else {
				updates += candidates.removeBoxCandidatesNotInCol(digi, candBox, keepRowCol );				
			}
		}
		return updates;
	}

	@Override
	/** 
	 * MultipleLines - Two lines of digits in boxes 0,1 can remove candidates in box 2
	 * 
	 * Search for only two same candidates in each block,
  	 * see if row or col is the same, 
     * if row match, see if other candidates exist on same row outside of block
	 * if col match, see if other candidates exist on same col outside of block
	 */
	public List<int[]> locations(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		ArrayList<int[]> locations = new ArrayList<int[]>();
		for (int digi = 1; digi <= DIGITS; digi++) {
			if (!board.digitCompleted(digi)) {
				for (final boolean rowOrientation : new boolean [] { true, false } ){
				// String orientation = rowOrientation ? "row" : "col";

				// For each row of boxes (0,3,6) count 3 rows of 3 cells
				// For example, box 0 has row counts for rows {0,1,2} summing cols {0,1,2}
				// For example, box has row counts for rows {0,1,2} summing cols {3,4,5}
				// For example, box 2 has row counts for rows {0,1,2} summing cols {6,7,8}
				// ...
				// For example, box 8 has row counts for rows {6,7,8} summing cols {6,7,8}

				// Counts are by boxes with each box having 3 rowCol segments of 3 cells
				int[][] counts = new int[][] { 
					new int[] { 0, 0, 0 }, new int[] { 0, 0, 0 }, new int[] { 0, 0, 0 }, // boxi
				};
				// for (int boxTuplei = BOXES/3 - 1; boxTuplei >= 0; boxTuplei++) {
				for (int boxTuplei = 0; boxTuplei < BOXES / 3; boxTuplei++) {
					for (int tupi = 0; tupi < BOXES / 3; tupi++) {
						// Rows boxis go 0, 1, 2, col boxis go 0,3,6
						int boxi = rowOrientation ? (boxTuplei * 3 + tupi) : (tupi * 3 + boxTuplei);
						
						// If first block of rowCol, zero out counts (Beware first blocki might !=0 for cols)
						if (0 == tupi) {
							for (int i = 0; i < BOXES / 3; i++) {
								int[] boxCounts = counts[i];
								for (int j = 0; j < boxCounts.length; j++) {
									boxCounts[j] = 0;
								}
							}
						}
						
						// Sum candidate count for each row of each block
						int[][] locs = rowOrientation ? 
								Board.getBoxRowCols(boxi) : Board.getBoxRowColsC(boxi);
						for (int segi = 0; segi < 3; segi++) {
							// Take first, middle, or last 3 locations - Make a first set, middle, last API							
							counts[tupi][segi] += candidates.candidateDigitRowColCount(digi,
									new int[][] { locs[segi * 3], locs[segi * 3 + 1], locs[segi * 3 + 2] });
						}
						
						// If last block of rowCol, see if any locations can be encoded
						if (2 == tupi) {
							// Check for a match in this block tuple
							int [] match = multiMatch( counts );
							if (null != match) {
								// We are at the end of a row column and have counts for this digit.
								// We handed multiMatch a list of 3 boxes with 3 segment counts.
								// MultiMatch analyzed the scores and returns the
								// segment indexes 0,1 which may be cleared, followed by
								// segment index 2, and box index, which contains the candidate,
								// and should not be cleared.								
								// System.out.println(java.lang.String.format(
								// 	"Box %d digit %d %s counts=%s,%s,%s", blocki,
								// 	digi, orientation, 
								//	Arrays.toString(counts[0]),Arrays.toString(counts[1]),Arrays.toString(counts[2])));
								
								// System.out.println(java.lang.String.format(
								// 	"Box %d digit %d %s multi lines in box indexes %d,%d, keeper candidates in box index %d", 
								// 	boxi,	digi, orientation, 
								// 	match[0], match[1], match[2] ));

								// Translate to hard block and row information.								
							    // Rows block start at 0, 3, 6, col blocks start at  0,1,2
								int startBoxi = rowOrientation ? (boxTuplei * 3) : (boxTuplei); 
							    int boxi0ML = rowOrientation ? (startBoxi + match[ 0 ]) : (startBoxi + 3 * match[ 0 ]);
							    int boxi1ML = rowOrientation ? (startBoxi + match[ 1 ]) : (startBoxi + 3 * match[ 1 ]);
							    int boxi2ML = rowOrientation ? (startBoxi + match[ 2 ]) : (startBoxi + 3 * match[ 2 ]);
							    
							    int multLine0 = rowOrientation ? startBoxi + match[ 3 ] : startBoxi * 3 + match[ 3 ];
							    int multLine1 = rowOrientation ? startBoxi + match[ 4 ] : startBoxi * 3 + match[ 4 ];
							    int keepLine = rowOrientation ? startBoxi + match[ 5 ] : startBoxi * 3 + match[ 5 ];
								int [] encoding = new int [] {
									digi, rowOrientation?0:1, 
									boxi0ML, boxi1ML, boxi2ML,
									multLine0, multLine1, keepLine,
								};
								locations.add(encoding);
								System.out.println(encodingToString(encoding));
							}
						}
					} // tuple index
				} // box tuple
				} // rowCol orientation
			} // digi
		}
		return locations;
	}

	/** 
	 * Given a two dimensional array with 3 box counts of 3 segments each, 
	 * returns the box and segment indexes which have multiple lines,
	 * and which may be cleared, lastly candidate index and location 
	 * which should be saved.
	 * @returns null with no combo
	 */
	public int [] multiMatch( int [][] counts ) {
		if ( null == counts || counts.length < 3 )
			throw new IllegalArgumentException( "Counts should be non null and length of 3" );
		// The first 2 combo digits are the multiple rowCols, the third is the non combo candidate rowCol 
		int [][] combos = new int[][]{ new int[] {0,1,2}, new int[]{0,2,1}, new int[]{1,2,0}};
		for ( int combi = 0; combi < combos.length; combi++) {
			int [] combo = combos[ combi ];
			// Example, boxwa 0/3/6 each have 3 segment counts for digit 5.
			//    Box 0 digit 5 col counts [2, 1, 0]
			//    Box 3 digit 5 col counts [3, 0, 2]
			//    Box 6 digit 5 col counts [1, 2, 0]
			// Multiple lines connect box indexes 0,2 in segments 0,1.
			// Box index 1, segment index 2 is candidate, 10,11 may be cleared.
			// Multiple lines occurs when 
			//   2 of the 3 box indexes combos (01,02,12)
			//   have 2 of 3 segments (01,02,12) 
			//   with count patterns ++0 in the segment combo
			//   and count pattern **+ in the non segment combo
			// For this example
			//   Box indexes 0,2 have non-zeros in segment 0,1, zeros in segment index 2
			//   Box index 1 has non-zeros in segments 0,1, non-zero in segment index 2
			// Return multiple line indexes 0,1 and save candidate in block index 1, seg index 2.
			
			// Assume combo contains the multiples and has one empty rowCol 
			if ( 1 == Utils.count( counts[ combo[0] ], 0 )  && 
				 1 == Utils.count( counts[ combo[1] ], 0 ) ) {
				// Zeros must be in same location rowCol
				if (Utils.location( counts[ combo[0] ], 0) == Utils.location( counts[ combo[1] ], 0) ) {
					// Check that the non combo rowCol has a candidate count
					int saveCandidateLoc = Utils.location( counts[ combo[0] ], 0 );
					// Ensure candidates to save
					if ( counts[ combo[2] ][ saveCandidateLoc ] > 0 ) {
					    // Ensure candidates to remove in non save locations						
						if ((counts[combo[2]][(saveCandidateLoc + 1) % 3] > 0) ||
						    (counts[combo[2]][(saveCandidateLoc + 2) % 3] > 0)) {
							int removeLoc0 = (0 == saveCandidateLoc) ? 1 : 0;
							int removeLoc1 = (2 == saveCandidateLoc) ? 1 : 2;
							// Return multiple line box and segment indexes 0 and 1, which may be cleared,
							// then the candidate box index and location which should be saved.
							// Also return the rowCol for clear, rowCol for save
							return new int[] { combo[0], combo[1], combo[2], removeLoc0, removeLoc1, saveCandidateLoc };
						}
					}		
				}
			}			
		}
		return null;
	}
	
	/**
	 * Gives a useful string for the encoding
	 */
	public String encodingToString( int [] enc ) {
		if ( null == enc ) return "null";
		if ( enc.length != 8 ) return "bad length " + enc.length;

		// Encoding [] = digit, row 0|col 1, blockML0, blockML1, saveBlock, ml0, ml1, keepline
		String orientation = enc[1] == 0 ? "row" : "col";
		return java.lang.String.format(
			"%s digit %d %ss %d,%d in boxes %d,%d, keeper candidates %s %d in box %d", 
			ruleName(), enc[0], 
			orientation, enc[5], enc[6], enc[2], enc[3],
			orientation, enc[7],enc[4]
		);
	}
	
	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}