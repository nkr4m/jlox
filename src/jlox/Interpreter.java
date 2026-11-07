package jlox;

import jlox.ast.Binary;
import jlox.ast.Expr;
import jlox.ast.Grouping;
import jlox.ast.Literal;
import jlox.ast.Unary;

class Interpreter {

  // --- THE PUBLIC API ---

  void interpret(Expr expression) {
    try {
      Object value = evaluate(expression);
      System.out.println(stringify(value));
    } catch (RuntimeError error) {
      Lox.runtimeError(error);
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

    // 3. UNARY
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

    // 4. BINARY
    if (expr instanceof Binary) {
      Binary binary = (Binary) expr;
      Object left = evaluate(binary.left);
      Object right = evaluate(binary.right);

      switch (binary.operator.type) {
        // Math Operations
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

        // Comparisons
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
          
        // Equality
        case BANG_EQUAL: return !isEqual(left, right);
        case EQUAL_EQUAL: return isEqual(left, right);
      }
    }

    // Unreachable if the parser did its job
    return null;
  }

  // --- TYPE CHECKERS (Runtime Error Guards) ---

  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double) return;
    throw new RuntimeError(operator, "Operand must be a number.");
  }

  private void checkNumberOperands(Token operator, Object left, Object right) {
    if (left instanceof Double && right instanceof Double) return;
    throw new RuntimeError(operator, "Operands must be numbers.");
  }

  // --- LOX SEMANTICS RULES ---

  private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean) return (boolean) object;
    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;
    return a.equals(b);
  }

  // --- OUTPUT FORMATTING ---

  private String stringify(Object object) {
    if (object == null) return "nil";

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