package jlox.ast;

public class Literal extends Expr {
  public final Object value;

  public Literal(Object value) {
    this.value = value;
  }
}
