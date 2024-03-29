package info.danbecker.ss;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

import info.danbecker.ss.rules.LegalCandidates;
import info.danbecker.ss.tree.ChangeData;

import org.junit.jupiter.api.BeforeEach;

import static info.danbecker.ss.Board.ROWCOL;
import static info.danbecker.ss.Candidates.*;
import static info.danbecker.ss.Utils.Unit.*;
import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;

public class CandidatesTest {
	@BeforeEach
	public void setup() {
	}

	public static String BOARD1 = "...1....1\n38.....2.\n.2..96..4\n.4...7...\n9.......3\n...4...5.\n5..67..3.\n.3.....79\n8....2...";
	public static String BOARD2 = "...2....1\n38.....2.\n.2..96..4\n.4...7...\n9.......3\n...4...5.\n5..67..3.\n.3.....79\n8....2...";

	@Test
	public void testBasics() throws ParseException {
		Board board1 = new Board(BOARD1);
		Candidates candidates1 = new Candidates(board1);
		Board board2 = new Board(BOARD2);
		Candidates candidates2 = new Candidates(board2);
		Board board3 = new Board(BOARD1);
		Candidates candidates3 = new Candidates(board3);

		assertEquals(1, candidates1.compareTo(null));
		assertEquals(0, candidates1.compareTo(candidates1));
		assertEquals(0, candidates1.compareTo(candidates3));
		assertEquals(-1, candidates1.compareTo(candidates2));

		assertEquals(1, Candidates.compareCandidates(Arrays.asList(6, 1), Arrays.asList(1, 5)));
		assertEquals(-1, Candidates.compareCandidates(Arrays.asList(1, 4), Arrays.asList(5, 1)));
		assertEquals(0, Candidates.compareCandidates(Arrays.asList(1, 4), Arrays.asList(4, 1)));
		assertEquals(1, Candidates.compareCandidates(Arrays.asList(1, 2, 3), Arrays.asList(1, 2)));
		assertEquals(-1, Candidates.compareCandidates(Arrays.asList(1, 2), Arrays.asList(1, 2, 3)));

		Candidates clone = new Candidates(candidates3);
		assertEquals(clone, candidates3);
		clone.setOccupied(ROWCOL[0][0], 1);
		assertEquals(1, clone.compareTo(candidates3));

		// Test sorted rowCols and sorted candidate lists
		(new LegalCandidates()).update(board1, null, candidates1, null);
		// System.out.println( "Candidates=\n" + candidates1.toStringBoxed());
		List<RowCol> pairLocs = candidates1.getGroupLocations(ALL_DIGITS, 2);
		pairLocs.sort(RowCol.RowColComparator);
		// Returns sorted RowCols, such as [[1,3], [1,4], [1,5], [2,0], [6,1], [6,8], [7,3], [8,8]]
		// System.out.println( "Pair rowCols=" + pairLocs.toString());
		assertEquals(ROWCOL[1][3], pairLocs.get(0));
		assertEquals(ROWCOL[8][8], pairLocs.get(pairLocs.size() - 1));

		List<List<Integer>> pairCands = new LinkedList<>();
		for (RowCol rowCol : pairLocs) {
			List<Integer> cands = candidates1.getCandidatesList(rowCol);
			if (!pairCands.contains(cands))
				pairCands.add(cands);
		}
		pairCands.sort(Candidates.CandidatesComparator);
		// Returns sorted candidate pairs such as [[1,7], [1,9], [2,8], [4,5], [5,6], [5,7], [5, 8]]
		assertEquals(Arrays.asList(1, 7), pairCands.get(0));
		assertEquals(Arrays.asList(5, 8), pairCands.get(pairCands.size() - 1));
	}

