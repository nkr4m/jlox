package jlox;

import jlox.ast.Binary;
import jlox.ast.Expr;
import jlox.ast.ExprVisitor;
import jlox.ast.Grouping;
import jlox.ast.Literal;
import jlox.ast.Unary;

// We implement the interface, telling Java that the visitor returns a String
class AstPrinter implements ExprVisitor<String> {

  // This is the starter button. You hand it a box, and it knocks on the front door.
  String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visit(Expr expr) {
    if (expr instanceof Binary) {
      Binary binary = (Binary) expr;
      return parenthesize(binary.operator.lexeme, binary.left, binary.right);
    }
    if (expr instanceof Grouping) {
      Grouping grouping = (Grouping) expr;
      return parenthesize("group", grouping.expression);
    }
    if (expr instanceof Literal) {
      Object value = ((Literal) expr).value;
      return value == null ? "nil" : value.toString();
    }
    if (expr instanceof Unary) {
      Unary unary = (Unary) expr;
      return parenthesize(unary.operator.lexeme, unary.right);
    }
    throw new IllegalArgumentException("Unknown expression type: " + expr.getClass().getName());
  }

  // --- THE RECURSIVE ENGINE ---
  
  // This helper method takes a name (like "+" or "*") and a list of boxes.
  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    
    // Loop through every box we were handed
    for (Expr expr : exprs) {
      builder.append(" ");
      // THE MAGIC TRICK: The printer knocks on the door of the inner box!
      // This forces the inner box to route the printer to the correct method again.
      builder.append(expr.accept(this)); 
    }
    builder.append(")");

    return builder.toString();
  }
  
  public static void main(String[] args) {
	    // Manually packing the boxes for: -123 * (45.67)
	    Expr expression = new Binary(
	        new Unary(
	            new Token(TokenType.MINUS, "-", null, 1),
	            new Literal(123)
	        ),
	        new Token(TokenType.STAR, "*", null, 1),
	        new Grouping(
	            new Literal(45.67)
	        )
	    );

	    // Turn on the printer!
	    System.out.println(new AstPrinter().print(expression));
	  }
}