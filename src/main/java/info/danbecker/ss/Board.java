package info.danbecker.ss;

import static java.lang.String.format;

import java.lang.ArrayIndexOutOfBoundsException;
import java.text.ParseException;

import static info.danbecker.ss.Utils.ROWS;
import static info.danbecker.ss.Utils.COLS;
import static info.danbecker.ss.Utils.BOXES;

/**
 * Sudoku board
 * 
 * Records cell status as NOT_OCCUPIED or a digit (positive int).
 * Also a few utility APIs related to boxes.
 * 
 * See Candidates for cell candidate actions.
 * 
 * @author <a href="mailto://dan@danbecker.info>Dan Becker</a>
 */
public class Board implements Comparable<Board> {
	public static short NOT_OCCUPIED = 0;
	public static int NOT_FOUND = -1;
	private short[][] digits;
	// Cache for the digitComplete check.
	private boolean [] digitCompleted = new boolean [] {
		false, false, false, false, false, false, false, false, false,
	};
	
	public enum Direction { UP_DOWN, RIGHT_LEFT };
	
	/** List of row,col for each box with row order (rows change slower, cols change faster) */
	public static final int[][][] BOXR = new  int[][][] { 
		new int[][] { new int[]{0,0}, new int[]{0,1}, new int[]{0,2}, new int[]{1,0}, new int[]{1,1}, new int[]{1,2}, new int[]{2,0}, new int[]{2,1}, new int[]{2,2} }, // box 0
		new int[][] { new int[]{0,3}, new int[]{0,4}, new int[]{0,5}, new int[]{1,3}, new int[]{1,4}, new int[]{1,5}, new int[]{2,3}, new int[]{2,4}, new int[]{2,5} }, // box 1
		new int[][] { new int[]{0,6}, new int[]{0,7}, new int[]{0,8}, new int[]{1,6}, new int[]{1,7}, new int[]{1,8}, new int[]{2,6}, new int[]{2,7}, new int[]{2,8} },

		new int[][] { new int[]{3,0}, new int[]{3,1}, new int[]{3,2}, new int[]{4,0}, new int[]{4,1}, new int[]{4,2}, new int[]{5,0}, new int[]{5,1}, new int[]{5,2} },
		new int[][] { new int[]{3,3}, new int[]{3,4}, new int[]{3,5}, new int[]{4,3}, new int[]{4,4}, new int[]{4,5}, new int[]{5,3}, new int[]{5,4}, new int[]{5,5} },
		new int[][] { new int[]{3,6}, new int[]{3,7}, new int[]{3,8}, new int[]{4,6}, new int[]{4,7}, new int[]{4,8}, new int[]{5,6}, new int[]{5,7}, new int[]{5,8} },

		new int[][] { new int[]{6,0}, new int[]{6,1}, new int[]{6,2}, new int[]{7,0}, new int[]{7,1}, new int[]{7,2}, new int[]{8,0}, new int[]{8,1}, new int[]{8,2} },
		new int[][] { new int[]{6,3}, new int[]{6,4}, new int[]{6,5}, new int[]{7,3}, new int[]{7,4}, new int[]{7,5}, new int[]{8,3}, new int[]{8,4}, new int[]{8,5} },
		new int[][] { new int[]{6,6}, new int[]{6,7}, new int[]{6,8}, new int[]{7,6}, new int[]{7,7}, new int[]{7,8}, new int[]{8,6}, new int[]{8,7}, new int[]{8,8} }, // box 8
	};
	/** List of row,col for each box with col order (rows change faster, cols change slower) */
	public static final int[][][] BOXC = new  int[][][] { 
		new int[][] { new int[]{0,0}, new int[]{1,0}, new int[]{2,0}, new int[]{0,1}, new int[]{1,1}, new int[]{2,1}, new int[]{0,2}, new int[]{1,2}, new int[]{2,2} }, // box 0
		new int[][] { new int[]{0,3}, new int[]{1,3}, new int[]{2,3}, new int[]{0,4}, new int[]{1,4}, new int[]{2,4}, new int[]{0,5}, new int[]{1,5}, new int[]{2,5} }, // box 1
		new int[][] { new int[]{0,6}, new int[]{1,6}, new int[]{2,6}, new int[]{0,7}, new int[]{1,7}, new int[]{2,7}, new int[]{0,8}, new int[]{1,8}, new int[]{2,8} },

		new int[][] { new int[]{3,0}, new int[]{4,0}, new int[]{5,0}, new int[]{3,1}, new int[]{4,1}, new int[]{5,1}, new int[]{3,2}, new int[]{4,2}, new int[]{5,2} },
		new int[][] { new int[]{3,3}, new int[]{4,3}, new int[]{5,3}, new int[]{3,4}, new int[]{4,4}, new int[]{5,4}, new int[]{3,5}, new int[]{4,5}, new int[]{5,5} },
		new int[][] { new int[]{3,6}, new int[]{4,6}, new int[]{5,6}, new int[]{3,7}, new int[]{4,7}, new int[]{5,7}, new int[]{3,8}, new int[]{4,8}, new int[]{5,8} },

		new int[][] { new int[]{6,0}, new int[]{7,0}, new int[]{8,0}, new int[]{6,1}, new int[]{7,1}, new int[]{8,1}, new int[]{6,2}, new int[]{7,2}, new int[]{8,2} },
		new int[][] { new int[]{6,3}, new int[]{7,3}, new int[]{8,3}, new int[]{6,4}, new int[]{7,4}, new int[]{8,4}, new int[]{6,5}, new int[]{7,5}, new int[]{8,5} },
		new int[][] { new int[]{6,6}, new int[]{7,6}, new int[]{8,6}, new int[]{6,7}, new int[]{7,7}, new int[]{8,7}, new int[]{6,8}, new int[]{7,8}, new int[]{8,8} }, // box 8
	};
	
