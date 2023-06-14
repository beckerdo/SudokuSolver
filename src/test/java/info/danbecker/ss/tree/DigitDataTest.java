package info.danbecker.ss.tree;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.*;

public class DigitDataTest {
	@BeforeEach
    public void setup() {
	}
		
	@Test
    public void testBasics() {
		DigitData cData1 = new DigitData( 1, ROWCOL[1][1], 0);
		DigitData cData2 = new DigitData( 1, ROWCOL[1][1], 2);
		DigitData cData3 = new DigitData( 2, ROWCOL[1][1], 0);
		DigitData cData4 = new DigitData( 1, ROWCOL[7][7], 0);
		DigitData cData5 = new DigitData( 1, ROWCOL[1][1], 0);

		assertEquals( cData1, cData5 );
		assertNotEquals( cData1, cData2 );
		assertNotEquals( cData1, cData3 );
		assertNotEquals( cData1, cData4 );

		assertEquals( 1, cData1.digit );
		assertEquals(  ROWCOL[1][1], cData1.rowCol );
		assertEquals(  0, cData1.color );

		assertTrue( cData4.toString().contains( "Digi=1" ));
		assertTrue( cData4.toString().contains( "rowCol=[7,7]" ));
		assertTrue( cData4.toString().contains( "color=0" ));

		assertEquals( 0, cData1.compareRowCol.compareTo(cData5));
	}

	@Test
    public void testCloneable() {
		DigitData cData1 = new DigitData( 1, ROWCOL[1][1], 0);
		DigitData cDataClone = (DigitData) cData1.clone();

		assertNotSame( cData1, cDataClone );
		assertEquals( cData1, cDataClone );
		assertEquals( cData1.digit, cDataClone.digit );
		assertEquals( cData1.rowCol, cDataClone.rowCol );
		assertEquals( cData1.color, cDataClone.color );
	}
}