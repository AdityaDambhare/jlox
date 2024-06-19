package jlox;

class RunTimeError extends RuntimeException{
    Token token;
    RunTimeError(Token token,String message){
        super(message);
        this.token = token;
    }
}