package info.danbecker.ss.tree;

import info.danbecker.ss.Candidates;
import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;

import java.util.Comparator;
import java.util.List;

/**
 * PairData - useful for RemotePairs
 * <p>
 * TODO - make immutable or record class
 */
public class PairData implements Comparable<PairData>{
    public List<Integer> digits; // ones based
	public RowCol rowCol;
	public int color; // -1 for no color, 0,1,... for other colors

	public PairData(List<Integer> digits, RowCol rowCol, int color ) {
		this.digits = digits;
		this.rowCol = rowCol;
		this.color = color;
	}

	@Override
	public int compareTo(PairData that) {
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
	 * one would like to sort by the PairData natural order
	 */
	public static final Comparator<? super PairData> Comparator =
		(PairData pd1, PairData pd2) -> pd1.compareTo( pd2 );

	public static final Comparator<? super PairData> RowColComparator =
		(PairData pd1, PairData pd2) -> pd1.rowCol.compareTo( pd2.rowCol );

	public static final Comparator<? super PairData> DigitsComparator =
		(PairData pd1, PairData pd2) -> Candidates.compareCandidates( pd1.digits, pd2.digits );

	@Override
	public boolean equals(Object obj) {
        // Compare with self   
        if (obj == this) return true; 
  
        // Compare with class type
        if (!(obj instanceof PairData that)) return false;

        // Cast to same type, using pattern matching
		return 0 == this.compareTo( that );
	}

	@Override
	public String toString() {
		return String.format( "Digits=%s, rowCol=%s, color=%d",
			Utils.digitListToString(digits), rowCol, color);
	}

	public static String toString(List<TreeNode<PairData>> list)  {
		if ( null == list )
			return "null";
		StringBuilder sb = new StringBuilder( list.size() + " items");
		if (list.size() > 0 ) sb.append(" ");
		for ( int i = 0; i < list.size(); i++ ) {
			if ( i > 0 ) sb.append(", ");
			TreeNode<PairData> item = list.get( i );
			sb.append( "" + i + "-" + item.toString());
		}
		return sb.toString();
	}

	public static class RowColMatch implements Comparable<PairData> {
		RowCol rowCol;

		public RowColMatch( PairData colorData ) {
			this.rowCol = colorData.rowCol;
		}

		@Override
		public int compareTo(PairData that) {
			if (null == that) return 1;
			return this.rowCol.compareTo( that.rowCol );
		}
	}

	public static class AnyUnitMatch implements Comparable<PairData> {
		RowCol rowCol;

		public AnyUnitMatch( PairData colorData ) {
			this.rowCol = colorData.rowCol;
		}

		@Override
		public int compareTo(PairData that) {
			if (null == that) return 1;
			if ( this.rowCol.row() == that.rowCol.row()) return 0;
			if ( this.rowCol.col() == that.rowCol.col()) return 0;
			if ( this.rowCol.box() == that.rowCol.box()) return 0;
			return -1;
		}
	}

	public static class UnitMatch implements Comparable<PairData> {
		Utils.Unit unit;
		RowCol rowCol;

		public UnitMatch(Utils.Unit unit, PairData colorData ) {
			this.unit = unit;
			this.rowCol = colorData.rowCol;
		}

		@Override
		public int compareTo(PairData that) {
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