package jlox;

import jlox.ast.Binary;
import jlox.ast.Expr;
import jlox.ast.ExprVisitor;
import jlox.ast.Grouping;
import jlox.ast.Literal;
import jlox.ast.Unary;

class AstPrinter implements ExprVisitor<String> {

  String print(Expr expr) {
    if (expr == null) return "";
    return expr.accept(this);
  }

  @Override
  public String visit(Expr expr) {
    if (expr instanceof Binary) {
      Binary binary = (Binary) expr;
      // INFIX: (left + right)
      return "(" + binary.left.accept(this) + " " + binary.operator.lexeme + " " + binary.right.accept(this) + ")";
    }
    
    if (expr instanceof Grouping) {
      Grouping grouping = (Grouping) expr;
      // INFIX: (expression)
      return "(" + grouping.expression.accept(this) + ")";
    }
    
    if (expr instanceof Literal) {
      Object value = ((Literal) expr).value;
      return value == null ? "nil" : value.toString();
    }
    
    if (expr instanceof Unary) {
      Unary unary = (Unary) expr;
      // INFIX: (-right)
      return "(" + unary.operator.lexeme + unary.right.accept(this) + ")";
    }
    
    throw new IllegalArgumentException("Unknown expression type: " + expr.getClass().getName());
  }
}