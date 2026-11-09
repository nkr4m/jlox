package jlox.ast;

import jlox.Token;

//--- NEW: Logical Node ---
public class Logical extends Expr {
  public final Expr left;
  public final Token operator;
  public final Expr right;

  public Logical(Expr left, Token operator, Expr right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }
}
