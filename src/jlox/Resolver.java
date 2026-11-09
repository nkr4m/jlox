package jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import jlox.ast.Assign;
import jlox.ast.Binary;
import jlox.ast.Call;
import jlox.ast.Expr;
import jlox.ast.Grouping;
import jlox.ast.Literal;
import jlox.ast.Logical;
import jlox.ast.Stmt;
import jlox.ast.Unary;
import jlox.ast.Variable;

public class Resolver { // <-- PUBLIC
  private final Interpreter interpreter;

  // The stack of memory scopes for static analysis
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();

  Resolver(Interpreter interpreter) { // <-- PUBLIC
    this.interpreter = interpreter;
  }

  // --- THE PUBLIC API ---
  public void resolve(List<Stmt> statements) { // <-- PUBLIC
    for (Stmt statement : statements) {
      resolve(statement);
    }
  }

  // --- STATEMENT ROUTER ---
  private void resolve(Stmt stmt) {
    if (stmt == null)
      return;

    if (stmt instanceof Stmt.Block) {
      beginScope();
      resolve(((Stmt.Block) stmt).statements);
      endScope();
      return;
    }

    if (stmt instanceof Stmt.Var) {
      Stmt.Var varStmt = (Stmt.Var) stmt;
      declare(varStmt.name);
      if (varStmt.initializer != null) {
        resolve(varStmt.initializer);
      }
      define(varStmt.name);
      return;
    }

    if (stmt instanceof Stmt.Function) {
      Stmt.Function function = (Stmt.Function) stmt;
      declare(function.name);
      define(function.name);
      resolveFunction(function);
      return;
    }

    if (stmt instanceof Stmt.Expression) {
      resolve(((Stmt.Expression) stmt).expression);
      return;
    }

    if (stmt instanceof Stmt.If) {
      Stmt.If ifStmt = (Stmt.If) stmt;
      resolve(ifStmt.condition);
      resolve(ifStmt.thenBranch);
      if (ifStmt.elseBranch != null)
        resolve(ifStmt.elseBranch);
      return;
    }

    if (stmt instanceof Stmt.Print) {
      resolve(((Stmt.Print) stmt).expression);
      return;
    }

    if (stmt instanceof Stmt.Return) {
      Stmt.Return retStmt = (Stmt.Return) stmt;
      if (retStmt.value != null) {
        resolve(retStmt.value);
      }
      return;
    }

    if (stmt instanceof Stmt.While) {
      Stmt.While whileStmt = (Stmt.While) stmt;
      resolve(whileStmt.condition);
      resolve(whileStmt.body);
      return;
    }
  }

  // --- EXPRESSION ROUTER ---
  private void resolve(Expr expr) {
    if (expr == null)
      return;

    if (expr instanceof Variable) {
      Variable varExpr = (Variable) expr;
      if (!scopes.isEmpty() && scopes.peek().get(varExpr.name.lexeme) == Boolean.FALSE) {
        Lox.error(varExpr.name, "Can't read local variable in its own initializer.");
      }
      resolveLocal(varExpr, varExpr.name);
      return;
    }

    if (expr instanceof Assign) {
      Assign assignExpr = (Assign) expr;
      resolve(assignExpr.value);
      resolveLocal(assignExpr, assignExpr.name);
      return;
    }

    if (expr instanceof Binary) {
      resolve(((Binary) expr).left);
      resolve(((Binary) expr).right);
      return;
    }

    if (expr instanceof Call) {
      Call call = (Call) expr;
      resolve(call.callee);
      for (Expr argument : call.arguments) {
        resolve(argument);
      }
      return;
    }

    if (expr instanceof Grouping) {
      resolve(((Grouping) expr).expression);
      return;
    }

    if (expr instanceof Literal) {
      return;
    }

    if (expr instanceof Logical) {
      resolve(((Logical) expr).left);
      resolve(((Logical) expr).right);
      return;
    }

    if (expr instanceof Unary) {
      resolve(((Unary) expr).right);
      return;
    }
  }

  // --- HELPER METHODS ---
  private void resolveFunction(Stmt.Function function) {
    beginScope();
    for (Token param : function.params) {
      declare(param);
      define(param);
    }
    resolve(function.body);
    endScope();
  }

  private void beginScope() {
    scopes.push(new HashMap<String, Boolean>());
  }

  private void endScope() {
    scopes.pop();
  }

  private void declare(Token name) {
    if (scopes.isEmpty())
      return;
    Map<String, Boolean> scope = scopes.peek();
    scope.put(name.lexeme, false);
  }

  private void define(Token name) {
    if (scopes.isEmpty())
      return;
    scopes.peek().put(name.lexeme, true);
  }

  private void resolveLocal(Expr expr, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(expr, scopes.size() - 1 - i);
        return;
      }
    }
  }
}