package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;

import static info.danbecker.ss.Board.ROWCOL;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

import static info.danbecker.ss.Utils.ROWS;
import static info.danbecker.ss.Utils.COLS;
import static info.danbecker.ss.Utils.DIGITS;
import static info.danbecker.ss.Utils.BOXES;
import static info.danbecker.ss.Board.NOT_FOUND;

/**
 * Finds digits that can be placed in just one row/candidate/block
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class SinglePositions implements UpdateCandidatesRule {

	public SinglePositions() {
	}

	@Override
	public int updateCandidates(Board board, Board solution, Candidates candidates, List<int[]> locations) {
		if (locations.size() > 0) {
			int[] location = locations.get(0);
			int rowi = location[ 0 ];
			int coli = location[ 1 ];
			// int boxi = location[ 2 ];
			int digit = location[ 3 ];

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
		for (int digi = 1; digi <= DIGITS; digi++) {
			if (!board.digitCompleted(digi)) {
				for (int rowi = 0; rowi < ROWS; rowi++) {
					if (1 == candidates.candidateRowCount(rowi, digi)) {
						// Add this digit, row
						locations.add(new int[] { rowi, candidates.candidateRowLocation(rowi, digi), NOT_FOUND, digi });
					}
				}
				for (int coli = 0; coli < COLS; coli++) {
					if (1 == candidates.candidateColCount(coli, digi)) {
						// Add this digit, coli
						locations.add(new int[] { candidates.candidateColLocation(coli, digi), coli, NOT_FOUND, digi });
					}
				}
				// Might not be needed. Row col searches seem to work
				for (int boxi = 0; boxi < BOXES; boxi++) {
					if (1 == candidates.candidateBoxCount(boxi, digi)) {
						// Add this digit, boxi
						RowCol rowCol = candidates.candidateBoxLocation(boxi, digi);
						locations.add(new int[] { rowCol.row(), rowCol.col(), boxi, digi });
					}
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
