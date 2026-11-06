package jlox.ast;

public class Literal extends Expr {
  public final Object value;

  public Literal(Object value) {
    this.value = value;
  }

  @Override
  public <R> R accept(ExprVisitor<R> visitor) {
    return visitor.visit(this);
  }
}
