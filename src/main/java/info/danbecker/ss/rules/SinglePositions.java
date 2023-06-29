package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;

import static info.danbecker.ss.Board.ROWCOL;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static info.danbecker.ss.Utils.Unit;
import static info.danbecker.ss.Utils.UNITS;
import static info.danbecker.ss.Utils.DIGITS;

/**
 * Finds digits that can be placed in just one row/col/block
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class SinglePositions implements FindUpdateRule {
	@Override
	public int update(Board board, Board solution, Candidates candidates, List<int[]> encs) {
		int updateCount = 0;
		Map<Integer,List<RowCol>> updates = new HashMap<>();
		for ( int enci = 0; enci < encs.size(); enci++ ) {
			int[] enc = encs.get(enci);
			int digit = enc[0];
			RowCol loc = ROWCOL[enc[1]][enc[2]];
			// Validation if available
			if (null != solution) {
				int solutionDigit = solution.get(loc);
				if (solutionDigit != digit) {
					System.out.println("Candidates=\n" + candidates.toStringBoxed());
					throw new IllegalArgumentException(format("Rule %s would like to set digit %d at loc %s with solution digit %d",
							ruleName(), digit, loc, solutionDigit));
				}
			}
			board.set(loc, digit); // put a digit in the board
			int prevCount = candidates.getAllCount();
			candidates.setOccupied(loc, digit); // places entry, removes candidates
			int currCount = candidates.getAllCount();
			updates.computeIfAbsent(digit, x -> new ArrayList<>()).add(loc);
			updateCount += prevCount - currCount;
		}
		// Check counts
		int valueCount = updates.values()
				.stream()
				.mapToInt(List::size)
				.sum();
		// Pretty output
		if ( 0 < updateCount ) {
			System.out.printf( "Rule %s removes %d cands, places: %s%n", ruleName(), updateCount, Utils.digitMapToString( updates ) );
		}
		return updateCount;
//
//
//
//		if (encs.size() > 0) {
//			int[] location = encs.get(0);
//			int rowi = location[ 0 ];
//			int coli = location[ 1 ];
//			// int boxi = location[ 2 ];
//			int digit = location[ 3 ];
//
//			// Validation if available
//			if ( null != solution ) {
//				int solutionDigit = solution.get(ROWCOL[rowi][coli]);
//				if ( solutionDigit != digit ) {
//					System.out.println( "Candidates=\n" + candidates.toStringBoxed() );
//					throw new IllegalArgumentException( format("Rule %s would like to place digit %s at loc %s with solution digit %d.",
//							ruleName(), digit, ROWCOL[rowi][coli], solutionDigit));
//				}
//			}
//
//			System.out.println(format("Rule %s places digit %d at rowCol [%d,%d]", ruleName(), digit, rowi, coli));
//			board.set(ROWCOL[rowi][coli], digit); // simply puts a digit in the board
//			candidates.setOccupied(ROWCOL[rowi][coli], digit); // places entry, removes candidates
//			return 1;
//		}
//		return 0;
	}

	@Override
	public List<int[]> find(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		ArrayList<int[]> encs = new ArrayList<>();
		for (int digi = 1; digi <= DIGITS; digi++) {
			if (!board.digitCompleted(digi)) {
				for ( Unit unit : Unit.values() ) {
					for ( int uniti = 0; uniti < UNITS; uniti++ ) {
						List<RowCol> locs = candidates.getUnitDigitLocs( unit, uniti, digi );
						if ( 1 == locs.size() ) {
							// Only one in this unit
							Utils.addUnique( encs, new int[]{ digi, locs.get(0).row(), locs.get(0).col() } );
						}
					}
				}
			}
		}
		return encs;
	}

	@Override
	public String encodingToString(int[] enc) {
		int digit = enc[ 0 ];
		return String.format("digit %d at %s", digit, ROWCOL[enc[1]][enc[2]]);
	}

	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}