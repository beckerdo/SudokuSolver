package info.danbecker.ss;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.json.JSONArray;

import info.danbecker.ss.rules.CandidateLines;
import info.danbecker.ss.rules.DoublePairs;
import info.danbecker.ss.rules.NakedSubsets;
import info.danbecker.ss.rules.HiddenSubsets;
import info.danbecker.ss.rules.ForcingChains;
import info.danbecker.ss.rules.SingleCandidates;
import info.danbecker.ss.rules.SinglePositions;
import info.danbecker.ss.rules.Swordfish;
import info.danbecker.ss.rules.UpdateCandidatesRule;
import info.danbecker.ss.rules.XWings;
import info.danbecker.ss.rules.LegalCandidates;
import info.danbecker.ss.rules.MultipleLines;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import static java.lang.String.format;

/**
 * SudokuSolver
 * <p>
 * Sudoku is a popular number puzzle in which the board is a 9 by 9 square
 * of 81 cells (or blocks). There are 9 rows, 9 columns, and 9 boxes of
 * 3 by 3 cells. You must put the digits 1 through 9 in each cell so that
 * no row, column, or box has a duplicate digit.
 * <p>
 * This Sudoku solver runs a number of rules which can identify locations
 * of discoveries, and then update the puzzle based on locations.
 * See the Board, Candidate, and rule classes to get more solving info.
 * <p>
 * <pre>
 * Example command line "java SudokuSolver 
 *    -t text of puzzle 
 *    -i input file puzzle
 * </pre>
 * <p>
 * Puzzles in text contain 81 spaces,containing digits, ( .)(empty space), or (cr,lf,/,-)(end of row)
 * Comments are lines beginning with #   
 * 
 * This is a work in progress and has much refactoring and improvement to do.
 * 
 * TODOs:
 * Verbose command line option for all the printlns, perhaps specify rule, rowCol, digit in command line.
 * For row,cols have a defined object based on record class/immutable
 *    RowCol should contain Box, String, and other location APIs
 * 	  Remove int[] from APIs to simplify, Remove List<rowCol> and rowCol[] duality and replication.
 *    Perhaps use pair as rowcol as shown at https://stackoverflow.com/questions/521171/a-java-collection-of-value-pairs-tuples
 *    Use singleton pattern to have only one RowCol instantiation.
 *    Consider String and list output used on websites such as r1c5, r345c8, r4c89 
 * Update digits to be 0-based everywhere except input parsing and output strings
 * Refactor APIs to use Units and remove APIs that have Row/Col/Box in name.
 * Consider matchers for use with lists of locations. Match by candidate count, combo, etc.
 *    This might cut down on the number of candidate row col box methods.
 * Replace location in rules to encoded/decoded/string objects
 * Move json resources from main to test
 * All rule updates should report occupy and candidate location changes to aid in debugging.
 *    Change int updateCandidates to something that encodes actions occupy/add/remove, digit, and location.
 * For puzzles with known solutions (all of them), validate every location/update action (OCCUPY,REMOVE)
 *    Should rule perform validation, or the solver
 *    BUG LegalCandidates empties at [6,5] with 20230103-diabolical-24250.json. Possibly ForcingChains rule.
 *    BUG ColorChains 20221118-diabolical-17500.json removed digit 3 from [8,5] {-8}, remaining candidates {-8}
 * Check into GitHub
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class SudokuSolver {
	protected static String inputPuzzleText;
	protected static String inputPuzzleSolution;
	protected static String inputPuzzleFile;
	protected static List<String> statedPuzzleRules;

	/**
	 * Gather program options, read input flight plans, read and append comments, write outputs. 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Logger.getGlobal().info( "SudokuSolver by Dan Becker" );
		System.out.println("SudokuSolver by Dan Becker");

		parseGatherOptions(args);

		if ( null != inputPuzzleFile ) {
			// Logger.getGlobal().info( format( "Input puzzle file=%s", inputPuzzleFile ));
			
			// From https://stackoverflow.com/questions/6164448/convert-url-to-normal-windows-filename-java
			// The current recommendation (with JDK 1.7+) is to convert URL ??? URI ??? Path/File/String. 
			// So to convert a URL to File, you would say Paths.get(url.toURI()).toFile()
	        // Note that URI strings have leading slash, and file strings are normalized to local file system (/ versus \)
	        System.out.println( format( "inp option given=%s", inputPuzzleFile ));
	        if ( Files.exists( Path.of( inputPuzzleFile ) )) {
	        	inputPuzzleFile = Path.of( inputPuzzleFile ).normalize().toString();
	        } else	{
	        	inputPuzzleFile = Paths.get( ClassLoader.getSystemClassLoader().getResource(inputPuzzleFile).toURI() ).toFile().toString();

	            System.out.println( format( "inp option normalized=%s", inputPuzzleFile ));
	            JSONObject jsonPuzzle = Utils.parseJSON( inputPuzzleFile );
	            System.out.println( "JSON puzzle=" + jsonPuzzle );

	            inputPuzzleText = jsonPuzzle.getJSONArray("states").getString(0);
	            inputPuzzleSolution = jsonPuzzle.getString("solution");
	            JSONArray jsonArray = jsonPuzzle.getJSONArray("rules");
	            if ( null != jsonArray && jsonArray.length() > 0) {
	            	statedPuzzleRules = new ArrayList<String>( jsonArray.length());
	            	for(int i = 0; i < jsonArray.length(); i++){
	            	    statedPuzzleRules.add(jsonArray.getString(i));
	            	}		            	
	            }
	        }

		} else if ( null != inputPuzzleText ) {
			// Logger.getGlobal().info( format( "Input puzzle text=%s",inputPuzzleText ));
		}
		
		if ( null != inputPuzzleText ) {
			solve( inputPuzzleText, inputPuzzleSolution );
		}
	}
	
	/** Gather command line options for this application. Place info in this class instance variables. */
	public static void parseGatherOptions(String[] args) throws ParseException, URISyntaxException, IOException {
		// Parse the command line arguments
		Options options = new Options();
		// Use dash with shortcut (-h) or -- with name (--help).
        options.addOption("h", "help", false, "print the command line options");
        options.addOption("i", "if", true, "input file for puzzle");
        options.addOption("s", "is", true, "input string for puzzle");

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
            String option = line.getOptionValue("i");
            inputPuzzleFile = option;          
        }
        
        if (line.hasOption("s")) {
            String option = line.getOptionValue("s");
            inputPuzzleText = option;          
        }
	}
	
	/** A loop to apply rules and enter plays until a fail or
	 * zero more fixes.
	 * @return puzzle solved
	 * @throws java.text.ParseException 
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
		UpdateCandidatesRule [] rules = {
			new LegalCandidates(),
			new SingleCandidates(),
			new SinglePositions(),
			new CandidateLines(),
			new MultipleLines(),
			new NakedSubsets(2), // NakedPairs
			new NakedSubsets(3), // NakedTriples
			new DoublePairs(),
			new HiddenSubsets(2), // HiddenPairs
			new HiddenSubsets(3), // HiddenTriples
			new XWings(),
			// new ColorChains(),
			new Swordfish(),
			new ForcingChains(),
		};
		
		// Number of possibles/updates/timings reported
		int [] possibles = new int[ rules.length ];
		int [] updates = new int[ rules.length ];
		int [] timings = new int[ rules.length ];

		int iterations = 0;
		int rulesRun = 0;
		long cumStartTime = System.currentTimeMillis();
		boolean updated = false;
		int startingEntries = candidates.entryCount();
		int startingCandidates = candidates.candidateCount();
		do {
			updated = false;
			// Go through each rule.
			for ( int rulei = 0; rulei < rules.length; rulei++ ) {
			   System.out.println(format("i%d.%d Board entries=%d, candidates=%d", iterations, rulei,
				  candidates.entryCount(), candidates.candidateCount()));
			   // System.out.println( "Board=\n" + board );		
               // System.out.println( "Candidates=\n" + candidates.toStringCompact() );
			   UpdateCandidatesRule rule = rules[ rulei ];
			   long startTime = System.nanoTime();
			   List<int []> locations = rule.locations( board, candidates );
			   // Cannot assume value of each location since each rule reports it separately
			   if ( 0 != rulei ) {
				   // Rule 0 (ValidateLegalCandidates) never reports a location, only updates
				   System.out.print(format("Rule %s reports %d possible locations", rule.ruleName(), locations.size()));
				   if ( locations.size() > 0  ) {
					   possibles[ rulei ] += locations.size();
	   				   System.out.println( ": " + Utils.locationsString(locations));
				   } else {
					   System.out.println();
				   }

			   }
		       // System.out.println("Candidates=" + candidates.toString());
			   int changes = rule.updateCandidates( board, solution, candidates, locations);
			   
			   // Update metrics
			   long endTime = System.nanoTime();
			   long duration = (endTime - startTime) / 1000;  //divide by 1000000 to get milliseconds
			   timings[ rulei ] += (int) duration;
			   
			   if ( changes > 0) {
				   updated = true;
				   updates[ rulei ] += changes;
				   if ( 0 != rulei ) {
					   // Don't count validator as a rule. Don't break loop on validator.
					   rulesRun++;
					   break; // Return to rule 0 before validation to clean things up.					   
				   }
			   }
			   
			   // Do some validation checks.
			   if ( !board.legal())
				   throw new IllegalStateException( "***Warning, rule=" + rule.ruleName() + " illegal board");
			   List<int[]> emptyLocs = candidates.emptyLocations();
			   if ( 0 < emptyLocs.size()) {
				   System.out.println( format("***Warning, rule=%s, %d empty locations at %s", 
					rule.ruleName(), emptyLocs.size(), Utils.locationsString(candidates.emptyLocations()) ));
				   // No need to iterate through rules.
				   break;
			   }		   
			   if ( 0 == candidates.candidateCount()) {
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
		System.out.println( format( "Solving %s successful after %d rules, %d iterations, %dmS", 
			solvedText, rulesRun, iterations, (System.currentTimeMillis() - cumStartTime) ));
		System.out.println( format( "Entry count went from %d to %d. Candidate count went from %d to %d.", 
			startingEntries, candidates.entryCount(), startingCandidates, candidates.candidateCount() ));
		System.out.println("Board=" + board.toSudokuString("-"));
		if (!solved) {
			System.out.println("Remaining candidates=\n" + candidates.toStringBoxed());
			List<int[]> emptyLocs = candidates.emptyLocations();
			if (0 < emptyLocs.size()) {
				System.out.println(format("***Warning, %d empty locations at %s", 
						emptyLocs.size(), Utils.locationsString(candidates.emptyLocations())));
			}
		}
		if ( null != statedPuzzleRules ) {
			System.out.println( "Stated rules=" + statedPuzzleRules );
		}
		
		// Print metrics
		System.out.println( format("%-18s, %10s, %10s, %10s", "Rule", "Locations", "Updates", "Time (uS)" ));
		for ( int rulei = 0; rulei < rules.length; rulei++ ) {
			System.out.println( format("%-18s, %10d, %10d, %10d", 
				rules[ rulei ].ruleName(), possibles[ rulei ], updates[ rulei ], timings[ rulei ] ));
		}
		
		return solved;
	}

	/** Utility that is helpful for testing. */
	public static int runOnce( Board board, Candidates candidates, UpdateCandidatesRule rule) {
        List<int []> locations = rule.locations( board, candidates );
        System.out.println(format("Rule %s reports %d possible locations", rule.ruleName(), locations.size()));
   	    int changes = rule.updateCandidates( board, null, candidates, locations);
        System.out.println(format("Rule %s made %d changes", rule.ruleName(), changes));
		(new LegalCandidates()).updateCandidates(board, null, candidates, null);
        return changes;
	}
}