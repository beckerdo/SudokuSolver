package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.Utils;

import java.util.ArrayList;
import java.util.List;

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
	public int updateCandidates(Board board, Board solution, Candidates candidates, List<int[]> locations) {
		int updates = 0;
		if ( null == locations) return updates;
		if (locations.size() > 0) {
			int[] loc = locations.get(0);
			// Just correct item 1.
			// rowi,coli,boxi,digit
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
		ArrayList<int[]> locations = new ArrayList<int[]>();
		for (int digi = 1; digi <= DIGITS; digi++) {
			if (!board.digitCompleted(digi)) {
				for (int boxi = 0; boxi < BOXES; boxi++) {
					List<int[]> potentials = candidates.candidateBoxLocations(boxi, digi);
					// Check for row/col match of all potentials
					if (2 == potentials.size() || 3 == potentials.size()) {
						boolean rowMatch = Utils.rowsMatch(potentials);
						if (rowMatch && candidates.candidateRowCount(potentials.get(0)[0], digi) > potentials.size()) {
							// More candidates not in this box
							int rowi = potentials.get(1)[0];
							int[] location = new int[] { rowi, -1, boxi, digi };
							locations.add(location);
							// System.out.println(format("Rule %s reports %d item row line potential at
							// row/col/boxi/digi %s",
							// ruleName(), potentials.size(), Arrays.toString(location)));
							// for ( int coli = 0; coli< COLS; coli++ ) {
							// System.out.println( format("Row%d/col%d candidates:%s", rowi,coli,
							// candidates.compactCandidates(rowi, coli)));
							// }
						}
						boolean colMatch = Utils.colsMatch(potentials);
						if (colMatch && candidates.candidateColCount(potentials.get(0)[1], digi) > potentials.size()) {
							// More candidates not in this box
							int coli = potentials.get(1)[1];
							int[] location = new int[] { -1, coli, boxi, digi };
							locations.add(location);
							// System.out.println(format("Rule %s reports %d item col line potential at
							// row/col/boxi/digi %s",
							// ruleName(), potentials.size(), Arrays.toString(location)));
							// for ( int rowi = 0; rowi< ROWS; rowi++ ) {
							// System.out.println( format("Row%d/col%d candidates:%s", rowi,coli,
							// candidates.compactCandidates(rowi, coli)));
							// }
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