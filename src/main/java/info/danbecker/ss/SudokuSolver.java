package info.danbecker.ss;

import info.danbecker.ss.rules.*;
import org.apache.commons.cli.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * SudokuSolver
 * <p>
 * Sudoku is a popular number puzzle in which the board is a 9 by 9 array
 * of 81 cells (or blocks).
 * There are 9 rows, 9 columns, and 9 boxes (or squares) of 9 cells.
 * You must put the digits 1 through 9 in each cell so that
 * no row, column, or box has a duplicate digit.
 * <p>
 * Some solvers call the row, column, and box groups "houses", but here
 * they are referred to as "units".
 * <p>
 * This Sudoku solver runs a number of rules which can identify locations
 * of discoveries, and then update the puzzle based on locations.
 * See the Board, Candidate, and rule classes to get more solving info.
 * <p>
 * This is a work in progress and has much refactoring and improvement to do.
 * <p>
 * <pre>
 * Example command line "java SudokuSolver 
 *    -i input file puzzle
 *    -t text string of puzzle
 *    -s solution of puzzle
 * </pre>
 * <p>
 * Puzzles in text contain 81 spaces,containing digits, ( .)(empty space), or (cr,lf,/,-)(end of row)
 * Comments are lines beginning with #   
 * <p>
 * TODOs:
 * Verbose command line option for all the logging printlns, perhaps specify rule, rowCol, digit in command line,
 *    or specify find logging or update logging.
 * Command line option for solving rules or groups: -r "Basic,Subsets,Coloring,Chains"
 * Update digits/rows/cols/boxes to be 0-based everywhere internally and 1-based externally (input parsing and output strings).
 * FindUpdateRule changes
 *    Add solution checking to all rules.
 *    All rule updates should report occupy and candidate location changes to aid in debugging.
 *    Change int update to something that encodes actions occupy/add/remove, digit, and location.
 *       "play digit 5 at [2,3], remove cands {35} at [2,6][8,3]"
 *    Change setOccupied, removeCandidates to support reporting
 * Consider undo/redo (by saving previous Board Candidate state and/or deltas between changes).
 * Refactor APIs to use Units and remove APIs that have Row/Col/Box in name.
 * Regularize Candidate APIs so they are short and consistent.
 * Consider matchers for use with lists of locations. Match by candidate count, combo, etc.
 *    This might cut down on the number of candidate row col box methods.
 *    Consider matchers for chains/coloring. Single digit, group 2,...
 * Move json resources from main to test
 * Replace glut of comparators with Comparator lambdas based on compareTo.
 * Remove unused methods/comparators. Consolidate little used methods to more general use methods.
 * Consider RowCol String and list output used on websites such as r1c5, r345c8, r4c89
 * Implement one of the logic notations (<a href="https://hodoku.sourceforge.net/en/tech_chains.php#in5"></a>) to check candidates, etc.
 *    - Forcing chain notation (= <> =>) r2c7<>4 => r2c7=5 => r2c2<>5 => r2c2=4 => r3c1<>4 => r3c1=5 => r6c1<>5 => r6c1=4
 *    - AIC/Eureka notation https://www.sudopedia.org/wiki/Eureka (= -) (5=1)r3c3-(1=6)r7c3-(6=1)r8c1-(1=5)r8c5
 * Simplify find and remove candidates. find = good, remove = bad
 * <p>
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class SudokuSolver {
	protected static String inputPuzzleText;
	protected static String inputPuzzleSolution;
	protected static String inputPuzzleFile;
	protected static List<String> statedPuzzleRules;

	public static void main(String[] args) throws Exception {
		Logger.getGlobal().info( "SudokuSolver by Dan Becker" );
		System.out.println("SudokuSolver by Dan Becker");

		parseGatherOptions(args);

		if ( null != inputPuzzleFile ) {
			Logger.getGlobal().info( format( "Input puzzle file=%s", inputPuzzleFile ));
			
			// From https://stackoverflow.com/questions/6164448/convert-url-to-normal-windows-filename-java
			// The current recommendation (with JDK 1.7+) is to convert URL → URI → Path/File/String. 
			// So to convert a URL to File, you would say Paths.get(url.toURI()).toFile()
	        // Note that URI strings have leading slash, and file strings are normalized to local file system (/ versus \)
	        System.out.printf( "inp option given=%s\n", inputPuzzleFile );
	        if ( Files.exists( Path.of( inputPuzzleFile ) )) {
	        	inputPuzzleFile = Path.of( inputPuzzleFile ).normalize().toString();
	        } else	{
	        	inputPuzzleFile = Paths.get( ClassLoader.getSystemClassLoader().getResource(inputPuzzleFile).toURI() ).toFile().toString();

	            System.out.printf( "inp option normalized=%s\n", inputPuzzleFile );
	            JSONObject jsonPuzzle = Utils.parseJSON( inputPuzzleFile );
	            System.out.println( "JSON puzzle=" + jsonPuzzle );

	            inputPuzzleText = jsonPuzzle.getJSONArray("states").getString(0);
	            inputPuzzleSolution = jsonPuzzle.optString("solution");
	            JSONArray jsonArray = jsonPuzzle.getJSONArray("rules");
	            if ( null != jsonArray && jsonArray.length() > 0) {
	            	statedPuzzleRules = new ArrayList<>( jsonArray.length());
	            	for(int i = 0; i < jsonArray.length(); i++){
	            	    statedPuzzleRules.add(jsonArray.getString(i));
	            	}		            	
	            }
	        }

		} else if ( null != inputPuzzleText ) {
			Logger.getGlobal().info( format( "Input puzzle text=%s",inputPuzzleText ));
		}
		
		if ( null != inputPuzzleText ) {
			solve( inputPuzzleText, inputPuzzleSolution );
		}
	}
	
	/** Gather command line options for this application. Place info in this class instance variables. */
	public static void parseGatherOptions(String[] args) throws ParseException {
		// Parse the command line arguments
		Options options = new Options();
		// Use dash with shortcut (-h) or -- with name (--help).
        options.addOption("h", "help", false, "print the command line options");
        options.addOption("i", "if", true, "input file for puzzle");
		options.addOption("t", "it", true, "input text puzzle");
		options.addOption("s", "is", true, "input solution for puzzle");

		CommandLineParser cliParser = new DefaultParser();
		CommandLine line = cliParser.parse(options, args);

		// Gather command line arguments for execution
		if (line.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("java -jar sudokusolver.jar <options> info.danbecker.ss.SudokuSolver",
					options);
			System.exit(0);
		}

        // Gather command line arguments for execution
		System.out.println( "Command parse options:");
        if (line.hasOption("i")) {
			inputPuzzleFile = line.getOptionValue("i");
        }
        
        if (line.hasOption("t")) {
			inputPuzzleText = line.getOptionValue("t");
        }
	}
	
	/** A loop to apply rules and enter plays until a fail or
	 * zero more fixes.
	 * @return puzzle solved
	 * @throws java.text.ParseException with malformed or mis-numbered text
	 */
	public static boolean solve( String puzzleText, String puzzleSolution ) throws java.text.ParseException {
		Board board = new Board( puzzleText );
		Board solution = null;
		if ( null != puzzleSolution && 0 < puzzleSolution.length() ) {
			solution = new Board( puzzleSolution );
		}
        System.out.println( "Board string=\n" + board.toSudokuString("-") );		
		Candidates candidates = new Candidates( board );

		// List of rules to run
		// To do, allow command line to list rules or rule groups
		FindUpdateRule[] rules = {
			new LegalCandidates(),
			// Singles
			new SingleCandidates(),
			new SinglePositions(),
			// Intersections
			new CandidateLines(),
			new MultipleLines(),
			// Subsets
			new NakedSubsets(2), // NakedPairs
			new NakedSubsets(3), // NakedTriples
			new DoublePairs(),
			new HiddenSubsets(2), // HiddenPairs
			new HiddenSubsets(3), // HiddenTriples
			// Fish
			new XWings(), // XWing is a 2 set fish and not a wing
			new Swordfish(), // Swordfish is a 3 set fish
			// Single Digit Patterns
			new Skyscraper(),
			new TwoStringKite(),
			// Wings
			new XYWing(),
			new WWing(),
			// Colors
			new SimpleColors(),
			// Chains
			new RemotePairs(),
			new XChain(),
			new XYChain(),
			new BiLocCycleDigitRepeat(),
			new BiLocCycleNonRepeat(),
		};
		Set<String> rulesUsed = new TreeSet<>();
		
		// Number of possibles/updates/timings reported
		int [] possibles = new int[ rules.length ];
		int [] updates = new int[ rules.length ];
		int [] timings = new int[ rules.length ];

		int iterations = 0;
		int rulesRun = 0;
		long cumStartTime = System.currentTimeMillis();
		boolean updated;
		int startingEntries = candidates.getAllOccupiedCount();
		int startingCandidates = candidates.getAllCount();
		do {
			updated = false;
			// Go through each rule.
			for ( int rulei = 0; rulei < rules.length; rulei++ ) {
			   System.out.printf("i%d.%d Board entries=%d, candidates=%d\n", iterations, rulei,
				  candidates.getAllOccupiedCount(), candidates.getAllCount());
			   // System.out.println( "Board=\n" + board );		
               // System.out.println( "Candidates=\n" + candidates.toStringCompact() );
			   FindUpdateRule rule = rules[ rulei ];
			   long startTime = System.nanoTime();
			   List<int[]> encs = rule.find( board, candidates );
			   if ( 0 != rulei ) {
				   // Rule 0 (ValidateLegalCandidates) never reports a location, only updates
				   if ( encs.size() > 0  ) {
					   System.out.printf("Rule %s reports %d possibles%n",
							   rule.ruleName(), encs.size());
					   possibles[ rulei ] += encs.size();
				   }
			   }
		       // System.out.println("Candidates=" + candidates.toString());
			   int changes = 0;
			   try {
				   changes = rule.update(board, solution, candidates, encs);
			   } catch ( IllegalArgumentException e ) {
				   System.out.print( "Rules used: ");
				   for( String used : rulesUsed) {
					   System.out.print( used + ",");
				   }
				   System.out.println();
					throw e;
			   }

			   // Update metrics
			   long endTime = System.nanoTime();
			   long duration = (endTime - startTime) / 1000;  //divide by 1000000 to get milliseconds
			   timings[ rulei ] += (int) duration;
			   
			   if ( changes > 0) {
				   updated = true;
				   updates[ rulei ] += changes;
				   rulesUsed.add( rule.ruleName() );
				   if ( 0 != rulei ) {
					   // Don't count validator as a rule. Don't break loop on validator.
					   rulesRun++;
					   break; // Return to rule 0 before validation to clean things up.					   
				   }
			   }
			   
			   // Do some validation checks.
			   if ( !board.legal())
				   throw new IllegalStateException( "***Warning, rule=" + rule.ruleName() + " illegal board");
			   List<RowCol> emptyLocs = candidates.emptyLocations();
			   if ( 0 < emptyLocs.size()) {
				   System.out.printf("***Warning, rule=%s, %d empty locations at %s\n",
					rule.ruleName(), emptyLocs.size(), RowCol.toString(candidates.emptyLocations()));
				   // No need to iterate through rules.
				   break;
			   }		   
			   if ( 0 == candidates.getAllCount()) {
				   if ( !board.completed() )
					   System.out.println( "***Warning unsolved board, no candidates, rule=" + rule.ruleName());
				   // No need to iterate through rules.
				   break;
			   }
			}
		    iterations++;
		} while (updated);
		
		boolean solved = board.completed();
		String solvedText = solved ? "was" : "was not";
		if ( null != inputPuzzleFile )
			System.out.println( "Sudoku file " + inputPuzzleFile );
		else
			System.out.println( "Sudoku text " + inputPuzzleText );
		System.out.printf( "Solving %s successful after %d rules, %d iterations, %dmS\n",
			solvedText, rulesRun, iterations, (System.currentTimeMillis() - cumStartTime) );
		System.out.printf( "Entry count went from %d to %d. Candidate count went from %d to %d.\n",
			startingEntries, candidates.getAllOccupiedCount(), startingCandidates, candidates.getAllCount());
		System.out.println("Board=" + board.toSudokuString("-"));
		if (!solved) {
			System.out.println("Remaining candidates=\n" + candidates.toStringBoxed());
			List<RowCol> emptyLocs = candidates.emptyLocations();
			if (0 < emptyLocs.size()) {
				System.out.printf("***Warning, %d empty locations at %s\n",
						emptyLocs.size(), RowCol.toString(candidates.emptyLocations()));
			}
		}
		if ( null != statedPuzzleRules ) {
			System.out.println( "Stated rules=" + statedPuzzleRules );
		}
		
		// Print metrics
		System.out.printf( "%-18s, %10s, %10s, %10s", "Rule", "Locations", "Updates", "Time (uS)\n" );
		int [] totals = new int[]{ 0, 0, 0 };
		for ( int rulei = 0; rulei < rules.length; rulei++ ) {
			System.out.printf( "%-21s, %10d, %10d, %10d\n",
				rules[ rulei ].ruleName(), possibles[ rulei ], updates[ rulei ], timings[ rulei ] );
			    totals[ 0 ] += possibles[ rulei ];
				totals[ 1 ] += updates[ rulei ];
			    totals[ 2 ] += timings[ rulei ];
		}
		System.out.printf("%-21s, %10d, %10d, %10d\n", "Total", totals[0], totals[1], totals[2] );
		return solved;
	}

	/** Utility that is helpful for testing. */
	public static int[] runOnce( Board board, Candidates candidates, String solution, FindUpdateRule rule) throws java.text.ParseException {
		int [] results = new int[]{0,0};
        List<int []> locations = rule.find( board, candidates );
		results[ 0 ] = locations.size();
        System.out.printf("Rule %s reports %d possible locations\n", rule.ruleName(), locations.size());
		Board solutionBoard = (null != solution) ? new Board( solution ) : null;
   	    int changes = rule.update( board, solutionBoard, candidates, locations);
		results[ 1 ] = changes;
        System.out.printf("Rule %s made %d changes\n", rule.ruleName(), changes);
		(new LegalCandidates()).update(board, null, candidates, null);
        return results;
	}
}