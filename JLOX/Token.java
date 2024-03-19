package  jlox;

class Token{
    final TokenType type;//the tokentype
    final String lexeme;//the actual string
    final Object literal;//the value of the token in case of a literal
    final int line;//the line number of token.only accesed during errors .
    
    //the constructor
    Token(TokenType type,String lexeme,Object literal,int line){
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }
    public String toString(){
        return type+ " " + lexeme +" " + literal;
    }
}