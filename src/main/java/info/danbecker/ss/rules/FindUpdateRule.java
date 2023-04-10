package info.danbecker.ss.rules;

import java.util.List;

import info.danbecker.ss.Board;
import info.danbecker.ss.Candidates;

/**
 * These rules find and update the board and candidates via checking
 * board cells of rows/cols/boxes.
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public interface FindUpdateRule {
	/** 
	 * Update board and candidates based on given encoding
	 * from the find method.
	 * The rule should throw an exception if the action violates the solution.
	 * @return number of board cells or candidates updated
	 */
	int update( Board board, Board solution, Candidates candidates, List<int[]> encs );
	
	/**
	 * Returns information on where this rule has a hit.
	 * The int[] encoding is rule specific, and should
	 * be left to the rule to encode and decode.
	 * Callers can convert this to a string for printing or logging.

	 * @return list of encoded positions to be used in update or toString. The encoding is rule dependent.
	 */
	List<int[]> find(Board board, Candidates candidates );

	/**
	 * A utility to convert the rule specific encoding
	 * to a string for printing or logging.
	 * @param enc
	 * @return string of encoding data to be used in printing or logging.
	 */
	String encodingToString( int [] enc );

	/**
	 * Return of the name of this rule.
	 * @return
	 */
	String ruleName();
}
