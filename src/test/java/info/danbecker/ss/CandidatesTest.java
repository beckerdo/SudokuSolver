package info.danbecker.ss;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import info.danbecker.ss.Utils.Unit;
import info.danbecker.ss.rules.LegalCandidates;
import info.danbecker.ss.tree.ChangeData;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static info.danbecker.ss.Candidates.NAKED;
import static info.danbecker.ss.Candidates.NOT_NAKED;
import static info.danbecker.ss.Candidates.FULL_COMBI_MATCH;

import java.text.ParseException;

public class CandidatesTest {
	public static String EMPTY = ".........\n.........\n.........\n.........\n.........\n.........\n.........\n.........\n.........";
	public static String BOARD1 = "...1....1\n38.....2.\n.2..96..4\n.4...7...\n9.......3\n...4...5.\n5..67..3.\n.3.....79\n8....2...";
	public static String BOARD2 = "...2....1\n38.....2.\n.2..96..4\n.4...7...\n9.......3\n...4...5.\n5..67..3.\n.3.....79\n8....2...";
	public static String CANDIDATES = ".1......1\n.......2.\n....36..3\n.4...7...\n5.......5\n.......5.\n5..67..3.\n.3.....79\n8....2...";
	public static String PAIRS = 
		"4..27.6..-798156234-.2.84...7-237468951-849531726-561792843-.82.15479-.7..243..-..4.87..2";
	
	@BeforeEach
    public void setup() {
	}
		
	@Test
    public void testBasics() throws ParseException {
		Board board1 = new Board( BOARD1 );
		Candidates candidates1 = new Candidates( board1 );
		Board board2 = new Board( BOARD2 );
		Candidates candidates2 = new Candidates( board2 );
		Board board3 = new Board( BOARD1 );
		Candidates candidates3 = new Candidates( board3 );

     	assertTrue( candidates1.compareTo( null ) == 1  );
     	assertTrue( candidates1.compareTo( candidates1 ) == 0  );
     	assertTrue( candidates1.compareTo( candidates3 ) == 0  );
     	assertTrue( candidates1.compareTo( candidates2 ) == -1  );
     	
     	Candidates clone = new Candidates( candidates3 );
     	assertEquals( clone, candidates3 );
     	// System.out.println( "RowCol 0,0 candidates=" + Arrays.toString( clone.getCandidates( 0, 0 )) );
     	clone.setOccupied(0, 0, 1);
     	assertEquals( 1, clone.compareTo( candidates3 ));
    }
	
	@Test
    public void testCandidateAdd() throws ParseException {
		Board board1 = new Board( BOARD1 );
		Candidates candidates1 = new Candidates( board1 );

     	// Single candidate, negative because it was given in Board
     	assertTrue( Arrays.equals( new short[] {-1,0,0,0,0,0,0,0,0}, candidates1.getCandidates( 0, 3 )));
     	assertTrue( Arrays.equals( new short[] {0,0,0,0,0,0,0,-8,0}, candidates1.getCandidates( 1, 1 )));
     	// All candidates
     	assertTrue( Arrays.equals( new short[] {1,2,3,4,5,6,7,8,9}, candidates1.getCandidates( 8, 8 )));
     	candidates1.setCandidates(8, 0, new short[]{ 0,2,0,4,0,6,0,8,0} );
     	assertTrue( Arrays.equals( new short[] {0,2,0,4,0,6,0,8,0}, candidates1.getCandidates( 8, 0 )));
     	
     	List<Integer> cands = candidates1.getCandidatesList( 8, 0 );
     	assertEquals( 4, cands.size());
     	// assertEquals( new int[] {2,4,6,8}, cands.toArray(new int[0]) );
     	// assertEquals( new Integer[] {2,4,6,8}, cands.toArray(new Integer[0]) );
     	assertTrue( Arrays.deepEquals( new Integer[] {2,4,6,8}, cands.toArray(new Integer[0])) );
     	
     	// Candidate remove, add, multiple times
     	candidates1.removeCandidate( 8, 8, 9 );
     	assertTrue( Arrays.equals( new short[] {1,2,3,4,5,6,7,8,0}, candidates1.getCandidates( 8, 8 )));
     	candidates1.removeCandidate( 8, 8, 9 );
     	assertTrue( Arrays.equals( new short[] {1,2,3,4,5,6,7,8,0}, candidates1.getCandidates( 8, 8 )));
     	candidates1.addCandidate( 8, 8, 9 );
     	assertTrue( Arrays.equals( new short[] {1,2,3,4,5,6,7,8,9}, candidates1.getCandidates( 8, 8 )));

     	candidates1.addCandidate( 8, 8, 9 );
     	assertTrue( Arrays.equals( new short[] {1,2,3,4,5,6,7,8,9}, candidates1.getCandidates( 8, 8 )));
     	assertTrue( candidates1.isCandidate( 8,8,9 ));
     	candidates1.removeCandidate( 8, 8, 9 );
     	assertFalse( candidates1.isCandidate( 8,8,9 ));
     	
        // Get Set occupied
     	assertEquals( (short) 8, candidates1.getOccupied( 1, 1 ));
     	assertEquals( (short) 0, candidates1.getOccupied( 8, 8 ));
     	candidates1.setOccupied(8, 8, 5);
     	assertEquals( (short) 5, candidates1.getOccupied( 8, 8 ));
	}
	
