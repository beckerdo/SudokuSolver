package info.danbecker.ss.rules;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.*;

public class SinglePositionsTest {

	@BeforeEach
    public void setup() {
	}

	public static String SINGLEPOSITION =
			"..6.3.7.8.3......12.....6..1..35...6.79.4.15.5...17..4..2.....76......8.4.7.6.2..";
	public static String SINGLEPOSITION_SOLUTION =
			"946135728735682941281974635124359876379846152568217394812493567653721489497568213";

	@Test
    public void testBasics() throws ParseException {
		Board board = new Board( SINGLEPOSITION );
		Candidates candidates = new Candidates( board );
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		FindUpdateRule rule = new SinglePositions();
		assertEquals( rule.ruleName(), rule.getClass().getSimpleName() );

		List<int[]> encs = rule.find(board, candidates);
		assertNotNull( encs  );
		assertEquals( 5, encs.size() );
		int [] enc = encs.get( 0 );
		assertNotNull( enc  );
		assertEquals( 2, enc[ 0 ] );
		assertEquals( 4, enc[ 1 ] );
		assertEquals( 8, enc[ 2 ] );
		enc = encs.get( encs.size() - 1 );
		assertNotNull( enc  );
		assertEquals( 7, enc[ 0 ] );
		assertEquals( 1, enc[ 1 ] );
		assertEquals( 0, enc[ 2 ] );

		int prevCount = candidates.getAllCount();
		int updates = rule.update(board, new Board(SINGLEPOSITION_SOLUTION), candidates, encs);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());
		assertEquals( 16, updates );
		assertEquals( 16, prevCount - candidates.getAllCount());
	}

	@Test
	public void testEncoding() {
		SinglePositions rule = new SinglePositions();
		int[] enc = {1,2,3};
		String encStr = rule.encodingToString( enc );
		assertTrue( encStr.contains( "1 at "));
		assertTrue( encStr.contains( "at [2,3]"));

		Map<Integer,List<RowCol>> digitMap = new Hashtable<>();
		digitMap.put( 1, Arrays.asList(ROWCOL[2][3]));
		digitMap.put( 2, Arrays.asList(ROWCOL[3][4],ROWCOL[4][5]));
		digitMap.put( 3, Arrays.asList(ROWCOL[4][5],ROWCOL[5][6],ROWCOL[7][8]));

		String encs = Utils.digitMapToString(digitMap);
		assertTrue( encs.contains( "digit 1:[2,3]"));
		assertTrue( encs.contains( "digit 2:[3,4],[4,5]"));
		assertTrue( encs.contains( "digit 3:[4,5],[5,6],[7,8]"));
	}
}