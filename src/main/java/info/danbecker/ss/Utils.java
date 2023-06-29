package info.danbecker.ss;

import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

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
	public static short UNITS= 9;

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
	public static int zerobasedIntsToOnebasedCombo(int[] ints) {
		if ( ints == null) return -1;
		int value = 0;
		for( int i = 0; i < ints.length; i++ ) {
			value = value * 10 + ints[ i ] + 1;
		}
		return value;
	}

	/**
	 * Zero based to one based ints, in place.
	 * Updates each location in zbInts by one, in place
	 * @param zbInts collection of zero based ints
	 * @return the same location of updated ints
	 */
	public static int[] zbToobIntsInPlace(int[] zbInts) {
		for ( int i = 0; i< zbInts.length; i++ ) {
			zbInts[i]++;
		}
		return zbInts;
	}

	/**
	 * Zero based to one based ints, copy.
	 * Updates each location in zbInts by one, in a new collection.
	 * @param zbInts collection of zero based ints
	 * @return a new collection of updated ints
	 */
	public static int[] zbToobIntsCopy(int[] zbInts) {
		int[] obInts = Arrays.copyOf(zbInts,zbInts.length);
		return zbToobIntsInPlace( obInts );
	}

	/**
	 * One based to zero based ints, in place.
	 * Updates each location in obInts by one, in place
	 * @param obInts collection of one based ints
	 * @return the same location of updated ints
	 */
	public static int[] obTozbIntsInPlace(int[] obInts) {
		for ( int i = 0; i< obInts.length; i++ ) {
			obInts[i]--;
		}
		return obInts;
	}

	/**
	 * One based to zero based ints, copy.
	 * Updates each location in obInts by one, in a new collection.
	 * @param obInts collection of one based ints
	 * @return a new collection of updated ints
	 */
	public static int[] obTozbIntsCopy(int[] obInts) {
		int[] zbInts = Arrays.copyOf(obInts,obInts.length);
		return obTozbIntsInPlace( zbInts );
	}

	public static int[] onebasedIntsToZerobasedInts(int[] zbInts) {
		int[] obInts = Arrays.copyOf(zbInts,zbInts.length);
		for ( int i = 0; i< obInts.length; i++ ) {
			obInts[i]++;
		}
		return obInts;
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
	public static int [] onebasedComboToZeroBasedInts(int combo ) {
	   String stringInt = Integer.toString(combo);
	   int [] ints = new int[ stringInt.length() ];
	   for( int i = 0; i < ints.length; i++ ) {
		   ints[ i ] = Integer.parseInt(stringInt.substring(i,i+1)) - 1;
	   }
	   return ints;
	}

	/**
	 * Renders the int[] of digits as a String.
	 * @param digits array of ints
	 * @return string representing digits in array
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
	 * Renders the List<Integer> of digits as a String.
	 * @param digits list of ints
	 * @return string representing digits
	 */
	public static String digitListToString(List<Integer> digits ) {
		String digitsStr = digits
				.stream()
				.map( Object::toString )
				.collect(Collectors.joining(""));
		return "{" + digitsStr + "}";
	}

	/**
	 * Turns a map of digits->locations into a readable string
	 * @param digitMap
	 * @return
	 */
	public static String digitMapToString(Map<Integer,List<RowCol>> digitMap ) {
		String updateString = digitMap
				.entrySet()
				.stream()
				.map( e -> format( "digit %d:%s", e.getKey(), RowCol.toString(e.getValue())) )
				.collect(Collectors.joining(","));
		return updateString;
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

	public static String indent(int repeats) {
		return "   ".repeat(repeats);
	}

	/**
	 * Converts a list of one-based digits to an int[] of zero-based combos.
	 * @param digits
	 * @return
	 */
	public static int[] digitsToCombo( List<Integer> digits ) {
		int[] combo = new int[ digits.size()];
		for ( int i = 0; i < digits.size(); i++) {
			combo[i] = digits.get(i) - 1;
		}
		return combo;
	}
	/**
	 * Converts an int[] of zero-based combos to a list of one-based digits.
	 * @param combo
	 * @return
	 */
	public static List<Integer> comboToDigits( int[] combo ) {
		List<Integer> digits = new ArrayList<>();
		for ( int i = 0; i < combo.length; i++) {
			digits.add( combo[i] + 1 );
		}
		return digits;
	}

	public static boolean deepEquals( int[] i1, int[] i2 ) {
		if (i1 == i2) return true;
		if ((null == i1) || (null == i2)) return false;
		if (i1.length != i2.length) return false;
		for ( int i = 0; i < i1.length; i++) {
			if (i1[i] != i2[i]) return false;
		}
		return true;
	}

	public static int findFirst( List<int[]> list, int[] item ) {
		for ( int listi = 0; listi < list.size(); listi++ ) {
			int[] test = list.get(listi);
			if (Utils.deepEquals( test, item)) {
				return listi;
			}
		}
        return Board.NOT_FOUND;
	}

	/**
	 * Converts a list of Integer to int[] with no ones/zeros conversion.
	 */
	public static int[] listToArray( List<Integer> digits ) {
		int[] ints = new int[digits.size()];
		for ( int i = 0; i < digits.size(); i++)
			ints[i] = digits.get(i);
		return ints;
	}

	/**
	 * Converts a int[] to List<Integer> with no ones/zeros conversion.
	 */
	public static List<Integer> arrayToList( int[] ints ) {
		List<Integer> list = Arrays.stream(ints).boxed().toList();
		return list;
	}

	/** Add non-duplicate encodings from potentials to the bigList
	 *
	 * @param bigList
	 * @param potentials
	 * @return number of new potentials added.
	 */
	public static int addUniques( List<int[]> bigList, List<int[]> potentials ) {
		int added = 0;
		for (int loci = 0; loci < potentials.size(); loci++) {
			int [] enc = potentials.get(loci);
			added += addUnique( bigList, enc );
		}
		return added;
	}
	public static int addUnique( List<int[]> bigList, int[] enc ) {
		int added = 0;
		// if (!bigList.contains( enc )) { // bad, equals of two arrays (addresses)
		if (!bigList.stream().anyMatch(a -> Arrays.equals(a, enc))) { // good, tests all array members
			bigList.add(enc);
			added++;
		}
		return added;
	}

	public static int compareTo( List<Integer> list1, List<Integer> list2) {
		if (null == list1 && null == list2) return 0;
		if (null == list1) return -1;
		if (null == list2) return 1;

		if (list1.size() < list2.size()) return -1;
		if (list1.size() > list2.size()) return 1;

		int[] sorted1 = listToArray(list1);
		Arrays.sort( sorted1 );
		int[] sorted2 = listToArray(list2);
		Arrays.sort( sorted2 );
		for ( int i = 0; i < sorted1.length; i++ ) {
			if (sorted1[i] < sorted2[i] ) return -1;
			if (sorted1[i] > sorted2[i] ) return 1;
		}
		return 0;
	}
}