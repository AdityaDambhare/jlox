package jlox;
import java.util.List;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
 class Interpreter implements Expr.Visitor<Object>,Stmt.Visitor<Void>{//statements produce no value unlike expressions
    final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<Expr,Integer> locals = new HashMap<>();
    Interpreter(){

    globals.define
    (
     "clock", 
      new LoxCallable() 
    {
      @Override
      public int arity() { return 0; }

      @Override
      public Object call(Interpreter interpreter,
                         List<Object> arguments) {
        return (double)System.currentTimeMillis() / 1000.0;
      }

      @Override
      public String toString() { return "<native fn>"; }
    }
    );


    globals.define
    (
     "println", 
      new LoxCallable() 
    {
      @Override
      public int arity() { return 1; }

      @Override
      public Object call(Interpreter interpreter,
                         List<Object> arguments) {
        System.out.println(stringify(arguments.get(0)));
        return null;
      }

      @Override
      public String toString() { return "<native fn>"; }
    }
    
    );

    }

    void interpret(List<Stmt> statements){
        try{
            for(Stmt statement : statements){
                execute(statement);
            }
        }
        catch(RunTimeError error){
            Lox.runtimeError(error);//print error but don't close the shell
        }
        catch(Return error){
            Lox.runtimeError(
                new RunTimeError(error.position,"return statement outside function call")
            );
        }
    }

    String interpret(Expr expr){
        try{
            Object value = evaluate(expr);
            return stringify(value);   
        }
        catch(RunTimeError error){
            Lox.runtimeError(error);
            return null;
        }
        catch(Return error){
            Lox.runtimeError(
                new RunTimeError(error.position,"return statement outside function call")
            );
            return null;
        }
    }

    private void execute(Stmt statement){
        statement.accept(this);
    }

    void resolve(Expr expr,int depth){
        locals.put(expr,depth);
    }

    private Object evaluate(Expr expr){
        return expr.accept(this); 
    }

    //visitExpressionStmt and vistPrintStmt retun Void which is a wrapper for void . 
    //we use Void because void cannot be passed as a  generic argument for some reason

    
    void executeBlock(List<Stmt> statements,Environment environment){
        Environment enclosing = this.environment;
        Object value;
        try{
            this.environment = environment;
            for(Stmt statement:statements){
               execute(statement);
            }
        }
        finally{
            this.environment = enclosing;
            
        }
    }
    @Override
    public Void visitIfStmt(Stmt.If stmt){
        if(isTruthy(evaluate(stmt.condition))){
            execute(stmt.then_branch);
        }
        else if (stmt.else_branch != null){
            execute(stmt.else_branch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt){
        while(isTruthy(evaluate(stmt.condition))){
            execute(stmt.statement);
        }
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt){
        Object value =  null;
        if(stmt.initializer != null){
            value = evaluate(stmt.initializer);
        }
        else
        {value = new NULL(); };
        environment.define(stmt.name.lexeme,value);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt){
        String name = stmt.name.lexeme;
        LoxFunction function = new LoxFunction(name,stmt.function,environment,false);
        environment.define(stmt.name.lexeme,function);
        return null;
    }
    @Override
    public Void visitReturnStmt(Stmt.Return stmt){
        Object value  = null;
        if(stmt.value!=null) value = evaluate(stmt.value);
        throw new Return(value,stmt.keyword);//we unwind all the way back to call() with this where we catch the exception and return the value 
    }
    @Override
    public Object visitAssignExpr(Expr.Assign expr){
        Object value  = evaluate(expr.value);
        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } 
        else{
            globals.assign(expr.name, value);
        }
        return value;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt){
        executeBlock(stmt.statements,new Environment(this.environment));
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class stmt){
        Object superclass = null;
        
        if(stmt.superclass!=null){
            superclass = evaluate(stmt.superclass);
            if(!(superclass instanceof LoxClass)){
                throw new RunTimeError(stmt.superclass.identifier,"Superclass must be a class");
            }
        }

        environment.define(stmt.name.lexeme,null);

        if(stmt.superclass != null){
            environment = new Environment(environment);
            environment.define("super",superclass);
        }

        Map<String,LoxFunction> methods = new HashMap<>();
        for(Stmt.Function method:stmt.methods){
            LoxFunction function = new LoxFunction(method.name.lexeme,method.function,this.environment,method.name.lexeme.equals("init"));
            methods.put(method.name.lexeme,function);
        }

        LoxClass klass = new LoxClass(stmt.name.lexeme,(LoxClass) superclass ,methods);
        if(superclass != null){
            environment = environment.enclosing;
        }
        environment.assign(stmt.name,klass);
        return null;
    }
    @Override
    public Object visitThisExpr(Expr.This expr){
        return lookUpVariable(expr.keyword,expr);
    }
    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt){
        evaluate(stmt.expression);
        return null;
    }

    

    @Override
    public Void visitPrintStmt(Stmt.Print stmt){
        Object print_value = evaluate(stmt.expression);
        System.out.println(stringify(print_value));
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal literal){
        return literal.value;
    }
    @Override
    public Object visitLogicalExpr(Expr.Logical expr){
        Object left  = evaluate(expr.left);
        if(expr.operator.type == TokenType.OR){
            if (isTruthy(left)) return left;
        }
        else{
            if(!isTruthy(left)) return left;
        }
        return evaluate(expr.right);
    }

    @Override
    public Object visitSetExpr(Expr.Set expr){
        Object object  = evaluate(expr.object);
        if(!(object instanceof LoxInstance)){
            throw new RunTimeError(expr.name,"Only instances have fields");
        }
        Object value = evaluate(expr.value);
        ((LoxInstance)object).set(expr.name,value);
        return value;
    }
    @Override
    public Object visitSuperExpr(Expr.Super expr){
        int distance = locals.get(expr);
        LoxClass superclass = (LoxClass) environment.getAt(distance,"super");
        LoxInstance object = (LoxInstance) environment.getAt(distance-1,"this");
        LoxFunction method = superclass.findMethod(expr.method.lexeme);
        if (method == null){
            throw new RunTimeError(expr.method,"Undefined property "+expr.method.lexeme);
        }
        return method.bind(object);
    }
    @Override
    public Object visitGroupingExpr(Expr.Grouping expr){
        return evaluate(expr.expression);
    }
    //checkNumberOperand() is called for every operation concerning doubles (except addition)
    //will throw RunTimeError if both values are not double in binary expression (except for addition)
    @Override
    public Object visitUnaryExpr(Expr.Unary expr){
        Object right = evaluate(expr.right);

        switch(expr.operator.type){
            case MINUS: 
                checkNumberOperand(expr.operator,right);
                return -(double) right;
            case BANG:
                return !isTruthy(right);
        }
        return null;
    }



    @Override
    public Object visitVariableExpr(Expr.Variable expr){
        return lookUpVariable(expr.identifier,expr);
    }

    private Object lookUpVariable(Token name,Expr expr){
        Integer distance = locals.get(expr);
        if(distance != null){
            return environment.getAt(distance,name.lexeme);
        }
        else{
            return globals.get(name);
        }
    }

    @Override
    public Object visitFunctionExpr(Expr.Function expr){
        return new LoxFunction(null,expr,environment,false);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr){
        Object right = evaluate(expr.right);
        Object left = evaluate(expr.left);
        
        switch(expr.operator.type){

            case COMMA:
            return right;

            case MINUS : 
            checkNumberOperand(expr.operator,left,right);
            return (double)left - (double)right;

            case STAR :
            checkNumberOperand(expr.operator,left,right);
            return (double)left * (double)right;
            case SLASH :
            checkNumberOperand(expr.operator,left,right);
            if((double)right == 0){
                throw new RunTimeError(expr.operator,"cannot divide number by zero");
            }
            return (double)left / (double)right;

            case POWER:
            checkNumberOperand(expr.operator,left,right);
            return Math.pow((double)left , (double)right);

            case PLUS:
            if(left instanceof Double && right instanceof Double ){
                return (double) left + (double) right;
            }
            else if(left instanceof String || right instanceof String){
                return stringify(left) + stringify(right);
            }
            throw new RunTimeError(expr.operator,"Operands must be both number or both strings");
            

            case GREATER:
                checkNumberOperand(expr.operator,left,right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperand(expr.operator,left,right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperand(expr.operator,left,right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperand(expr.operator,left,right);
                return (double) left <= (double) right;
            
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
        }
        return null;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr){
        Object condition = evaluate(expr.condition);//evaluate the condition
        if(isTruthy(condition)){//if condition is true or any non zero double the if_branch is evaluated and else_branch is discarded
            return evaluate(expr.if_branch);
        }
        else{
            return evaluate(expr.else_branch);//if condtion results in false,nil or zero valued double the else_branch is evaluated
        }
    }
    @Override
    public Object visitGetExpr(Expr.Get expr){
        Object object = evaluate(expr.object);
        if ( object instanceof LoxInstance){
            return ((LoxInstance)object).get(expr.name);
        }
        throw new RunTimeError(expr.name,"Only instances have properties");
    }

    @Override
    public Object visitCallExpr(Expr.Call expr){
        Object callee = evaluate(expr.callee);
        List<Object> arguments = new ArrayList<>();
        for(Expr argument:expr.arguments){
            arguments.add(evaluate(argument));
        }

        if(!(callee instanceof LoxCallable )){
            throw new RunTimeError(expr.paren,"Can only call function and classes");
        }

        LoxCallable function = (LoxCallable) callee;

        if(arguments.size() != function.arity()){
            throw new RunTimeError(expr.paren,"Expected " + function.arity() +" arguments but got "+arguments.size());
        }
        try{
            return function.call(this,arguments);
        }
        catch(RunTimeError err){
            if(err.token == null){
                err.token = expr.paren;
            }
            throw err;
        }
    }
    

    //helper methods
    
    //you may ask why use this helper method when you can directly print the object in System.out.println()
    //toString() method throws exception when the object is a null value 
    private String stringify(Object object) {
    if (object == null) return "nil";

    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return object.toString();
  }

    private void checkNumberOperand(Token operator,Object... objects){
        for(Object object:objects){
            if(! (object instanceof Double)) {
                throw new RunTimeError(operator,"Operand must be a number");
            }
        }
    }

    private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean) return (boolean)object;
    if (object instanceof Double && (double)object==0){return false;}
    return true;
    }


    private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }

}