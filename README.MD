tree walking interpreter written in java from robert nystorm's book "crafting interpreters"

slight changes from the language written in the book
-added power(^), comma(,) and ternary operator(?:)
-implemented a reverse polish notation printer for the Ast


todo:
-implement tail-call elimination 
-implement the --lex and --parse flags in the command line to show the lexing and parsing results
-implement function expressions
-fix the ugly hack i used for implementing "using uninitialized variable" error
-add classes and methods
-add scope resolving

