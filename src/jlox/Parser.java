package jlox;

import java.util.ArrayList;
import java.util.List;

import jlox.ast.Assign;
import jlox.ast.Binary;
import jlox.ast.Expr;
import jlox.ast.Grouping;
import jlox.ast.Literal;
import jlox.ast.Stmt;
import jlox.ast.Unary;
import jlox.ast.Variable;

class Parser {
  private static class ParseError extends RuntimeException {
  }

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  // --- THE IGNITION SWITCH ---
  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();

    while (!isAtEnd()) {
      try {
        statements.add(declaration());
      } catch (ParseError error) {
        synchronize();
      }
    }

    return statements;
  }

  // --- DECLARATIONS & STATEMENTS ---
  private Stmt declaration() {
    if (match(TokenType.VAR))
      return varDeclaration();
    return statement();
  }

  private Stmt varDeclaration() {
    Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

    Expr initializer = null;
    if (match(TokenType.EQUAL)) {
      initializer = expression();
    }

    consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
    return new Stmt.Var(name, initializer);
  }

  private Stmt statement() {
    if (match(TokenType.PRINT))
      return printStatement();

    // NEW: Check for a block!
    if (match(TokenType.LEFT_BRACE))
      return new Stmt.Block(block());

    return expressionStatement();
  }

  private Stmt printStatement() {
    Expr value = expression();
    consume(TokenType.SEMICOLON, "Expect ';' after value.");
    return new Stmt.Print(value);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(TokenType.SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(expr);
  }

  // --- EXPRESSIONS ---
  private Expr expression() {
    return assignment();
  }

  private Expr assignment() {
    Expr expr = equality();

    if (match(TokenType.EQUAL)) {
      Token equals = previous();
      Expr value = assignment(); // Recursive call allows a = b = 5;

      if (expr instanceof Variable) {
        Token name = ((Variable) expr).name;
        return new Assign(name, value);
      }

      error(equals, "Invalid assignment target.");
    }

    return expr;
  }

  private Expr equality() {
    Expr expr = comparison();

    while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {
    Expr expr = term();

    while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while (match(TokenType.MINUS, TokenType.PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while (match(TokenType.SLASH, TokenType.STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    if (match(TokenType.BANG, TokenType.MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Unary(operator, right);
    }

    return primary();
  }

  private Expr primary() {
    if (match(TokenType.FALSE))
      return new Literal(false);
    if (match(TokenType.TRUE))
      return new Literal(true);
    if (match(TokenType.NIL))
      return new Literal(null);

    if (match(TokenType.NUMBER, TokenType.STRING)) {
      return new Literal(previous().literal);
    }

    // Read variable identifiers!
    if (match(TokenType.IDENTIFIER)) {
      return new Variable(previous());
    }

    if (match(TokenType.LEFT_PAREN)) {
      Expr expr = expression();
      consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
      return new Grouping(expr);
    }

    throw error(peek(), "Expect expression.");
  }

  // --- THE POINTER MECHANICS ---
  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }

  private Token consume(TokenType type, String message) {
    if (check(type))
      return advance();
    throw error(peek(), message);
  }

  private boolean check(TokenType type) {
    if (isAtEnd())
      return false;
    return peek().type == type;
  }

  private Token advance() {
    if (!isAtEnd())
      current++;
    return previous();
  }

  private boolean isAtEnd() {
    return peek().type == TokenType.EOF;
  }

  private Token peek() {
    return tokens.get(current);
  }

  private Token previous() {
    return tokens.get(current - 1);
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    // Keep parsing declarations until we hit the closing '}' or run out of file
    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
    return statements;
  }

  // --- ERROR HANDLING ---
  private ParseError error(Token token, String message) {
    Lox.error(token, message);
    return new ParseError();
  }

  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == TokenType.SEMICOLON)
        return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }
      advance();
    }
  }
}