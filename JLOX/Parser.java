package jlox;
import java.util.ArrayList;
import java.util.Arrays;
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
unary -> ("!"|"-")* (primary | call);
call -> primary ("arguments?")*;
arguments -> expression ( "," expression)*;
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

    Object ParseRepl(){
        Lox.allowedExpression = true;
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()){
            statements.add(declaration());
            if(Lox.foundExpression){
                Stmt last = statements.get(statements.size()-1);
                Lox.foundExpression = false;
                return ((Stmt.Expression) last).expression;
            }
            Lox.allowedExpression = false;
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

    private boolean checkNext(TokenType tokenType) {
        if (isAtEnd()) return false;
        if (tokens.get(current + 1).type == EOF) return false;
        return tokens.get(current + 1).type == tokenType;
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
        if(check(FUN)&&checkNext(IDENTIFIER)){
            advance();
            return function("function");
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

    private Stmt.Function function(String kind){//weird parameter i know. 
        Token name = consume(IDENTIFIER,"Expect "+kind+" name.");  
        Expr.Function body = function_expression(kind);
        return new Stmt.Function(name,body);
    }

    private Stmt statement(){
        if(match(IF)){
           return if_statement();
        }
        if(match(WHILE)){
            return while_statement();
        }
        if(match(FOR)){
            return for_statement();
        }
        if(match(PRINT)){
            return print_statement();
        }
        if(match(LEFT_BRACE)){
            return new Stmt.Block(block());
        }
        if(match(RETURN)){
            return return_statement();
        }
        return expression_statement();
    }
    

    private Stmt if_statement(){
            consume(LEFT_PAREN,"expect '(' after if statement");
            Expr condition = expression();
            consume(RIGHT_PAREN,"expect ')' after if statement expression");
            Stmt then_branch = statement();
            Stmt else_branch = null;
            if(match(ELSE)){
                else_branch = statement();
            }
            return new Stmt.If(condition,then_branch,else_branch);
    }

    private Stmt while_statement(){
        consume(LEFT_PAREN,"Expect '(' after \"while\"");
        Expr condition = expression();
        consume(RIGHT_PAREN,"Expect ')' after expression");
        Stmt statement = statement();
        return new Stmt.While(condition,statement);
    }

    private Stmt for_statement(){
        consume(LEFT_PAREN,"Expect '(' after \"for\" ");

        Stmt initializer;
        if(match(SEMICOLON)){
            initializer = null;
        }
        else if(match(VAR)){
            initializer = var_declaration();
        }
        else{
            initializer  = expression_statement();
        }
        
        Expr condition = null;
        if(!check(SEMICOLON)){
            condition = expression();
        }
        consume(SEMICOLON,"Expect ';' after loop condition");

        Expr increment = null;
        if(!check(SEMICOLON)){
            increment = expression();
        }
        consume(RIGHT_PAREN,"Expect ')' after for clauses");

        Stmt body = statement();

        if(increment!=null){
            body = new Stmt.Block(
                Arrays.asList(
                    body,
                    new Stmt.Expression(increment)
                )
            );
        }

        if(condition==null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition,body);

        if(initializer!=null){
            body = new Stmt.Block(Arrays.asList(initializer,body));
        }
        return body;
    }

    private Stmt return_statement(){
        Token keyword = previous();
        Expr value = null;
        if(!check(SEMICOLON)){
            value = expression();
        }
        consume(SEMICOLON,"Expect \";\" after return value");
        return new Stmt.Return(keyword,value);
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
        if(Lox.allowedExpression && isAtEnd()){
            Lox.foundExpression = true;
        }
        else{
        consume(SEMICOLON,"Expect ; after statement");
        }
        return new Stmt.Expression(expression);
    }

    private Expr expression(){
        return assignment();
    }

    private Expr assignment(){
        Expr expr = comma();

        if(match(EQUAL)){
            Token equals = previous();
            Expr value = comma();
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
        Expr expr = or();
        if(match(QUESTION)){
            Expr if_branch = ternary();
            consume(COLON,"Expect ':' after ?[expression] ");
            Expr else_branch = ternary();
            expr = new Expr.Ternary(expr,if_branch,else_branch);
        }
        return expr;
    }

    private Expr or(){
        Expr expr = and();
        while(match(OR)){
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr,operator,right);
        }
        return expr;
    }

    private Expr and(){
        Expr expr = equality();
        while(match(AND)){
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr,operator,right);
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
        return call();
    }

    private Expr.Function function_expression(String kind){
        consume(LEFT_PAREN,"Expect '(' after function name");
        List<Token> parameters = new ArrayList<>();
        if(!check
        (RIGHT_PAREN)){
            do{
                if(parameters.size()>=255){
                    error(peek(),"Can't have more than 255 parameters");
                }
                parameters.add(consume(IDENTIFIER,"Expect parameter name"));
            }
            while(match(COMMA));
        }
        consume(RIGHT_PAREN,"Expect ')' after parameters");
        consume(LEFT_BRACE,"Expect '{' after function parameters");
        List<Stmt> body = block();
       
        return new Expr.Function(parameters,body);
    }

    private Expr call(){
        Expr expr = primary();
        while(match(LEFT_PAREN)){
            expr = patchCall(expr);
        }
        return expr;
    }

    private Expr patchCall(Expr callee){
        List<Expr> arguments = new ArrayList<>();
        int args = 0;
        if(!check(RIGHT_PAREN)){
            do{ 
                arguments.add(ternary());
                if(++args > 255){
                    error(peek(),"Can't have more than 255 arguments ");
                }
                // we don't call expression() as it would call comma() which 
                // would consume all commmas leading to only one argument being passed
                //to the function  
                //this also means than any operator with lower precedence than the comma operator will not be evaluated
                //i *could* try to allow users to pass assignment expressions to function calls but that would be too 
                //uhh *ugly* . idk what other word to use .
            }
            while(match(COMMA));
        } 
        Token paren = consume(RIGHT_PAREN,"Expect \")\" after function call");
        return new Expr.Call(callee,paren,arguments);       
    }

    //grouping just starts a new Expr node for the code inside parenthesis
    private Expr primary(){
        if(match(FUN)) return function_expression("function expression");
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