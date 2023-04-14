package info.danbecker.ss.tree;

import info.danbecker.ss.RowCol;
import info.danbecker.ss.Utils;

import java.util.List;

/**
 * ColorData - useful for ColorChains where locations
 * have a digit that must be one or the other.
 * Data recorded is digit, rowCol, color.
 * <p>
 * TODO - make immutable or record class
 */
public class ColorData implements Comparable<ColorData>{
	public int digit; // ones-based digit
	public RowCol rowCol;
	public int color; // -1 for no color, 0,1,... for other colors
	
	public ColorData ( int digit, RowCol rowCol, int color ) {
		this.digit = digit;
		this.rowCol = rowCol;
		this.color = color;		
	}

	public Comparable<ColorData> compareRowCol = new Comparable<ColorData>() {
		@Override
		public int compareTo(ColorData that) {
			if (null == that)
				return 1;
			if (null == that.rowCol )
				return 1;

			return rowCol.compareTo(that.rowCol );
		}
	};

	@Override
	public int compareTo(ColorData that) {
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
        if (!(obj instanceof ColorData that)) return false;

        // Cast to same type, using pattern matching
		return 0 == this.compareTo( that );
	}

	@Override
	public String toString() {
		return java.lang.String.format( "Digi=%d, rowCol=%s, color=%d",
			digit, rowCol, color);
	}

	public static String toString(List<TreeNode<ColorData>> list)  {
		if ( null == list )
			return "null";
		StringBuilder sb = new StringBuilder( list.size() + " items");
		if (list.size() > 0 ) sb.append(" ");
		for ( int i = 0; i < list.size(); i++ ) {
			if ( i > 0 ) sb.append(", ");
			TreeNode<ColorData> item = list.get( i );
			sb.append( "" + i + "-" + item.toString());
		}
		return sb.toString();
	}
	public static class RowColMatch implements Comparable<ColorData> {
		RowCol rowCol;

		public RowColMatch( ColorData colorData ) {
			this.rowCol = colorData.rowCol;
		}

		@Override
		public int compareTo(ColorData that) {
			if (null == that) return 1;
			return this.rowCol.compareTo( that.rowCol );
		}
	}
	public static class AnyUnitMatch implements Comparable<ColorData> {
		RowCol rowCol;

		public AnyUnitMatch( ColorData colorData ) {
			this.rowCol = colorData.rowCol;
		}

		@Override
		public int compareTo(ColorData that) {
			if (null == that) return 1;
			if ( this.rowCol.row() == that.rowCol.row()) return 0;
			if ( this.rowCol.col() == that.rowCol.col()) return 0;
			if ( this.rowCol.box() == that.rowCol.box()) return 0;
			return -1;
		}
	}

	public static class UnitMatch implements Comparable<ColorData> {
		Utils.Unit unit;
		RowCol rowCol;

		public UnitMatch(Utils.Unit unit, ColorData colorData ) {
			this.unit = unit;
			this.rowCol = colorData.rowCol;
		}

		@Override
		public int compareTo(ColorData that) {
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