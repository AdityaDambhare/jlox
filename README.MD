tree walking interpreter written in java from robert nystorm's book [crafting interpreters](https://craftinginterpreters.com). 

slight changes from the language written in the book
-added power(^), comma(,) and ternary operator(?:)
-implemented a reverse polish notation printer for the Ast alongside a parenthesized ast printer
-The runtime error for accessing uninitialized variable is thrown directily by Enviornment.Get()
-The interpreter will simply catch any stack overflow exceptions and continue running




