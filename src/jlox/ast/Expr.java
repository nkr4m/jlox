package jlox.ast;

public abstract class Expr {
  public abstract <R> R accept(ExprVisitor<R> visitor);
}