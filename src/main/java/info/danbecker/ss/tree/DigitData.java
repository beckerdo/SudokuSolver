package info.danbecker.ss.tree;

import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;

import java.util.List;

/**
 * DigitData
 * Useful for chains involving a single digit.
 * <p>
 * Data recorded is digit, rowCol, color.
 * <p>
 * TODO - make immutable or record class
 */
public class DigitData implements Comparable<DigitData>, Cloneable {
	public int digit; // ones-based digit
	public RowCol rowCol;
	public int color; // -1 for no color, 0,1,... for other colors
	
	public DigitData(int digit, RowCol rowCol, int color ) {
		this.digit = digit;
		this.rowCol = rowCol;
		this.color = color;		
	}

	@Override
	public Object clone() {
		try {
			return (DigitData) super.clone();
		} catch (CloneNotSupportedException e) {
			return new DigitData(this.digit, this.rowCol, this.color);
		}
	}

	public Comparable<DigitData> compareRowCol = new Comparable<DigitData>() {
		@Override
		public int compareTo(DigitData that) {
			if (null == that)
				return 1;
			if (null == that.rowCol )
				return 1;

			return rowCol.compareTo(that.rowCol );
		}
	};

	@Override
	public int compareTo(DigitData that) {
		//System.out.println( "compareTo");
		if (null == that) return 1;
		if ( this.digit < that.digit) return -1;
		if ( this.digit > that.digit) return 1;
		
		if ( this.rowCol.row() < that.rowCol.row()) return -1;
		if ( this.rowCol.row() > that.rowCol.row()) return 1;
		if ( this.rowCol.col() < that.rowCol.col()) return -1;
		if ( this.rowCol.col() > that.rowCol.col()) return 1;

		if ( this.color < that.color) return -1;
		if ( this.color > that.color) return 1;
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
        // Compare with self   
        if (obj == this) return true; 
  
        // Compare with class type
        if (!(obj instanceof DigitData that)) return false;

        // Cast to same type, using pattern matching
		return 0 == this.compareTo( that );
	}

	@Override
	public String toString() {
		return java.lang.String.format( "Digi=%d, rowCol=%s, color=%d",
			digit, rowCol, color);
	}

	public static String toString(List<TreeNode<DigitData>> list)  {
		if ( null == list )
			return "null";
		StringBuilder sb = new StringBuilder( list.size() + " items");
		if (list.size() > 0 ) sb.append(" ");
		for ( int i = 0; i < list.size(); i++ ) {
			if ( i > 0 ) sb.append(", ");
			TreeNode<DigitData> item = list.get( i );
			sb.append( "" + i + "-" + item.toString());
		}
		return sb.toString();
	}

	public static class RowColMatch implements Comparable<DigitData> {
		RowCol rowCol;

		public RowColMatch( DigitData digitData) {
			this.rowCol = digitData.rowCol;
		}

		@Override
		public int compareTo(DigitData that) {
			if (null == that) return 1;
			return this.rowCol.compareTo( that.rowCol );
		}
	}

	public static class AnyUnitMatch implements Comparable<DigitData> {
		RowCol rowCol;

		public AnyUnitMatch( DigitData digitData) {
			this.rowCol = digitData.rowCol;
		}

		@Override
		public int compareTo(DigitData that) {
			if (null == that) return 1;
			if ( this.rowCol.row() == that.rowCol.row()) return 0;
			if ( this.rowCol.col() == that.rowCol.col()) return 0;
			if ( this.rowCol.box() == that.rowCol.box()) return 0;
			return -1;
		}
	}

	public static class UnitMatch implements Comparable<DigitData> {
		Utils.Unit unit;
		RowCol rowCol;

		public UnitMatch(Utils.Unit unit, DigitData digitData) {
			this.unit = unit;
			this.rowCol = digitData.rowCol;
		}

		@Override
		public int compareTo(DigitData that) {
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