package jlox;

class RuntimeError extends RuntimeException {
  final Token operator;

  RuntimeError(Token operator, String message) {
    super(message);
    this.operator = operator;
  }
}