	@Test
    public void testCandidateRemove() throws ParseException {
		Board board = new Board( PAIRS );
		Candidates candidates = new Candidates( board );
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		
		int digit = 8;
		int[] loc = new int[] { 7, 7 };
		candidates.setOccupied( loc, digit);
		assertEquals(4, candidates.removeCandidatesSameUnit(loc, digit, Unit.ROW));
		assertEquals(3, candidates.removeCandidatesSameUnit(loc, digit, Unit.COL));
		assertEquals(1, candidates.removeCandidatesSameUnit(loc, digit, Unit.BOX));

		candidates = new Candidates( board );
		candidates.setOccupied( loc, digit);
		assertEquals(8, candidates.removeCandidatesSameUnits(loc, digit));
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
	}
	
	@Test
    public void testCandidatesMatch() throws ParseException {
		Board board = new Board( EMPTY );
		Candidates candidates = new Candidates( board );
		
		// Every candidate in every block
		int [] pair = new int[] { 0, 1 };
		assertEquals( false, candidates.candidatesMatch( 0, 0, pair, NAKED, FULL_COMBI_MATCH ));
		assertEquals( true, candidates.candidatesMatch( 0, 0, pair, NOT_NAKED, FULL_COMBI_MATCH));
		assertEquals( false, candidates.candidatesMatch( 0, 0, pair, NAKED, 2 ));
		assertEquals( true, candidates.candidatesMatch( 0, 0, pair, NOT_NAKED, 2 ));
		
		candidates.setCandidates( 8, 8, new short [] { 1,2,0,0,0,0,0,0,0 }); 
		// System.out.println( "Candidates=" + candidates.toStringCompact());
		assertEquals( true, candidates.candidatesMatch( 8, 8, pair, NAKED, FULL_COMBI_MATCH));
		assertEquals( true, candidates.candidatesMatch( 8, 8, pair, NOT_NAKED, FULL_COMBI_MATCH));
		assertEquals( true, candidates.candidatesMatch( 8, 8, pair, NAKED, 2 ));
		assertEquals( true, candidates.candidatesMatch( 8, 8, pair, NOT_NAKED, 2 ));

		candidates.setCandidates( 8, 8, new short [] { 1,2,3,0,0,0,0,0,0 }); 
		// System.out.println( "Candidates=" + candidates.toStringCompact());
		assertEquals( false, candidates.candidatesMatch( 8, 8, pair, NAKED, FULL_COMBI_MATCH));
		assertEquals( true, candidates.candidatesMatch( 8, 8, pair, NOT_NAKED, FULL_COMBI_MATCH));
		assertEquals( false, candidates.candidatesMatch( 8, 8, pair, NAKED, 2 ));
		assertEquals( true, candidates.candidatesMatch( 8, 8, pair, NOT_NAKED, 2 ));

		int [] triple = new int[] { 0, 1, 2 };
		candidates.setCandidates( 8, 8, new short [] { 1,2,0,0,0,0,0,0,0 }); 
		assertEquals( false, candidates.candidatesMatch( 8, 8, triple, NAKED, FULL_COMBI_MATCH ));
		assertEquals( false, candidates.candidatesMatch( 8, 8, triple, NOT_NAKED, FULL_COMBI_MATCH ));
		assertEquals( true, candidates.candidatesMatch( 8, 8, triple, NAKED, 2 ));
		assertEquals( true, candidates.candidatesMatch( 8, 8, triple, NOT_NAKED, 2 ));

		candidates.setCandidates( 8, 8, new short [] { 1,2,3,4,0,0,0,0,0 }); 
		assertEquals( false, candidates.candidatesMatch( 8, 8, triple, NAKED, FULL_COMBI_MATCH ));
		assertEquals( true, candidates.candidatesMatch( 8, 8, triple, NOT_NAKED, FULL_COMBI_MATCH ));
		assertEquals( false, candidates.candidatesMatch( 8, 8, triple, NAKED, 2 ));
		assertEquals( true, candidates.candidatesMatch( 8, 8, triple, NOT_NAKED, 2 ));
	}
	
	
	@Test
    public void testCounts() throws ParseException {
		Board board = new Board( CANDIDATES );
		Candidates candidates = new Candidates( board );
		
		// Fresh new board
		assertEquals( 549, candidates.candidateCount() );
		assertEquals( 9, candidates.candidateCount( 0, 0 ));
		assertEquals( 9, candidates.candidateCount( 8, 8 ));
		
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// After removing legal moves
		assertEquals( 307, candidates.candidateCount() );
		assertEquals( 6, candidates.candidateCount( 0, 0 ));
		assertEquals( 2, candidates.candidateCount( 8, 8 ));
		
		// int [] locations
		int [] rowLocations = candidates.candidateRowLocations(0, 1);
		assertEquals( 0, rowLocations.length );
		rowLocations = candidates.candidateRowLocations(1, 1);
		assertEquals( 3, rowLocations.length );
		assertTrue( Arrays.equals( new int[]{3,4,5}, rowLocations) );
		
		int [] colLocations = candidates.candidateColLocations(8, 3);
		assertEquals( 0, colLocations.length );
		colLocations = candidates.candidateColLocations(2, 3);
		assertEquals( 5, colLocations.length );
		assertTrue( Arrays.equals( new int[]{0,1,3,4,5}, colLocations) );
		
		int [] boxLocation = candidates.candidateBoxLocation(8, 7);
		assertEquals( null, boxLocation );
		boxLocation = candidates.candidateBoxLocation(8, 8);
		assertEquals( 2, boxLocation.length );
		assertEquals( 6, boxLocation[0] );
		List<int[]> boxLocations = candidates.candidateBoxLocations(8, 8);
		assertEquals( 3, boxLocations.size() );
		// System.out.println( Arrays.toString(boxLocations.get(0)));
		assertTrue( Arrays.equals( new int[]{6,6}, boxLocations.get(0)) );
	}
	
	
	@Test
    public void testGroupCounts() throws ParseException {
		// Test getLocations
		Board board = new Board( PAIRS );
		Candidates candidates = new Candidates( board );
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=" + candidates.toStringCompact());

		// Returns a list of locations having these particulars		
		// public List<int[]> getLocations( int digi, int count )
		List<int[]> locs = candidates.getGroupLocations(3,2);
		// System.out.println( "Digit 3, pair locations=" + Utils.locationsString(locs));
		assertEquals( 5, locs.size());
		int[] secondLoc = locs.get( 1 ); // More interesting than first
		assertTrue( Arrays.equals( new int[] { 0, 2 }, locs.get( 0 ) ));
		assertTrue( Arrays.equals( new int[] { 6, 3 }, locs.get( locs.size() - 1 ) ));
		List<int[]> unitLocs = candidates.getGroupSameUnitLocations( Utils.Unit.ROW, 3, 2, secondLoc );
		assertEquals( 1, unitLocs.size());
		unitLocs = candidates.getGroupSameUnitLocations( Utils.Unit.COL, 3, 2, secondLoc );
		assertEquals( 1, unitLocs.size());
		unitLocs = candidates.getGroupSameUnitLocations( Utils.Unit.BOX, 3, 2, secondLoc );
		assertEquals( 1, unitLocs.size());
		unitLocs = candidates.getGroupAllUnitLocations( 3, 2, secondLoc );
		assertEquals( 3, unitLocs.size());

		locs = candidates.getGroupLocations(Candidates.ALL_DIGITS,2);
		// System.out.println( "All digit, pair locations=" + Utils.locationsString(locs));
		assertEquals( 15, locs.size());
		assertTrue( Arrays.equals( new int[] { 0, 1 }, locs.get( 0 ) ));
		assertTrue( Arrays.equals( new int[] { 8, 7 }, locs.get( locs.size() - 1 ) ));

		locs = candidates.getGroupLocations(3,Candidates.ALL_COUNTS);
		// System.out.println( "Digit 3, locations=" + Utils.locationsString(locs));
		assertEquals( 9, locs.size());
		assertTrue( Arrays.equals( new int[] { 0, 2 }, locs.get( 0 ) ));
		assertTrue( Arrays.equals( new int[] { 8, 3 }, locs.get( locs.size() - 1 ) ));		
		
		locs = candidates.getGroupLocations(Utils.Unit.ROW,6, 3, 2);
		// System.out.println( "Digit 3, row 6 pair locations=" + Utils.locationsString(locs));
		assertEquals( 2, locs.size());
		assertTrue( Arrays.equals( new int[] { 6, 0 }, locs.get( 0 ) ));
		assertTrue( Arrays.equals( new int[] { 6, 3 }, locs.get( locs.size() - 1 ) ));
		
		int groupCount = candidates.candidateGroupCount( Utils.Unit.ROW, 6, 3, 2);
		assertEquals( 2, groupCount ); // 2 pairs
		int found = candidates.candidateGroupFind( Utils.Unit.ROW, 6, 3, 2, 1);
		assertEquals( 3, found ); // col 3

		locs = candidates.getGroupLocations(Utils.Unit.COL,2, 3,2);
		// System.out.println( "Digit 3, col 2 pair locations=" + Utils.locationsString(locs));
		assertEquals( 1, locs.size());
		assertTrue( Arrays.equals( new int[] { 0, 2 }, locs.get( 0 ) ));
		assertTrue( Arrays.equals( new int[] { 0, 2 }, locs.get( locs.size() - 1 ) ));

		locs = candidates.getGroupLocations(Utils.Unit.BOX,0, 3,3);
		// System.out.println( "Digit 3, box 0 triples locations=" + Utils.locationsString(locs));
		assertEquals( 2, locs.size());
		assertTrue( Arrays.equals( new int[] { 2, 0 }, locs.get( 0 ) ));
		assertTrue( Arrays.equals( new int[] { 2, 2 }, locs.get( locs.size() - 1 ) ));
	}
	
