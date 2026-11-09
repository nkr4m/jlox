package jlox;

import java.util.List;

public interface LoxCallable {
    // Returns the number of arguments the function expects
    int arity();

    // The actual execution payload
    Object call(Interpreter interpreter, List<Object> arguments);
}