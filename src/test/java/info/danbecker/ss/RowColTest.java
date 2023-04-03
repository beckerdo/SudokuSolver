package info.danbecker.ss;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static info.danbecker.ss.Board.ROWCOL;
import static org.junit.jupiter.api.Assertions.*;

public class RowColTest {
	@BeforeEach
	void setup() {
	}

	@Test
    public void testBasics() {
		RowCol b0 = new RowCol( 1, 1 );
		assertEquals( 1, b0.row() );
		assertEquals( 1, b0.col() );
		assertEquals( 0, b0.box() );

		RowCol b4 = new RowCol( 5, 5 );
		assertEquals( 5, b4.row() );
		assertEquals( 5, b4.col() );
		assertEquals( 4, b4.box() );

		assertEquals( 1, b0.compareTo( null ));
		assertEquals( -1, b0.compareTo( b4 ));
		assertEquals( 1, b4.compareTo( b0 ));
		assertEquals( 0, b4.compareTo( b4 ));
		assertTrue( b0.box() < b4.box());
		assertTrue( b4.box() > b0.box());

		assertEquals( "[1,1]", b0.toString());
		assertEquals( "[5,5,4]", b4.toStringWithBox());
	}

	@Test
	public void testUtils() {
		RowCol[] rowColArray = new RowCol[]{new RowCol(0,1),new RowCol(0,2),new RowCol(1,1)};
        List<RowCol> rowColList = new LinkedList<>();
		rowColList.add( new RowCol(0,1) );
		rowColList.add( new RowCol(0,2) );
		rowColList.add(new RowCol(1, 1));

		// assertEquals( rowColArray, RowCol.toArray(rowColList));
		assertTrue( Arrays.deepEquals( rowColArray, RowCol.toArray(rowColList)));
		assertEquals( rowColList, RowCol.toList(rowColArray));
		assertEquals( "[0,1],[0,2],[1,1]", RowCol.toString( rowColArray ));
		assertEquals( "[0,1],[0,2],[1,1]", RowCol.toString( rowColList ));

		RowCol[] roundTripArray = RowCol.toArray( rowColList );
		assertTrue( Arrays.equals(rowColArray, roundTripArray));

		List<RowCol> roundTripList = RowCol.toList( rowColArray );
		assertTrue( rowColList.equals(roundTripList));
	}

	@Test
	public void testRowColBlockMatch() {
		List<RowCol> rowMatch = new ArrayList<>();
		rowMatch.add(ROWCOL[0][0]);
		rowMatch.add(ROWCOL[0][1]);
		List<RowCol> colMatch = new ArrayList<>();
		colMatch.add(ROWCOL[0][1]);
		colMatch.add(ROWCOL[1][0]);
		List<RowCol> noMatch = new ArrayList<>();
		noMatch.add(ROWCOL[0][0]);
		noMatch.add(ROWCOL[8][8]);

		assertTrue(RowCol.rowsMatch(rowMatch));
		assertTrue(RowCol.rowsMatch(RowCol.toArray(rowMatch)));
		assertEquals(0, RowCol.unitMatch( Utils.Unit.ROW, new RowCol( 0,0 ), new RowCol( 0,1)));
		assertTrue( Arrays.equals(new int[]{0,0}, RowCol.unitMatch( new RowCol( 0,0 ), new RowCol( 0,1 ))));
		assertEquals( RowCol.NOT_FOUND, RowCol.unitMatch( new RowCol( 0,0 ), new RowCol( 8,8)));
		assertEquals(1, RowCol.rowCount(RowCol.toArray(rowMatch)));
		assertFalse(RowCol.colsMatch(colMatch));
		assertFalse(RowCol.colsMatch(RowCol.toArray(colMatch)));
		assertEquals(Board.NOT_FOUND, RowCol.unitMatch( Utils.Unit.COL, new RowCol( 0,0 ), new RowCol( 0,1)));
		assertEquals(2, RowCol.colCount( RowCol.toArray(colMatch)));

		assertTrue(RowCol.boxesMatch(rowMatch));
		assertTrue(RowCol.boxesMatch(colMatch));
		assertFalse(RowCol.boxesMatch(noMatch));

		// Test all blocks in board
		for (int blocki = 0; blocki < Board.BOXR.length; blocki++) {
			// System.out.println("Blocki=" + blocki + ", BOXR=" + RowCol.toString(Board.BOXR[blocki]));
			assertTrue(RowCol.boxesMatch(Board.BOXR[blocki]));
		}
		for (int blocki = 0; blocki < Board.BOXC.length; blocki++) {
			// System.out.println("Blocki=" + blocki + ", BOXC=" + RowCol.toString(Board.BOXR[blocki]));
			assertTrue(RowCol.boxesMatch(Board.BOXC[blocki]));
		}
	}
}