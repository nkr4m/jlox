package jlox;

/**
 * A lexical unit produced by {@link Scanner}.
 *
 * <p>The lexeme preserves the source spelling, while literal holds the
 * interpreted value for literal tokens such as strings and numbers.
 */
public class Token {
  /** Syntactic category of this token. */
  public final TokenType type;
  /** Exact source text that formed this token. */
  public final String lexeme;
  /** Parsed value for a literal token, or {@code null} when not applicable. */
  public final Object literal;
  /** One-based source line where this token ends. */
  public final int line;

  /** Creates a token with its type, source text, literal value, and line. */
  public Token(TokenType type, String lexeme, Object literal, int line) {
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
