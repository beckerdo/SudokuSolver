package info.danbecker.ss.rules;

import info.danbecker.ss.Board;

import info.danbecker.ss.Candidates;
import info.danbecker.ss.Utils;
import info.danbecker.ss.Utils.Unit;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import static info.danbecker.ss.Candidates.ALL_COUNTS;
import static info.danbecker.ss.Utils.ROWS;
import static info.danbecker.ss.Utils.DIGITS;

/**
 * Swordfish occurs when a board has three rows/cols 
 * that contain two of more candidates for a number.
 * 
 * Use Swordfish when there are three rows/cols that 
 * each contain either two or three of a given candidate, 
 * and those numbers are aligned on exactly three cols/rows.
 * These numbers form a grid of nine squares.
 * 
 * At least six of these nine squares must be unsolved and 
 * contain the same candidate. The row/cols of the grid 
 * contain at least two and no more than three of this candidate, 
 * with at least one of the cols/rows containing more than three
 * of this candidate
 *  
 * For example, rows:
 * {189}{}*{89}{}*{*}
 * {}*
 * {89}{}*{*}{}*{18}
 * {}*
 * {*}{}*{58}{}*{58}
 * No extra 8s in rows, extra 8s in columns.
 * Remove the extra 8s in columns.
 * 
 * Info based on clues given at
 * https://www.sudokuoftheday.com/techniques/swordfish
 * 
 * Thonky has a very good counting explanation at
 * https://www.thonky.com/sudoku/sword-fish
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class Swordfish implements UpdateCandidatesRule {
	
	@Override
	// Location int [] index map
	// digit plus four rowCols A, B, C, D
	// digit at index 0 
	// first pair AB rowCols at indexes 1,2 and 3,4
	// second pair CD rowCols at indexes 5,6 and 7,8
	public int updateCandidates(Board board, Board solution, Candidates candidates, List<int[]> encs) {
		int updates = 0;
		if ( null == encs) return updates;
		for ( int enci = 0; enci < encs.size(); enci++ ) {
		   int[] enc = encs.get( enci );
		   int digit = enc[0];
		   // int rowCol = enc[1];
		   List<int[]> swLocs = new ArrayList<int[]>();
		   List<int[]> exLocs = new ArrayList<int[]>();
		   Swordfish.decode( enc, swLocs, exLocs );
		   
		   for (int exloci = 0; exloci < exLocs.size(); exloci++) {
			  int[] loc = exLocs.get(exloci);

			  // Validation, if available
			  if (null != solution) {
				  int cellStatus = solution.get(loc[0], loc[1]);
				  if (cellStatus == digit) {
					  throw new IllegalArgumentException(
						format("Rule %s would like to remove solution digit %d at loc %s.", 
							ruleName(), digit,	Utils.locationString(loc[0], loc[1])));
				  }
			  }

			  if (candidates.removeCandidate(loc[0], loc[1], digit))
				updates += 1;
		   }
		   System.out.println( format("Swordfish removed digit %d times from %d locations %s", 
				digit, exLocs.size(), Utils.locationsString(exLocs)));
		}
		return updates;
	}

	@Override
	/** 
     * a candidate digit two or three times in a rowCol and 
     * that rowCol is repeated on 3 different aligning rowCols
	 * can knock out candidates in other boxes in the same row/col
	 */
	public List<int[]> locations(Board board, Candidates candidates) {
		List<int[]> matched = new LinkedList<int[]>();
		for (int digi = 1; digi <= DIGITS; digi++) {
    		List<int[]> thisDigitMatch = locations(board, candidates, digi );
    		if ( 0 < thisDigitMatch.size()) {
    			matched.addAll( thisDigitMatch );
				return matched; // For now only return first find
    		}
		}
		return matched;
	}

	/** 
     * a candidate digit two or three times in a rowCol and 
     * that rowCol is repeated on 3 different aligning rowCols
	 * can knock out candidates in other boxes in the same row/col
	 */
	public List<int[]> locations(Board board, Candidates candidates, int digi) {
		List<int[]> matched = new LinkedList<int[]>();
		if (!board.digitCompleted(digi)) {
			// Look at rows and cols
			for ( int uniti = 0; uniti < 2; uniti++ ) {
	    		Unit unit = Unit.values()[ uniti ];
	    		List<int[]> thisUnitMatch = locations(board, candidates, digi, unit );
	    		if ( 0 < thisUnitMatch.size()) {
	    			matched.addAll( thisUnitMatch );
					return matched; // For now only return first find
	    		}
			}
		}
		return matched;
	}

	/** 
     * a candidate digit two or three times in a rowCol and 
     * that rowCol is repeated on 3 different aligning rowCols
	 * can knock out candidates in other boxes in the same row/col
	 */
	public List<int[]> locations(Board board, Candidates candidates, int digi, Unit unit) {
		List<int[]> matched = new LinkedList<int[]>();
		Unit otherUnit = (Unit.ROW == unit) ? Unit.COL : Unit.ROW;

		for (int uniti1 = 0; uniti1 < ROWS; uniti1++) {
			List<int[]> unit1Locs = candidates.candidateUnitGroupLocs( unit, uniti1, digi, ALL_COUNTS);
			if (eligible(unit, unit1Locs, candidates)) {
				for (int uniti2 = uniti1 + 1; uniti2 < ROWS; uniti2++) {
					List<int[]> unit2Locs = candidates.candidateUnitGroupLocs(unit, uniti2, digi, ALL_COUNTS);
					if (eligible(unit, unit2Locs, candidates)) {
						// Check that row1 and row 2 have at least one column match
						int loc12MatchCount = locMatches(otherUnit, unit1Locs, unit2Locs);
						if (0 < loc12MatchCount) {
							for (int uniti3 = uniti2 + 1; uniti3 < ROWS; uniti3++) {
								List<int[]> unit3Locs = candidates.candidateUnitGroupLocs(unit, uniti3, digi, ALL_COUNTS);
								if (eligible(unit, unit3Locs, candidates)) {
									// Check that row1 and row 2 have at least one column match
									int loc23MatchCount = locMatches(otherUnit, unit2Locs, unit3Locs);
									if (0 < loc23MatchCount) {
										// Check that we have exactly three cols
										int [] unitLocCounts = unitLocCounts( otherUnit, unit1Locs, unit2Locs, unit3Locs);
										if (unitAlignment(otherUnit, unitLocCounts)) {
											System.out.println(format("Rule %s digit %d has %s alignment on %ss %s, %s, %s",
													ruleName(), digi, otherUnit.name(), unit.name(),
													Utils.locationsString(unit1Locs), Utils.locationsString(unit2Locs),
													Utils.locationsString(unit3Locs)));
											// Now check for extra candidates
											List<int[]> locs = mergeLists(unit1Locs, unit2Locs, unit3Locs);
											List<int[]> extraCandidates = extraCandidates( candidates, otherUnit, digi, unitLocCounts, locs );
											if ( 0 < extraCandidates.size() ) {
												// Encode and locations
												System.out.println(format("Rule %s digit %d has %s extra %s candidates at %s",
													ruleName(), digi, extraCandidates.size(), otherUnit.name(),
													Utils.locationsString(extraCandidates)));
												matched.add( encode(digi,unit.ordinal(),locs,extraCandidates));
												return matched; // For now only return first find

											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return matched;
	}

	/** Must be two locations, and each location has 2 or 3 of in the group.
	 * Might want to add a check that the digit is in the group.
	 */
	public boolean eligible( Unit unit, List<int[]> locs, Candidates candidates ) {
		if ( 2 == locs.size() ) {
			if ( 2 == candidates.candidateCount( locs.get( 0 )) ||
			     3 == candidates.candidateCount( locs.get( 0 )) ) {
				if ( 2 == candidates.candidateCount( locs.get( 1 )) ||
				     3 == candidates.candidateCount( locs.get( 1 )) ) {
					return true;
				}				
			}
		}
		return false;
	}     

	/** Must be two locations, and each location has 2 or 3 of in the group.
	 * Might want to add a check that the digit is in the group.
	 */
	public int locMatches( Unit unit, List<int[]> locs1, List<int[]> locs2 ) {
		int count = 0;
		for ( int[] loc1 : locs1 ) {
			for ( int [] loc2 : locs2 ) {
				switch (unit) {
					case ROW: {
					if ( loc1[0] == loc2[0] )
						count++;
					break;
					}
					case COL: {
					if ( loc1[1] == loc2[1] )
						count++;
					break;
					}
					case BOX: {	}				
				}
			}
		}
		return count;
	}     

	/** Units must match and be covered by two of three lists..
	 * Might want to add a check that the digit is in the group.
	 */
	public boolean unitAlignment( Unit unit, int [] unitCounts ) {
		// There must be exactly 3 non-zero units with two or more participants.
		int nonZeros = 0;
		for ( int uniti=0; uniti < ROWS; uniti++ ) {
			if ( 0 < unitCounts[ uniti ] ) {
				nonZeros++;
				if ( nonZeros > 3)
				   return false;
				if ( !(2 == unitCounts[ uniti] || 3 == unitCounts[ uniti ]) )
					return false;
			}
		}
		if ( nonZeros == 3) 
			return true;
		return false;
	}     

	/**
	 * Checks the total digit candidates in the unit to list the
	 * excess candidate rowCols
	 */
	public List<int []> extraCandidates( Candidates candidates, Unit unit, int digi, int[] unitLocCounts, List<int[]> locs ) {
		List<int[]> matched = new LinkedList<int[]>();
		for ( int uniti = 0; uniti < ROWS; uniti++) {
			if ( 2 == unitLocCounts[ uniti ] || 3 == unitLocCounts[ uniti ] ) {
				int unitCount = candidates.candidateCount( unit, uniti, digi);
				if ( unitCount > unitLocCounts[ uniti ] ) {
					// We know this uniti has more candidates than just the swordfish locations. Now enumerate them.
					for( int extrai = 0; extrai < ROWS; extrai++) {
						int[] extraLoc = new int[] {-1,-1};
						if ( Unit.ROW == unit ) {
							extraLoc[0] = uniti; extraLoc[1] = extrai;							
						} else if ( Unit.COL == unit ) {
							extraLoc[1] = uniti; extraLoc[0] = extrai;														
						}
						
						if (Board.NOT_FOUND == Utils.indexOf( locs, extraLoc )) {
							if ( candidates.isCandidate( extraLoc, digi)) {
								matched.add(extraLoc);
							}						
						}
					}					
				}
			}			
		}		
		return matched;		
	}

	public int[] unitLocCounts( Unit unit, List<int[]> locs1, List<int[]> locs2, List<int[]> locs3 ) {
		int [] unitCounts = new int [] {0, 0, 0, 0, 0, 0, 0, 0, 0 };
		for ( int[] loc: locs1 ) {
			switch (unit) {
			case ROW: { unitCounts[loc[0]]++; break; }
			case COL: { unitCounts[loc[1]]++; break; }
			case BOX: {	}				
			}
		}
		for ( int[] loc: locs2 ) {
			switch (unit) {
			case ROW: { unitCounts[loc[0]]++; break;}
			case COL: { unitCounts[loc[1]]++; break;}
			case BOX: {	}				
			}
		}
		for ( int[] loc: locs3 ) {
			switch (unit) {
			case ROW: { unitCounts[loc[0]]++; break;}
			case COL: { unitCounts[loc[1]]++; break;}
			case BOX: {	}				
			}
		}
		return unitCounts;		
	}

	
	/** Converts the 3 rowCol lists (which may be size 2) into one 9 rowCol list.
	 */
	public List<int[]> mergeLists( List<int[]> locs1, List<int[]> locs2, List<int[]> locs3 ) {
		SortedSet<Integer> rows = new TreeSet<Integer>(); 
		SortedSet<Integer> cols = new TreeSet<Integer>();
		
		for ( int[] loc: locs1 ) {
			rows.add( loc[0]);
			cols.add( loc[1]);			
		}
		for ( int[] loc: locs2 ) {
			rows.add( loc[0]);
			cols.add( loc[1]);			
		}
		for ( int[] loc: locs3 ) {
			rows.add( loc[0]);
			cols.add( loc[1]);			
		}
		if ( 3 != rows.size() ) {
			throw new IllegalArgumentException(format("Expected 3 but only %d rows from lists %s, %s, %s",
				rows.size(),
				Utils.locationsString(locs1), Utils.locationsString(locs2),	Utils.locationsString(locs3)));
		}
		if ( 3 != cols.size() ) {
			throw new IllegalArgumentException(format("Expected 3 but only %d cols from lists %s, %s, %s",
				rows.size(),
				Utils.locationsString(locs1), Utils.locationsString(locs2),	Utils.locationsString(locs3)));
		}
		List<int[]> locs = new ArrayList<int[]>();
		for( int rowi : rows ) {
			for ( int coli : cols ) {
				locs.add( new int [] { rowi, coli } );
			}
		}		
		return locs;		
	}
		
	// Encode int [] index map
	// - digit at index 0, 1-based
	// - rowCol orientation at index 1, 0 == row, 1 == col
	// - nine Swordfish rowCols A,B,C, D,E,F, G,H,I
	// Because Swordfish locs form a tic/tac/tow box, 
	// there should be a match of either
	//    - rows, (ABC), (DEF), (GHI) 
	//    - cols, (ADG), (BEH), (CFI)
	// - extra candidate location count (0..n)
	// - extra candidate locations
	// Repeat for other digits 
	public static int [] encode( int digi, int rowCol, List<int[]> swlocs, List<int[]> exlocs ){		
		if ( digi < 1 || digi > 9) 
			throw new IllegalArgumentException( "digit=" + digi);
		if ( rowCol < 0 || rowCol > 1) 
			throw new IllegalArgumentException( "rowCol=" + rowCol);
		if ( null == swlocs ) 
			throw new IllegalArgumentException( "swlocs=null" );
		if ( 9 != swlocs.size() ) 
			throw new IllegalArgumentException( "swlocs length=" + swlocs.size() );
		if ( null == exlocs ) 
			throw new IllegalArgumentException( "exlocs=null" );
		
		String error = "";
		if (0 == rowCol) {
			for ( int uniti = 0; uniti < 3; uniti++ ) {
				int anchor = swlocs.get(uniti * 3)[0];
				if ( anchor != swlocs.get(uniti * 3 + 1)[0] || 
				     anchor != swlocs.get(uniti * 3 + 2)[0]) 
						error += " row " + anchor + " mismatch";				
			}
		} else {
			for ( int uniti = 0; uniti < 3; uniti++ ) {
				int anchor = swlocs.get(uniti)[1];
				if ( anchor != swlocs.get(uniti + 3)[1] || 
				     anchor != swlocs.get(uniti + 6)[1]) 
						error += " col " + anchor + " mismatch";				
			}
		}
		if (error.length() > 0)
			throw new IllegalArgumentException(error);

		int [] enc = new int[ 2 + 2*swlocs.size() + 1 + 2*exlocs.size()];
		enc[ 0 ] = digi;
		enc[ 1 ] = rowCol;
		// Row order ABC, DEF, GHI
		int offset = 2;
		for ( int loci = 0; loci < swlocs.size(); loci++ ) {
			int [] loc = swlocs.get(loci);
			enc[offset+2*loci] = loc[0];
			enc[offset+2*loci+1] = loc[1];
		}
		// Encode extra candidate size and locations.
		offset = 2 + 2 * swlocs.size();
		enc[offset] = exlocs.size();
		offset += 1;
		for ( int loci = 0; loci < exlocs.size(); loci++ ) {
			int [] loc = exlocs.get( loci );
			enc[ offset ] = loc[0]; 
			enc[ offset + 1 ] = loc[1];
			offset += 2;
		}	
		
		return enc;
	}
	
	// Decode the encoding back into the Swordfish locations and extra candidate locations
	// The caller must provide empty lists and also manually decode digit and rowCol.
	public static void decode( int [] enc, List<int[]> swlocs, List<int[]> exlocs ) {
		if( null == enc )
			throw new IllegalArgumentException( "enc must not be null");
		if( null == swlocs )
			throw new IllegalArgumentException( "swlocs must not be null");
		if( 0 != swlocs.size())
			throw new IllegalArgumentException( "swlocs must be empty");
		if( null == exlocs )
			throw new IllegalArgumentException( "exlocs must not be null");
		if( 0 != exlocs.size())
			throw new IllegalArgumentException( "exlocs must be empty");

		// int [] enc = new int[ 2 + 2*swlocs.size() + 1 + 2*exlocs.size()];
		int swlocsOffset=2;
		int exsizeOffset=2 + 9*2;
		int exsize= (enc.length - exsizeOffset - 1) / 2;
		if ( exsize != enc[exsizeOffset] )
			throw new IllegalArgumentException( "Enc length=" + enc.length + ",enc[] exsize=" + enc[exsizeOffset] + ", exsize=" + exsize );
		
		// Swordfish locations row order ABC, DEF, GHI
		for ( int loci = 0; loci < 9; loci++ ) {
			int [] loc = new int [] {enc[swlocsOffset+2*loci],enc[swlocsOffset+2*loci+1]}; 
			swlocs.add(loc);			
		}
		
		// Extra candidate locations base on size
		exsizeOffset += 1; // skip over size int
		for ( int loci = 0; loci < exsize; loci++ ) {
			int [] loc = new int [] {enc[exsizeOffset+2*loci],enc[exsizeOffset+2*loci+1]}; 
			exlocs.add(loc);			
		}
	}
	
	public static String encodingToString( int[] enc) {
		List<int[]> swLocs = new ArrayList<int[]>();
		List<int[]> exLocs = new ArrayList<int[]>();
		decode( enc, swLocs, exLocs );

		int digit = enc[0];
		int rowCol = enc[1];	
		StringBuffer sb = new StringBuffer(format( "digit %d %s at locs=", digit, (rowCol==0)?"rows":"cols"));
		for ( int loci=0; loci < 3; loci++) {
			String delim = (0==loci)?"":",";
			int tuple = loci*3;
			sb.append(format("%s[%d,%d][%d,%d][%d,%d]",
				delim, 
				swLocs.get(tuple)[0],swLocs.get(tuple)[1],
				swLocs.get(tuple+1)[0],swLocs.get(tuple+1)[1],
				swLocs.get(tuple+2)[0],swLocs.get(tuple+2)[1]));
		}
		sb.append( format(", extra locs (size=%d)", exLocs.size()));
		if ( exLocs.size() > 0 )
			sb.append("=");
		for ( int loci=0; loci < exLocs.size(); loci++) {
			String delim = (0==loci)?"":",";
			int[] loc = swLocs.get(loci);
			sb.append(format("%s[%d,%d]",
				delim, loc[0],loc[1]));
		}
		return sb.toString();
	}
	
	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}