package jlox;
//prints AST in reverse polish notations
class RpnPrinter implements Expr.Visitor<String>
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
        return expr.value.toString();
        //print literal value
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr){
        if(expr.operator.type == TokenType.MINUS){return expr.right.accept(this) + "!";}
        //in case of negation print ! after expression .
        return expr.right.accept(this) + expr.operator.lexeme;
    }

}