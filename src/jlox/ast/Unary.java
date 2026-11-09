package jlox.ast;

import jlox.Token;

public class Unary extends Expr {
  public final Token operator;
  public final Expr right;

  public Unary(Token operator, Expr right) {
    this.operator = operator;
    this.right = right;
  }
}
