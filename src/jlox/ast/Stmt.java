package jlox.ast;

public abstract class Stmt {

  // A standalone expression followed by a semicolon
  public static class Expression extends Stmt {
    public final Expr expression;

    public Expression(Expr expression) {
      this.expression = expression;
    }
  }

  // A print keyword, followed by an expression, followed by a semicolon
  public static class Print extends Stmt {
    public final Expr expression;

    public Print(Expr expression) {
      this.expression = expression;
    }
  }
}