package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts Lox source text into a sequence of tokens.
 *
 * <p>The scanner advances through the source once. {@code start} marks the
 * beginning of the token being read, while {@code current} marks the next
 * character to consume.
 */
class Scanner {
  /** Source text being scanned. */
  private final String source;
  /** Tokens emitted so far, in source order. */
  private final List<Token> tokens = new ArrayList<>();
  /** Start offset of the token currently being scanned. */
  private int start = 0;
  /** Offset of the next source character to consume. */
  private int current = 0;
  /** One-based line number of the next source character. */
  private int line = 1;

  /** Maps reserved words to their token types; other words are identifiers. */
  private static final Map<String, TokenType> keywords;

  /** Initializes the language's reserved-word lookup table. */
  static {
    keywords = new HashMap<>();
    keywords.put("and", TokenType.AND);
    keywords.put("class", TokenType.CLASS);
    keywords.put("else", TokenType.ELSE);
    keywords.put("false", TokenType.FALSE);
    keywords.put("for", TokenType.FOR);
    keywords.put("fun", TokenType.FUN);
    keywords.put("if", TokenType.IF);
    keywords.put("nil", TokenType.NIL);
    keywords.put("or", TokenType.OR);
    keywords.put("print", TokenType.PRINT);
    keywords.put("return", TokenType.RETURN);
    keywords.put("super", TokenType.SUPER);
    keywords.put("this", TokenType.THIS);
    keywords.put("true", TokenType.TRUE);
    keywords.put("var", TokenType.VAR);
    keywords.put("while", TokenType.WHILE);
  }

  /** Creates a scanner for the given source text. */
  Scanner(String source) {
    this.source = source;
  }

  /**
   * Scans the complete source and appends an end-of-file token.
   *
   * @return all tokens, including the final {@link TokenType#EOF} token
   */
  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

  /** Returns whether the cursor has consumed every source character. */
  private boolean isAtEnd() {
    return current >= source.length();
  }

  /** Consumes one character and dispatches based on its lexical category. */
  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(':
        addToken(TokenType.LEFT_PAREN);
        break;
      case ')':
        addToken(TokenType.RIGHT_PAREN);
        break;
      case '{':
        addToken(TokenType.LEFT_BRACE);
        break;
      case '}':
        addToken(TokenType.RIGHT_BRACE);
        break;
      case ',':
        addToken(TokenType.COMMA);
        break;
      case '.':
        addToken(TokenType.DOT);
        break;
      case '-':
        addToken(TokenType.MINUS);
        break;
      case '+':
        addToken(TokenType.PLUS);
        break;
      case ';':
        addToken(TokenType.SEMICOLON);
        break;
      case '*':
        addToken(TokenType.STAR);
        break;
      // add multi-character operators and literals
      case '!':
        addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
        break;
      case '=':
        addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
        break;
      case '<':
        addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
        break;
      case '>':
        addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
        break;
      case '/':
        if (match('/')) {
          // Line comments are discarded; the newline is handled on the next scan.
          while (peek() != '\n' && !isAtEnd())
            advance();
        } else {
          addToken(TokenType.SLASH);
        }
        break;

      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;

      case '\n':
        line++;
        break;
      case '"':
        string();
        break;
      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          System.err.println(line + ": Unexpected character.");
        }
        break;

    }
  }

  /** Consumes and returns the character at the cursor. */
  private char advance() {
    return source.charAt(current++);
  }

  /** Emits a token with no associated literal value. */
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  /** Emits a token spanning the current lexeme and optional literal value. */
  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  /** Consumes the next character only when it matches the expected character. */
  private boolean match(char expected) {
    if (isAtEnd())
    return false;
    if (source.charAt(current) != expected)
      return false;

    current++;
    return true;
  }

  /** Peeks at the next character without advancing the cursor. */
  private char peek() {
    if (isAtEnd())
      return '\0';
    return source.charAt(current);
  }

  /**
   * Scans a quoted string, preserving its contents as the literal value.
   *
   * <p>Newlines inside a string advance the line counter. An unterminated
   * string is reported and does not produce a token.
   */
  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n')
        line++;
      advance();
    }

    if (isAtEnd()) {
      System.err.println(line + ": Unterminated string.");
      return;
    }

    // Include the closing quote in the lexeme, but not in the literal value.
    advance();

    String value = source.substring(start + 1, current - 1);
    addToken(TokenType.STRING, value);
  }

  /** Returns whether a character is an ASCII decimal digit. */
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  /** Scans an integer or decimal number; a decimal point needs a following digit. */
  private void number() {
    while (isDigit(peek()))
      advance();

    // Avoid consuming a dot that belongs to a later token (for example, a property access).
    if (peek() == '.' && isDigit(peekNext())) {
      advance();

      while (isDigit(peek()))
        advance();
    }

    addToken(TokenType.NUMBER,
        Double.parseDouble(source.substring(start, current)));
  }

  /** Peeks one character beyond {@link #peek()} without advancing the cursor. */
  private char peekNext() {
    if (current + 1 >= source.length())
      return '\0';
    return source.charAt(current + 1);
  }

  /** Returns whether a character may begin an identifier. */
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
        (c >= 'A' && c <= 'Z') ||
        c == '_';
  }

  /** Returns whether a character may appear after the first character of an identifier. */
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  /** Scans an identifier and classifies it as a keyword when reserved. */
  private void identifier() {
    while (isAlphaNumeric(peek()))
      advance();

    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null)
      type = TokenType.IDENTIFIER;
    addToken(type);
  }
}
