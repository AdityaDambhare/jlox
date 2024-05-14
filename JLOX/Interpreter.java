package jlox;
import java.lang.Math;
class Interpreter implements Expr.Visitor<Object>{
    public Object interpret(Expr expr){
        return expr.accept(this);
    }
    private Object evaluate(Expr expr){
        return expr.accept(this);
    }
    @Override
    public Object visitLiteralExpr(Expr.Literal literal){
        return literal.value;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr){
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr){
        Object right = evaluate(expr.right);

        switch(expr.operator.type){
            case MINUS: 
                return -(double) right;
            case BANG:
                return !isTruthy(right);
        }
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr){
        Object right = evaluate(expr.right);
        Object left = evaluate(expr.left);
        
        switch(expr.operator.type){

            case MINUS : 
            return (double)left - (double)right;

            case STAR :
            return (double)left * (double)right;
            case SLASH :
            return (double)left / (double)right;

            case POWER:
            return Math.pow((double)left , (double)right);

            case PLUS:
            if(left instanceof Double && right instanceof Double ){
                return (double) left + (double) right;
            }
            else if(left instanceof String && right instanceof String){
                return (String) left + (String) right;
            }
            break;

            case GREATER:
                return (double) left > (double) right;
            case GREATER_EQUAL:
                return (double) left >= (double) right;
            case LESS:
                return (double) left < (double) right;
            case LESS_EQUAL:
                return (double) left <= (double) right;
            
            case BANG_EQUAL: return !isEqual(left, right);
            case EQUAL_EQUAL: return isEqual(left, right);
        }
        return null;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr){
        Object condition = evaluate(expr.condition);
        if(isTruthy(condition)){
            return evaluate(expr.if_branch);
        }
        else{
            return evaluate(expr.else_branch);
        }
    }
    //helper methods

    private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean) return (boolean)object;
    return true;
    }

    private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }

}