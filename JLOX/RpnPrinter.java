package jlox;
import java.util.List;
//prints AST in reverse polish notations
class RpnPrinter implements Expr.Visitor<String>,Stmt.Visitor<String>
{ 
     /*
    public static void main(String[] args){
        //creating abstract syntax tree from scratch
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
        //printing the tree

        System.out.println(new RpnPrinter().print(expression));
    }
    */

    public String print(Expr expr){
        return expr.accept(this);
    }

    public String print(List<Stmt> statements){
        String result = "";
        for(Stmt statement:statements){
            result += statement.accept(this) + "\n";
        }
        return result;
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt){
        if(stmt.initializer == null){
            return "Var " + stmt.name.lexeme + " ;"; 
        }
        return   stmt.initializer.accept(this) + " " + "(Var "+stmt.name.lexeme + ") = ;";
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt){
        return stmt.expression.accept(this) + " ;";
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt){
        return stmt.expression.accept(this) + " print ;";
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt){
        String block = "{";
        for(Stmt statement:stmt.statements
        ){
            block += statement.accept(this);
        }
        return block + "}";
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
}