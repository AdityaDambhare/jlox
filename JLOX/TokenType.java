package jlox;

enum TokenType{
    //single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE,RIGHT_BRACE,COMMA,DOT,MINUS,PLUS,SEMICOLON,SLASH,STAR,POWER,
    //i added a power token for '^' . one of the first changes i am adding to the language

    //one or two character tokens
    BANG, BANG_EQUAL,EQUAL,EQUAL_EQUAL,GREATER,GREATER_EQUAL,LESS,LESS_EQUAL,PLUS_EQUAL,MINUS_EQUAL,STAR_EQUAL,SLASH_EQUAL,POWER_EQUAL,
    //Added +=, -=, *=, /=, and ^= as well . i am not sure if i should add the other ones like %=, &=, |=, etc. i will add them if i feel like it.

    //literals
    IDENTIFIER,STRING,NUMBER,

    //keywords
    AND,CLASS,ELSE,FALSE,FUN,FOR,IF,NIL,OR,PRINT,RETURN,SUPER,THIS,TRUE,VAR,WHILE, // SUPER - Super class
                                                                                   // FUN - function

    EOF // end of file

}//list of all token types
//the lexer could just use strings but
//thats kind of ugly