package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

class Interpreter {

  // --- THE GLOBAL MEMORY ---
  public final Environment globals = new Environment();
  private Environment environment = globals;

  // Stores the calculated hop distances from the Resolver
  private final Map<Expr, Integer> locals = new HashMap<>();

  public void resolve(Expr expr, int depth) {
    locals.put(expr, depth);
  }

  // Constructor to inject native functions
  public Interpreter() {
    globals.define("clock", new LoxCallable() {
      @Override
      public int arity() {
        return 0;
      }

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) System.currentTimeMillis() / 1000.0;
      }

      @Override
      public String toString() {
        return "<native fn>";
      }
    });
  }

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

  private void execute(Stmt stmt) {

    // Route Function Declarations
    if (stmt instanceof Stmt.Function) {
      Stmt.Function function = (Stmt.Function) stmt;
      LoxFunction loxFunction = new LoxFunction(function, this.environment);
      environment.define(function.name.lexeme, loxFunction);
      return;
    }

    // Route Return Statements
    if (stmt instanceof Stmt.Return) {
      Object value = null;
      if (((Stmt.Return) stmt).value != null) {
        value = evaluate(((Stmt.Return) stmt).value);
      }
      throw new Return(value);
    }

    if (stmt instanceof Stmt.Block) {
      executeBlock(((Stmt.Block) stmt).statements, new Environment(this.environment));
      return;
    }

    if (stmt instanceof Stmt.If) {
      Stmt.If ifStmt = (Stmt.If) stmt;
      if (isTruthy(evaluate(ifStmt.condition))) {
        execute(ifStmt.thenBranch);
      } else if (ifStmt.elseBranch != null) {
        execute(ifStmt.elseBranch);
      }
      return;
    }

    if (stmt instanceof Stmt.Print) {
      Object value = evaluate(((Stmt.Print) stmt).expression);
      System.out.println(stringify(value));
      return;
    }

    if (stmt instanceof Stmt.Var) {
      Object value = null;
      if (((Stmt.Var) stmt).initializer != null) {
        value = evaluate(((Stmt.Var) stmt).initializer);
      }
      environment.define(((Stmt.Var) stmt).name.lexeme, value);
      return;
    }

    if (stmt instanceof Stmt.Expression) {
      evaluate(((Stmt.Expression) stmt).expression);
      return;
    }

    if (stmt instanceof Stmt.While) {
      Stmt.While whileStmt = (Stmt.While) stmt;
      while (isTruthy(evaluate(whileStmt.condition))) {
        execute(whileStmt.body);
      }
      return;
    }
  }

  void executeBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.environment;
    try {
      this.environment = environment;
      for (Stmt statement : statements) {
        execute(statement);
      }
    } finally {
      this.environment = previous;
    }
  }

  // --- THE CORE EVALUATOR ---
  Object evaluate(Expr expr) {

    // Use the calculated hop distance!
    if (expr instanceof Variable) {
      Variable varExpr = (Variable) expr;
      Integer distance = locals.get(varExpr);
      if (distance != null) {
        return environment.getAt(distance, varExpr.name.lexeme);
      } else {
        return globals.get(varExpr.name);
      }
    }

    // Use the calculated hop distance for assignment!
    if (expr instanceof Assign) {
      Assign assignExpr = (Assign) expr;
      Object value = evaluate(assignExpr.value);

      Integer distance = locals.get(assignExpr);
      if (distance != null) {
        environment.assignAt(distance, assignExpr.name, value);
      } else {
        globals.assign(assignExpr.name, value);
      }
      return value;
    }

    if (expr instanceof Literal) {
      return ((Literal) expr).value;
    }

    if (expr instanceof Grouping) {
      return evaluate(((Grouping) expr).expression);
    }

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

    if (expr instanceof Logical) {
      Logical logical = (Logical) expr;
      Object left = evaluate(logical.left);

      if (logical.operator.type == TokenType.OR) {
        if (isTruthy(left))
          return left;
      } else {
        if (!isTruthy(left))
          return left;
      }

      return evaluate(logical.right);
    }

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

    if (expr instanceof Call) {
      Call call = (Call) expr;
      Object callee = evaluate(call.callee);

      List<Object> arguments = new ArrayList<>();
      for (Expr argument : call.arguments) {
        arguments.add(evaluate(argument));
      }

      if (!(callee instanceof LoxCallable)) {
        throw new RuntimeError(call.paren, "Can only call functions and classes.");
      }

      LoxCallable function = (LoxCallable) callee;

      if (arguments.size() != function.arity()) {
        throw new RuntimeError(call.paren, "Expected " +
            function.arity() + " arguments but got " +
            arguments.size() + ".");
      }

      return function.call(this, arguments);
    }

    return null;
  }

  // --- TYPE CHECKERS ---
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