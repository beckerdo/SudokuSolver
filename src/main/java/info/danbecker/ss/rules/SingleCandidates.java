package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;

import static info.danbecker.ss.Board.ROWCOL;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import static info.danbecker.ss.Utils.ROWS;
import static info.danbecker.ss.Utils.COLS;

/**
 * Finds locations with just one candidate.
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class SingleCandidates implements UpdateCandidatesRule {

	public SingleCandidates() {
	}

	@Override
	public int updateCandidates(Board board, Board solution, Candidates candidates, List<int[]> locations) {
		if (locations.size() > 0) {
			int[] location = locations.get(0);
			int rowi = location[0];
			int coli = location[1];
			int digit = candidates.getCandidateDigit(ROWCOL[rowi][coli]);

			System.out.println(format("Rule %s places digit %d at rowCol [%d,%d]", ruleName(), digit, rowi, coli));
			board.set(ROWCOL[rowi][coli], digit); // simply puts a digit in the board
			candidates.setOccupied(ROWCOL[rowi][coli], digit); // places entry, removes candidates
			return 1;
		}
		return 0;
	}

	@Override
	public List<int[]> locations(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		ArrayList<int[]> locations = new ArrayList<>();
		for (int rowi = 0; rowi < ROWS; rowi++) {
			for (int coli = 0; coli < COLS; coli++) {
				if (1 == candidates.candidateCount(ROWCOL[rowi][coli])) {
					locations.add(new int[] { rowi, coli });
				}
			}
		}
		return locations;
	}

	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}
