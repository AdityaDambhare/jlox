package jlox;
import java.lang.Math;
class Interpreter implements Expr.Visitor<Object>{
    void interpret(Expr expr){
        try{
            Object value = evaluate(expr);
            System.out.println(stringify(value));//convert object to string
            
        }
        catch(RunTimeError error){
            Lox.runtimeError(error);//print error but don't close the shell
        }
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
            return (double)left / (double)right;

            case POWER:
            checkNumberOperand(expr.operator,left,right);
            return Math.pow((double)left , (double)right);

            case PLUS:
            if(left instanceof Double && right instanceof Double ){
                return (double) left + (double) right;
            }
            else if(left instanceof String && right instanceof String){
                return (String) left + (String) right;
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