package info.danbecker.ss.rules;

import info.danbecker.ss.RowCol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static info.danbecker.ss.Board.ROWCOL;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;
import info.danbecker.ss.Utils.Unit;
import info.danbecker.ss.tree.TreeNode;
import info.danbecker.ss.tree.ColorData;

import java.text.ParseException;
import java.util.List;

public class ColorChainsTest {
	// https://www.sudokuoftheday.com/techniques/x-wings
	// This puzzle appears to have additional candidates removed
	public static String COLOR_CHAIN_SOTD = 
	"...5...39-.39.41257-..5.3968.-.9.32..6.-..895674.-....1892.-.461.53..-.87.9.51.-.5.......";

	// https://www.thonky.com/sudoku/simple-coloring
	// Opposite Colors in the Same Unit
	// Digit 4 chain	
	public static String COLOR_CHAIN_TH2 =
	"..85.21.3-35...12.8-.21.3..5.-56324.7.1-4821.753.-179.53..2-.3..2581.-8.731..25-215.843..";

	@BeforeEach
	public void setup() {
	}

	// https://www.thonky.com/sudoku/simple-coloring
	// Color appears twice in Same Unit
	// Digit 2 chain starting from [3,0], clash at [3,3]
	public static String COLOR_CHAIN_TH1 =
	"123...587-..5817239-987...164-.51..8473-39.75.618-7.81..925-.76...891-53..81746-81..7.352";

	// https://www.thonky.com/sudoku/simple-coloring
	// Seeing two opposite colors
	// Digit 2 chain	
	public static String COLOR_CHAIN_TH3 =
	"..463.5..-6.54.1..3-37..5964.-938.6.154-457198362-216345987-.435.6.19-.6.9.34.5-5.9.14.36";
	@Test
	public void testBuildColorTree() throws ParseException {
		Board board = new Board(COLOR_CHAIN_TH3);
		assertTrue(board.legal());

		ColorChains rule = new ColorChains();

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// No color clash in pairs tree
		int digit = 2;
		int groupSize = 2;
		RowCol startLoc = ROWCOL[2][2];
		TreeNode<ColorData> tree = new TreeNode<>(new ColorData(digit, startLoc, 0), 3);
		List<int[]> colorClash = rule.buildColorTree( candidates, tree, digit, groupSize );

		RowCol testLoc = ROWCOL[8][3];
		int testDigit = 8;
		
		assertEquals( 8, tree.size());
		assertEquals( 1, tree.findTreeNodes(new RowColMatch( ROWCOL[2][2] )).size() );
		assertEquals( 1, tree.findTreeNodes(new RowColMatch( ROWCOL[8][1] )).size() );
		assertEquals( 0, tree.findTreeNodes(new RowColMatch( testLoc )).size() );
		assertEquals( 0, colorClash.size());

		// Induce color clash
		candidates.removeCandidate( testLoc, testDigit ); // can see two colors
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		tree = new TreeNode<>(new ColorData(digit, startLoc, 0), 3);
		colorClash = rule.buildColorTree( candidates, tree, digit, groupSize );
		tree.printTree();

		assertEquals( 8, tree.size());
		assertEquals( 1, tree.findTreeNodes(new RowColMatch( ROWCOL[2][2] )).size() );
		assertEquals( 1, tree.findTreeNodes(new RowColMatch( ROWCOL[8][1] )).size() );
		assertEquals( 0, tree.findTreeNodes(new RowColMatch( testLoc )).size() );
		assertEquals( 3, colorClash.size());
	}
	
