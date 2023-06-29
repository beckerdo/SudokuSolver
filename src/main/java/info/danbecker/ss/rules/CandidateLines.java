package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;

import java.util.ArrayList;
import java.util.List;

import static info.danbecker.ss.Utils.Unit;
import static info.danbecker.ss.Utils.DIGITS;
import static info.danbecker.ss.Utils.BOXES;
import static java.lang.String.format;

/**
 * CandidateLines - 
 * A line of digits in box 0 can knock out candidates in boxes 1, 2
 *
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class CandidateLines implements FindUpdateRule {
	/**
	 * Update candidates.
	 * locations of form
	 * int[] location = new int[]{rowi, -1, boxi, digi};
	 * int[] location = new int[]{-1, coli, boxi, digi};
	 */
	@Override
	public int update(Board board, Board solution, Candidates candidates, List<int[]> encs) {
		int updates = 0;
		if ( null == encs) return updates;
		for ( int enci = 0; enci < encs.size(); enci++ ) {
			int[] enc = encs.get(enci);
			int boxi = enc[2];
			int digit = enc[3];
			Unit unit = ( -1 == enc[0] ) ? Unit.COL : Unit.ROW;
			int uniti = ( -1 == enc[0] ) ? enc[1] : enc[0];

			// Validate if available
			List<RowCol> locs = candidates.findCandidatesNotInBox(enc[0],enc[1],boxi,digit);
			if ( null != solution) {
				for (int loci = 0; loci < locs.size(); loci++) {
					RowCol loc = locs.get(loci);
					int cellSolution = solution.get(loc);
					if (cellSolution == digit) {
						System.out.println("Candidates=\n" + candidates.toStringBoxed());
						throw new IllegalArgumentException(format("%s digit %d in %s %d of BOX %d wants to remove solution in %s.%n",
								ruleName(), digit, unit, uniti, boxi, loc));
					}
				}
			}
			System.out.printf( "%s digit %d in %s %d of BOX %d will remove candidates in %s.%n",
					ruleName(), digit, unit, uniti, boxi, RowCol.toString(locs) );
			updates += candidates.removeCandidateNotInBox(enc[0],enc[1],boxi,digit);
		}
		return updates;
	}

	/**
	 * A pair/triplet of candidates, if in the same row/col,
	 * can knock out candidates in other boxes in the same row/col
	 * <p>
	 * Search for only two same candidates in each box,
	 * see if row or col is the same,
     * if row match, see if other candidates exist on same row outside of box
	 * if col match, see if other candidates exist on same col outside of box
	 */
	@Override
	public List<int[]> find(Board board, Candidates candidates) {
		ArrayList<int[]> encs = new ArrayList<>();
		for (int digi = 1; digi <= DIGITS; digi++) {
			if (!board.digitCompleted(digi)) {
				for (int boxi = 0; boxi < BOXES; boxi++) {
					List<RowCol> potentials = candidates.getBoxLocs(boxi, digi);
					if ( 0 < potentials.size() ) {
						// Check for row/col match of all potentials
						RowCol first = potentials.get(0);
						if (2 == potentials.size() || 3 == potentials.size()) {
							boolean rowMatch = RowCol.rowsMatch(potentials);
							if (rowMatch && candidates.getRowCount(first.row(), digi) > potentials.size()) {
								// More candidates not in this box
								int rowi = potentials.get(1).row();
								int[] enc = new int[]{rowi, -1, boxi, digi};
								Utils.addUnique( encs, enc);
							}
							boolean colMatch = RowCol.colsMatch(potentials);
							if (colMatch && candidates.getColCount(first.col(), digi) > potentials.size()) {
								// More candidates not in this box
								int coli = potentials.get(1).col();
								int[] enc = new int[]{-1, coli, boxi, digi};
								Utils.addUnique( encs, enc);
							}
						}
					}
				} // boxi
			} // digit complete
		} // digit
		return encs;
	}

	/**
	 * encoding of form
	 * int[] enc = new int[]{rowi, -1, boxi, digi};
	 * int[] enc = new int[]{-1, coli, boxi, digi};
	 */
	@Override
	public String encodingToString(int[] enc) {
		if ( null == enc || 4 != enc.length)
			throw new IllegalArgumentException( "encoding should be length 4 for row,col,box,digit");
		String unit = -1 == enc[0] ? "col" : "row";
		int uniti = -1 == enc[0] ? enc[1] : enc[0];
		return String.format( "digit %d candidates in box %d point on %s %d",
				enc[3], enc[2], unit, uniti );
	}

	@Override
	public String ruleName() {
		return this.getClass().getSimpleName();
	}
}