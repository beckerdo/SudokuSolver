# SudokuSolver by Dan Becker
A tool for reading Sudoku puzzles and applying rules to identify answers

Sudoku puzzles follow standard rules of 81 cells, 9 rows, 9 cols, 9 boxes, 9 digits.

Example command line `java info.danbecker.ss.SudokuSolver -i 20221118-diabolical-17500.json`
```
   -i input file puzzle
   -s text version of puzzle
```     
Puzzles in text contain 81 spaces, containing digits, ( .)(empty space), or (cr,lf,-)(end of row)

An example run shows which rules ran and their timings:
```
Sudoku text ..1.28759-.879.5132-952173486-.2.7..34.-...5..27.-714832695-....9.817-.78.51963-19..87524
Solving was successful after 26 rules, 27 iterations, 24mS
Entry count went from 56 to 81. Candidate count went from 225 to 0.
Board=431628759-687945132-952173486-825769341-369514278-714832695-543296817-278451963-196387524
Rule              ,  Locations,    Updates,  Time (uS)
LegalCandidates   ,          0,        197,       2078
SingleCandidates  ,         72,         24,       1406
SinglePositions   ,          6,          1,        263
CandidateLines    ,          0,          0,        100
MultipleLines     ,          0,          0,        162
NakedSubsets2     ,          0,          0,       3693
NakedSubsets3     ,          0,          0,        557
DoublePairs       ,          0,          0,        311
HiddenSubsets2    ,          0,          0,        555
HiddenSubsets3    ,          0,          0,        764
XWings            ,          0,          0,        192
Swordfish         ,          0,          0,       1009
Skyscraper        ,          1,          1,      10003
Total             ,         79,        223,      21093
```


