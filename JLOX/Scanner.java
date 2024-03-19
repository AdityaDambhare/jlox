package jlox;

import static jlox.TokenType.*;//static import to avoid writing TokenType every time.
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;//for storing tokens
import java.util.Map;

Class Scanner{
    private final String source;
    private final List<Token> tokens = new ArrayList<>();//we scan all the tokens at once i guess

    private int start = 0;//start of individual lexeme
    private int current = 0;//current character we are scanning
    private int line = 1;//the line number of the current token
    
    //identifiers should match the regex [a-zA-Z_][a-zA-Z_0-9]* 
    Scanner(String source){
        this.source = source;
    }//constructor



    List<Token> ScanTokens(){
        while(!isAtEnd()){
            start = current;
            ScanToken();
        }
        tokens.add(new Token(EOF,"",null,line));//appends EOF token so the parser knows when to stop
        return tokens;
    }// so we DO scan all tokens at once . totally different from clox.

    private void ScanToken(){
        char c = advance();
        switch(c){
            case '(' : addToken(LEFT_PAREN); break;
            case ')' : addToken(RIGHT_PAREN); break;
            case '{' : addToken(LEFT_BRACE); break;
            case '}' : addToken(RIGHT_BRACE); break;
            case ',' : addToken(COMMA); break;
            case '.' : addToken(DOT); break;
            case '-' : addToken(MINUS); break;
            case '+' : addToken(PLUS); break;
            case ';' : addToken(SEMICOLON); break;
            case '*' : addToken(STAR); break;
            case '!' : addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=' : addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<' : addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>' : addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            case '^' : addToken(match('=') ? POWER_EQUAL : POWER); break;
            default : break; // todo : add error handling
        }
    }

    private boolean isAtEnd(){
        return current >= source.length();
    }//checks for EOF

    private char advance(){
        current++;
        return source.charAt(current-1);
    }//advnaces by one character

    private bool match(char expected){
        if(isAtEnd()) return false;
        if(source.charAt(current)!=expected) return false;
        current++;//so we dont scan the token twice 
        return true;
    }

    private char peek(){
        if(isAtEnd()) return '\0';
        return source.charAt(current);//remember from advance() that current character is at current-1 
    }//like advance() and match() but does not increase current offset 

    private void addToken(TokenType type){
        addToken(type,null);
    }//adds a token with no literal
    private void addToken(TokenType type,Object literal){
        String text = source.subtring(start,current);//the actual lexeme
        tokens.add(new Token(type,text,literal,line));
    }
}

