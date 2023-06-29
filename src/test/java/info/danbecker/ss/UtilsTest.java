package info.danbecker.ss;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static info.danbecker.ss.Utils.DIGITS;
import static org.junit.jupiter.api.Assertions.*;

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
		}
	}

	@Test
	public void testComboEncoding() {
		// For example int[]{1,8} becomes integer 29.
		// For example int[]{0,4,5} becomes integer 156.
		assertEquals( 29, Utils.zerobasedIntsToOnebasedCombo( new int[] {1, 8} ));
		assertEquals( 156, Utils.zerobasedIntsToOnebasedCombo( new int[] {0, 4, 5} ));
		assertArrayEquals( new int[] {1, 8}, Utils.onebasedComboToZeroBasedInts( 29 ));
		assertArrayEquals( new int[] {0, 4, 5}, Utils.onebasedComboToZeroBasedInts( 156 ));
	}

	@Test
	public void arraysLists() {
		// Test collection conversion with no encoding change.
		int[] ints = { 1,2,3 };
		List<Integer> list = Arrays.stream(ints).boxed().toList();
		assertArrayEquals( ints, Utils.listToArray(list));
		assertEquals( list, Utils.arrayToList(ints));
	}
	@Test
	public void zerobasedAndonebasedConversion() {
		// Test collection conversion
		int[] obInts = { 1,2,3 };
		int[] obIntsCopy = { 1,2,3 };
		int[] zbInts = { 0,1,2 };
		int[] zbIntsCopy = { 0,1,2 };

		// Test copies
		assertNotEquals( zbInts, Utils.obTozbIntsCopy( obInts ));
		assertArrayEquals( zbInts, Utils.obTozbIntsCopy( obInts ));
		assertNotEquals( obInts, Utils.zbToobIntsCopy( zbInts ));
		assertArrayEquals( obInts, Utils.zbToobIntsCopy( zbInts ));

		// Test in place
		assertEquals( obInts, Utils.obTozbIntsInPlace( obInts ));
		assertArrayEquals( zbIntsCopy, obInts );
		assertEquals( zbInts, Utils.zbToobIntsInPlace( zbInts ));
		assertArrayEquals( obIntsCopy, zbInts );
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
		assertEquals( "         ", Utils.indent(3));
	}
}