	protected Board() {
		
	}
	
	public Board ( String text ) throws ParseException {
		parse( text );
	}
	
	// parse from text such as
	// "...3....1\n38.....2.\n.2..96..4\n.4...7...\n9.......3\n...4...5.\n5..67..3.\n.3.....79\n8....2..."
	// No guess = ". 0"
	// Row end = "\n\r"
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

		short [][] temp = new short[ ROWS ][]; // Do not destroy digits until text is validated
		for( int rowi = 0; rowi < ROWS; rowi++ ) {
			String row = rows[ rowi ];
			// System.out.println( "row=" + row );
			
			if ( row.length() != COLS ) 
				throw new ParseException( format( "row %d is length %d for text %s", rowi, row.length(), row), row.length());

			temp[ rowi ] = new short[ COLS ];
			for ( int coli = 0; coli < COLS; coli++) {
				String entry = row.substring( coli, coli + 1);
				// System.out.println( format( "r,c %d,%d=%s", rowi, coli, entry) );
				
				if ( "0 .".contains( entry ) ) {
					temp[ rowi ][ coli ] = NOT_OCCUPIED;
				} else if ( "123456789".contains( entry ) ) {
					temp[ rowi ][ coli ] = Short.parseShort( entry );
				} else
					throw new ParseException( format( "row %i, col %i in row \"%s\" contains illegal character %s", rowi, coli, row, entry), row.length());
			}
			digits = temp;
		}
	}
	
	public short get( int row, int col ) {
		if (( row < 0 ) || (row >= ROWS)) throw new ArrayIndexOutOfBoundsException( "row=" + row); 
		if (( col < 0 ) || (col >= COLS)) throw new ArrayIndexOutOfBoundsException( "col=" + col);
		return digits[ row ][ col ];
	}
	
	public short set( int row, int col, int val ) {
		if (( row < 0 ) || (row >= ROWS)) throw new ArrayIndexOutOfBoundsException( "row=" + row); 
		if (( col < 0 ) || (col >= COLS)) throw new ArrayIndexOutOfBoundsException( "col=" + col);
		digits[ row ][ col ] = (short) val;
		return digits[ row ][ col ];
	}
	
	/** Returns box index, given row and col. */
	// 0, 1, 2
	// 3, 4, 5
	// 6, 7, 8
	public static int getBox( int rowi, int coli ) {
		if (( rowi < 0 ) || (rowi >= ROWS)) throw new ArrayIndexOutOfBoundsException( "row=" + rowi); 
		if (( coli < 0 ) || (coli >= COLS)) throw new ArrayIndexOutOfBoundsException( "col=" + coli);
        return rowi/3 * 3 + coli / 3;		
	}
	public static int getBox( int [] rowCol ) {
		if (null == rowCol ||  2 != rowCol.length ) 
			throw new IllegalArgumentException( "rowCol=" + rowCol);
		return getBox( rowCol[0], rowCol[1]);
	}

	/** Returns one of 9 int[] with int[] of row col. This is in row order (rows change slower, cols change faster) */
	public static int[][] getBoxRowCols( int boxi ) {
	    if (( boxi < 0 ) || (boxi >= BOXES)) throw new ArrayIndexOutOfBoundsException( "box=" + boxi);
	    return BOXR[ boxi ];
	}

	/** Returns one of 9 int[] with int[] of row col. This is in col order (rows change faster, cols change slower) */
	public static int[][] getBoxRowColsC( int boxi ) {
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
	public int[] boxLocation( int boxi, int digit ) {
		int [][] boxes = Board.getBoxRowCols(boxi);
		for( int bi = 0; bi < BOXES; bi++ ) {
			int rowi = boxes[bi][0];
			int coli = boxes[bi][1];
        	if ( digits[ rowi ][ coli ] == digit ) {
			   return new int [] { rowi, coli };
        	}
		}
	   return null;
	}

	/** States if digit is in this box. */
	public boolean boxTaken( int boxi, int digit ) {
	   return null != boxLocation( boxi, digit );
	}

	/** Counts this digits in box whether legal or not. */
	public int boxCount( int boxi, int digit ) {
	   int count = 0;
       int [][] boxes = Board.getBoxRowCols(boxi);
	   for( int celli = 0; celli < BOXES; celli++  ) {
			int rowi = boxes[celli][0];
			int coli = boxes[celli][1];
		   if ( digits[ rowi ][ coli ] == (short) digit ) {
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
	 * States if one of the combo digits ahave 9 placements on this board, whether legal or not.
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
	 * @return
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
	
	/** Determines if board state is completed.
	 * All positions filled. Could be illegal, perhaps duplicated digits.
	 * @return
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
		target.digits = new short[ ROWS ][];
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			target.digits[ rowi ] = new short[ COLS ];
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
		target.digits = new short[ ROWS ][];
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			target.digits[ rowi ] = new short[ COLS ];
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
		target.digits = new short[ ROWS ][];
		for ( int rowi = 0; rowi < ROWS; rowi++ ) {
			target.digits[ rowi ] = new short[ COLS ];
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
        if (!(obj instanceof Board)) return false; 

        // Cast to same type  
        Board that = (Board) obj; 
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
	 * @delimter for rows
	 * @return
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