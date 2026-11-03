package jlox;

/**
 * A lexical unit produced by {@link Scanner}.
 *
 * <p>The lexeme preserves the source spelling, while literal holds the
 * interpreted value for literal tokens such as strings and numbers.
 */
class Token {
  /** Syntactic category of this token. */
  final TokenType type;
  /** Exact source text that formed this token. */
  final String lexeme;
  /** Parsed value for a literal token, or {@code null} when not applicable. */
  final Object literal;
  /** One-based source line where this token ends. */
  final int line;

  /** Creates a token with its type, source text, literal value, and line. */
  Token(TokenType type, String lexeme, Object literal, int line) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
  }

  /** Formats the token fields for the scanner's debugging output. */
  public String toString() {
    return type + " " + lexeme + " " + literal;
  }
}