	@Test
	public void testLocations() throws ParseException {
		// Test for a rowCol color mismatch, which is the same
		// as saying two parent nodes can see rowCol and
		// want to color it differently.
		Board board = new Board(COLOR_CHAIN_TH3);
		assertTrue(board.legal());

		ColorChains rule = new ColorChains();

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// No color clash in pairs tree
		int digit = 2;
		List<int[]> encs = rule.locations(board, candidates, digit );		
		assertEquals( 3, encs.size());
		
		int [] enc = encs.get(0);
		System.out.println( format( "Rule %s, color clash %s", rule.ruleName(), rule.encodingToString( enc ) ));
		assertEquals( 1, enc[ 5 ] );
		assertEquals( 4, enc[ 6 ] );
		enc = encs.get(1);
		System.out.println( format( "Rule %s, color clash %s", rule.ruleName(), rule.encodingToString( enc ) ));
		assertEquals( 7, enc[ 5 ] );
		assertEquals( 4, enc[ 6 ] );
		enc = encs.get(2);
		System.out.println( format( "Rule %s, color clash %s", rule.ruleName(), rule.encodingToString( enc ) ));
		assertEquals( 8, enc[ 5 ] );
		assertEquals( 3, enc[ 6 ] );
		
		// Test
        int prevEntries = candidates.entryCount();
		int prevCandidates = candidates.candidateCount();
		int updates = rule.updateCandidates(board, null, candidates, encs);
		// Entries same. Candidate loses 1. 
		// Candidates loses 1.
		assertEquals( prevEntries, candidates.entryCount() );
		assertEquals( prevCandidates, candidates.candidateCount() + updates);
	}
	
	@Test
	public void testEncode()  {
		// Encode tree as int []
		// encodes as
		// - digit						0
		// - unit ordinal				1
		// - unit number				2
		// - tree root rowCol			34
		// - intended/cand rowCol		56
		// - intended/cand color		7
		// - parent rowCol				89
		// - parent color 				A
		// - sameunit rowCol			BC
		// - sameunit color				D
		
		int testDigit = 5;
		Unit testUnit = Unit.COL;
		RowCol testRoot = ROWCOL[1][1];
		
		RowCol cLoc = ROWCOL[5][5];
		int cColor = 1;
		RowCol pLoc = ROWCOL[6][6];
		int pColor = 0;
		RowCol sLoc = ROWCOL[7][7];
		int sColor = 1;
		
		int [] enc = ColorChains.encode( testDigit, testUnit, testRoot.col(), testRoot,
				cLoc, cColor, pLoc, pColor, sLoc, sColor );

		assertEquals( testDigit, enc[0]);
		assertEquals( testUnit.ordinal(), enc[1]);
		assertEquals( testRoot.col(), enc[2]);

		assertEquals( testRoot.row(), enc[3]);
		assertEquals( testRoot.col(), enc[4]);
		
		assertEquals( cLoc.row(), enc[5]);
		assertEquals( cLoc.col(), enc[6]);
		assertEquals( cColor, enc[7]);
		assertEquals( pLoc.row(), enc[8]);
		assertEquals( pLoc.col(), enc[9]);
		assertEquals( pColor, enc[10]);
		assertEquals( sLoc.row(), enc[11]);
		assertEquals( sLoc.col(), enc[12]);
		assertEquals( sColor, enc[13]);
		
		
		String encString = (new ColorChains()).encodingToString( enc );
		// System.out.println( "Enc=" + encString);
		assertTrue( encString.contains( "digit 5"));
		assertTrue( encString.contains( "origin [1,1]"));
		assertTrue( encString.contains( "candidate [5,5]"));
		assertTrue( encString.contains( "-1"));
		assertTrue( encString.contains( "COL 1"));
		assertTrue( encString.contains( "[6,6]"));
		assertTrue( encString.contains( "-0"));
		assertTrue( encString.contains( "[7,7]"));
	}


	public static String P20221118_DIABOLICAL_17500 =
	"7...4.8..-.18...5..-9..8...3.-3..4.7..2-....8....-1..9.6..3-.9...4..8-..2...67.-..7.2...4";			
	public static String P20221118_DIABOLICAL_17500_PARTIAL =
	"7.3.4.8..-218...54.-94.8..23.-38.4179.2-.79.8.4..-124956783-.91..4328-4.21.867.-8.7.2.1.4";			
	public static String P20221118_SOLUTION = 
	"763245819218739546945861237386417952579382461124956783691574328432198675857623194";

	@Test
	public void testBug20221118() throws ParseException {
		Board board = new Board(P20221118_DIABOLICAL_17500_PARTIAL);
		assertTrue(board.legal());
		Board solution = new Board(P20221118_SOLUTION);
		assertTrue(solution.legal());

		ColorChains rule = new ColorChains();

		Candidates candidates = new Candidates(board);
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
		// System.out.println( "Candidates=\n" + candidates.toStringBoxed());

		// No color clash in pairs tree
		List<int[]> encs = rule.locations(board, candidates );		
		assertEquals( 27, encs.size());
		
	}
}