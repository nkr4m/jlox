package jlox;

import java.util.List;

import jlox.ast.Assign;
import jlox.ast.Binary;
import jlox.ast.Expr;
import jlox.ast.Grouping;
import jlox.ast.Literal;
import jlox.ast.Stmt;
import jlox.ast.Unary;
import jlox.ast.Variable;

class Interpreter {

  // --- THE GLOBAL MEMORY ---
  private Environment environment = new Environment();

  // --- THE PUBLIC API ---
  void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
    }
  }

  // --- STATEMENT EXECUTOR (No Visitor Pattern) ---
  private void execute(Stmt stmt) {
    if (stmt instanceof Stmt.Print) {
      // ... existing code
      return;
    }
    if (stmt instanceof Stmt.Var) {
      // ... existing code
      return;
    }
    if (stmt instanceof Stmt.Expression) {
      // ... existing code
      return;
    }

    // NEW: Route the Block node!
    if (stmt instanceof Stmt.Block) {
      // Create a brand new local Environment, passing the current global/parent one
      // into the constructor
      executeBlock(((Stmt.Block) stmt).statements, new Environment(this.environment));
      return;
    }
  }

  void executeBlock(List<Stmt> statements, Environment environment) {
    // 1. Save a pointer to the current (outer) environment
    Environment previous = this.environment;

    try {
      // 2. Swap the active environment to the new local one
      this.environment = environment;

      // 3. Execute all statements inside the block
      for (Stmt statement : statements) {
        execute(statement);
      }
    } finally {
      // 4. Restore the outer environment when the block ends
      this.environment = previous;
    }
  }

  // --- THE CORE EVALUATOR (No Visitor Pattern) ---
  Object evaluate(Expr expr) {

    // 1. LITERALS
    if (expr instanceof Literal) {
      return ((Literal) expr).value;
    }

    // 2. GROUPING
    if (expr instanceof Grouping) {
      return evaluate(((Grouping) expr).expression);
    }

    // 3. VARIABLE ACCESS (print a;)
    if (expr instanceof Variable) {
      return environment.get(((Variable) expr).name);
    }

    // 4. VARIABLE ASSIGNMENT (a = 2;)
    if (expr instanceof Assign) {
      Assign assignExpr = (Assign) expr;
      Object value = evaluate(assignExpr.value);
      environment.assign(assignExpr.name, value);
      return value;
    }

    // 5. UNARY
    if (expr instanceof Unary) {
      Unary unary = (Unary) expr;
      Object right = evaluate(unary.right);

      switch (unary.operator.type) {
        case BANG:
          return !isTruthy(right);
        case MINUS:
          checkNumberOperand(unary.operator, right);
          return -(double) right;
      }
    }

    // 6. BINARY
    if (expr instanceof Binary) {
      Binary binary = (Binary) expr;
      Object left = evaluate(binary.left);
      Object right = evaluate(binary.right);

      switch (binary.operator.type) {
        case MINUS:
          checkNumberOperands(binary.operator, left, right);
          return (double) left - (double) right;
        case SLASH:
          checkNumberOperands(binary.operator, left, right);
          return (double) left / (double) right;
        case STAR:
          checkNumberOperands(binary.operator, left, right);
          return (double) left * (double) right;
        case PLUS:
          if (left instanceof Double && right instanceof Double) {
            return (double) left + (double) right;
          }
          if (left instanceof String && right instanceof String) {
            return (String) left + (String) right;
          }
          throw new RuntimeError(binary.operator, "Operands must be two numbers or two strings.");
        case GREATER:
          checkNumberOperands(binary.operator, left, right);
          return (double) left > (double) right;
        case GREATER_EQUAL:
          checkNumberOperands(binary.operator, left, right);
          return (double) left >= (double) right;
        case LESS:
          checkNumberOperands(binary.operator, left, right);
          return (double) left < (double) right;
        case LESS_EQUAL:
          checkNumberOperands(binary.operator, left, right);
          return (double) left <= (double) right;
        case BANG_EQUAL:
          return !isEqual(left, right);
        case EQUAL_EQUAL:
          return isEqual(left, right);
      }
    }

    return null;
  }

  // --- TYPE CHECKERS (Runtime Error Guards) ---
  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double)
      return;
    throw new RuntimeError(operator, "Operand must be a number.");
  }

  private void checkNumberOperands(Token operator, Object left, Object right) {
    if (left instanceof Double && right instanceof Double)
      return;
    throw new RuntimeError(operator, "Operands must be numbers.");
  }

  // --- LOX SEMANTICS RULES ---
  private boolean isTruthy(Object object) {
    if (object == null)
      return false;
    if (object instanceof Boolean)
      return (boolean) object;
    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null)
      return true;
    if (a == null)
      return false;
    return a.equals(b);
  }

  // --- OUTPUT FORMATTING ---
  private String stringify(Object object) {
    if (object == null)
      return "nil";
    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }
    return object.toString();
  }
}