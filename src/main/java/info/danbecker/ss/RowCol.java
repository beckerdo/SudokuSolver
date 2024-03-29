package info.danbecker.ss;

import static info.danbecker.ss.Board.ROWCOL;
import java.util.*;
import static info.danbecker.ss.Utils.Unit;
import java.util.stream.Collectors;

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

	public RowCol (int row, int col, int box ) {
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
		return RowColComparator.compare(this, that);
	}

	/**
	 * RowColComparator is useful for Collections.sort where
	 * one would like to sort by the RowCol natural order
	 */
	public static final Comparator<? super RowCol> RowColComparator =
		Comparator.nullsLast(Comparator.comparing(RowCol::row).thenComparing(RowCol::col));

	@Override
	public String toString() {
		return format( "[%d,%d]", row, col);
	}

	public String toStringWithBox() {
		return format( "[%d,%d,%d]", row, col, box);
	}

	public static RowCol parse( String rowColStr ) {
		Scanner s = new Scanner(rowColStr).useDelimiter("\\s*[\\[,\\]]\\s*");
		return ROWCOL[s.nextInt()][s.nextInt()];
	}

	/**
	 * Convert list to array. Shallow copy
	 * @param rowCols list of rowCols
	 * @return array of rowCols
	 */
	public static RowCol[] toArray( List<RowCol> rowCols ) {
		return rowCols.toArray(new RowCol[0]);
	}

	/**
	 * Convert list to array. Shallow copy
	 * @param rowCols array of rowCols
	 * @return list of rowCols
	 */
	public static List<RowCol> toList( RowCol[] rowCols ) {
		return Arrays.asList( rowCols );
	}

	/**
	 * Return 0, 1, 2, or 3 completely matching units of the list
	 */
	public static List<Unit> getMatchingAllUnits( List<RowCol> rowCols ){
		List<Unit> matching = new ArrayList<>();
		if ( rowsMatch( rowCols  ))
			matching.add( Unit.ROW );
		if ( colsMatch( rowCols  ))
			matching.add( Unit.COL );
		if ( boxesMatch( rowCols ))
			matching.add( Unit.BOX );
		return matching;
	}

	public int unitIndex( Unit unit ) {
		return switch ( unit ) {
			case ROW -> this.row();
			case COL -> this.col();
			case BOX -> this.box();
		};
	}

	/** Return first matching unit or null for no match
	 *
	 * @param rc1 first rowCol
	 * @param rc2 second rowCol
	 * @return first matching unit or null for no match
	 */
	public static Unit firstUnitMatch(RowCol rc1, RowCol rc2 ){
		if ( rc1.row() == rc2.row() )
			return Unit.ROW;
		if ( rc1.col() == rc2.col() )
			return Unit.COL;
		if ( rc1.box() == rc2.box() )
			return Unit.BOX ;
		return null;
	}

	/**
	 * Return first matching unit or null for no match
	 * @param locs list of RowCol
	 * @return first matching unit or null for no match
	 */
	public static Unit firstUnitMatch(List<RowCol> locs ){
		if ( null == locs || 2 > locs.size())
			return null;
		boolean[] match = new boolean[]{ true, true, true };
		RowCol base = locs.get( 0 );
		for ( int loci = 1; loci < locs.size(); loci++ ) {
			RowCol loc = locs.get( loci );
			if ( base.row() != loc.row( )) match[ Unit.ROW.ordinal() ] = false;
			if ( base.col() != loc.col( )) match[ Unit.COL.ordinal() ] = false;
			if ( base.box() != loc.box( )) match[ Unit.BOX.ordinal() ] = false;
		}
		for ( Unit unit : Unit.values() ) {
			if ( match[ unit.ordinal() ] ) return unit;
		}
		return null;
	}

	/** Return first matching unit index or null for no match
	 *
	 * @param rc1 first rowCol
	 * @param rc2 second rowCol
	 * @return first matching unit index or -1 for no match
	 */
	public static int firstUnitMatchIndex(RowCol rc1, RowCol rc2 ){
		if ( rc1.row() == rc2.row() )
			return rc1.row();
		if ( rc1.col() == rc2.col() )
			return rc1.col();
		if ( rc1.box() == rc2.box() )
			return rc1.box();
		return -1;
	}

	/**
	 * Return 0, 1, 2, or 3 matching units of the two locations.
	 * If rc1.equals(rc2), the list will be length 3.
	 * @param rc1 first rowCol
	 * @param rc2 second rowCol
	 * @return list of units that match these two locations
	 */
	public static List<Unit> getMatchingUnits(RowCol rc1, RowCol rc2 ){
		List<Unit> matching = new LinkedList<>();
		if ( rc1.row() == rc2.row() )
			matching.add( Unit.ROW );
		if ( rc1.col() == rc2.col() )
			matching.add( Unit.COL );
		if ( rc1.box() == rc2.box() )
			matching.add( Unit.BOX );
		return matching;
	}

	/**
	 * Returns unit index if the given unit matches in the locations.
	 * Returns NOT_FOUND if there is no unit matchs.
	 * @param unit one of the houses ROW, COL, BOX
	 * @param loc1 location 1
	 * @param loc2 location 2
	 * @return index of unit match or NOT_FOUND
	 */
	public static int unitMatch(Unit unit, RowCol loc1, RowCol loc2) {
		int notFound = Board.NOT_FOUND;
		return switch ( unit ) {
			case ROW -> loc1.row() == loc2.row() ? loc1.row() : notFound;
			case COL -> loc1.col() == loc2.col() ? loc1.col() : notFound;
			case BOX -> loc1.box() == loc2.box() ? loc1.box() : notFound;
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
		for ( Unit unit : Unit.values() ) {
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
	 * Add non-duplicate RowCol to the list
	 *
	 * @param list
	 * @param rowCol
	 * @return number of new potentials added.
	 */
	public static int addUnique(List<RowCol> list, RowCol rowCol) {
		if (!list.contains(rowCol)) {
			list.add(rowCol);
			return 1;
		}
		return 0;
	}

	/** Add non-duplicate RowCols from potentials to the bigList
	 *
	 * @param bigList
	 * @param potentials
	 * @return number of new potentials added.
	 */
	public static int addUniques( List<RowCol> bigList, List<RowCol> potentials ) {
		int added = 0;
		for ( RowCol rowCol : potentials ) {
			if (!bigList.contains( rowCol )) {
				bigList.add( rowCol );
				added++;
			}
		}
		return added;
	}

	/**
	 * Create a String of row,cols from List<RowCol>
	 * More compact than Arrays.toString() */
	public static String toString( List<RowCol> locations ) {
		return locations.stream().map(Object::toString).collect(Collectors.joining(","));
	}

	/**
	 * Create a String of rowCols from the List<List<RowCol>>
	 * with the provided delimiter
	 * @param lists
	 * @param delimiter
	 * @return
	 */
	public static String toString(List<List<RowCol>> lists, String delimiter) {
		// return lists.stream().map(l->RowCol.toString(l)).collect(Collectors.joining(delimiter));
		return lists.stream().map(RowCol::toString).collect(Collectors.joining(delimiter));
	}

	/**
	 * Create a String of row,cols from RowCol []
	 * More compact than Arrays.toString() */
	public static String toString( RowCol[] rowCols ) {
		return toString( toList(rowCols) );
	}

	/**
     * Returns first index of list in lists List or NOT_FOUND.
     * Note that order of the lists matters!
     * @param lists a list of RowCol lists
     * @param list a list of RowCols
     * @return index of list or NOT_FOUND
     */
	public static int indexOf(List<List<RowCol>> lists, List<RowCol> list) {
		for( int listi = 0; listi < lists.size(); listi++ ) {
			List<RowCol> test = lists.get( listi );
			if ( test.equals( list )) {
				return listi;
			}
		}
		return Board.NOT_FOUND;
	}

	/** Returns the index of the first list that contains the location
	 *
	 * @param lists
	 * @param loc
	 * @return index of the first list that contains the location or NOT_FOUND
	 */
	public static int indexOf( List<List<RowCol>> lists, RowCol loc) {
		for (int linki = 0; linki < lists.size(); linki++) {
			List<RowCol> list = lists.get(linki);
			for (int listi = 0; listi < list.size(); listi++) {
				if (loc.equals(list.get(listi))) {
					return linki;
				}
			}
		}
		return Board.NOT_FOUND;
	}

	public static class AnyUnitMatch implements Comparable<RowCol> {
		RowCol rowCol;

		public AnyUnitMatch( RowCol rowCol ) {
			this.rowCol = rowCol;
		}

		@Override
		public int compareTo(RowCol that) {
			if (null == that) return 1;
			if (this.rowCol == that) return 0;
			if ( this.rowCol.row() == that.row()) return 0;
			if ( this.rowCol.col() == that.col()) return 0;
			if ( this.rowCol.box() == that.box()) return 0;
			return -1;
		}
	}

	public static class UnitMatch implements Comparable<RowCol> {
		Unit unit;
		RowCol rowCol;

		public UnitMatch(Unit unit, RowCol rowCol ) {
			this.unit = unit;
			this.rowCol = rowCol;
		}

		@Override
		public int compareTo(RowCol that) {
			if (null == that) return 1;
			if (this.rowCol == that) return 0;
			if (Unit.ROW == unit) {
				if ( this.rowCol.row() == that.row()) return 0;
			} else if (Unit.COL == unit) {
				if ( this.rowCol.col() == that.col()) return 0;
			} else if (Unit.BOX == unit) {
				if ( this.rowCol.box() == that.box()) return 0;
			}
			return -1;
		}
	}

	/**
	 * Return a list of rowCols from the list RowCols in the same unit as this.
	 * @param rowCols the starting pool of rowCols
	 * @return the subset of rowCols (not including this) that rowCol can see
	 */
	public List<RowCol> sameUnit( List<RowCol> rowCols ) {
		List<RowCol> canSee = rowCols.stream()
				.filter( item -> new AnyUnitMatch( item ).compareTo( this ) == 0 )
				.collect(Collectors.toList());
		canSee.remove( this );
		return canSee;
	}
}