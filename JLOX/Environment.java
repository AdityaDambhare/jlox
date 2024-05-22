package jlox;

import java.util.Map;
import java.util.HashMap;

class Environment{
    private final Map<String,Object> values = new HashMap<>();

    void define(String name,Object value){
        values.put(name,value);
    }

    Object get(Token name){
        if(values.containsKey(name.lexeme)){
            return values.get(name.lexeme);
        }
        throw new RunTimeError(name , "Undefined variable '"+name.lexeme+"'");
    }

    void assign(Token name,Object value){
        if(values.containsKey(name.lexeme)){
            values.put(name.lexeme,value);
            return;
        }
        throw new RunTimeError(name , "undefined variable '"+name.lexeme+"'");
    }
}