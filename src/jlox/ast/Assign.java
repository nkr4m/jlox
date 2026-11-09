package jlox.ast;

import jlox.Token;

public class Assign extends Expr {
  public final Token name;
  public final Expr value;

  public Assign(Token name, Expr value) {
    this.name = name;
    this.value = value;
  }
}