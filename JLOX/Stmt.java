package jlox;

import java.util.List;

abstract class Stmt {
 interface Visitor<R> {
 R visitIfStmt(If stmt);
 R visitReturnStmt(Return stmt);
 R visitFunctionStmt(Function stmt);
 R visitWhileStmt(While stmt);
 R visitExpressionStmt(Expression stmt);
 R visitPrintStmt(Print stmt);
 R visitVarStmt(Var stmt);
 R visitBlockStmt(Block stmt);
 R visitClassStmt(Class stmt);
}
static class If extends Stmt{
    If(Expr condition, Stmt then_branch, Stmt else_branch) {
      this.condition = condition;
      this.then_branch = then_branch;
      this.else_branch = else_branch;
 }
    final Expr condition;
    final Stmt then_branch;
    final Stmt else_branch;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }
}
static class Return extends Stmt{
    Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
 }
    final Token keyword;
    final Expr value;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }
}
static class Function extends Stmt{
    Function(Token name, Expr.Function function, String kind) {
      this.name = name;
      this.function = function;
      this.kind = kind;
 }
    final Token name;
    final Expr.Function function;
    final String kind;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }
}
static class While extends Stmt{
    While(Expr condition, Stmt statement) {
      this.condition = condition;
      this.statement = statement;
 }
    final Expr condition;
    final Stmt statement;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }
}
static class Expression extends Stmt{
    Expression(Expr expression) {
      this.expression = expression;
 }
    final Expr expression;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
}
static class Print extends Stmt{
    Print(Expr expression) {
      this.expression = expression;
 }
    final Expr expression;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }
}
static class Var extends Stmt{
    Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
 }
    final Token name;
    final Expr initializer;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }
}
static class Block extends Stmt{
    Block(List<Stmt> statements) {
      this.statements = statements;
 }
    final List<Stmt> statements;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }
}
static class Class extends Stmt{
    Class(List<Stmt.Function> methods, Expr.Variable superclass , Token name) {
      this.methods = methods;
      this.superclass = superclass;
      this.name = name;
 }
    final List<Stmt.Function> methods;
    final Expr.Variable superclass ;
    final Token name;

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }
}

 abstract <R> R accept(Visitor<R> visitor);
}
