package jlox;

import java.util.List;

class LoxFunction implements LoxCallable{
    private final String name;
    private final Expr.Function declaration;
    private final Environment closure;
    private boolean isInitializer;
    private boolean isGetter;
    LoxFunction(String name,Expr.Function declaration,Environment closure,boolean isInitializer,boolean isGetter){
        this.name = name;
        this.closure = closure;
        this.declaration = declaration;
        this.isInitializer = isInitializer;
        this.isGetter = isGetter;
    }

    LoxFunction bind(LoxInstance instance){
        Environment environment = new Environment(closure);
        environment.define("this",instance);
        return new LoxFunction(this.name,declaration,environment,isInitializer,isGetter);
    }
    public boolean isGetter(){
        return isGetter;
    }
    @Override
    public Object call(Interpreter interpreter,List<Object> arguments){
        Environment environment = new Environment(closure);
        if(!isGetter){
            for(int i = 0;i<declaration.params.size();i++){
                environment.define(declaration.params.get(i).lexeme,arguments.get(i));
            }
        }
        try{
        interpreter.executeBlock(declaration.body,environment);
        }
        catch(Return value){
            return value.value;
        }
        catch(StackOverflowError e){
            throw new RunTimeError(null,"Stack Overflow at function "+name);
        }
        if( isInitializer) return closure.getAt(0,"this");
        return null;
    }

    @Override
    public int arity(){
        return declaration.params.size();
    }

    @Override
    public String toString(){
        if(name==null){return "<fn>";}
        return "<fn "+name+">";
    }
}