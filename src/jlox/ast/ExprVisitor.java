package jlox.ast;

public interface ExprVisitor<R> {
  R visit(Expr expr);
}
