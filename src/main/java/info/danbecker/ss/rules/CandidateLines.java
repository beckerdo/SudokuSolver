package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;

import java.util.ArrayList;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Utils.DIGITS;
import static info.danbecker.ss.Utils.BOXES;

/**
 * CandidateLines - 
 * A line of digits in box 0 can knock out candidates in boxes 1, 2
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class CandidateLines implements UpdateCandidatesRule {

	public CandidateLines() {
	}

	@Override
	/**
	 * Update candidates.
	 * locations of form
	 * int[] location = new int[]{rowi, -1, boxi, digi};
	 * int[] location = new int[]{-1, coli, boxi, digi};
	 */
	public int updateCandidates(Board board, Board solution, Candidates candidates, List<int[]> locations) {
		int updates = 0;
		if ( null == locations) return updates;
		if (locations.size() > 0) {
			int[] loc = locations.get(0);
			// Just correct item 1.
			// rowi,coli,boxi,digit
			// removeCandidateNotInBox(RowCol rowCol, int boxi, int digit)
			updates += candidates.removeCandidateNotInBox(loc[0],loc[1],loc[2],loc[3]);
		}
		return updates;
	}

	@Override
	/** 
	 * A pair/triplet of candidates, if in the same row/col, 
	 * can knock out candidates in other boxes in the same row/col
	 * 
	 * Search for only two same candidates in each box,
  	 * see if row or col is the same, 
     * if row match, see if other candidates exist on same row outside of box
	 * if col match, see if other candidates exist on same col outside of box
	 */
	public List<int[]> locations(Board board, Candidates candidates) {
		if (null == candidates)
			return null;
		ArrayList<int[]> locations = new ArrayList<>();
		for (int digi = 1; digi <= DIGITS; digi++) {
			if (!board.digitCompleted(digi)) {
				for (int boxi = 0; boxi < BOXES; boxi++) {
					List<RowCol> potentials = candidates.candidateBoxLocations(boxi, digi);
					if ( 0 < potentials.size() ) {
						// Check for row/col match of all potentials
						RowCol first = potentials.get(0);
						if (2 == potentials.size() || 3 == potentials.size()) {
							boolean rowMatch = RowCol.rowsMatch(potentials);
							if (rowMatch && candidates.candidateRowCount(first.row(), digi) > potentials.size()) {
								// More candidates not in this box
								int rowi = potentials.get(1).row();
								int[] location = new int[]{rowi, -1, boxi, digi};
								locations.add(location);
							}
							boolean colMatch = RowCol.colsMatch(potentials);
							if (colMatch && candidates.candidateColCount(first.col(), digi) > potentials.size()) {
								// More candidates not in this box
								int coli = potentials.get(1).col();
								int[] location = new int[]{-1, coli, boxi, digi};
								locations.add(location);
							}
						}
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