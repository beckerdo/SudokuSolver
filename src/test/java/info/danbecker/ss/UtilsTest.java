package info.danbecker.ss;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static java.lang.String.format;

import static info.danbecker.ss.Utils.DIGITS;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

public class UtilsTest {
	@BeforeEach
    public void setup() {
	}
		
	@Test
	public void testCombos() {
		for( int subsetSize = 3; subsetSize >= 1; subsetSize-- ) {
			List<int[]> combinations = Utils.comboGenerate(DIGITS, subsetSize);
			// Combinatorics says C(n,r) = n! / r! * (n-r)!
			int setSize = Utils.factorial( DIGITS ) / (Utils.factorial( subsetSize ) * Utils.factorial(DIGITS - subsetSize));
			assertEquals( setSize, combinations.size());
			// Note combos are 0 based
			// System.out.println(format("generated %d combos of %d/%d digits %s",
			// 	combinations.size(), subsetSize, DIGITS, Utils.digitsToString(combinations)) );
		}
	}

	@Test
	public void testComboEncoding() {
		// For example int[]{1,8} becomes integer 29.
		// For example int[]{0,4,5} becomes integer 156.
		assertEquals( 29, Utils.intsToCombo( new int[] {1, 8} ));
		assertEquals( 156, Utils.intsToCombo( new int[] {0, 4, 5} ));
		assertArrayEquals( new int[] {1, 8}, Utils.comboToInts( 29 ));
		assertArrayEquals( new int[] {0, 4, 5}, Utils.comboToInts( 156 ));
	}

	@Test
	public void arraysLists() {
		// Test collection convertion with no encoding change.
		int[] ints = new int[]{ 1,2,3 };
		List<Integer> list = Arrays.stream(ints).boxed().toList();
		assertArrayEquals( ints, Utils.listToArray(list));
		assertEquals( list, Utils.arrayToList(ints));
	}
	@Test
	public void compareTo() {
		List<Integer> one = Utils.arrayToList( new int[]{ 1 });
		List<Integer> two = Utils.arrayToList( new int[]{ 1, 2 });
		List<Integer> two2 = Utils.arrayToList( new int[]{ 2, 1 });

		assertEquals( 0, Utils.compareTo(null, null));
		assertEquals( -1, Utils.compareTo( null, one));
		assertEquals( 1, Utils.compareTo( one, null));
		assertEquals( 0, Utils.compareTo( one, one));

		assertEquals( -1, Utils.compareTo( one, two));
		assertEquals( 1, Utils.compareTo( two, one));
		assertEquals( 0, Utils.compareTo( two, two2));
	}

	@Test
	public void testMisc() {
		assertEquals( "         ", Utils.createIndent(3));
	}
}