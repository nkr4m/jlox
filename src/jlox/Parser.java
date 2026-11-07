package jlox;

import java.util.ArrayList;
import java.util.List;

import jlox.ast.Binary;
import jlox.ast.Expr;
import jlox.ast.Grouping;
import jlox.ast.Literal;
import jlox.ast.Stmt; // <-- New Import!
import jlox.ast.Unary;

class Parser {
  // A custom exception used to escape deep recursive calls when syntax is
  // invalid.
  private static class ParseError extends RuntimeException {
  }

  private final List<Token> tokens;
  private int current = 0;

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  // --- THE IGNITION SWITCH (Updated for Statements) ---

  List<Stmt> parse() {
    List<Stmt> statements = new ArrayList<>();

    while (!isAtEnd()) {
      try {
        statements.add(statement());
      } catch (ParseError error) {
        // If one statement fails, synchronize and try to parse the next one!
        synchronize();
      }
    }

    return statements;
  }

  // --- STATEMENT GRAMMAR RULES ---

  // rule: statement -> printStmt | exprStmt ;
  private Stmt statement() {
    if (match(TokenType.PRINT))
      return printStatement();

    return expressionStatement();
  }

  // rule: printStmt -> "print" expression ";" ;
  private Stmt printStatement() {
    Expr value = expression();
    consume(TokenType.SEMICOLON, "Expect ';' after value.");
    return new Stmt.Print(value);
  }

  // rule: exprStmt -> expression ";" ;
  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(TokenType.SEMICOLON, "Expect ';' after expression.");
    return new Stmt.Expression(expr);
  }

  // --- EXPRESSION RECURSIVE DESCENT GRAMMAR RULES ---

  // rule: expression -> equality ;
  private Expr expression() {
    return equality();
  }

  // rule: equality -> comparison ( ( "!=" | "==" ) comparison )* ;
  private Expr equality() {
    Expr expr = comparison();

    while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Binary(expr, operator, right);
    }

    return expr;
  }

  // rule: comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
  private Expr comparison() {
    Expr expr = term();

    while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
      Token operator = previous();
      Expr right = term();
      expr = new Binary(expr, operator, right);
    }

    return expr;
  }

  // rule: term -> factor ( ( "-" | "+" ) factor )* ;
  private Expr term() {
    Expr expr = factor();

    while (match(TokenType.MINUS, TokenType.PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Binary(expr, operator, right);
    }

    return expr;
  }

  // rule: factor -> unary ( ( "/" | "*" ) unary )* ;
  private Expr factor() {
    Expr expr = unary();

    while (match(TokenType.SLASH, TokenType.STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Binary(expr, operator, right);
    }

    return expr;
  }

  // rule: unary -> ( "!" | "-" ) unary | primary ;
  private Expr unary() {
    if (match(TokenType.BANG, TokenType.MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Unary(operator, right);
    }

    return primary();
  }

  // rule: primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression
  // ")" ;
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

    if (match(TokenType.LEFT_PAREN)) {
      Expr expr = expression();
      consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
      return new Grouping(expr);
    }

    throw error(peek(), "Expect expression.");
  }

  // --- THE POINTER MECHANICS (Safe Array Navigators) ---

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

  // --- ERROR HANDLING & RECOVERY ---

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