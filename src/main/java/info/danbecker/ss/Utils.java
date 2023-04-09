package info.danbecker.ss;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * SudokuSolver
 * <p>
 * Utilities to be used by all classes.
 * Should all be static classes.
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class Utils {
	public static short ROWS = 9;
	public static short COLS = 9;
	public static short BOXES = 9; 
	public static short DIGITS = 9;

	/** Enum for different divisions of a Sudoku Board */
	public enum Unit { ROW, COL, BOX }
	
	/** Given a file path, parse file as JSON and return JSONObject */
	public static JSONObject parseJSON( String pathString ) throws IOException {
    	if (null == pathString || 0 == pathString.length() )
			throw new IllegalArgumentException( "input file path string must not be null or empty");
		Path path = Path.of( pathString );
		if (!Files.exists( path ))
			throw new IllegalArgumentException( "input file path must exist, path=" + path );
		if (!Files.isReadable( path  ))
			throw new IllegalArgumentException( "input file path must be readable, path=" + path);
		return new JSONObject(Files.readString( path, StandardCharsets.UTF_8));
	}

	public static boolean contains( int[] arr, int digit ) {
		return Board.NOT_FOUND != location( arr, digit );
	}
	
	public static int count( int[] arr, int digit ) {
		int count = 0;
		for( int i = 0; i < arr.length; i++) {
			if ( arr[ i ] == digit ) count++;
		}
		return count;
	}
	
	public static int location( int[] arr, int digit ) {
		for( int i = 0; i < arr.length; i++) {
			if ( arr[ i ] == digit ) return i;
		}
		return Board.NOT_FOUND;
	}

	// Generate combinations of n elements (9 digits), r at a time.
	// Combinatorics says C(n,r) = n! / r! * (n-r)!
	// which is written recursively as C(n,r)=C(n-1,r-1) + C(n-1,r)
	// doubles=12,13,14,...,23,24,....89
	// triples=112,113,114,...,123,124,...189,
	// Note that these combos are 0 based	
	
	/**
	 * Generate combinations of n non-repeating digits, r at a time.
	 * <p>
	 * Combinatorics says C(n,r) = n! / r! * (n-r)!
	 * which is written recursively as C(n,r)=C(n-1,r-1) + C(n-1,r)
	 * <p>
	 * Note that these combos are 0 based.
	 * <p>
	 * For example:
	 * singles (n=9,r=1)->0,1,2,...,8
	 * doubles (n=9,r=2)->01,02...08,12,13...18,21...78
	 * triples (n=9,r=3)->012,013,...,018,023,024,...028,034,035,...038,...,678
	 * <p>
	 * @param n is the set size for example 9 for digits 0 through 8
	 * @param r is the subset size for example r = 2 takes 2 digits from 9
	 * @return int [] of 0 based integer combinations 
	 */
	public static List<int[]> comboGenerate(int n, int r) {
	    List<int[]> combinations = new LinkedList<>();
	    comboHelper(combinations, new int[r], 0, n-1, 0);
	    return combinations;
	}
	
	// Helps with the (tail) recursion of method comboGenerate
	private static void comboHelper(List<int[]> combinations, int[] data, int start, int end, int index) {
	    if (index == data.length) {
	        int[] combination = data.clone();
	        combinations.add(combination);
	    } else if (start <= end) {
	        data[index] = start;
	        comboHelper(combinations, data, start + 1, end, index + 1);
	        comboHelper(combinations, data, start + 1, end, index);
	    }
	}
	
	/* Returns factorial n! of given number
	 */
	public static int factorial(int n) {
	    int fact = 1;
	    for (int i = 2; i <= n; i++) {
	        fact = fact * i;
	    }
	    return fact;
	}

	/**
	 * Convert an int [] of ints to a single int combo.
	 * <p>
	 * The param int[] is 0-based. The return int is 1-based
	 * to prevent losing leading zeros.
	 * <p>
	 * For example int[]{1,8} becomes integer 29.
	 * For example int[]{0,4,5} becomes integer 156.
	 * 
	 * @param ints int[] of 0 based integers
	 * @return single integer of 1 based digits
	 */
	public static int intsToCombo(int[] ints) {
	   if ( ints == null) return -1;
	   int value = 0;
	   for( int i = 0; i < ints.length; i++ ) {
		   value = value * 10 + ints[ i ] + 1;
	   }
	   return value;
	}
	
	/**
	 * Convert an int combo to an int[] of ints
	 * <p>
	 * The param int is 1-based. The return int [] is 0-based
	 * to prevent losing leading zeros.
	 * <p>
	 * For example 29 becomes int[]{1,8}
	 * For example 156 becomes int[]{0,4,5}
	 * 
	 * @param combo int of one-based digits
	 * @return int [] of 0 based integers
	 */
	public static int [] comboToInts(int combo ) {
	   String stringInt = Integer.toString(combo);
	   int [] ints = new int[ stringInt.length() ];
	   for( int i = 0; i < ints.length; i++ ) {
		   ints[ i ] = Integer.parseInt(stringInt.substring(i,i+1)) - 1;
	   }
	   return ints;
	}

	/**
	 * Renders the int[] of digits as a String.
	 * @param digits
	 * @return
	 */
	public static String digitsToString(int [] digits ) {
		StringBuilder sb = new StringBuilder("{");
		for( int i = 0; i < digits.length; i++ ) {
			sb.append( digits[i] );
		}
		sb.append("}");
		return sb.toString();
	}
	/**
	 * Create a String of ints from List<int[]>
	 * More compact than Arrays.toString().
	 * Keep this until the Candidates rule gets rid of encoding */
	public static String digitsToString(List<int[]> digits ) {
		StringBuilder sb = new StringBuilder();
		for( int digi = 0; digi < digits.size(); digi++) {
			if ( 0 < digi ) sb.append(",");
			sb.append( digitsToString( digits.get(digi) ));
		}
		return sb.toString();
	}

	public static String createIndent(int repeats) {
		return "   ".repeat(repeats);
	}
}