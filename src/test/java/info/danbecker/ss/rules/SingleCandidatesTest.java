package info.danbecker.ss.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;

import java.text.ParseException;
import java.util.List;

public class SingleCandidatesTest {
	public static String SINGLECANDIDATE = """
...3....1
38.....2.
.2..96..4
.4...7...
9.......3
...4...5.
5..67..3.
.3.....79
8....2...
""";
	
	@BeforeEach
    public void setup() {
	}
		
	@Test
    public void testBasics() throws ParseException {
		Board board = new Board( SINGLECANDIDATE );
		Candidates candidates = new Candidates( board );
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		UpdateCandidatesRule rule = new SingleCandidates();
		assertEquals( rule.ruleName(), rule.getClass().getSimpleName() );

		List<int[]> locations = rule.locations(board, candidates);
		assertNotNull( locations  );
		assertEquals( 1, locations.size() );
		int [] location = locations.get( 0 );
		assertNotNull( location  );
		assertEquals( 2, location[ 0 ] );
		assertEquals( 7, location[ 1 ] );
    }
}