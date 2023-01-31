package info.danbecker.ss;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class SudokuSolverTest {
	@BeforeEach
	public void setup() {
	}

	@Test
	public void jsonObjectByNames() {
		String str = "{" + "\"trueKey\":true," + "\"falseKey\":false," + "\"nullKey\":null,"
				+ "\"stringKey\":\"hello world!\"," + "\"escapeStringKey\":\"h\be\tllo w\u1234orld!\","
				+ "\"intKey\":42," + "\"doubleKey\":-23.45e67" + "}";
		JSONObject jsonObject = new JSONObject(str);
		// System.out.println("jsonObject=" + jsonObject);
		assertTrue( null != jsonObject );

		// validate JSON
		String[] keys = { "falseKey", "stringKey", "nullKey", "doubleKey" };
		JSONObject jsonObjectByName = new JSONObject(jsonObject, keys);
		assertTrue( null != jsonObjectByName );
		//System.out.println("jsonObjectByName=" + jsonObjectByName);
	}
}