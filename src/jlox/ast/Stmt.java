package jlox.ast;

import java.util.List;
import jlox.Token;

public abstract class Stmt {

  // --- NEW: Block Node ---
  public static class Block extends Stmt {
    public final List<Stmt> statements;

    public Block(List<Stmt> statements) {
      this.statements = statements;
    }
  }

  // ... keep all your existing Stmt classes (Expression, Print, Var) down here
  public static class Expression extends Stmt {
    public final Expr expression;

    public Expression(Expr expression) {
      this.expression = expression;
    }
  }

  public static class Print extends Stmt {
    public final Expr expression;

    public Print(Expr expression) {
      this.expression = expression;
    }
  }

  public static class Var extends Stmt {
    public final Token name;
    public final Expr initializer;

    public Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }
  }
}