package info.danbecker.ss.rules;

import java.util.List;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;

/**
 * These rules list and update the board and candidates via checking 
 * board cells of rows/cols/boxes.
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public interface UpdateCandidatesRule {
	/** 
	 * Update candidates based on board and candidates.
	 * The rule should throw an exception if the action violates the solution
	 * @return number of candidates updated
	 */
	public int updateCandidates( Board board, Board solution, Candidates candidates, List<int[]> locations );
	
	/** Returns information on where this rule has a hit.
	 * The int [] is typically int[]{ rowi, coli, boxi, digiti },
	 * but the encoding is rule-dependent and should be left to
	 * the rule to encode/decode/toString.
	 * @return list of encoded positions. The encoding is rule dependent.
	 */
	public List<int[]> locations( Board board, Candidates candidates );
	
	public String ruleName();
}
