package info.danbecker.ss;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static info.danbecker.ss.Utils.DIGITS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UtilsTest {
	@BeforeEach
    public void setup() {
	}
		
	@Test
    public void testLocations() {
		List<int[]> locs = new ArrayList<>();
		locs.add(new int[]{0,1});
		locs.add(new int[]{2,3});
		locs.add(new int[]{3,4});	
		assertEquals( "[0,1],[2,3],[3,4]", Utils.locationsString(locs));
		
		int [][] test2 = new int[][]{new int[]{5,6},new int[]{7,8},new int[]{9,0}};
		assertEquals( "[5,6],[7,8],[9,0]", Utils.locationsString(test2));
		
		int [][] rowCols = new int[][]{new int[]{5,6},new int[]{7,8},new int[]{9,0}};
		List<int[]> list = Utils.convertArrayToList( rowCols );
		assertEquals( rowCols.length, list.size());
		for ( int i = 0; i < rowCols.length; i++ )
			assertEquals( rowCols[i], list.get(i));
		
		int[][] roundTrip = Utils.convertListToArray( list );
		assertTrue( Arrays.equals(rowCols, roundTrip));
    }
	
	@Test
	public void testCombos() {
		for( int subsetSize = 3; subsetSize >= 1; subsetSize-- ) {
			ArrayList<int[]> combinations = Utils.comboGenerate(DIGITS, subsetSize);
			// Combinatorics says C(n,r) = n! / r! * (n-r)!
			int setSize = Utils.factorial( DIGITS ) / (Utils.factorial( subsetSize ) * Utils.factorial(DIGITS - subsetSize));
			assertEquals( setSize, combinations.size());
			// Note combos are 0 based
			// System.out.println(format("generated %d combinations of %d items from %d %s", 
			// 	combinations.size(), subsetSize, DIGITS, Utils.locationsString(combinations)) );
		}
	}
	
	@Test
	public void testComboEncoding() {
		// For example int[]{1,8} becomes integer 29.
		// For example int[]{0,4,5} becomes integer 156.
		assertEquals( 29, Utils.comboToInt( new int[] {1, 8} ));
		assertEquals( 156, Utils.comboToInt( new int[] {0, 4, 5} ));
		assertTrue( Arrays.equals( new int[] {1, 8}, Utils.intToCombo( 29 )));
		assertTrue( Arrays.equals( new int[] {0, 4, 5}, Utils.intToCombo( 156 )));
	}

	@Test
	public void testRowColBlockMatch() {
		List<int[]> rowMatch = new ArrayList<int []>();
		rowMatch.add( new int[] { 0, 0 });
		rowMatch.add( new int[] { 0, 1 });
		List<int[]> colMatch = new ArrayList<int []>();
		colMatch.add( new int[] { 0, 0 });
		colMatch.add( new int[] { 1, 0 });
		List<int[]> noMatch = new ArrayList<int []>();
		noMatch.add( new int[] { 0, 0 });
		noMatch.add( new int[] { 8, 8 });
		
		assertTrue( Utils.rowsMatch( rowMatch ));
		assertTrue( Utils.rowsMatch( new int[][]{ new int[]{0,0}, new int[]{0,1} }));
		assertFalse( Utils.rowsMatch( noMatch ));
		assertEquals( 1, Utils.rowCount( new int[][]{ new int[]{0,0}, new int[]{0,1} }));
		assertFalse( Utils.rowsMatch( new int[][]{ new int[]{0,0}, new int[]{1,0} }));
		assertEquals( 2, Utils.rowCount( new int[][]{ new int[]{0,0}, new int[]{1,0} }));
		
		assertTrue( Utils.colsMatch( colMatch ));
		assertTrue( Utils.colsMatch( new int[][]{ new int[]{0,0}, new int[]{1,0} }));
		assertEquals( 1, Utils.colCount( new int[][]{ new int[]{0,0}, new int[]{1,0} }));
		assertFalse( Utils.colsMatch( noMatch ));
		assertFalse( Utils.colsMatch( new int[][]{ new int[]{0,0}, new int[]{0,1} }));
		assertEquals( 2, Utils.colCount( new int[][]{ new int[]{0,0}, new int[]{1,1} }));

		assertTrue( Utils.boxesMatch( rowMatch ));
		assertTrue( Utils.boxesMatch( colMatch ));
		assertFalse( Utils.boxesMatch( noMatch ));

		assertTrue( Utils.boxesMatch( new int[][]{ new int[]{0,0}, new int[]{0,1} } ));
		assertTrue( Utils.boxesMatch( new int[][]{ new int[]{0,0}, new int[]{1,0} } ));
		assertFalse( Utils.boxesMatch( new int[][]{ new int[]{0,0}, new int[]{8,8} } ));
		
		// Test all blocks in board
		for ( int blocki = 0; blocki < Board.BOXR.length; blocki++ ) {
           assertTrue( Utils.boxesMatch( Board.BOXR[blocki] ));
		}

	}
	

	
}