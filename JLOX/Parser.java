package jlox;
import java.util.List;
import static jlox.TokenType.*;
//this is a recursive descent parser . we start from the base expression and make our way to the "leaves" of the ast . 
//the "leaves" are expression of highest precedence 
class Parser{
    static class ParseError extends RuntimeException{}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    Expr parse(){
        try{
            return  expression();
        }
        catch(ParseError error){
            return null;
        }
    }

    private ParseError error(Token token,String message){
        Lox.error(token,message);
        return new ParseError();
    }

    private void synchronize() 
    {
    
    advance();
    while (!isAtEnd()) 
    {
      
      if (previous().type == SEMICOLON) return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }

   }


    private Token consume(TokenType type,String message){
        if(check(type)){return advance();}
        throw error(peek(),message);
    }

    private boolean match(TokenType... types){
        for(TokenType type:types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    private Token previous(){
        return tokens.get(current-1);
    }

    private boolean check(TokenType type){
        if(isAtEnd()){return false;}
        return peek().type == type;
    }

    private Token peek(){
        return tokens.get(current);
    }

    private boolean isAtEnd(){
        return peek().type == TokenType.EOF;
    }

    private Token advance(){
        if(!isAtEnd())
        {
        current++;
        }
        return previous();
    }


// the functions that appear first have the lowest precedence and vica verse
    private Expr expression(){
        return equality();
    }

    //equality has lower precedence than comparison
    private Expr equality(){
        Expr expr = comparison();
        //Expr node for left operand
        while(match(BANG_EQUAL,EQUAL_EQUAL)){//advance to right operand in case binary expression
            Token operator = previous();    
            Expr right = comparison();  // Expr node for right operand
            expr = new Expr.Binary(expr,operator,right);    // create binary expression 
        }
        //in case of no binary expression , just return the Expr node for left operand
        return expr;
    }
    //term is addition and subtraction . it has higher precedence than <,>,<=,>=
    private Expr comparison(){
        Expr expr = term();

        while(match(GREATER,GREATER_EQUAL,LESS,LESS_EQUAL)){ 
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr,operator,right);
        }
        //just return the term in case of no binary expression
        return expr;
    }
    //"factor" or multiplication and division obviously has higher precedence than "term"
    private Expr term(){
        Expr expr = factor();

        while(match(PLUS,MINUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr,operator,right);
        }

        return expr;
    }
    //i've decided to give power (^) more precedence than factor
    private Expr factor(){
        Expr expr = power();
        while(match(SLASH,STAR)){
            Token operator = previous();
            Expr right = power(); 
            expr = new Expr.Binary(expr,operator,right);
        }
        return expr;
    }
    // unary and primary have the highest precedence behind grouping and literals
    private Expr power(){
        Expr expr = unary();
        while(match(POWER)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr,operator,right);
        }
        return expr;
    }

    private Expr unary(){
        if(match(BANG,MINUS)){
            Token operator = previous();
            Expr right = unary();//right will return primary() in case of no more unary operators
            return new Expr.Unary(operator,right); 
        }
        return primary();
    }
    //grouping just starts a new Expr node for the code inside parenthesis
    private Expr primary(){
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE)) return new Expr.Literal(true);
        if(match(NIL))  return new Expr.Literal(null);

        if(match(NUMBER,STRING)){
            return new Expr.Literal(previous().literal);
        }

        if(match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN,"Expected a ')' after expression");
            return new Expr.Grouping(expr);
        }
        throw error(peek(),"Expect expression");
    }

    
}

/*
HELPER METHODS
peek() returns the next token type without consuming it
check(TokenType type) returns true if the next tokentype matches with the argument
match(TokenType... types) if next token matches with any of the types given in the argument , 
                          the parser advances to the next token and the method returns true 

isAtEnd() returns true if EOF token detected
previous() gets the previous token
advance() parser moves to the next token        
*/