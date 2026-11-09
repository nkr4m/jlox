package jlox.ast;

import java.util.List;
import jlox.Token;

public abstract class Stmt {

  // --- NEW: Function Declaration ---
  public static class Function extends Stmt {
    public final Token name;
    public final List<Token> params;
    public final List<Stmt> body;

    public Function(Token name, List<Token> params, List<Stmt> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }
  }

  // --- NEW: Return Statement ---
  public static class Return extends Stmt {
    public final Token keyword;
    public final Expr value;

    public Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
    }
  }

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

  // --- NEW: If Node ---
  public static class If extends Stmt {
    public final Expr condition;
    public final Stmt thenBranch;
    public final Stmt elseBranch;

    public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }
  }

  // --- NEW: While Node ---
  public static class While extends Stmt {
    public final Expr condition;
    public final Stmt body;

    public While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }
  }
}