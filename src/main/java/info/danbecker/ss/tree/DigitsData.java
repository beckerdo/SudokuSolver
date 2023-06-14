package info.danbecker.ss.tree;

import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * DigitsData
 * Useful for RemotePairs, W-Wing or other rules that need to work with multiple digits.
 * <p>
 * Data recorded is a list of digits, rowCol, and color.
 * <p>
 * TODO - make immutable or record class
 */
public class DigitsData implements Comparable<DigitsData>, Cloneable {
    public List<Integer> digits; // ones based
	public RowCol rowCol;
	public int color; // -1 for no color, 0,1,... for other colors

	public DigitsData(List<Integer> digits, RowCol rowCol, int color ) {
		this.digits = digits;
		this.rowCol = rowCol;
		this.color = color;
	}

	@Override
	public Object clone() {
		return new DigitsData( new LinkedList<>(this.digits), this.rowCol, this.color);
	}

	@Override
	public int compareTo(DigitsData that) {
		//System.out.println( "compareTo");
		if (null == that) return 1;
		// Arrays.equals( this.digits, that.digits )
		int compare = Utils.compareTo( this.digits, that.digits);
		if ( 0 != compare ) return compare;

		compare = RowCol.RowColComparator.compare( this.rowCol, that.rowCol );
		if ( 0 != compare ) return compare;

		return( Integer.compare( this.color, that.color ));
	}

	/**
	 * Comparator is useful for Collections.sort where
	 * one would like to sort by the DigitsData natural order
	 */
	public static final Comparator<? super DigitsData> Comparator =
		(DigitsData pd1, DigitsData pd2) -> pd1.compareTo( pd2 );

	public static final Comparator<? super DigitsData> RowColComparator =
		(DigitsData pd1, DigitsData pd2) -> pd1.rowCol.compareTo( pd2.rowCol );

	public static final Comparator<? super DigitsData> DigitsComparator =
		(DigitsData pd1, DigitsData pd2) -> Candidates.compareCandidates( pd1.digits, pd2.digits );

	@Override
	public boolean equals(Object obj) {
        // Compare with self   
        if (obj == this) return true; 
  
        // Compare with class type
        if (!(obj instanceof DigitsData that)) return false;

        // Cast to same type, using pattern matching
		return 0 == this.compareTo( that );
	}

	@Override
	public String toString() {
		return String.format( "Digits=%s, rowCol=%s, color=%d",
			Utils.digitListToString(digits), rowCol, color);
	}

	public static String toString(List<TreeNode<DigitsData>> list)  {
		if ( null == list )
			return "null";
		StringBuilder sb = new StringBuilder( list.size() + " items");
		if (list.size() > 0 ) sb.append(" ");
		for ( int i = 0; i < list.size(); i++ ) {
			if ( i > 0 ) sb.append(", ");
			TreeNode<DigitsData> item = list.get( i );
			sb.append( "" + i + "-" + item.toString());
		}
		return sb.toString();
	}

	public static class RowColMatch implements Comparable<DigitsData> {
		RowCol rowCol;

		public RowColMatch( DigitsData colorData ) {
			this.rowCol = colorData.rowCol;
		}

		@Override
		public int compareTo(DigitsData that) {
			if (null == that) return 1;
			return this.rowCol.compareTo( that.rowCol );
		}
	}

	public static class AnyUnitMatch implements Comparable<DigitsData> {
		RowCol rowCol;

		public AnyUnitMatch( DigitsData colorData ) {
			this.rowCol = colorData.rowCol;
		}

		@Override
		public int compareTo(DigitsData that) {
			if (null == that) return 1;
			if ( this.rowCol.row() == that.rowCol.row()) return 0;
			if ( this.rowCol.col() == that.rowCol.col()) return 0;
			if ( this.rowCol.box() == that.rowCol.box()) return 0;
			return -1;
		}
	}

	public static class UnitMatch implements Comparable<DigitsData> {
		Utils.Unit unit;
		RowCol rowCol;

		public UnitMatch(Utils.Unit unit, DigitsData colorData ) {
			this.unit = unit;
			this.rowCol = colorData.rowCol;
		}

		@Override
		public int compareTo(DigitsData that) {
			if (null == that) return 1;
			if (Utils.Unit.ROW == unit) {
				if ( this.rowCol.row() == that.rowCol.row()) return 0;
			} else if (Utils.Unit.COL == unit) {
				if ( this.rowCol.col() == that.rowCol.col()) return 0;
			} else if (Utils.Unit.BOX == unit) {
				if ( this.rowCol.box() == that.rowCol.box()) return 0;
			}
			return -1;
		}
	}
}