package jlox;
import java.util.List;

class AstPrinter implements Expr.Visitor<String>,Stmt.Visitor<String>
{
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
    public String visitIfStmt(Stmt.If stmt){
        String else_branch = (stmt.else_branch==null)?(""):(" else " + stmt.else_branch.accept(this));
        return "(if " + stmt.condition.accept(this) + " " + stmt.then_branch.accept(this) + else_branch + " )";
    }
    @Override
    public String visitWhileStmt(Stmt.While stmt){
        return "( while " + "(" + stmt.condition.accept(this) + ") "  + stmt.statement.accept(this) + ")";
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
