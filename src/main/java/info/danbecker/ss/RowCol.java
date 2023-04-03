package info.danbecker.ss;

import java.util.Arrays;
import java.util.List;

import static java.lang.String.format;

/**
 * RowCol - encapsulation of row, column, box.
 * Used to refer to locations on Board (not to
 * store digit or candidates).
 * <p>
 * Made a record/immutable so only one instance exists.
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public record RowCol ( int row, int col, int box ) implements Comparable<RowCol>{
	public RowCol ( int row, int col, int box ) {
		if (( row < 0 ) || (row >= Board.ROWS)) throw new ArrayIndexOutOfBoundsException( "row=" + row);
		if (( col < 0 ) || (col >= Board.COLS)) throw new ArrayIndexOutOfBoundsException( "col=" + col);

		this.row = row;
		this.col = col;

		this.box = getBox(row, col);
	}

	public RowCol (int row, int col ) {
		this( row, col, 0 );
	}

	/** Returns box index, given row and col. */
	// 0, 1, 2
	// 3, 4, 5
	// 6, 7, 8
	public static int getBox( int rowi, int coli ) {
		return rowi/3 * 3 + coli / 3;
	}

	@Override
	public int compareTo(RowCol that) {
		//System.out.println( "compareTo");
		if (null == that) return 1;
		if ( this.row < that.row) return -1;
		if ( this.row > that.row) return 1;
		
		if ( this.col < that.col) return -1;
		if ( this.col > that.col) return 1;
		return 0;
	}
	
	@Override
	public String toString() {
		return format( "[%d,%d]", row, col);
	}

	public String toStringWithBox() {
		return format( "[%d,%d,%d]", row, col, box);
	}

	/**
	 * Convert list to array. Shallow copy
	 * @param rowCols
	 * @return
	 */
	public static RowCol[] toArray( List<RowCol> rowCols ) {
		return rowCols.toArray(new RowCol[0]);
	}

	/**
	 * Convert list to array. Shallow copy
	 * @param rowCols
	 * @return
	 */public static List<RowCol> toList( RowCol[] rowCols ) {
		return Arrays.asList( rowCols );
	}

	/**
	 * Returns unit index if the given unit matches in the locations.
	 * Returns NOT_FOUND if there is no unit matchs.
	 *
	 * @param unit
	 * @param loc1
	 * @param loc2
	 * @return
	 */
	public static int unitMatch (Utils.Unit unit, RowCol loc1, RowCol loc2) {
		return switch ( unit ) {
			case ROW: if ( loc1.row() == loc2.row() ) yield loc1.row(); else yield Board.NOT_FOUND;
			case COL: if ( loc1.col() == loc2.col() ) yield loc1.col(); else yield Board.NOT_FOUND;
			case BOX: if ( loc1.box() == loc2.box() ) yield loc1.box(); else yield Board.NOT_FOUND;
			// default: yield Board.NOT_FOUND;
		};
	}

	public static int [] NOT_FOUND = new int[]{ Board.NOT_FOUND, Board.NOT_FOUND };
	/** Returns unit and index if a unit and index matches in the given locations.
	 * Returns NOT_FOUND if there is no unit matchs.
	 * @param loc1
	 * @param loc2
	 * @return
	 */
	public static int [] unitMatch ( RowCol loc1, RowCol loc2 ) {
		for ( Utils.Unit unit : Utils.Unit.values() ) {
			int unitMatch = unitMatch( unit, loc1, loc2 );
			if ( Board.NOT_FOUND != unitMatch ) {
				return new int [] { unit.ordinal(), unitMatch };
			}
		}
		return NOT_FOUND;
	}

	/**
	 * States if ALL the rows match in the rowCols
	 *  For instance row/cols=[[8,1],[8,6]], rowsMatch returns true */
	public static boolean rowsMatch( RowCol[] rowCols ) {
		for( int i = 0; i < rowCols.length - 1; i++) {
			if(rowCols[i].row() != rowCols[i+1].row())
				return false;
		}
		return true;
	}
	/**
	 * States if ALL the rows match in the rowCols
	 *  For instance row/cols={[8,1],[8,6]}, rowsMatch returns false */
	public static boolean rowsMatch( List<RowCol> rowCols ) {
		for( int i = 0; i < rowCols.size() - 1; i++) {
			if(rowCols.get(i).row() != rowCols.get(i+1).row())
				return false;
		}
		return true;
	}

	/** Returns the number of rows counted in the collection.
	 For instance, row/cols=[[8,1],[8,6]], rowsMatch returns 1
	 row/cols=[[7,1],[8,6]], rowsMatch returns 2 */
	public static int rowCount( RowCol[] rowCols ) {
		int count = 0;
		boolean [] counted = new boolean [] { false, false, false, false, false, false, false, false, false };
		for( int i = 0; i < rowCols.length; i++) {
			if( !counted[ rowCols[i].row()]) {
				counted[ rowCols[i].row() ] = true;
				count++;
			}
		}
		return count;
	}

	/**
	 * States if ALL the cols match in the rowCols
	 * For instance row/cols=[[8,1],[8,6]], colsMatch returns false */
	public static boolean colsMatch( RowCol[] rowCols ) {
		for( int i = 0; i < rowCols.length - 1; i++) {
			if (rowCols[i].col()  != rowCols[i+1].col() )
				return false;
		}
		return true;
	}

	/**
	 * States if ALL the rows match in the rowCols
	 * For instance row/cols={[8,1],[8,6]}, colsMatch returns false */
	public static boolean colsMatch( List<RowCol> rowCols ) {
		for( int i = 0; i < rowCols.size() - 1; i++) {
			if (rowCols.get(i).col() != rowCols.get(i+1).col() )
				return false;
		}
		return true;
	}

	/** Returns the number of cols counted in the collection.
	 For instance row/cols=[[0,7],[0,8]], rowsMatch returns 2
	 row/cols=[[0,8],[1,8]], rowsMatch returns 1 */
	public static int colCount( RowCol[] rowCols ) {
		int count = 0;
		boolean [] counted = new boolean [] { false, false, false, false, false, false, false, false, false };
		for( int i = 0; i < rowCols.length; i++) {
			if( !counted[ rowCols[i].col() ]) {
				counted[ rowCols[i].col() ] = true;
				count++;
			}
		}
		return count;
	}

	/**
	 * States whether ALL the boxes match in the collection.
	 * For instance row/cols=[[1,1],[1,8]], getBlock returns false */
	public static boolean boxesMatch( RowCol[] rowCols ) {
		for( int rowcoli = 0; rowcoli < rowCols.length - 1; rowcoli++) {
			if (rowCols[rowcoli].box() != rowCols[rowcoli+1].box()  )
				return false;
		}
		return true;
	}
	/**
	 * States whether ALL the boxes match in the collection.
	 * For instance row/cols=[[1,1],[1,8]], getBlock returns false */
	public static boolean boxesMatch( List<RowCol> rowCols ) {
		for( int rowcoli = 0; rowcoli < rowCols.size() - 1; rowcoli++) {
			if (rowCols.get(rowcoli).box() != rowCols.get(rowcoli+1).box()  )
				return false;
		}
		return true;
	}

	/**
	 * Create a String of row,cols from List<RowCol>
	 * More compact than Arrays.toString() */
	public static String toString( List<RowCol> locations ) {
		StringBuilder sb = new StringBuilder();
		for (int loci = 0; loci < locations.size(); loci++) {
			if (loci > 0) sb.append(",");
			sb.append(locations.get(loci));
		}
		return sb.toString();
	}

	/**
	 * Create a String of row,cols from RowCol []
	 * More compact than Arrays.toString() */
	public static String toString( RowCol[] rowCols ) {
		return toString( toList(rowCols) );
	}
}