package jlox;
import java.util.List;
//prints AST in reverse polish notations
class RpnPrinter implements Expr.Visitor<String>,Stmt.Visitor<String>
{ 
     
    private  enum FunctionType{
        NONE,
        METHOD
    };
    private FunctionType currentFunction = FunctionType.NONE;
    public String print(Expr expr){
        return expr.accept(this);
    }
    public String print(Object object){
        if(object instanceof Expr){
            return print((Expr) object);
        }
        if(object instanceof List){
            return print((List<Stmt>) object);
        }
        return object.toString();
    }
    public String print(List<Stmt> statements){
        String result = "";
        for(Stmt statement:statements){
            result += statement.accept(this) + "\n";
        }
        return result;
    }
    @Override
    public String visitClassStmt(Stmt.Class stmt){
        String Class = "class " + stmt.name.lexeme ;
        FunctionType last = currentFunction;
        currentFunction = FunctionType.METHOD;
        if(stmt.superclass!=null){
            Class += " < " + stmt.superclass.accept(this) + " ";
        }
        Class += "{ \n";
        for(Stmt.Function method:stmt.methods){
            Class += method.accept(this) + "\n";
        }
        currentFunction = last;
        return Class + "\n}";
    }
    @Override
    public String visitFunctionStmt(Stmt.Function stmt){
        String Function = stmt.name.lexeme ;
        if(currentFunction!=FunctionType.METHOD){
            Function  = "fun "+ Function;
        }
        Function +=  stmt.function.accept(this);
        return Function;
    }
    @Override
    public String visitFunctionExpr(Expr.Function expr){
        String Function = "( ";
        if(currentFunction==FunctionType.NONE){
            Function  = "fun "+ Function;
        }
        for(int i = 0; i<expr.params.size();i++){
            Function += expr.params.get(i).lexeme;
            if(i<expr.params.size()-1){
                Function += ", ";
            }
        }
        Function += " ) \n";
        Function += print(expr.body);
        return Function;
    }
    @Override
    public String visitReturnStmt(Stmt.Return stmt){
        if(stmt.value == null){
            return "return ;";
        }
        return "return "+stmt.value.accept(this) + " ;";
    }
    @Override
    public String visitIfStmt(Stmt.If stmt){
        String If = "if ";
        if(stmt.else_branch != null){
            If += "("+stmt.condition.accept(this) + ") then \n" + stmt.then_branch.accept(this) + " \nelse \n" + stmt.else_branch.accept(this);
        }
        else{
            If += "("+stmt.condition.accept(this) + " then \n" + stmt.then_branch.accept(this);
        }
        return If;
    }
    @Override
    public String visitWhileStmt(Stmt.While stmt){
        return  "While (" + stmt.condition.accept(this) + ") " + stmt.statement.accept(this);
    }
    @Override
    public String visitVarStmt(Stmt.Var stmt){
        if(stmt.initializer == null){
            return "Var " + stmt.name.lexeme + " ;"; 
        }
        return   "Var "+stmt.name.lexeme + " = " + stmt.initializer.accept(this) + " ;";    
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt){
        return stmt.expression.accept(this) + " ;";
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt){
        return "print "+stmt.expression.accept(this) + "  ;";
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt){
        String block = "{\n";
        block += print(stmt.statements);
        return block + "\n}";
    }
    @Override
    public String visitLogicalExpr(Expr.Logical expr){
        return expr.left.accept(this) + " " + expr.right.accept(this) + " " + expr.operator.lexeme+" ";
    }
    @Override
    public String visitVariableExpr(Expr.Variable expr){
        return expr.identifier.lexeme;
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr){
        return  expr.value.accept(this)+ " " + expr.name.lexeme + " = ";
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr){
        return expr.left.accept(this) + " " + expr.right.accept(this) + " " + expr.operator.lexeme +" ";
        //just print first two expressions then print the operator hehe
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr){
        return expr.expression.accept(this);
        //print the expression
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr){
        if(expr.value == null){return "nil";}
        if(expr.value instanceof String){
            return "\"" + (String) expr.value + "\"";
        }
        return expr.value.toString();
        //print literal value
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr){
        if(expr.operator.type == TokenType.MINUS){return expr.right.accept(this) + " !";}
        //in case of negation print ! after expression .
        return expr.right.accept(this) + expr.operator.lexeme;
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr){
        return expr.condition.accept(this) + " ? " + expr.if_branch.accept(this) + " "+expr.else_branch.accept(this) + " : ";
    }
    @Override
    public String visitThisExpr(Expr.This expr){
        return "this";
    }
    @Override
    public String visitSuperExpr(Expr.Super expr){
        return "super." + expr.method.lexeme;
    }
    @Override 
    public String visitGetExpr(Expr.Get expr){
        return expr.object.accept(this) + "." + expr.name.lexeme;
    }
    @Override
    public String visitSetExpr(Expr.Set expr){
        return expr.object.accept(this) + "." + expr.name.lexeme + " = " + expr.value.accept(this);
    }
    @Override
    public String visitCallExpr(Expr.Call expr){
        String call = expr.callee.accept(this) + " ( ";
        for(Expr argument:expr.arguments){
            call += argument.accept(this) + " ";
        }
        return call + " )";
    }
}