	@Test
    public void testCandidateBoxLocations() throws ParseException {
		Board board = new Board( CANDIDATES );
		Candidates candidates = new Candidates( board );
			
		LegalCandidates rule = new LegalCandidates();
		rule.updateCandidates( board, null, candidates, null);

		// Digits 1 and 2 in blocks 0 and 1
		assertEquals( 0, candidates.candidateBoxLocations(0, 1).size()); 
		assertEquals( 3, candidates.candidateBoxLocations(1, 2).size());

		// Test alternate way of counting by block locations
		// System.out.println( candidates.toStringCompact() );
		assertEquals( 0, candidates.candidateDigitRowColCount( 1, Board.getBoxRowCols(0)));
		assertEquals( 0, candidates.candidateDigitRowColCount( 1, Board.getBoxRowColsC(0)));
		assertEquals( 3, candidates.candidateDigitRowColCount( 2, Board.getBoxRowCols(1)));
		assertEquals( 3, candidates.candidateDigitRowColCount( 2, Board.getBoxRowColsC(1)));
	}

	@Test
    public void testCandidateComboLocations() throws ParseException {
		Board board = new Board(PAIRS);
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=" + candidates.toStringCompact());

		// Test row/col/all search with certain combo
		int[] combo15 = new int[]{0,4};
		List<int[]> locations = candidates.candidateComboRowLocations( 0, combo15, NAKED, FULL_COMBI_MATCH );
		// assert size and location
		assertEquals( 1, locations.size());
		assertTrue( Arrays.equals(new int[] {0, 1}, locations.get(0)));
		// Partial on triple combo
		int[] combo159 = new int[]{0,4,8};
		locations = candidates.candidateComboRowLocations( 0, combo159, NAKED, 2 );
		// assert size and location
		assertEquals( 1, locations.size());
		assertTrue( Arrays.equals(new int[] {0, 1}, locations.get(0)));
		
		int[] combo189 = new int[]{0,7,8};
		// Combo size == subsetSize (another way of saying FULL_COMBI_MATCH
		locations = candidates.candidateComboRowLocations( 0, combo189, NAKED, 3 );
		// assert size and location
		assertEquals( 1, locations.size());
		assertTrue( Arrays.equals(new int[] {0, 7}, locations.get(0)));	
		
		locations = candidates.candidateComboRowLocations( 8, combo15, NAKED, FULL_COMBI_MATCH );
		// assert size and location/
		assertEquals( 2, locations.size());
		assertTrue( Arrays.equals(new int[] {8, 1}, locations.get(0)));
		assertTrue( Arrays.equals(new int[] {8, 6}, locations.get(1)));
		locations = candidates.candidateComboRowLocations( 8, combo159, NAKED, 2 );
		// assert size and location/
		assertEquals( 2, locations.size());
		assertTrue( Arrays.equals(new int[] {8, 1}, locations.get(0)));
		assertTrue( Arrays.equals(new int[] {8, 6}, locations.get(1)));
		
		locations = candidates.candidateComboColLocations( 1, combo15, NAKED, FULL_COMBI_MATCH );
		// assert size and location/
		assertEquals( 2, locations.size());
		assertTrue( Arrays.equals(new int[] {0, 1}, locations.get(0)));
		assertTrue( Arrays.equals(new int[] {8, 1}, locations.get(1)));
		locations = candidates.candidateComboColLocations( 1, combo159, NAKED, 2 );
		// assert size and location/
		assertEquals( 2, locations.size());
		assertTrue( Arrays.equals(new int[] {0, 1}, locations.get(0)));
		assertTrue( Arrays.equals(new int[] {8, 1}, locations.get(1)));
		locations = candidates.candidateComboColLocations( 6, combo15, NAKED, FULL_COMBI_MATCH );
		// assert size and location/
		assertEquals( 2, locations.size());
		assertTrue( Arrays.equals(new int[] {2, 6}, locations.get(0)));
		assertTrue( Arrays.equals(new int[] {8, 6}, locations.get(1)));
		locations = candidates.candidateComboColLocations( 6, combo159, NAKED, 2 );
		// assert size and location/
		assertEquals( 2, locations.size());
		assertTrue( Arrays.equals(new int[] {2, 6}, locations.get(0)));
		assertTrue( Arrays.equals(new int[] {8, 6}, locations.get(1)));

		// Not enough combos, location counts
		int[] combo1589 = new int[]{0,4,7,8};
		// assert size and location/
		locations = candidates.candidateComboRowLocations( 0, combo1589, NAKED, 3 ); // 3 in col 7
		assertEquals( 1, locations.size());
		assertEquals( 3, candidates.candidateComboLocCount(combo1589, locations));
		locations = candidates.candidateComboRowLocations( 0, combo1589, NAKED, 2 ); // 2 in cols 1, 7, 8
		assertEquals( 3, locations.size());
		assertEquals( 7, candidates.candidateComboLocCount(combo1589, locations));
		assertTrue( Arrays.equals(new int[] {0,1}, locations.get(0)));
		assertTrue( Arrays.equals(new int[] {0,7}, locations.get(1)));
		assertTrue( Arrays.equals(new int[] {0,8}, locations.get(2)));
		
		locations = candidates.candidateComboAllLocations( combo15, FULL_COMBI_MATCH );
    	assertEquals( 4, locations.size());	
		assertTrue( Arrays.equals(new int[] {0, 1}, locations.get(0)));
		assertTrue( Arrays.equals(new int[] {2, 6}, locations.get(1)));
		assertTrue( Arrays.equals(new int[] {8, 1}, locations.get(2)));
		assertTrue( Arrays.equals(new int[] {8, 6}, locations.get(3)));
	}
	
