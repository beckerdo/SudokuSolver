package info.danbecker.ss.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import java.text.ParseException;
import java.util.List;

public class MultipleLinesTest {
	// This board and candidates equals the example at 
	// https://www.sudokuoftheday.com/techniques/multiple-lines
	public static String MULTIPLELINES = """
..9.3.6..
.36.14.89
1..869.35
.9....8..
.1.....9.
.68.9.17.
6.19.3..2
97264.3..
..3.2.9..
""";


	@BeforeEach
	public void setup() {
	}

	@Test
	public void testBasics() throws ParseException {
		Board board = new Board(MULTIPLELINES);
		assertTrue(board.legal());	
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		System.out.println( "Candidates=" + candidates.toStringCompact());

		UpdateCandidatesRule rule = new MultipleLines();
		assertEquals(rule.ruleName(), rule.getClass().getSimpleName());

		List<int[]> locations = rule.locations(board, candidates);
		assertEquals( 1, locations.size());
		int [] encoding = locations.get(0);
		assertEquals( 5, encoding[ 0 ]); // digit
		assertEquals( 1, encoding[ 1 ]); // rowCol orientation
		assertEquals( 0, encoding[ 2 ]); // first ML block
		assertEquals( 6, encoding[ 3 ]); // second ML block
		assertEquals( 3, encoding[ 4 ]); // keeper block
		assertEquals( 0, encoding[ 5 ]); // first ML row
		assertEquals( 1, encoding[ 6 ]); // second ML row
		assertEquals( 2, encoding[ 7 ]); // keeper row

		// Try 3 rotations and see if that works
		board = Board.rotateRight(board);
		assertTrue(board.legal());	
		candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=" + candidates.toStringCompact());
		locations = rule.locations(board, candidates);
		assertEquals( 1, locations.size());
		encoding = locations.get(0);
		assertEquals( 5, encoding[ 0 ]); // digit
		assertEquals( 0, encoding[ 1 ]); // rowCol orientation
		assertEquals( 0, encoding[ 2 ]); // first ML block
		assertEquals( 2, encoding[ 3 ]); // second ML block
		assertEquals( 1, encoding[ 4 ]); // keeper block
		assertEquals( 0, encoding[ 5 ]); // first ML row
		assertEquals( 1, encoding[ 6 ]); // second ML row
		assertEquals( 2, encoding[ 7 ]); // keeper row
        
		board = Board.rotateRight(board);
		assertTrue(board.legal());	
		candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=" + candidates.toStringCompact());
		locations = rule.locations(board, candidates);
		assertEquals( 1, locations.size());
		encoding = locations.get(0);
		assertEquals( 5, encoding[ 0 ]); // digit
		assertEquals( 1, encoding[ 1 ]); // rowCol orientation
		assertEquals( 2, encoding[ 2 ]); // first ML block
		assertEquals( 8, encoding[ 3 ]); // second ML block
		assertEquals( 5, encoding[ 4 ]); // keeper block
		assertEquals( 7, encoding[ 5 ]); // first ML row
		assertEquals( 8, encoding[ 6 ]); // second ML row
		assertEquals( 6, encoding[ 7 ]); // keeper row
        
		board = Board.rotateRight(board);
		assertTrue(board.legal());	
		candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=" + candidates.toStringCompact());
		locations = rule.locations(board, candidates);
		assertEquals( 1, locations.size());
		encoding = locations.get(0);
		assertEquals( 5, encoding[ 0 ]); // digit
		assertEquals( 0, encoding[ 1 ]); // rowCol orientation
		assertEquals( 6, encoding[ 2 ]); // first ML block
		assertEquals( 8, encoding[ 3 ]); // second ML block
		assertEquals( 7, encoding[ 4 ]); // keeper block
		assertEquals( 7, encoding[ 5 ]); // first ML row
		assertEquals( 8, encoding[ 6 ]); // second ML row
		assertEquals( 6, encoding[ 7 ]); // keeper row
        
	}
	@Test
	public void testUpdateRemoval() throws ParseException {
		Board board = new Board(MULTIPLELINES);
		assertTrue(board.legal());	
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);

		UpdateCandidatesRule rule = new MultipleLines();
		assertEquals(rule.ruleName(), rule.getClass().getSimpleName());

		// System.out.println( "Candidates=" + candidates.toStringCompact());
		List<int[]> locations = rule.locations(board, candidates);
		assertEquals( 1, locations.size());
		int [] encoding = locations.get(0);
		assertEquals( 5, encoding[ 0 ]); // digit
		assertEquals( 1, encoding[ 1 ]); // rowCol orientation
		assertEquals( 0, encoding[ 2 ]); // first ML block
		assertEquals( 6, encoding[ 3 ]); // second ML block
		assertEquals( 3, encoding[ 4 ]); // keeper block
		assertEquals( 0, encoding[ 5 ]); // first ML row
		assertEquals( 1, encoding[ 6 ]); // second ML row
		assertEquals( 2, encoding[ 7 ]); // keeper row
		
		// Assure candidates are updated
		int prevCandCount = candidates.getAllCandidateCount();
		int updates = rule.updateCandidates(board, null, candidates, locations); // remove digi 5, block 3
		assertEquals( 3, updates );
		assertEquals( prevCandCount, updates + candidates.getAllCandidateCount());

		// Assure rule does not report updated locations
		locations = rule.locations(board, candidates);
		prevCandCount = candidates.getAllCandidateCount();
		updates = rule.updateCandidates(board, null, candidates, locations); // remove digi 5, block 4
		assertEquals( 6, updates);
		assertEquals( prevCandCount, updates + candidates.getAllCandidateCount());
		// System.out.println( "Candidates=" + candidates.toStringCompact());
	}
}