package jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>,Stmt.Visitor<Void>{
    private final Interpreter interpreter;
    private final Stack< Map<String,Boolean> > scopes = new Stack<>();
    private FunctionType currentfunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;
    Resolver(Interpreter interpreter){
        this.interpreter = interpreter;
    }
    private enum FunctionType{
        NONE,
        FUNCTION,
        INITIALIZER,
        GETTER,
        METHOD
    }
    private enum ClassType{
        NONE,
        CLASS,
        SUBCLASS
    }

private void resolve(Expr expr){
    expr.accept(this);
}

private void resolve(Stmt statement){
    statement.accept(this);
}

void resolve(List<Stmt> statements){
    for(Stmt stmt:statements){
        resolve(stmt);
    }
}

private void resolveLocal(Expr expr,Token name){
    for(int i = scopes.size()-1;i>=0;i--){
        if(scopes.get(i).containsKey(name.lexeme)){
            interpreter.resolve(expr,scopes.size()-1-i);
            return ;
        }
    }
}

private void resolveFunction(Expr.Function function,FunctionType type){
    FunctionType enclosing = currentfunction;
    currentfunction = type;
    beginScope();
    if(type != FunctionType.GETTER){
    for(Token param:function.params){
        declare(param);
        define(param);
    }
    }
    resolve(function.body);
    endScope();
    currentfunction = enclosing;
}

private void beginScope(){
    scopes.push(new HashMap<String,Boolean>());
}

private void endScope(){
    scopes.pop();
}

private void declare(Token name){
    if(scopes.isEmpty()) return;
    Map<String,Boolean> scope = scopes.peek();
    if(scope.containsKey(name.lexeme)){
        Lox.error(name,"Already a Variable with this name in scope");
    }
    scope.put(name.lexeme,false);
}

private void define(Token name){
    if(scopes.isEmpty()) return;
    scopes.peek().put(name.lexeme,true);
}


@Override
public Void visitExpressionStmt(Stmt.Expression stmt){
    resolve(stmt.expression);
    return null;
}

@Override
public Void visitClassStmt(Stmt.Class stmt){
    ClassType enclosing = currentClass;
    currentClass = ClassType.CLASS;
    declare(stmt.name);
    define(stmt.name);  

    if(stmt.superclass !=null && stmt.name.lexeme.equals(stmt.superclass.identifier.lexeme)){
        Lox.error(stmt.superclass.identifier,"A class can't inherit from itself");
    }

    if(stmt.superclass!=null){
        currentClass = ClassType.SUBCLASS;
        resolve(stmt.superclass);
    }
    if(stmt.superclass!=null){
        beginScope();
        scopes.peek().put("super",true);
    }
    beginScope();
    scopes.peek().put("this",true);
    for(Stmt.Function function : stmt.methods){
        FunctionType declaration = FunctionType.METHOD;
        if(function.kind.equals("getter")){
            declaration = FunctionType.GETTER;
        }
        if (function.name.lexeme.equals("init")){
            declaration = FunctionType.INITIALIZER;
        }

        resolveFunction(function.function,declaration);
    }
    endScope();
    if(stmt.superclass!=null){
        endScope();
    }
    currentClass = enclosing;
    return null;
}
@Override
public Void visitThisExpr(Expr.This expr){
    if(currentClass == ClassType.NONE){
        Lox.error(expr.keyword,"Can't use 'this' outside of a class");
        return null;
    }
    resolveLocal(expr,expr.keyword);
    return null;
}
@Override
public Void visitSuperExpr(Expr.Super expr){
    if(currentClass == ClassType.NONE){
        Lox.error(expr.keyword,"Can't use 'super' outside of a class");
    }
    else if(currentClass != ClassType.SUBCLASS){
        Lox.error(expr.keyword,"Can't use 'super' in a class with no superclass");
    }
    resolveLocal(expr,expr.keyword);
    return null;

}
@Override
public Void visitPrintStmt(Stmt.Print stmt){
    resolve(stmt.expression);
    return null;
}

@Override
public Void visitIfStmt(Stmt.If stmt){
    resolve(stmt.condition);
    resolve(stmt.then_branch);
    if(stmt.else_branch!=null){resolve(stmt.else_branch);}
    return null;
}

@Override
public Void visitWhileStmt(Stmt.While stmt){
    resolve(stmt.condition);
    resolve(stmt.statement);
    return null;
}

@Override
public Void visitVarStmt(Stmt.Var stmt){
    declare(stmt.name);
    if(stmt.initializer != null){
        resolve(stmt.initializer);
    }
    define(stmt.name);
    return null;
}

@Override
public Void visitFunctionStmt(Stmt.Function stmt){
    declare(stmt.name);
    define(stmt.name);
    resolveFunction(stmt.function,FunctionType.FUNCTION);
    return null;
}

@Override
public Void visitReturnStmt(Stmt.Return stmt){
    

    if(currentfunction == FunctionType.NONE){
        Lox.error(stmt.keyword,"Can't return from top-level code");
    }

    if(stmt.value != null){
        if(currentfunction == FunctionType.INITIALIZER)
        {
        Lox.error(stmt.keyword,"Can't return a value from an initializer");
        }
        resolve(stmt.value);
    }
    return null;
}

@Override
public Void visitBlockStmt(Stmt.Block stmt){
    beginScope();
    resolve(stmt.statements);
    endScope();
    return null;
}

@Override
public Void visitFunctionExpr(Expr.Function expr){
    resolveFunction(expr,FunctionType.FUNCTION);
    return null;
}

@Override
public Void visitVariableExpr(Expr.Variable expr){
    if(!scopes.isEmpty() && scopes.peek().get(expr.identifier.lexeme)== Boolean.FALSE){
        Lox.error(expr.identifier,"Can't read local variable in its own initializer");
    }
    resolveLocal(expr,expr.identifier);
    return null;
}

@Override
public Void visitAssignExpr(Expr.Assign expr){
    resolve(expr.value);
    resolveLocal(expr,expr.name);
    return null;
}
@Override
public Void visitSetExpr(Expr.Set expr){
    resolve(expr.value);
    resolve(expr.object);
    return null;
}
@Override
public Void visitTernaryExpr(Expr.Ternary expr){
    resolve(expr.condition);
    resolve(expr.if_branch);
    resolve(expr.else_branch);
    return null;
}

@Override
public Void visitBinaryExpr(Expr.Binary expr){
    resolve(expr.left);
    resolve(expr.right);
    return null;
}

@Override
public Void visitCallExpr(Expr.Call expr){
    resolve(expr.callee);
    for(Expr argument:expr.arguments){
        resolve(argument);
    }
    return null;
}
@Override
public Void visitGetExpr(Expr.Get expr){
    resolve(expr.object);
    return null;
}
@Override
public Void visitGroupingExpr(Expr.Grouping expr){
    resolve(expr.expression);
    return null;
}

@Override
public Void visitLiteralExpr(Expr.Literal expr){
    return null;
}

@Override
public Void visitUnaryExpr(Expr.Unary expr){
    resolve(expr.right);
    return null;
}

@Override
public Void visitLogicalExpr(Expr.Logical expr){
    resolve(expr.left);
    resolve(expr.right);
    return null;
}

}