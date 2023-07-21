package info.danbecker.ss;

import static java.lang.String.format;

import java.lang.ArrayIndexOutOfBoundsException;
import java.text.ParseException;

/**
 * Sudoku board
 * <p>
 * Records cell status as NOT_OCCUPIED or a digit (positive int).
 * Also, a few utility APIs related to boxes.
 * <p>
 * See Candidates for cell candidate actions.
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class Board implements Comparable<Board> {
	public static short ROWS = 9;
	public static short COLS = 9;
	public static short BOXES = 9;
	public static short DIGITS = 9;

	public static short NOT_OCCUPIED = 0;
	public static int NOT_FOUND = -1;
	private int[][] digits;
	// Cache for the digitComplete check.
	private final boolean [] digitCompleted = new boolean [] {
		false, false, false, false, false, false, false, false, false,
	};
	
	public enum Direction { UP_DOWN, RIGHT_LEFT }

	/** Immutable cache of RowCol references. Saves a lot of newing/instantiation */
	public static final RowCol [][] ROWCOL;
	static {
		ROWCOL = new RowCol[ROWS][COLS];
		for (int rowi = 0; rowi < ROWS; rowi++) {
			for (int coli = 0; coli < COLS; coli++) {
				ROWCOL[rowi][coli] = new RowCol(rowi, coli);
			}
		}
	}
	/** List of row,col for each box with row order (rows change slower, cols change faster). Calculated placement. */
	public static final RowCol[][] BOXR;
	static {
		BOXR = new RowCol[BOXES][ROWS];
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++ ) {
				RowCol rowCol = ROWCOL[ rowi ][coli ];
				BOXR[rowCol.box()][rowi%3 * 3 + coli%3] = rowCol;
			}
		}
	}

	/** List of row,col for each box with row order (rows change slower, cols change faster). Calculated placement. */
	public static final RowCol[][] BOXC;
	static {
		BOXC = new RowCol[BOXES][COLS];
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++ ) {
				RowCol rowCol = ROWCOL[rowi][coli];
				int boxi = Board.getBox( rowCol );
				BOXC[boxi][rowi%3 + coli%3 *3] = rowCol;
			}
		}
	}

	protected Board() {
	}
	
	public Board ( String text ) throws ParseException {
		parse( text );
	}

	/**
	 * parse from text such as
	 * "...3....1\n38.....2.\n.2..96..4\n.4...7...\n9.......3\n...4...5.\n5..67..3.\n.3.....79\n8....2..."
	 * No guess = ". 0"
	 * Row end = "\n\r"
	 */
	public void parse( String text ) throws ParseException {
		String [] rows = text.split( "\n" );
		if ( 1 == rows.length )
			rows = text.split( "-" ); // + is a regex meta char
		// Consider parsing 1 to 9 rows, just do 9 at a time when applicable.
		if ( (1 == rows.length) ) {
			if (81 != rows[0].length())
			   throw new ParseException( format( "row count is %d, length %d for input %s", 
			      rows.length, rows[0].length(), rows[ 0 ]), rows[0].length());

			// Parse rows 9 chars at a time
			String singleString = rows[0];
			rows = new String[9];
			for ( int rowi = 0; rowi < rows.length; rowi++) {
				rows[ rowi ] = singleString.substring(rowi * 9, rowi * 9 + 9);
				// System.out.println( "Row " + rowi + "=" + rows[ rowi ]);
			}
		}

		int [][] temp = new int[ ROWS ][]; // Do not destroy digits until text is validated
		for( int rowi = 0; rowi < ROWS; rowi++ ) {
			String row = rows[ rowi ];
			// System.out.println( "row=" + row );
			
			if ( row.length() != COLS ) 
				throw new ParseException( format( "row %d is length %d for text %s", rowi, row.length(), row), row.length());

			temp[ rowi ] = new int[ COLS ];
			for ( int coli = 0; coli < COLS; coli++) {
				String entry = row.substring( coli, coli + 1);
				// System.out.println( format( "r,c %d,%d=%s", rowi, coli, entry) );
				
				if ( "0 .".contains( entry ) ) {
					temp[ rowi ][ coli ] = NOT_OCCUPIED;
				} else if ( "123456789".contains( entry ) ) {
					temp[ rowi ][ coli ] = Short.parseShort( entry );
				} else
					throw new ParseException( format( "row %d, col %d in row \"%s\" contains illegal character %s", rowi, coli, row, entry), row.length());
			}
			digits = temp;
		}
	}
	
	public int get( RowCol rowCol ) {
		return digits[ rowCol.row() ][ rowCol.col() ];
	}
	
	public int set( RowCol rowCol, int val ) {
		digits[ rowCol.row() ][ rowCol.col() ] = val;
		return digits[ rowCol.row() ][ rowCol.col() ];
	}
	
	/** Returns box index, given row and col. */
	// 0, 1, 2
	// 3, 4, 5
	// 6, 7, 8
	public static int getBox( RowCol rowCol ) {
        return rowCol.row()/3 * 3 + rowCol.col() / 3;
	}
	
	/** Returns one of 9 int[] with int[] of row col. This is in row order (rows change slower, cols change faster) */
	public static RowCol[] getBoxRowCols( int boxi ) {
	    if (( boxi < 0 ) || (boxi >= BOXES)) throw new ArrayIndexOutOfBoundsException( "box=" + boxi);
	    return BOXR[ boxi ];
	}

	/** Returns one of 9 int[] with int[] of row col. This is in col order (rows change faster, cols change slower) */
	public static RowCol[] getBoxRowColsC( int boxi ) {
	    if (( boxi < 0 ) || (boxi >= BOXES)) throw new ArrayIndexOutOfBoundsException( "boxi=" + boxi);
	    return BOXC[ boxi ];
	}
	
	/** Returns column index of the given digit in this row.
	 * @return -1 if not found in this row.
	 */
	public int rowLocation( int rowi, int digit ) {	
	   for( int coli = 0; coli < COLS; coli++  ) {
		   if ( digits[ rowi ][ coli ] == (short) digit ) {
			   return coli;
		   }
	   }
       return NOT_FOUND;
	}

	/** States if digit is in this row. */
	public boolean rowTaken( int rowi, int digit ) {
		return -1 != rowLocation( rowi, digit );
	}

	/** Counts this digit in row, whether legal or not. */
	public int rowCount( int rowi, int digit ) {
	   int count = 0;
	   for( int coli = 0; coli < COLS; coli++  ) {
		   if ( digits[ rowi ][ coli ] == (short) digit ) {
		      count++;
		   }
	   }
       return count;
	}
	
	/** Returns row index of the given digit in this col.
	 * @return -1 if not found in this row.
	 */
	public int colLocation( int coli, int digit ) {
	   for( int rowi = 0; rowi < ROWS; rowi++  ) {
		   if ( digits[ rowi ][ coli ] == digit ) {
			   return rowi;
		   }
	   }
       return NOT_FOUND;
	}

	/** States if digit is in this col. */
	public boolean colTaken( int coli, int digit ) {
		return -1 != colLocation( coli, digit );
	}

	/** Counts this digit in col, whether legal or not. */
	public int colCount( int coli, int digit ) {
	   int count = 0;
	   for( int rowi = 0; rowi < ROWS; rowi++  ) {
		   if ( digits[ rowi ][ coli ] == (short) digit ) {
		      count++;
		   }
	   }
       return count;
	}

	/** Returns row and col index of the given digit in this box.
	 * @return null if not found in this box.
	 */
	public RowCol boxLocation( int boxi, int digit ) {
		RowCol[] rowCols = Board.getBoxRowCols(boxi);
		for( int loci = 0; loci < BOXES; loci++ ) {
			RowCol rowCol = rowCols[ loci ];
        	if ( digits[ rowCol.row() ][ rowCol.col() ] == digit ) {
			   return rowCol;
        	}
		}
	   return null;
	}

	/** States if digit is in this box. */
	public boolean boxTaken( int boxi, int digit ) {
	   return null != boxLocation( boxi, digit );
	}

	/** Counts digits in box whether legal or not. */
	public int boxCount( int boxi, int digit ) {
	   int count = 0;
       RowCol [] rowCols = Board.getBoxRowCols(boxi);
	   for( int celli = 0; celli < BOXES; celli++  ) {
		   RowCol rowCol = rowCols[celli];
		   if ( digits[rowCol.row()][rowCol.col() ] == digit ) {
		      count++;
		   }
	   }
       return count;
	}

	/** Counts this digit in entire board, whether legal or not. */
	public int digitCount( int digit ) {
	   int count = 0;
	   for( int rowi = 0; rowi < ROWS; rowi++  ) {
	      count += rowCount( rowi, digit );
	   }
       return count;
	}
	
	/** 
	 * States whether 9 or more digits are placed on this board, whether legal or not.
	 *  Ones-based digits. */
	public boolean digitCompleted( int digit ) {
		if (digitCompleted[ digit - 1]) return true;
		digitCompleted[ digit - 1] = (digitCount( digit ) >= Utils.DIGITS);
		return digitCompleted[ digit - 1];
	}

	/** 
	 * States if one of the combo digits have 9 placements on this board, whether legal or not.
	 * Zero-based digits. */
	public boolean comboCompleted( int [] combo ) {		
		for ( int combi = 0; combi < combo.length; combi++ ) {
			int digit = combo[ combi ];
			if (digitCompleted[ digit ]) return true;
			digitCompleted[ digit ] = (digitCount( digit + 1) >= Utils.DIGITS);
			if (digitCompleted[ digit ]) return true;			
		}
		return false;
	}

	/** Determines if board state is legal
	 * No set digits duplicated according to rules.
	 * @return whether or not this board is legal
	 */
	public boolean legal() {
		for( int digit = 1; digit <= 9; digit++ ) {
			for( int item = 0; item < ROWS; item++) {
				if ( rowCount( item, digit ) > 1 ) return false;
				if ( colCount( item, digit ) > 1 ) return false;
				if ( boxCount( item, digit ) > 1 ) return false;
			}
		}
		return true;
	}

	/** Determines how many cells are completed.
	 * All positions filled. Could be illegal, perhaps duplicated digits.
	 * @return number of completed board entries.
	 */
	public int getOccupiedCount() {
		int count = 0;
		for( int rowi = 0; rowi < ROWS; rowi++  ) {
			for( int coli = 0; coli < COLS; coli++  ) {
				if ( digits[ rowi ][ coli ] > 0 ) count++;
			}
		}
		return count;
	}

	/** Determines if board state is completed.
	 * All positions filled. Could be illegal, perhaps duplicated digits.
	 * @return whether or not this board is completed
	 */
	public boolean completed() {
		for( int rowi = 0; rowi < ROWS; rowi++  ) {
			for( int coli = 0; coli < COLS; coli++  ) {
				if ( digits[ rowi ][ coli ] == 0 ) return false;
			}
		}
		return true;
	}

	/** 
	 * Create a new board which is the origin board with top rotated to the right.
	 * The origin board is not affected. 
	 */
	public static Board rotateRight( Board origin ) {
		Board target = new Board();
		// Fully create digits spaces
		target.digits = new int[ ROWS ][];
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			target.digits[ rowi ] = new int[ COLS ];
		}
		// Move digits from originating row,cols to target
		for( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++) {
				target.digits[coli][COLS-rowi-1] = origin.digits[ rowi ][coli ];
			}
		}
		return target;
	}
	
	/** 
	 * Create a new board which is the origin board with top rotated to the left.
	 * The origin board is not affected. 
	 */
	public static Board rotateLeft( Board origin ) {
		Board target = new Board();
		// Fully create digits spaces
		target.digits = new int[ ROWS ][];
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			target.digits[ rowi ] = new int[ COLS ];
		}
		// Move digits from originating row,cols to target
		for( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++) {
				target.digits[ROWS-coli-1][rowi] = origin.digits[ rowi ][coli ];
			}
		}
		return target;
	}
	
	/** 
	 * Create a new board which is the origin board with mirror image given.
	 * The origin board is not affected. 
	 */
	public static Board mirror( Board origin, Direction direction) {
		Board target = new Board();
		// Fully create digits spaces
		target.digits = new int[ ROWS ][];
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			target.digits[ rowi ] = new int[ COLS ];
		}
		if ( Direction.UP_DOWN == direction ) {
			// Move digits from originating row,cols to target
			for( int rowi = 0; rowi < ROWS; rowi++ ) {
				for ( int coli = 0; coli < COLS; coli++) {
					target.digits[ROWS-rowi-1][coli] = origin.digits[ rowi ][coli ];
				}
			}
		} else { // RIGHT_LEFT
			// Move digits from originating row,cols to target
			for( int rowi = 0; rowi < ROWS; rowi++ ) {
				for ( int coli = 0; coli < COLS; coli++) {
					target.digits[rowi][COLS-coli-1] = origin.digits[ rowi ][coli ];
				}
			}
		}
		return target;
	}
	
	@Override
	public int compareTo(Board that) {
		//System.out.println( "compareTo");
		if (null == that) return -1;
		if ((null != digits) && (that.digits == null)) return 1;
		if ((null == digits) && (that.digits != null)) return -1;

		for( int rowi = 0; rowi < ROWS; rowi++ ) {
			for ( int coli = 0; coli < COLS; coli++) {
				if ( digits[ rowi ][ coli ] > that.digits[ rowi ][ coli ]) return 1;
				if ( digits[ rowi ][ coli ] < that.digits[ rowi ][ coli ]) return -1;
			}
		}		
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
        // Compare with self   
        if (obj == this) return true; 
  
        // Compare with class type
        if (!(obj instanceof Board that)) return false;

        // Cast to same type  
		return 0 == this.compareTo( that );
	}
	
	@Override
	public String toString() {
		if (null == digits)
			return "null";

		StringBuilder sb = new StringBuilder();
		for (int rowi = 0; rowi < ROWS; rowi++) {
			if (rowi > 0)
				sb.append("\n");

			for (int coli = 0; coli < COLS; coli++) {
				sb.append(digits[rowi][coli]);
			}

		}
		return sb.toString();
	}
	
	/** A Sudoku string of board entries and dots for no placement.
	 * Works with sites such as https://www.thonky.com/sudoku/solution-count
	 * @param delimiter character to separate rows
	 * @return String of 81 digits, empties (.), and optional delimiters
	 */
	public String toSudokuString( String delimiter ) {
		if (null == digits)
			return "null";

		StringBuilder sb = new StringBuilder();
		for (int rowi = 0; rowi < ROWS; rowi++) {
			if ( rowi > 0 && null != delimiter && delimiter.length() > 0)
				sb.append(delimiter);
			for (int coli = 0; coli < COLS; coli++) {
				if (0 == digits[rowi][coli]) {
					sb.append(".");
				} else {
					sb.append(digits[rowi][coli]);
				}
			}

		}
		return sb.toString();
	}
}