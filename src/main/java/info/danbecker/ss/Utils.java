package info.danbecker.ss;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

		JSONObject jsonObject = new JSONObject(Files.readString( path, StandardCharsets.UTF_8));
		return jsonObject;
	}

	/**
	 * Create a String of ints from List<int[]>
	 * More compact than Arrays.toString().
	 * Keep this until the Candidates rule gets rid of encoding */
	public static String locationsString( List<int[]> locations ) {
		StringBuilder sb = new StringBuilder();
		for( int loci = 0; loci < locations.size(); loci++) {
			if (loci > 0 ) sb.append( ",");
			int [] loc = locations.get(loci);
			sb.append( "[" + loc[0] + "," + loc[1] + "]" );
		}
		return sb.toString();
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
	
	/*
	 * Generate combinations of n elements (9 digits), r at a time.
	 * 
	 * Combinatorics says C(n,r) = n! / r! * (n-r)!
	 * which is written recursively as C(n,r)=C(n-1,r-1) + C(n-1,r)
	 * doubles=12,13,14,...,23,24,....89
	 * triples=112,113,114,...,123,124,...189,
	 *
	 * Note that these combos are 0 based 
	 * 	 
	 * @param n is the set size for example 9 for digits 1 through 9
	 * @param r is the subset size for example take r = 2 from 9
	 * @return int [] of 0 based integer combinations 
	 *    for example n=9,r=1 returns [0],[1],...[8] 
	 */
	public static ArrayList<int[]> comboGenerate(int n, int r) {
	    ArrayList<int[]> combinations = new ArrayList<>();
	    comboHelper(combinations, new int[r], 0, n-1, 0);
	    return combinations;
	}
	
	// Helps with the recursion of method comboGenerate
	private static void comboHelper(ArrayList<int[]> combinations, int data[], int start, int end, int index) {
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
	 * Convert a combo [] of integers to a single int.
	 * <p>
	 * The param int[] is 0-based. The return int is 1-based
	 * to prevent losing leading zeros.
	 * <p>
	 * For example int[]{1,8} becomes integer 29.
	 * For example int[]{0,4,5} becomes integer 156.
	 * 
	 * @param combo int[] of 0 based integers
	 * @return single integer of 1 based digits
	 */
	public static int comboToInt( int[] combo) {
	   if ( combo == null) return -1;
	   int value = 0;
	   for( int i = 0; i < combo.length; i++ ) {
		   value = value * 10 + combo[ i ] + 1;		   
	   }
	   return value;
	}
	
	/* 
	 * Convert an int to an int[] of digits
	 * 
	 * The param int is 1-based. The return int [] is 0-based
	 * to prevent losing leading zeros.
	 * 
	 * For example 29 becomes int[]{1,8}
	 * For example 156 becomes int[]{0,4,5}
	 * 
	 * @param single int of 1 based digits
	 * @return int [] of 0 based integers
	 */
	public static int [] intToCombo( int integer ) {
	   String stringInt = Integer.toString(integer);
	   int [] value = new int[ stringInt.length() ];
	   for( int i = 0; i < value.length; i++ ) {
		   value[ i ] = Integer.parseInt(stringInt.substring(i,i+1)) - 1;	   
	   }
	   return value;
	}

	public static String createIndent(int repeats) {
		return "   ".repeat(repeats);
	}
	
}