package jlox;

import static jlox.TokenType.*;//static import to avoid writing TokenType every time.
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;//for storing tokens
import java.util.Map;

class Scanner{
    private final String source;
    private final List<Token> tokens = new ArrayList<>();//we scan all the tokens at once i guess

    private int start = 0;//start of individual lexeme
    private int current = 0;//current character we are scanning
    private int line = 1;//the line number of the current token
    
    //identifiers should match the regex [a-zA-Z_][a-zA-Z_0-9]* 
    Scanner(String source){
        this.source = source;
    }//constructor

    private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and",    AND);
    keywords.put("class",  CLASS);
    keywords.put("else",   ELSE);
    keywords.put("false",  FALSE);
    keywords.put("for",    FOR);
    keywords.put("fun",    FUN);
    keywords.put("if",     IF);
    keywords.put("nil",    NIL);
    keywords.put("or",     OR);
    keywords.put("print",  PRINT);
    keywords.put("return", RETURN);
    keywords.put("super",  SUPER);
    keywords.put("this",   THIS);
    keywords.put("true",   TRUE);
    keywords.put("var",    VAR);
    keywords.put("while",  WHILE);
  }//hashmap for reserved words
    //we handle booleans and nil as keywords
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
            case '/' :  
                        if(match('*')){ //in case of block comment advance till end of comment
                            while((peek()!='*'&&peekNext()!='/')&&!isAtEnd()){
                                if(peek()=='\n') line++;
                                advance();
                            }
                        }
                        else if(match('/')){ //in case of comment advance till new line
                            while(peek() != '\n' && !isAtEnd()) advance();
                        }
                        else{ // else emit the divison token
                            addToken(SLASH);
                        }
                        break;

            case ' ' : case '\r' : case '\t' : break;//ignore whitespace

            case '\n' : line++; break;//increment line number

            //now time for string literals
            case '"' : string(); break; 
            default : 
                if(isDigit(c)){
                    number();
                }
                else if(isAlpha(c)){
                    identifier();
                }
                else{
                    break;
                    //todo : error handling
                }
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

    private char peekNext(){
        if(current+1>=source.length()) return '\0';
        return source.charAt(current+1);
    }

    private boolean isDigit(char c){
        return (c<='9'&&c>='0');
    }//we could use Character.isDigit() but that allows funny stuff like devnagri digits

    private boolean isAlpha(char c){
        return c<='z'&&c>='a'||c<='Z'&&c>='A'||c=='_';
    }

    private void isAlphaNumeric(char c){
        return isAlpha(c)||isDigit(c);
    }

    private void addToken(TokenType type){
        addToken(type,null);
    }//adds a token with no literal

    private void addToken(TokenType type,Object literal){
        String text = source.subtring(start,current);//the actual lexeme
        tokens.add(new Token(type,text,literal,line));
    }

    private void string(){
        while(peek()!='"'&&!isAtEnd()){
            if(peek()=='\n') line++;
            advance();
        }
        if(isAtEnd()){
            //todo : error handling
            return;
        }
        advance();//consume the closing quote
        String value = source.substring(start+1,current-1);
        addToken(STRING,value);//add token for string literal
    }
    //jlox supports multiline strings . 
    private void number(){
        while(isDigit(peek()))advance();
        if(peek()=='.'&&isDigit(peekNext())){
            advance();//consume the '.'
            while(isDigit(peek()))advance();
        }
        addtoken(NUMBER,Double.parseDouble(source.substring(start,current)));
    }
    
    private void identifier(){
        while(isAlphaNumeric(peek()))advance();
        String text = source.substring(start,current);
        TokenType type = keywords.get(text); // in case reserved word used
        if(type==null) type = IDENTIFIER;
        addToken(type);
    }

}

