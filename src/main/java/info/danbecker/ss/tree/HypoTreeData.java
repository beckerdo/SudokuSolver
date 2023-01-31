package info.danbecker.ss.tree;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import info.danbecker.ss.Candidates;

/**
 * HypoTreeData represents 
 * a hypothetical set of candidate changes.
 * at a given location and digit.
 */
public class HypoTreeData implements Comparable<HypoTreeData>{

	public int digit; // ones-based digit
	public int [] rowCol;
	public Candidates beforeCandidates;
	public Candidates candidates;
	public List<ChangeData> actions;
	
	public HypoTreeData( int digit, int[] rowCol, Candidates candidates ) {
		this.digit = digit;
		this.rowCol = rowCol;
		this.actions = new LinkedList<ChangeData>();
		beforeCandidates = candidates;
		// Clone given  candidates so  changes can be made.
		this.candidates = new Candidates( candidates );
	}
	
	public Comparable<HypoTreeData> compareRowCol = new Comparable<HypoTreeData>() {
		@Override
		public int compareTo(HypoTreeData that) {
			if (null == that)
				return 1;
			if (null == that.rowCol )
				return 1;

			return Arrays.compare( rowCol, that.rowCol );
		}
	};
	
	@Override
	public int compareTo(HypoTreeData that) {
		//System.out.println( "compareTo");
		if (null == that) return 1;
		if ( this.digit < that.digit) return -1;
		if ( this.digit > that.digit) return 1;
		
		if ( this.rowCol[0] < that.rowCol[0]) return -1;
		if ( this.rowCol[0] > that.rowCol[0]) return 1;
		if ( this.rowCol[1] < that.rowCol[1]) return -1;
		if ( this.rowCol[1] > that.rowCol[1]) return 1;

		// Comparing by size might actually cause similars to be equal
		// Might want to fully compare items.
		if ( this.candidates.candidateCount() < that.candidates.candidateCount()) return -1;
		if ( this.candidates.candidateCount() > that.candidates.candidateCount()) return 1;

		if ( this.actions.size() < that.actions.size()) return -1;
		if ( this.actions.size() > that.actions.size()) return 1;

		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
        // Compare with self   
        if (obj == this) return true; 
  
        // Compare with class type
        if (!(obj instanceof HypoTreeData)) return false; 

        // Cast to same type  
        HypoTreeData that = (HypoTreeData) obj; 
		return 0 == this.compareTo( that );
	}
	
	@Override
	public String toString() {
		return java.lang.String.format( "Digit=%d,rowCol=[%d,%d],cands=%s,actions=%d,afterCands=%s",
			digit, rowCol[0],rowCol[1], 
			beforeCandidates.getCandidatesStringCompact(rowCol[0],rowCol[1]), actions.size(),
			candidates.getCandidatesStringCompact(rowCol[0],rowCol[1]));
	}
}