	@Test
    public void testCandidateChanges() throws ParseException {
		Board board = new Board(PAIRS);
		Candidates cFrom = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, cFrom, null);
		System.out.println( "Candidates=\n" + cFrom.toStringBoxed());
		
		Candidates cTo = new Candidates(cFrom);
		assertEquals( 0, Candidates.changes( cFrom, (Candidates) null ).size());
		assertEquals( 0, Candidates.changes( cFrom, cTo ).size());
		
		int digit = 9;
		int [] rowCol = new int[] { 0, 1 };
		cTo.addCandidate( rowCol, digit );
		assertEquals( 1, Candidates.changes( cFrom, cTo ).size());
		assertEquals( new ChangeData( digit, rowCol, Candidates.Action.ADD, 1 ), Candidates.changes( cFrom, cTo ).get(0));		
		cTo.removeCandidate( rowCol, digit );

		digit = 5;
		rowCol = new int[] { 0, 2 };
		cTo.removeCandidate( rowCol, digit );
		assertEquals( 1, Candidates.changes( cFrom, cTo ).size());
		assertEquals( new ChangeData( digit, rowCol, Candidates.Action.REMOVE, 1 ), Candidates.changes( cFrom, cTo ).get(0));		
		cTo.addCandidate( rowCol, digit );

		digit = 8;
		rowCol = new int[] { 7, 7 };
		cTo.setOccupied( rowCol, digit );
		assertEquals( 3, Candidates.changes( cFrom, cTo ).size()); // will have 2 candidate removes before occupy.
		assertEquals( new ChangeData( digit, rowCol, Candidates.Action.OCCUPY, 1 ), Candidates.changes( cFrom, cTo ).get(2));		

		cFrom.setOccupied( rowCol, digit );
		cTo.setUnoccupied( rowCol, digit );
		assertEquals( 1, Candidates.changes( cFrom, cTo ).size());
		assertEquals( new ChangeData( digit, rowCol, Candidates.Action.UNOCCUPY, 1 ), Candidates.changes( cFrom, cTo ).get(0));		
	}

}