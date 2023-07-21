package info.danbecker.ss.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class LabelEdgeTest {
	@Test
	public void testBasics() {
		LabelEdge test = new LabelEdge("1");
		assertEquals( "1", test.getLabel());
		test.setLabel( "2");
		assertEquals( "2", test.getLabel());
		test.addToLabel(3);
		assertEquals( "23", test.getLabel());
		test.addToLabel(3);
		assertEquals( "23", test.getLabel());
		test.removeFromToLabel(2);
		assertEquals( "3", test.getLabel());

		assertEquals( "3", test.toString());
		assertEquals( "null-3-null", test.toStringVerbose());
	}

	@Test
	public void testCompare() {
		assertEquals( 0, LabelEdge.labelExactCompare( null, null ));
		assertNotEquals( 0, LabelEdge.labelExactCompare( null, "9" ));
		assertNotEquals( 0, LabelEdge.labelExactCompare( "3", null ));
		assertEquals( 0, LabelEdge.labelExactCompare( "3", "3" ));
		assertNotEquals( 0, LabelEdge.labelExactCompare( "3", "9" ));
		assertNotEquals( 0, LabelEdge.labelExactCompare( "3", "39" ));
		assertNotEquals( 0, LabelEdge.labelExactCompare( "39", "3" ));

		assertEquals( 0, LabelEdge.labelLooseCompare( null, null ));
		assertNotEquals( 0, LabelEdge.labelLooseCompare( null, "9" ));
		assertNotEquals( 0, LabelEdge.labelLooseCompare( "3", null ));
		assertEquals( 0, LabelEdge.labelLooseCompare( "3", "3" ));
		assertNotEquals( 0, LabelEdge.labelLooseCompare( "3", "9" ));
		assertEquals( 0, LabelEdge.labelLooseCompare( "3", "39" ));
		assertEquals( 0, LabelEdge.labelLooseCompare( "39", "3" ));
	}
}