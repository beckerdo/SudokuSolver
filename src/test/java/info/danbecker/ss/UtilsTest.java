package info.danbecker.ss;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static info.danbecker.ss.Board.ROWCOL;
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
	public void testMisc() {
		int[][] test = new int[][]{ new int[] {0,1}, new int[] {7,8}};
		assertEquals( "[0,1],[7,8]", Utils.locationsString( Arrays.asList(test )));

		int[] test2 = new int[]{ -1, 0, 1, -1 };
		assertTrue( Utils.contains( test2, 0 ) );
		assertEquals( 1, Utils.location( test2, 0 ));
		assertEquals( 2, Utils.count( test2, -1 ));
		assertEquals( "         ", Utils.createIndent(3));
	}

}