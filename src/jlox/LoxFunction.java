package jlox;

import java.util.List;
import jlox.ast.Stmt;

class LoxFunction implements LoxCallable {
  private final Stmt.Function declaration;
  private final Environment closure; // The memory bank captured when the function was born

  LoxFunction(Stmt.Function declaration, Environment closure) {
    this.closure = closure;
    this.declaration = declaration;
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    // Create a new local environment based on the captured closure
    Environment environment = new Environment(closure);
    
    // Bind the passed arguments to the parameter names
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme, arguments.get(i));
    }

    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue) {
      // We caught a return statement! Unwind and return the value.
      return returnValue.value;
    }

    // Default to returning nil if there is no explicit return statement
    return null;
  }

  @Override
  public String toString() {
    return "<fn " + declaration.name.lexeme + ">";
  }
}