	@Test
	public void testCandidateAdd() throws ParseException {
		Board board1 = new Board(BOARD1);
		Candidates candidates1 = new Candidates(board1);

		// Single candidate, negative because it was given in Board
		assertTrue(Arrays.equals(new int[]{-1, 0, 0, 0, 0, 0, 0, 0, 0}, candidates1.getCandidates(ROWCOL[0][3])));
		assertTrue(Arrays.equals(new int[]{0, 0, 0, 0, 0, 0, 0, -8, 0}, candidates1.getCandidates(ROWCOL[1][1])));
		// All candidates
		assertTrue(Arrays.equals(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, candidates1.getCandidates(ROWCOL[8][8])));
		candidates1.setCandidates(ROWCOL[8][0], new int[]{0, 2, 0, 4, 0, 6, 0, 8, 0});
		assertTrue(Arrays.equals(new int[]{0, 2, 0, 4, 0, 6, 0, 8, 0}, candidates1.getCandidates(ROWCOL[8][0])));

		List<Integer> cands = candidates1.getCandidatesList(ROWCOL[8][0]);
		assertEquals(4, cands.size());
		// assertEquals( new int[] {2,4,6,8}, cands.toArray(new int[0]) );
		// assertEquals( new Integer[] {2,4,6,8}, cands.toArray(new Integer[0]) );
		assertTrue(Arrays.deepEquals(new Integer[]{2, 4, 6, 8}, cands.toArray(new Integer[0])));

		// Candidate remove, add, multiple times
		candidates1.removeCandidate(ROWCOL[8][8], 9);
		assertTrue(Arrays.equals(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 0}, candidates1.getCandidates(ROWCOL[8][8])));
		candidates1.removeCandidate(ROWCOL[8][8], 9);
		assertTrue(Arrays.equals(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 0}, candidates1.getCandidates(ROWCOL[8][8])));
		candidates1.removeCandidates(ROWCOL[8][8], new int[]{7, 8});
		assertTrue(Arrays.equals(new int[]{1, 2, 3, 4, 5, 6, 0, 0, 0}, candidates1.getCandidates(ROWCOL[8][8])));
		candidates1.addCandidate(ROWCOL[8][8], 9);
		assertTrue(Arrays.equals(new int[]{1, 2, 3, 4, 5, 6, 0, 0, 9}, candidates1.getCandidates(ROWCOL[8][8])));

		assertTrue(candidates1.isCandidate(ROWCOL[8][8], 9));
		candidates1.removeCandidate(ROWCOL[8][8], 9);
		assertFalse(candidates1.isCandidate(ROWCOL[8][8], 9));

		// Get Set occupied
		assertEquals(8, candidates1.getOccupied(ROWCOL[1][1]));
		assertEquals(0, candidates1.getOccupied(ROWCOL[8][8]));
		assertFalse(candidates1.isOccupied(ROWCOL[8][8]));
		candidates1.setOccupied(ROWCOL[8][8], 5);
		assertTrue(candidates1.isOccupied(ROWCOL[8][8]));
		assertEquals(5, candidates1.getOccupied(ROWCOL[8][8]));
	}

	@Test
	public void testCandidateRemove() throws ParseException {
		Board board = new Board(PAIRS);
		Candidates candidates = new Candidates(board);

		int digit = 8;
		RowCol loc = ROWCOL[7][7];
		candidates.setOccupied(loc, digit);
		assertEquals(4, candidates.removeCandidatesSameUnit(loc, digit, ROW));
		assertEquals(3, candidates.removeCandidatesSameUnit(loc, digit, COL));
		assertEquals(0, candidates.removeCandidatesSameUnit(loc, digit, BOX));

		candidates = new Candidates(board);
		candidates.setOccupied(loc, digit);
		assertEquals(7, candidates.removeCandidatesSameUnits(loc, digit));
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
	}

	public static String EMPTY = ".........\n.........\n.........\n.........\n.........\n.........\n.........\n.........\n.........";

	@Test
	public void testCandidatesMatch() throws ParseException {
		Board board = new Board(EMPTY);
		Candidates candidates = new Candidates(board);

		// Every candidate in every block
		int[] combi = new int[]{0, 1};
		RowCol loc = ROWCOL[0][0];
		assertEquals(false, candidates.candidatesMatch(loc, combi, NAKED, FULL_COMBI_MATCH));
		assertEquals(true, candidates.candidatesMatch(loc, combi, NOT_NAKED, FULL_COMBI_MATCH));
		assertEquals(false, candidates.candidatesMatch(loc, combi, NAKED, 2));
		assertEquals(true, candidates.candidatesMatch(loc, combi, NOT_NAKED, 2));

		loc = ROWCOL[8][8];
		candidates.setCandidates(loc, new int[]{1, 2, 0, 0, 0, 0, 0, 0, 0});
		// System.out.println( "Candidates=" + candidates.toStringCompact());
		assertEquals(true, candidates.candidatesMatch(loc, combi, NAKED, FULL_COMBI_MATCH));
		assertEquals(true, candidates.candidatesMatch(loc, combi, NOT_NAKED, FULL_COMBI_MATCH));
		assertEquals(true, candidates.candidatesMatch(loc, combi, NAKED, 2));
		assertEquals(true, candidates.candidatesMatch(loc, combi, NOT_NAKED, 2));

		candidates.setCandidates(loc, new int[]{1, 2, 3, 0, 0, 0, 0, 0, 0});
		// System.out.println( "Candidates=" + candidates.toStringCompact());
		assertEquals(false, candidates.candidatesMatch(loc, combi, NAKED, FULL_COMBI_MATCH));
		assertEquals(true, candidates.candidatesMatch(loc, combi, NOT_NAKED, FULL_COMBI_MATCH));
		assertEquals(false, candidates.candidatesMatch(loc, combi, NAKED, 2));
		assertEquals(true, candidates.candidatesMatch(loc, combi, NOT_NAKED, 2));

		int[] triple = new int[]{0, 1, 2};
		candidates.setCandidates(loc, new int[]{1, 2, 0, 0, 0, 0, 0, 0, 0});
		assertEquals(false, candidates.candidatesMatch(loc, triple, NAKED, FULL_COMBI_MATCH));
		assertEquals(false, candidates.candidatesMatch(loc, triple, NOT_NAKED, FULL_COMBI_MATCH));
		assertEquals(true, candidates.candidatesMatch(loc, triple, NAKED, 2));
		assertEquals(true, candidates.candidatesMatch(loc, triple, NOT_NAKED, 2));

		candidates.setCandidates(loc, new int[]{1, 2, 3, 4, 0, 0, 0, 0, 0});
		assertEquals(false, candidates.candidatesMatch(loc, triple, NAKED, FULL_COMBI_MATCH));
		assertEquals(true, candidates.candidatesMatch(loc, triple, NOT_NAKED, FULL_COMBI_MATCH));
		assertEquals(false, candidates.candidatesMatch(loc, triple, NAKED, 2));
		assertEquals(true, candidates.candidatesMatch(loc, triple, NOT_NAKED, 2));
	}

	public static String CANDIDATES = ".1......1\n.......2.\n....36..3\n.4...7...\n5.......5\n.......5.\n5..67..3.\n.3.....79\n8....2...";

	@Test
	public void testCounts() throws ParseException {
		Board board = new Board(CANDIDATES);
		Candidates candidates = new Candidates(board);

		// Fresh new board
		assertEquals(20, candidates.getAllOccupiedCount());
		assertEquals(549, candidates.getAllCount());
		assertEquals(9, candidates.candidateCellCount(ROWCOL[0][0]));
		assertEquals(9, candidates.candidateCellCount(ROWCOL[8][8]));
		assertTrue(Arrays.equals(new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}, candidates.getRemainingCandidates(ROWCOL[8][8])));
		assertTrue(candidates.containsDigits(ROWCOL[8][8], new int[]{1, 9}));
		// assertFalse( candidates.containsDigits( ROWCOL[8][8], new int[]{ 1,9 }));
		assertTrue(candidates.containsOnlyDigits(ROWCOL[8][8], new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9}));
		assertFalse(candidates.containsOnlyDigits(ROWCOL[8][8], new int[]{1, 9}));

		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// After removing legal moves
		assertEquals(307, candidates.getAllCount());
		assertEquals(6, candidates.candidateCellCount(ROWCOL[0][0]));
		assertEquals(2, candidates.candidateCellCount(ROWCOL[8][8]));

		int oneByOneCount = 0;
		int digit = 5;
		for (int rowi = 0; rowi < Board.ROWS; rowi++) {
			for (int coli = 0; coli < Board.COLS; coli++) {
				oneByOneCount += candidates.isCandidate(ROWCOL[rowi][coli], digit) ? 1 : 0;
			}
		}
		assertEquals(oneByOneCount, candidates.digitCount(digit));

		// int [] locations
		int[] rowLocations = candidates.candidateRowLocations(0, 1);
		assertEquals(0, rowLocations.length);
		rowLocations = candidates.candidateRowLocations(1, 1);
		assertEquals(3, rowLocations.length);
		assertTrue(Arrays.equals(new int[]{3, 4, 5}, rowLocations));
		List<RowCol> rowLocs = candidates.getRowLocs(1, 1);
		assertEquals(3, rowLocs.size());
		for (int i = 0; i < rowLocations.length; i++) {
			assertEquals(ROWCOL[1][rowLocations[i]], rowLocs.get(i));
		}

		int[] colLocations = candidates.candidateColLocations(8, 3);
		assertEquals(0, colLocations.length);
		colLocations = candidates.candidateColLocations(2, 3);
		assertEquals(5, colLocations.length);
		// System.out.println( "Col locations=" + Arrays.toString(colLocations) );
		assertTrue(Arrays.equals(new int[]{0, 1, 3, 4, 5}, colLocations));
		List<RowCol> colLocs = candidates.getColLocs(2, 3);
		assertEquals(5, colLocs.size());
		for (int i = 0; i < colLocations.length; i++) {
			assertEquals(ROWCOL[colLocations[i]][2], colLocs.get(i));
		}

		RowCol boxLocation = candidates.candidateBoxLocation(8, 5);
		assertNotNull(boxLocation);
		assertEquals(ROWCOL[7][6], boxLocation);
		boxLocation = candidates.candidateBoxLocation(8, 8);
		assertEquals(6, boxLocation.row());
		List<RowCol> boxLocations = candidates.getBoxLocs(8, 8);
		assertEquals(3, boxLocations.size());
		// System.out.println( RowCol.toString(boxLocations));
		assertEquals(ROWCOL[6][6], boxLocations.get(0));
		assertEquals(ROWCOL[7][6], boxLocations.get(2));

		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		List<RowCol> rowCols = candidates.digitLocs(1);
		assertNotNull(rowCols);
		// System.out.println( "Locs=" + RowCol.toString(rowCols));
		assertEquals(36, rowCols.size());
		assertEquals(ROWCOL[1][3], rowCols.get(0));
		assertEquals(ROWCOL[8][7], rowCols.get(rowCols.size() - 1));

		assertEquals(22, candidates.candidateLocationCount(RowCol.toList(Board.getBoxRowCols(8))));
		assertEquals(3, candidates.candidateLocationCount(2, RowCol.toList(Board.getBoxRowCols(8))));
		assertEquals(5, candidates.candidateRowLocCount(8, new int[]{7, 8}));
		assertEquals(22, candidates.candidateRowColCount(RowCol.toList(Board.getBoxRowCols(8))));

		assertEquals(5, candidates.candidateUnitCount(ROW, 8, 6));
		assertEquals(4, candidates.candidateUnitCount(COL, 8, 6));
		assertEquals(3, candidates.candidateUnitCount(BOX, 8, 8));
		assertEquals(1, candidates.candidateGroupCount(ROW, 8, 6, 2));
		assertEquals(1, candidates.candidateGroupCount(COL, 8, 6, 2));
		assertEquals(1, candidates.candidateGroupCount(BOX, 8, 8, 3));

		assertEquals(2, candidates.candidateColLocCount(8, new int[]{7, 8}));
		assertEquals(0, candidates.emptyLocations().size());

		assertEquals(307, candidates.getAllCount());
		candidates.removeAllCandidates();
		assertEquals(0, candidates.getAllCount());
	}

	@Test
	public void testCandidateBoxLocations() throws ParseException {
		Board board = new Board(CANDIDATES);
		Candidates candidates = new Candidates(board);

		LegalCandidates rule = new LegalCandidates();
		rule.update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// Digits 1 and 2 in blocks 0 and 1
		assertEquals(0, candidates.getBoxLocs(0, 1).size());
		assertEquals(3, candidates.getBoxLocs(1, 2).size());

		// Test alternate way of counting by block locations
		assertEquals(0, candidates.candidateDigitRowColCount(1, Board.getBoxRowCols(0)));
		assertEquals(3, candidates.candidateDigitRowColCount(2, Board.getBoxRowCols(1)));
		assertEquals(3, candidates.candidateDigitRowColCount(2, Board.getBoxRowColsC(1)));
	}

	public static String PAIRS =
			"4..27.6..-798156234-.2.84...7-237468951-849531726-561792843-.82.15479-.7..243..-..4.87..2";

	@Test
	public void testGroupCounts() throws ParseException {
		// Test getLocations
		Board board = new Board(PAIRS);
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);

		// Returns a list of locations having these particulars		
		// public List<int[]> getLocations( int digi, int count )
		List<RowCol> locs = candidates.getGroupLocations(3, 2);
		// System.out.println( "Digit 3, pair locations=" + Utils.locationsString(locs));
		assertEquals(5, locs.size());
		RowCol secondLoc = locs.get(1); // More interesting than first
		assertEquals(ROWCOL[0][2], locs.get(0));
		assertEquals(ROWCOL[6][3], locs.get(locs.size() - 1));
		List<RowCol> unitLocs = candidates.getGroupSameUnitLocations(ROW, 3, 2, secondLoc);
		assertEquals(1, unitLocs.size());
		unitLocs = candidates.getGroupSameUnitLocations(COL, 3, 2, secondLoc);
		assertEquals(1, unitLocs.size());
		unitLocs = candidates.getGroupSameUnitLocations(BOX, 3, 2, secondLoc);
		assertEquals(1, unitLocs.size());
		unitLocs = candidates.getGroupAllUnitLocations(3, 2, secondLoc);
		assertEquals(3, unitLocs.size());

		locs = candidates.getGroupLocations(Candidates.ALL_DIGITS, 2);
		// System.out.println( "All digit, pair locations=" + Utils.locationsString(locs));
		assertEquals(15, locs.size());
		assertEquals(ROWCOL[0][1], locs.get(0));
		assertEquals(ROWCOL[8][7], locs.get(locs.size() - 1));

		locs = candidates.getGroupLocations(3, Candidates.ALL_COUNTS);
		// System.out.println( "Digit 3, locations=" + Utils.locationsString(locs));
		assertEquals(9, locs.size());
		assertEquals(ROWCOL[0][2], locs.get(0));
		assertEquals(ROWCOL[8][3], locs.get(locs.size() - 1));

		locs = candidates.getGroupLocations(ROW, 6, 3, 2);
		// System.out.println( "Digit 3, row 6 pair locations=" + Utils.locationsString(locs));
		assertEquals(2, locs.size());
		assertEquals(ROWCOL[6][0], locs.get(0));
		assertEquals(ROWCOL[6][3], locs.get(locs.size() - 1));

		int groupCount = candidates.candidateGroupCount(ROW, 6, 3, 2);
		assertEquals(2, groupCount); // 2 pairs
		int found = candidates.candidateGroupFind(ROW, 6, 3, 2, 1);
		assertEquals(3, found); // col 3

		locs = candidates.getGroupLocations(COL, 2, 3, 2);
		assertEquals(1, locs.size());
		assertEquals(ROWCOL[0][2], locs.get(0));
		assertEquals(ROWCOL[0][2], locs.get(locs.size() - 1));

		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		locs = candidates.getGroupLocations(BOX, 0, 3, 3);
		// System.out.println( "Digit 3, box 0 triples locations=" + RowCol.toString(locs));
		assertEquals(2, locs.size());
		assertEquals(ROWCOL[2][0], locs.get(0));
		assertEquals(ROWCOL[2][2], locs.get(1));
	}


	@Test
	public void testCandidateComboLocations() throws ParseException {
		Board board = new Board(PAIRS);
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=" + candidates.toStringCompact());

		// Test row/col/all search with certain combo
		int[] combo15 = new int[]{0, 4};
		List<RowCol> locations = candidates.candidateComboRowLocations(0, combo15, NAKED, FULL_COMBI_MATCH);
		// assert size and location
		assertEquals(1, locations.size());
		assertEquals(ROWCOL[0][1], locations.get(0));
		// Partial on triple combo
		int[] combo159 = new int[]{0, 4, 8};
		locations = candidates.candidateComboRowLocations(0, combo159, NAKED, 2);
		// assert size and location
		assertEquals(1, locations.size());
		assertEquals(ROWCOL[0][1], locations.get(0));

		int[] combo189 = new int[]{0, 7, 8};
		// Combo size == subsetSize (another way of saying FULL_COMBI_MATCH
		locations = candidates.candidateComboRowLocations(0, combo189, NAKED, 3);
		// assert size and location
		assertEquals(1, locations.size());
		assertEquals(ROWCOL[0][7], locations.get(0));

		locations = candidates.candidateComboRowLocations(8, combo15, NAKED, FULL_COMBI_MATCH);
		// assert size and location/
		assertEquals(2, locations.size());
		assertEquals(ROWCOL[8][1], locations.get(0));
		assertEquals(ROWCOL[8][6], locations.get(1));
		locations = candidates.candidateComboRowLocations(8, combo159, NAKED, 2);
		// assert size and location/
		assertEquals(2, locations.size());
		assertEquals(ROWCOL[8][1], locations.get(0));
		assertEquals(ROWCOL[8][6], locations.get(1));

		locations = candidates.candidateComboColLocations(1, combo15, NAKED, FULL_COMBI_MATCH);
		// assert size and location/
		assertEquals(2, locations.size());
		assertEquals(ROWCOL[0][1], locations.get(0));
		assertEquals(ROWCOL[8][1], locations.get(1));

		locations = candidates.candidateComboColLocations(1, combo159, NAKED, 2);
		// assert size and location/
		assertEquals(2, locations.size());
		assertEquals(ROWCOL[0][1], locations.get(0));
		assertEquals(ROWCOL[8][1], locations.get(1));

		locations = candidates.candidateComboColLocations(6, combo15, NAKED, FULL_COMBI_MATCH);
		// assert size and location/
		assertEquals(2, locations.size());
		assertEquals(ROWCOL[2][6], locations.get(0));
		assertEquals(ROWCOL[8][6], locations.get(1));
		locations = candidates.candidateComboColLocations(6, combo159, NAKED, 2);
		// assert size and location/
		assertEquals(2, locations.size());
		assertEquals(ROWCOL[2][6], locations.get(0));
		assertEquals(ROWCOL[8][6], locations.get(1));

		// Not enough combos, location counts
		int[] combo1589 = new int[]{0, 4, 7, 8};
		// assert size and location/
		locations = candidates.candidateComboRowLocations(0, combo1589, NAKED, 3); // 3 in col 7
		assertEquals(1, locations.size());
		assertEquals(3, candidates.candidateComboLocCount(combo1589, locations));
		locations = candidates.candidateComboRowLocations(0, combo1589, NAKED, 2); // 2 in cols 1, 7, 8
		assertEquals(3, locations.size());
		assertEquals(7, candidates.candidateComboLocCount(combo1589, locations));
		assertEquals(ROWCOL[0][1], locations.get(0));
		assertEquals(ROWCOL[0][7], locations.get(1));
		assertEquals(ROWCOL[0][8], locations.get(2));

		locations = candidates.candidateComboAllLocations(combo15, FULL_COMBI_MATCH);
		assertEquals(4, locations.size());
		assertEquals(ROWCOL[0][1], locations.get(0));
		assertEquals(ROWCOL[2][6], locations.get(1));
		assertEquals(ROWCOL[8][1], locations.get(2));
		assertEquals(ROWCOL[8][6], locations.get(3));
	}

	@Test
	public void testCandidateChanges() throws ParseException {
		Board board = new Board(PAIRS);
		Candidates cFrom = new Candidates(board);
		(new LegalCandidates()).update(board, null, cFrom, null);
		// System.out.println( "Candidates=\n" + cFrom.toStringBoxed());

		Candidates cTo = new Candidates(cFrom);
		assertEquals(0, Candidates.changes(cFrom, (Candidates) null).size());
		assertEquals(0, Candidates.changes(cFrom, cTo).size());

		int digit = 9;
		RowCol rowCol = ROWCOL[0][1];
		cTo.addCandidate(rowCol, digit);
		assertEquals(1, Candidates.changes(cFrom, cTo).size());
		assertEquals(new ChangeData(digit, rowCol, Candidates.Action.ADD, 1), Candidates.changes(cFrom, cTo).get(0));
		cTo.removeCandidate(rowCol, digit);

		digit = 5;
		rowCol = ROWCOL[0][2];
		cTo.removeCandidate(rowCol, digit);
		assertEquals(1, Candidates.changes(cFrom, cTo).size());
		assertEquals(new ChangeData(digit, rowCol, Candidates.Action.REMOVE, 1), Candidates.changes(cFrom, cTo).get(0));
		cTo.addCandidate(rowCol, digit);

		digit = 8;
		rowCol = ROWCOL[7][7];
		cTo.setOccupied(rowCol, digit);
		assertEquals(3, Candidates.changes(cFrom, cTo).size()); // will have 2 candidate removes before occupy.
		assertEquals(new ChangeData(digit, rowCol, Candidates.Action.OCCUPY, 1), Candidates.changes(cFrom, cTo).get(2));

		cFrom.setOccupied(rowCol, digit);
		cTo.setUnoccupied(rowCol, digit);
		assertEquals(1, Candidates.changes(cFrom, cTo).size());
		assertEquals(new ChangeData(digit, rowCol, Candidates.Action.UNOCCUPY, 1), Candidates.changes(cFrom, cTo).get(0));
	}

	@Test
	public void testToString() throws ParseException {
		Board board = new Board(PAIRS);
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		String boxed = candidates.toStringBoxed();
		// System.out.println("Candidates=\n" + candidates.toStringBoxed());

		assertEquals( "{-4}", candidates.getCompactStr( ROWCOL[0][0]));
		assertEquals( "{356}", candidates.getCompactStr( ROWCOL[2][2]));
		assertEquals( "[0,0]{-4}", candidates.getLocCompactStr( ROWCOL[0][0]));
		assertEquals( "[2,2]{356}", candidates.getLocCompactStr( ROWCOL[2][2]));

		assertTrue( boxed.contains("{-4}{15}{35}   {-2}{-7}{39}  {-6}{189}{58}") );
		assertTrue( boxed.contains("{1369}{15}{-4} {369}{-8}{-7} {15}{16}{-2}") );

		String groupStr = candidates.toStringFocus( false, ALL_DIGITS, 2);
		// System.out.println("Candidates=\n" + groupStr);
		assertTrue( groupStr.contains("{  }{15}{35} {  }{  }{39} {  }{  }{58}") );
		assertTrue( groupStr.contains("{  }{15}{  } {  }{  }{  } {15}{16}{  }") );
		groupStr = candidates.toStringFocus( true, ALL_DIGITS, 2);
		assertTrue( groupStr.contains("{-4}{15}{35} {-2}{-7}{39} {-6}{  }{58}") );
		assertTrue( groupStr.contains("{  }{15}{-4} {  }{-8}{-7} {15}{16}{-2}") );

		String digitStr = candidates.toStringFocus( false, 1, ALL_COUNTS);
		assertTrue( digitStr.contains("{}{15}{}     {}{}{} {}{189}{}") );
		assertTrue( digitStr.contains("{1369}{15}{} {}{}{} {15}{16}{}") );
		digitStr = candidates.toStringFocus( true, 1, ALL_COUNTS);
		// System.out.println("Candidates=\n" + digitStr);
		assertTrue( digitStr.contains("{-4}{15}{}     {-2}{-7}{}   {-6}{189}{}") );
		assertTrue( digitStr.contains("{1369}{15}{-4} {}{-8}{-7}   {15}{16}{-2}") );

		String notOccupied = candidates.toStringFocus( false, ALL_DIGITS, ALL_COUNTS);
		// System.out.println("Candidates=\n" + notOccupied );
		assertTrue( notOccupied.contains("{}{15}{35}   {}{}{39}  {}{189}{58}") );
		assertTrue( notOccupied.contains("{1369}{15}{} {369}{}{} {15}{16}{}") );

		// Should be the same as toStringBoxed
		String allStr = candidates.toStringFocus( true, ALL_DIGITS, ALL_COUNTS);
		// System.out.println("Candidates=\n" + allStr );
		assertTrue( allStr.contains("{-4}{15}{35}   {-2}{-7}{39}  {-6}{189}{58}") );
		assertTrue( allStr.contains("{1369}{15}{-4} {369}{-8}{-7} {15}{16}{-2}") );
	}

	public static final String CANDSTR = """
		{1578}{78}{57}     {-2}{45}{489}    {-6}{179}{-3}
		{149}{24}{49}      {-3}{-7}{-6}     {-8}{-5}{12}
		{3578}{2378}{-6}   {58}{-1}{89}     {79}{-4}{27}

		{457}{-6}{-8}      {-9}{-2}{347}    {35}{17}{147}
		{-2}{-1}{457}      {4578}{45}{3478} {35}{-6}{-9}
		{34579}{347}{4579} {457}{-6}{-1}    {-2}{78}{478}

		{47}{-9}{-2}       {-6}{-8}{47}     {-1}{-3}{-5}
		{478}{-5}{-1}      {47}{-3}{-2}     {479}{789}{-6}
		{-6}{478}{-3}      {-1}{-9}{-5}     {47}{-2}{78}""";

	public static final String CANDSPACES = """
		-1 25 -6 -7 845 -3 -8 45 -9
		-3 -4 278 -9 856 128 167 1567 167
		-9 57 78 145 456 18 1367 -2 13467
		76 -1 -3 -2 -9 46 5 467 -8
		26 -8 -5 -3 -7 46 126 -9 146
		-4 267 -9 -8 -1 -5 2367 67 367
		-8 -3 47 145 245 12 -9 167 167
		267 267 247 14 -3 -9 17 -8 -5
		-5-9 -1 -6 -8 -7 -4 -3 -2			
	""";
	@Test
	public void testParse() throws ParseException {
		Candidates cand = new Candidates( CANDSTR );
		// System.out.println( "Candidates=\n" + cand.toStringBoxed());
		assertEquals("{1578}", cand.getCompactStr( ROWCOL[0][0]));
		assertEquals("{-6}", cand.getCompactStr( ROWCOL[5][4]));
		assertEquals("{78}", cand.getCompactStr( ROWCOL[8][8]));

		cand = new Candidates( CANDSPACES );
		// System.out.println( "Candidates=\n" + cand.toStringBoxed());
		assertEquals("{167}", cand.getCompactStr( ROWCOL[1][8]));
		assertEquals("{126}", cand.getCompactStr( ROWCOL[4][6]));
		assertEquals("{267}", cand.getCompactStr( ROWCOL[7][1]));
	}
}