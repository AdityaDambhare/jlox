package jlox;
import java.util.ArrayList;
import java.util.List;
import static jlox.TokenType.*;

/* 

this is a recursive descent parser . we start from the base expression and make our way to the "leaves" of the ast . 
the "leaves" are expression of highest precedence 


lemme write the grammer for Lox real quick

program -> declaration* ;
declaration -> vardeclaration | statement;
vardeclaration -> "var" IDENTIFIER ("=" expr)? ";" ;
statement -> printstatement|expresssionstatement|block;
block-> "{" declaration* "}";
printstatement -> "print" expr ";" ;
expressionstatement -> expr ";" ;
expr -> comma;
comma-> ternary ("," ternary)*;
ternary -> equality ("?" ternary ":" ternary)* ;
equality -> comparison (("!="||"==") comparison)*;
comparison -> term  ( (">"|"<"|"<="|">=") term )*;
term -> factor (("+"|"-") factor)*;
factor -> power (("*"|"/") power )*;
power -> unary ("^" unary)*;
unary -> ("!"|"-")* primary;
primary-> "true" | "false" | "nil" | "this" | NUMBER | STRING | IDENTIFIER | "(" expr ")" //also known as grouping ;
NUMBER -> DIGIT+ ("."  DIGIT+)?   
STRING -> "\"" (any char except "/"")* "\"";
IDENTIFIER -> ALPHA (ALPHA|DIGIT)*;
ALPHA -> 'a'.....'z'|'A'......'Z'|'_';
DIGIT -> '0'......'9';

*/

class Parser{
    static class ParseError extends RuntimeException{}
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()){
            statements.add(declaration());
        }
        return statements;
    }

    private ParseError error(Token token,String message){
        Lox.error(token,message);
        Lox.hadError = true;
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
    private Stmt declaration(){
        try
        {

        if(match(VAR)){
            return var_declaration();
        }
        return statement();

        }
        catch(ParseError error){
            synchronize();
            return null;
        }
    }

    private Stmt var_declaration(){
        Token name = consume(IDENTIFIER,"Expect variable name");

        Expr initializer = null;
        if(match(EQUAL)){
            initializer = expression();
        }

        consume(SEMICOLON,"Expect ; after variable declaration");
        return new Stmt.Var(name,initializer);
    }

    private Stmt statement(){
        if(match(PRINT)){
            return print_statement();
        }
        if(match(LEFT_BRACE)){
            return new Stmt.Block(block());
        }
        return expression_statement();
    }

    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();
        while(!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }
        consume(RIGHT_BRACE,"Expect } after block.");
        return statements;
    }

    private Stmt print_statement(){
        Expr value = expression();
        consume(SEMICOLON,"Expect ; after statement");
        return new Stmt.Print(value);
    }

    private Stmt expression_statement(){
        Expr expression  = expression();
        consume(SEMICOLON,"Expect ; after statement");
        return new Stmt.Expression(expression);
    }

    private Expr expression(){
        return assignment();
    }

    private Expr assignment(){
        Expr expr = comma();

        if(match(EQUAL)){
            Token equals = previous();
            Expr value = assignment();
            if(expr instanceof Expr.Variable){
                Token name = ((Expr.Variable) expr).identifier;
                return new Expr.Assign(name,value);
            }
            error(equals,"Invalid Assignment target");
        }

        return expr;
    }

    private Expr comma(){
        Expr expr =  ternary();
        while(match(COMMA)){
            Token operator = previous();
            Expr right = ternary();
            expr = new Expr.Binary(expr,operator,right);
        }
        return expr;
    }
//we will express ternary expression such as a?b:c in rpn like this :- a? b c :
    private Expr ternary(){
        Expr expr = equality();
        if(match(QUESTION)){
            Expr if_branch = ternary();
            consume(COLON,"Expect ':' after ?[expression] ");
            Expr else_branch = ternary();
            expr = new Expr.Ternary(expr,if_branch,else_branch);
        }
        return expr;
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
        if(match(IDENTIFIER)) return new Expr.Variable(previous());
        if(match(NUMBER,STRING)){
            return new Expr.Literal(previous().literal);
        }

        if(match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN,"Expected a ')' after expression");
            return new Expr.Grouping(expr);
        }
        
        if(match(BANG_EQUAL,EQUAL_EQUAL)){
            error(previous(),"Left hand Operand Missing");
            equality();
            return null;
        }
        if(match(POWER)){
            error(previous(),"Left hand Operand Missing");
            power();
            return null;
        }
        if(match(QUESTION,COLON)){
            error(previous(),"Missing left hand conditional");
            ternary();
            return null;
        }

        if(match(PLUS)){
            error(previous(),"Missing left hand operand");
            term();
            return null;
        }

        if(match(SLASH,STAR)){
            error(previous(),"Missing left hand operand");
            factor();
            return null;
        }

        if(match(GREATER,GREATER_EQUAL,LESS,LESS_EQUAL)){
            error(previous(),"Missing left hand operand");
            comparison();
            return null;
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