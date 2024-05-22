package jlox;

import java.util.Map;
import java.util.HashMap;

class Environment{
    private final Map<String,Object> values = new HashMap<>();
    final Environment enclosing;


    Environment(){
        this.enclosing = null;
    }

    Environment(Environment enclosing){
        this.enclosing = enclosing;
    }

    void define(String name,Object value){
        values.put(name,value);
    }

    Object get(Token name){
        if(values.containsKey(name.lexeme)){
            if(values.get(name.lexeme) instanceof NULL){
                
                throw new RunTimeError(name,"Uninitialized variable '"+name.lexeme+"'");
            }
            return values.get(name.lexeme);
        }
        //if variable is not in this environment , check in the enclosing environment
        if(enclosing !=null){return enclosing.get(name);}
        throw new RunTimeError(name , "Undefined variable '"+name.lexeme+"'");
    }

    void assign(Token name,Object value){
        if(values.containsKey(name.lexeme)){
            values.put(name.lexeme,value);
            return;
        }
        //if variable is not in this environment , check in the enclosing environment
        if(enclosing!=null){ enclosing.assign(name,value); return;}
        throw new RunTimeError(name , "undefined variable '"+name.lexeme+"'");
    }
}