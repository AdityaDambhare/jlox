tree walking interpreter written in java from robert nystorm's book "crafting interpreters"

slight changes from the language written in the book
-added power(^), comma(,) and ternary operator(?:)
-implemented a reverse polish notation printer for the Ast
-The runtime error for accessing uninitialized variable is thrown directily by Enviornment.Get()
-The interpreter will simply catch the stack overflow exception and continue running

todo:
-implement the --lex and --parse flags in the command line to show the lexing and parsing results


