package info.danbecker.ss;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
	public static short CELLS = (short) (ROWS * COLS);

	/** Enum for different divisions of a Sudoku Board */
	public static enum Unit { ROW, COL, BOX };
	
	/** Given a file path, parse file as JSON and return JSONObject */
	public static JSONObject parseJSON( String path ) throws IOException {
    	if (null == path || 0 == path.length() ) 
			throw new IllegalArgumentException( "input file path must not be null or empty");
		if (!Files.exists( Path.of( path ) )) 
			throw new IllegalArgumentException( "input file path must exist, path=" + path );
		if (!Files.isReadable( Path.of( path ) )) 
			throw new IllegalArgumentException( "input file path must be readable, path=" + path);

		JSONObject jsonObject = new JSONObject(Files.readString(Path.of( path ), StandardCharsets.UTF_8));
		return jsonObject;
	}

	
	// Convert from int[][] of rowCols to List<int[]> 
	public static List<int []> convertArrayToList( int[][] rowCols ) {
		List<int[]> list = Arrays.stream(rowCols).collect(Collectors.toList());
		return list;
	}
	
	// Convert from List<int[]> of rowCols to int[][]
	// List<T[]> list.toArray( new int[0] ) only works for reference types, not primitives
	// Universal Generics in JDK 20 might get rid of this impl. https://openjdk.org/jeps/8261529
	public static int [][] convertListToArray( List<int[]> rowCols ) {
		int[][] array = new int[rowCols.size()][];
		for(int i = 0; i < rowCols.size(); i++) array[i] = rowCols.get(i);
		return array;
	}

	public static String locationString( int row, int col ) {
		return "[" + row + "," + col + "]";
	}
	
	public static String locationString( int [] location ) {
		if (null == location) return "[null]";
		return "[" + location[0] + "," + location[1] + "]";
	}
	
	/** 
	 * Create a String of row,cols from List<int[]>
	 * More compact than Arrays.toString() */
	public static String locationsString( List<int []> locations ) {
		StringBuilder sb = new StringBuilder();
		for( int loci = 0; loci < locations.size(); loci++) {
			if (loci > 0 ) sb.append( ",");
			sb.append( locationString( locations.get(loci) ));
		}
		return sb.toString();		
	}

	/** Create a String of row,cols from int[][] 
	 * More info and compact than int[][] */
	public static String locationsString( int[][] locations ) {
		StringBuilder sb = new StringBuilder();
		for( int loci = 0; loci < locations.length; loci++) {
			if (loci > 0 ) sb.append( ",");
			sb.append( locationString( locations[loci] ));
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
	 * 
	 * The param int[] is 0-based. The return int is 1-based
	 * to prevent losing leading zeros.
	 * 
	 * For example int[]{1,8} becomes integer 29.
	 * For example int[]{0,4,5} becomes integer 156.
	 * 
	 * @param int[] of 0 based integers
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
	 * Convert an int to a int[] of digits
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
	
	/** Returns unit index if the given unit matches in the locations. 
	 * Returns NOT_FOUND if there is no unit matchs.
	 * @param unit
	 * @param loc1
	 * @param loc2
	 * @return
	 */
	public static int [] NOT_FOUND = new int[]{ Board.NOT_FOUND, Board.NOT_FOUND };
	public static int [] unitMatch ( int [] loc1, int [] loc2 ) {
		for ( Unit unit : Utils.Unit.values() ) {
			int unitMatch = unitMatch( unit, loc1, loc2 );
			if ( Board.NOT_FOUND != unitMatch ) {
				return new int [] { unit.ordinal(), unitMatch };
			}
		}
		return NOT_FOUND;
	}

	/** Returns unit index if the given unit matches in the locations. 
	 * Returns NOT_FOUND if there is no unit matchs.
	 * @param unit
	 * @param loc1
	 * @param loc2
	 * @return
	 */
	public static int unitMatch ( Unit unit, int [] loc1, int [] loc2) {
		return switch ( unit ) {
		case ROW: if ( loc1[ 0 ] == loc2[ 0 ] ) yield loc1[ 0 ]; else yield Board.NOT_FOUND;
		case COL: if ( loc1[ 1 ] == loc2[ 1 ] ) yield loc1[ 1 ]; else yield Board.NOT_FOUND;
		case BOX: if ( Board.getBox(loc1) == Board.getBox(loc2) ) yield Board.getBox(loc1); else yield Board.NOT_FOUND;
		default: yield Board.NOT_FOUND;
		};
	}
	
	/** Analyzes rows of an array of rowCols, returns count of rows 
        For instance row/cols=[[8,1],[8,6]], rowsMatch returns 1
	                 row/cols=[[7,1],[8,6]], rowsMatch returns 2 */
	public static int rowCount( int[][] rowCols ) {
		int count = 0;
		boolean [] counted = new boolean [] { false, false, false, false, false, false, false, false, false };
		for( int i = 0; i < rowCols.length; i++) {
			if( !counted[ rowCols[i][0] ]) {
				counted[ rowCols[i][0] ] = true;
				count++;
			}
		}
		return count;
	}

	/** Analyzes rows of an array of rowCols 
	 *  For instance row/cols=[[8,1],[8,6]], rowsMatch returns true */
	public static boolean rowsMatch( int[][] rowCols ) {
		for( int i = 0; i < rowCols.length - 1; i++) {
			if(rowCols[i][0] != rowCols[i+1][0])
			   return false;
		}
		return true;
	}
	/** Analyzes rows of a List of rowCols 
	 *  For instance row/cols={[8,1],[8,6]}, rowsMatch returns true */
	public static boolean rowsMatch( List<int[]> rowCols ) {
		for( int i = 0; i < rowCols.size() - 1; i++) {
			if(rowCols.get(i)[0] != rowCols.get(i+1)[0])
			   return false;
		}
		return true;
	}

	/** Analyzes rows of an array of rowCols, returns count of cols 
    For instance row/cols=[[8,0],[7,0]], rowsMatch returns 1
                 row/cols=[[8,1],[7,0]], rowsMatch returns 2 */
	public static int colCount( int[][] rowCols ) {
		int count = 0;
		boolean [] counted = new boolean [] { false, false, false, false, false, false, false, false, false };
		for( int i = 0; i < rowCols.length; i++) {
			if( !counted[ rowCols[i][1] ]) {
				counted[ rowCols[i][1] ] = true;
				count++;
			}
		}
		return count;
	}

	/** Analyzes cols of an array of rowCols 
	 *  For instance row/cols=[[8,1],[8,6]], colsMatch returns false */
	public static boolean colsMatch( int[][] rowCols ) {
		for( int i = 0; i < rowCols.length - 1; i++) {
			if (rowCols[i][1] != rowCols[i+1][1] )
				return false;
		}
		return true;
	}
	/** Analyzes cols of a List  of rowCols 
	 *  For instance row/cols={[8,1],[8,6]}, colsMatch returns false */
	public static boolean colsMatch( List<int[]> rowCols ) {
		for( int i = 0; i < rowCols.size() - 1; i++) {
			if (rowCols.get(i)[1] != rowCols.get(i+1)[1] )
				return false;
		}
		return true;
	}

	/** Analyzes block numbers of an array of rowCols 
	 *  For instance row/cols=[[1,1],[1,8]], getBlock returns false */
	public static boolean boxesMatch( int[][] rowCols ) {
		for( int rowcoli = 0; rowcoli < rowCols.length - 1; rowcoli++) {
			if (Board.getBox(rowCols[rowcoli][0],rowCols[rowcoli][1]) != 
				Board.getBox(rowCols[rowcoli+1][0],rowCols[rowcoli+1][1]) )
				return false;
		}
		return true;
	}
	/** Analyzes block numbers of an array of rowCols 
	 *  For instance row/cols=[[1,1],[1,8]], getBlock returns false */
	public static boolean boxesMatch( List<int[]> rowCols ) {
		for( int rowcoli = 0; rowcoli < rowCols.size() - 1; rowcoli++) {
			int [] rowCol0 = rowCols.get(rowcoli);
			int [] rowCol1 = rowCols.get(rowcoli+1);
			if (Board.getBox(rowCol0[0],rowCol0[1]) != 
				Board.getBox(rowCol1[0],rowCol1[1]) )
				return false;
		}
		return true;
	}
	
	public static String createIndent(int repeats) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < repeats; i++) {
			sb.append("   ");
		}
		return sb.toString();
	}
	
	/** Returns 0-based position of test in locs. 
	 * Returns Board.NOT_FOUND if not found.
	 * @param locs the array to search 
	 * @param test the item to test
	 * @return position or NOT_FOUND
	 */
	public static int indexOf(List<int[]> locs, int[] test ) {
		for ( int index = 0; index < locs.size(); index++ ) {
			if (Arrays.equals(locs.get(index), test))
				return index;			
		}
		return Board.NOT_FOUND;
	}

	/**  
	 * Test 2 rowCols for equality.
	 * @return position or NOT_FOUND
	 */
	public static boolean locEquals( int[] loc1, int[] loc2 ) {
		if ( null == loc1 && null == loc2 ) return true;
		if ( null == loc1 || null == loc2) return false;
		if ( loc1.length != loc2.length) return false;
		for ( int i = 0; i < loc1.length; i++) {
			if ( loc1[i] != loc2[i]) return false;
		}
		return true;
	}
	
}