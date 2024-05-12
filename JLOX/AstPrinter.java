package jlox;

class AstPrinter implements Expr.Visitor<String>
{
    public String print(Expr expr){
        return expr.accept(this);
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
