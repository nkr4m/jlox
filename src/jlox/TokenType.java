package jlox;

/** Lexical categories recognized by the Lox scanner. */
enum TokenType {
  // Single-character punctuation and arithmetic operators.
  LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
  COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

  // Operators whose optional '=' changes the token type.
  BANG, BANG_EQUAL,
  EQUAL, EQUAL_EQUAL,
  GREATER, GREATER_EQUAL,
  LESS, LESS_EQUAL,

  // User-defined names and literal values.
  IDENTIFIER, STRING, NUMBER,

  // Reserved words in the Lox language.
  AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
  PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

  // Sentinel emitted once after the final source token.
  EOF
}