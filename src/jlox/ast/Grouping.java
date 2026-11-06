package jlox.ast;

public class Grouping extends Expr {
  public final Expr expression;

  public Grouping(Expr expression) {
    this.expression = expression;
  }

  @Override
  public <R> R accept(ExprVisitor<R> visitor) {
    return visitor.visit(this);
  }
}
