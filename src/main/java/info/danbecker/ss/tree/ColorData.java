package info.danbecker.ss.tree;

import info.danbecker.ss.RowCol;

/**
 * ColorData - useful for ColorChains where locations
 * have a digit that must be one or the other.
 * Data recorded is digit, rowCol, color.
 * 
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
}