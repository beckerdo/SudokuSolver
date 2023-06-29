package info.danbecker.ss.rules;

import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;

import java.text.ParseException;
import java.util.*;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.*;

public class SingleCandidatesTest {

	@BeforeEach
    public void setup() {
	}

	public static String SINGLECANDIDATE =
			"..4..6.2...78..91.......3.8.183..2..3..789..1..9..1.6.8.3...5...45..36...265..1..";
	public static String SINGLECANDIDATE_SOLUTION =
			"984136725237854916651297348418365297362789451579421863893612574145973682726548139";

	@Test
    public void testBasics() throws ParseException {
		Board board = new Board( SINGLECANDIDATE );
		Candidates candidates = new Candidates( board );
		(new LegalCandidates()).update(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		FindUpdateRule rule = new SingleCandidates();
		assertEquals( rule.ruleName(), rule.getClass().getSimpleName() );

		List<int[]> encs = rule.find(board, candidates);
		assertNotNull( encs  );
		assertEquals( 3, encs.size() );
		int [] location = encs.get( 0 );
		assertNotNull( location  );
		assertEquals( 7, location[ 0 ] );
		assertEquals( 0, location[ 1 ] );
		assertEquals( 6, location[ 2 ] );
		location = encs.get( 1 );
		assertNotNull( location  );
		assertEquals( 2, location[ 0 ] );
		assertEquals( 4, location[ 1 ] );
		assertEquals( 2, location[ 2 ] );

		int prevCount = candidates.getAllCount();
		int updates = rule.update(board, new Board(SINGLECANDIDATE_SOLUTION), candidates, encs);
		assertEquals( 3, updates );
		assertEquals( prevCount - 3, candidates.getAllCount());
    }

	@Test
	public void testEncoding() throws ParseException {
		SingleCandidates rule = new SingleCandidates();
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