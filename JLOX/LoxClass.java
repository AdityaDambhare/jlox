package jlox;
import java.util.Map;
import java.util.HashMap;
class LoxClass implements LoxCallable{
    final String name;
    private final Map<String,LoxFunction> methods;
    private final LoxClass superclass;
    LoxClass(String name,LoxClass superclass,Map<String,LoxFunction> methods){
        this.superclass = superclass;
        this.name = name;
        this.methods = methods;
    }
    @Override
    public Object call(Interpreter interpreter,java.util.List<Object> arguments){
        LoxInstance instance = new LoxInstance(this);
        LoxFunction initializer = findMethod("init");
        if(initializer!=null){
            initializer.bind(instance).call(interpreter,arguments);
        }
        return instance;
    }
    
    LoxFunction findMethod(String name){
        if(methods.containsKey(name)){
            return methods.get(name);
        }
        if(superclass != null){
            return superclass.findMethod(name);
        }
        return null;
    }
    @Override
    public int arity(){
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }    
    @Override
    public String toString(){
        return "<class " + name + ">";
    } 
}