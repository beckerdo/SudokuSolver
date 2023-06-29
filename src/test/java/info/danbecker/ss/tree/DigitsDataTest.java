package info.danbecker.ss.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.*;

public class DigitsDataTest {
	@BeforeEach
    public void setup() {
	}
		
	@Test
    public void testBasics() {
		List<Integer> digits = Arrays.asList(1, 2, 3);
		DigitsData dData1 = new DigitsData( digits, ROWCOL[1][1], 0);
		DigitsData dData2 = new DigitsData( digits, ROWCOL[1][1], 2);
		DigitsData dData3 = new DigitsData( Arrays.asList( 1, 2), ROWCOL[1][1], 0);
		DigitsData dData4 = new DigitsData( digits, ROWCOL[7][7], 0);
		DigitsData dData5 = new DigitsData( digits, ROWCOL[1][1], 0);

		assertEquals( dData1, dData5 );
		assertNotEquals( dData1, dData2 );
		assertNotEquals( dData1, dData3 );
		assertNotEquals( dData1, dData4 );

		assertEquals( digits, dData1.digits );
		assertEquals(  ROWCOL[1][1], dData1.rowCol );
		assertEquals(  0, dData1.color );

		String dataStr = dData4.toString();
		System.out.println("Data=" + dataStr);
		assertTrue( dataStr.contains( "Digits={123}" ));
		assertTrue( dataStr.contains( "rowCol=[7,7]" ));
		assertTrue( dataStr.contains( "color=0" ));

		assertEquals( 0, new DigitsData.RowColMatch( dData5 ).compareTo( dData1 ));
	}

	@Test
    public void testCloneable() {
		List<Integer> digits = Arrays.asList(1, 2, 3);
		DigitsData dData1 = new DigitsData( digits, ROWCOL[1][1], 0);
		DigitsData dDataClone = (DigitsData) dData1.clone();

		assertNotSame( dData1, dDataClone );
		assertEquals( dData1.digits, dDataClone.digits );
		assertEquals( dData1.rowCol, dDataClone.rowCol );
		assertEquals( dData1.color, dDataClone.color );

		dDataClone.digits.set(2,4);
		System.out.println( dData1 );
		System.out.println( dDataClone );
		assertNotEquals( dData1, dDataClone );
		assertNotEquals( dData1.digits, dDataClone.digits );
	}
}