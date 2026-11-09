package jlox;

import jlox.ast.Assign;
import jlox.ast.Binary;
import jlox.ast.Expr;
import jlox.ast.Grouping;
import jlox.ast.Literal;
import jlox.ast.Unary;
import jlox.ast.Variable;

class AstPrinter {

  String print(Expr expr) {
    if (expr == null)
      return "";

    if (expr instanceof Binary) {
      Binary binary = (Binary) expr;
      // INFIX: (left + right)
      return "(" + print(binary.left) + " " + binary.operator.lexeme + " " + print(binary.right) + ")";
    }

    if (expr instanceof Grouping) {
      Grouping grouping = (Grouping) expr;
      // INFIX: (expression)
      return "(" + print(grouping.expression) + ")";
    }

    if (expr instanceof Literal) {
      Object value = ((Literal) expr).value;
      return value == null ? "nil" : value.toString();
    }

    if (expr instanceof Unary) {
      Unary unary = (Unary) expr;
      // INFIX: (-right)
      return "(" + unary.operator.lexeme + print(unary.right) + ")";
    }

    // --- NEW: Variable and Assignment support! ---
    if (expr instanceof Variable) {
      return ((Variable) expr).name.lexeme;
    }

    if (expr instanceof Assign) {
      Assign assign = (Assign) expr;
      return "(" + assign.name.lexeme + " = " + print(assign.value) + ")";
    }

    throw new IllegalArgumentException("Unknown expression type: " + expr.getClass().getName());
  }
}