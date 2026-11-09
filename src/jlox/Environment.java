package jlox;

import java.util.HashMap;
import java.util.Map;

class Environment {
  // NEW: The pointer to the parent scope (null if this is the global scope)
  final Environment enclosing;
  
  private final Map<String, Object> values = new HashMap<>();

  // NEW: Constructor for the Global scope
  Environment() {
    enclosing = null;
  }

  // NEW: Constructor for a Local scope inside a block
  Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  void define(String name, Object value) {
    values.put(name, value); // Always defines in the current local scope
  }

  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }

    // NEW: If not found here, travel up the chain to the parent
    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }

    // NEW: If not found here, travel up the chain to the parent
    if (enclosing != null) return enclosing.get(name);

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }
}