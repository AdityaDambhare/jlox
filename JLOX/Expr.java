package jlox;

import java.util.List;

abstract class Expr {
 interface Visitor<R> {
 R visitBinaryExpr(Binary expr);
 R visitGroupingExpr(Grouping expr);
 R visitLiteralExpr(Literal expr);
 R visitUnaryExpr(Unary expr);
 R visitTernaryExpr(Ternary expr);
 R visitVariableExpr(Variable expr);
 R visitAssignExpr(Assign expr);
}
static class Binary extends Expr{
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
 }
    final Expr left;
    final Token operator;
    final Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }
}
static class Grouping extends Expr{
    Grouping(Expr expression) {
      this.expression = expression;
 }
    final Expr expression;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }
}
static class Literal extends Expr{
    Literal(Object value) {
      this.value = value;
 }
    final Object value;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }
}
static class Unary extends Expr{
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
 }
    final Token operator;
    final Expr right;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }
}
static class Ternary extends Expr{
    Ternary(Expr condition, Expr if_branch, Expr else_branch) {
      this.condition = condition;
      this.if_branch = if_branch;
      this.else_branch = else_branch;
 }
    final Expr condition;
    final Expr if_branch;
    final Expr else_branch;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTernaryExpr(this);
    }
}
static class Variable extends Expr{
    Variable(Token identifier) {
      this.identifier = identifier;
 }
    final Token identifier;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }
}
static class Assign extends Expr{
    Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
 }
    final Token name;
    final Expr value;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }
}

 abstract <R> R accept(Visitor<R> visitor);
}
