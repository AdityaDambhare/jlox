package jlox;
import java.util.List;

class AstPrinter implements Expr.Visitor<String>,Stmt.Visitor<String>
{   private  enum FunctionType{
        NONE,
        METHOD,
        FUNCTION
    };
    private FunctionType currentFunction = FunctionType.NONE;

    public String print(Object object){
        if(object instanceof Expr){
            return print((Expr) object);
        }
        if(object instanceof List){
            return print((List<Stmt>) object);
        }
        return object.toString();
    }

    public String print(Expr expr){
        return expr.accept(this);
    }

    public String print(List<Stmt> statements){
        StringBuilder builder = new StringBuilder();
        for(Stmt statement:statements){
            builder.append(statement.accept(this) + "\n");
        }
        return builder.toString();
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
        FunctionType last = currentFunction;
        if(currentFunction!=FunctionType.METHOD){
            currentFunction = FunctionType.FUNCTION;
            Function  = "fun "+ Function;
        }
        Function +=  stmt.function.accept(this);
        currentFunction = last;
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
        Function += "{\n"+print(expr.body)+"\n}";
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
            If += "("+stmt.condition.accept(this) + ") then \n" + stmt.then_branch.accept(this);
        }
        return If;
    }
    @Override
    public String visitWhileStmt(Stmt.While stmt){
        return  "While (" + stmt.condition.accept(this) + ") " + stmt.statement.accept(this);
    }
    @Override
    public String visitExpressionStmt(Stmt.Expression stmt)
    {
        return "( " + stmt.expression.accept(this) + " ;)";
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt){
        return "( print " + stmt.expression.accept(this) + " ;)";
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt){
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for(Stmt statement:stmt.statements){
            builder.append(statement.accept(this));
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt){
        if(stmt.initializer==null){
            return "( var " + stmt.name.lexeme + " ;)";
        }
        return "( = (var "+ stmt.name.lexeme + ") " + stmt.initializer.accept(this) + " ;)";
    }
    @Override
    public String visitLogicalExpr(Expr.Logical expr){
        return "( " + expr.operator.lexeme + " " + expr.left.accept(this) + " " + expr.right.accept(this) + " )";
    }
    @Override
    public String visitAssignExpr(Expr.Assign expr){
        return "( = "+expr.name.lexeme  + expr.value.accept(this) + " ;)";
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr){
        return expr.identifier.lexeme;
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr){
        return Parenthisize(expr.operator.lexeme,expr.left,expr.right);
    }

    @Override 
    public String visitGroupingExpr(Expr.Grouping expr){
        return Parenthisize("group",expr.expression);
    }

    @Override 
    public String visitLiteralExpr(Expr.Literal expr){
        if(expr.value==null){return "nil";}
        return expr.value.toString();
    }

    @Override 
    public String visitUnaryExpr(Expr.Unary expr){
        return Parenthisize(expr.operator.lexeme,expr.right);
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr){
        return "( ? "+expr.condition.accept(this)+" "+Parenthisize(":",expr.if_branch,expr.else_branch) + " )"; 
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
    private String Parenthisize(String name,Expr... exprs){
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);
        
        for (Expr expr : exprs) 
        {
        builder.append(" ");
        builder.append(expr.accept(this));
        }
        builder.append(")");
        return builder.toString();
    }

/*
public static void main(String[] args) {
    //creating ast 
    Expr expression = new Expr.Binary
        (

         new Expr.Binary
         (
            new Expr.Literal(1233),
            new Token(TokenType.SLASH,"/",null,1),
            new Expr.Literal(44444)
         )
        ,new Token(TokenType.PLUS,"+",null,1)
        ,new Expr.Binary
        (
            new Expr.Literal(123),
            new Token(TokenType.STAR,"*",null,1),
            new Expr.Literal(456)
        )
        
        );
    //printing ast
    System.out.println(new AstPrinter().print(expression));
  }
    */
}
