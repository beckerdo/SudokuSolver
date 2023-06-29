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
import java.util.stream.Collectors;

import static info.danbecker.ss.Utils.ROWS;
import static info.danbecker.ss.Utils.COLS;

/**
 * Finds locations with just one candidate.
 * Updates multiple single candidate locations.
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class SingleCandidates implements FindUpdateRule {
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
			candidates.setOccupied(loc, digit); // places entry, removes candidates
			updates.computeIfAbsent(digit, x -> new ArrayList<>()).add(loc);
			updateCount++;
		}
		// Check counts
		int valueCount = updates.values()
				.stream()
				.mapToInt(List::size)
				.sum();
		if ( updateCount != valueCount )
			throw new IllegalStateException( format("update count=%d, location count=%d", updateCount, valueCount));
		// Pretty output
		if ( 0 < valueCount ) {
			System.out.printf( "Rule %s places: %s%n", ruleName(), Utils.digitMapToString( updates ) );
		}
		return updateCount;
	}

	@Override
	public List<int[]> find(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		ArrayList<int[]> locations = new ArrayList<>();
		for (int rowi = 0; rowi < ROWS; rowi++) {
			for (int coli = 0; coli < COLS; coli++) {
				List<Integer> cDigits = candidates.getCandidatesList(ROWCOL[rowi][coli]);
				if (1 == cDigits.size()) {
					locations.add(new int[] { cDigits.get(0), rowi, coli });
				}
			}
		}
		return locations;
	}

	@Override
	public String encodingToString(int[] enc) {
		int digit = enc[0];
		RowCol rowCol = ROWCOL[enc[1]][enc[2]];
		return String.format("single candidate %d at %s", digit, rowCol);
	}

	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}