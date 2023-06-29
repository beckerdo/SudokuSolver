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
	@BeforeEach
	public void setup() {
	}

	// This board and candidates equals the example at
	// https://www.sudokuoftheday.com/techniques/multiple-lines
	public static String MULTIPLELINES =
			"..9.3.6...36.14.891..869.35.9....8...1.....9..68.9.17.6.19.3..297264.3....3.2.9..";
	public static String MULTIPLELINES_SOLUTION =
			"849532617536714289127869435395471826714286593268395174681953742972648351453127968";

	@Test
	public void testBasics() throws ParseException {
		Board board = new Board(MULTIPLELINES);
		assertTrue(board.legal());	
		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		FindUpdateRule rule = new MultipleLines();
		assertEquals(rule.ruleName(), rule.getClass().getSimpleName());

		List<int[]> locations = rule.find(board, candidates);
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
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=" + candidates.toStringCompact());
		locations = rule.find(board, candidates);
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
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=" + candidates.toStringCompact());
		locations = rule.find(board, candidates);
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
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=" + candidates.toStringCompact());
		locations = rule.find(board, candidates);
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
		(new LegalCandidates()).update(board, null, candidates, null);

		FindUpdateRule rule = new MultipleLines();
		assertEquals(rule.ruleName(), rule.getClass().getSimpleName());

		// System.out.println( "Candidates=" + candidates.toStringCompact());
		List<int[]> locations = rule.find(board, candidates);
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
		int prevEntries = candidates.getAllOccupiedCount();
		int prevCandCount = candidates.getAllCount();
		int updates = rule.update(board, new Board(MULTIPLELINES_SOLUTION ), candidates, locations); // remove digi 5, block 3
		assertEquals( 3, updates );
		assertEquals( prevEntries, candidates.getAllOccupiedCount());
		assertEquals( prevCandCount, updates + candidates.getAllCount());
	}
}