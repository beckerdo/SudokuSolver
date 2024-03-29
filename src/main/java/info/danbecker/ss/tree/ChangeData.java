package info.danbecker.ss.tree;

import java.util.Arrays;

import info.danbecker.ss.Candidates.Action;
import info.danbecker.ss.RowCol;

/**
 * ChangeData represents an action (OCCUPY,ADD,REMOVE)
 * at a given location and digit.
 * Occupy applies to Boards and Candidates.
 * Add and remove applies to Candidates.
 * 
 * A list of ChangeData, should be able to map one
 * Board or Candidates to another.
 * 
 * TODO - make immutable or record class
 */
public class ChangeData implements Comparable<ChangeData>{

	public int digit; // ones-based digit
	public RowCol rowCol;
	public Action action;
	int updateCount;
	
	public ChangeData( int digit, RowCol rowCol, Action action, int updateCount ) {
		this.digit = digit;
		this.rowCol = rowCol;
		this.action = action;
		this.updateCount = updateCount;
	}
	
	public Comparable<ChangeData> compareRowCol = new Comparable<ChangeData>() {
		@Override
		public int compareTo(ChangeData that) {
			if (null == that)
				return 1;
			if (null == that.rowCol )
				return 1;

			return rowCol.compareTo(that.rowCol);
		}
	};
	
	@Override
	public int compareTo(ChangeData that) {
		//System.out.println( "compareTo");
		if (null == that) return 1;
		if ( this.digit < that.digit) return -1;
		if ( this.digit > that.digit) return 1;
		
		if ( this.rowCol.row() < that.rowCol.row()) return -1;
		if ( this.rowCol.row() > that.rowCol.col()) return 1;
		if ( this.rowCol.col() < that.rowCol.col()) return -1;
		if ( this.rowCol.col() > that.rowCol.col()) return 1;

		if ( this.action.ordinal() < that.action.ordinal()) return -1;
		if ( this.action.ordinal() > that.action.ordinal()) return 1;

		return this.updateCount - that.updateCount;
	}
	
	@Override
	public boolean equals(Object obj) {
        // Compare with self   
        if (obj == this) return true; 
  
        // Compare with class type
        if (!(obj instanceof ChangeData)) return false; 

        // Cast to same type  
        ChangeData that = (ChangeData) obj; 
		return 0 == this.compareTo( that );
	}
	
	@Override
	public String toString() {
		return java.lang.String.format( "Action=%s,digit=%d,rowCol=[%d,%d], updates=%d",
			action.name(),digit, rowCol.row(),rowCol.col(), updateCount);
	}
}