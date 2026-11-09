package jlox;

import java.util.ArrayList;
import java.util.List;

import jlox.ast.Assign;
import jlox.ast.Binary;
import jlox.ast.Call;
import jlox.ast.Expr;
import jlox.ast.Grouping;
import jlox.ast.Literal;
import jlox.ast.Logical;
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
    if (match(TokenType.FUN)) return function("function");
    if (match(TokenType.VAR)) return varDeclaration();
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
    if (match(TokenType.FOR))
      return forStatement();
    if (match(TokenType.IF))
      return ifStatement();
    if (match(TokenType.PRINT))
      return printStatement();
    if (match(TokenType.RETURN))
      return returnStatement(); // NEW
    if (match(TokenType.WHILE))
      return whileStatement();
    if (match(TokenType.LEFT_BRACE))
      return new Stmt.Block(block());

    return expressionStatement();
  }

  private Stmt.Function function(String kind) {
    Token name = consume(TokenType.IDENTIFIER, "Expect " + kind + " name.");
    consume(TokenType.LEFT_PAREN, "Expect '(' after " + kind + " name.");

    List<Token> parameters = new ArrayList<>();
    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (parameters.size() >= 255) {
          error(peek(), "Can't have more than 255 parameters.");
        }
        parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
      } while (match(TokenType.COMMA));
    }
    consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");

    consume(TokenType.LEFT_BRACE, "Expect '{' before " + kind + " body.");
    List<Stmt> body = block();

    return new Stmt.Function(name, parameters, body);
  }

  private Stmt returnStatement() {
    Token keyword = previous();
    Expr value = null;

    if (!check(TokenType.SEMICOLON)) {
      value = expression();
    }

    consume(TokenType.SEMICOLON, "Expect ';' after return value.");
    return new Stmt.Return(keyword, value);
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
    Expr expr = or(); // <-- CHANGED: Step down to or()

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

  // NEW: The 'or' logic
  private Expr or() {
    Expr expr = and();

    while (match(TokenType.OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Logical(expr, operator, right);
    }

    return expr;
  }

  // NEW: The 'and' logic
  private Expr and() {
    Expr expr = equality();

    while (match(TokenType.AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Logical(expr, operator, right);
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

    return call();
  }

  private Expr call() {
    Expr expr = primary();

    while (true) {
      // If we see a '(', it means the expression we just parsed is being called
      if (match(TokenType.LEFT_PAREN)) {
        expr = finishCall(expr);
      } else {
        break;
      }
    }

    return expr;
  }

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();

    // Parse arguments until we hit the closing ')'
    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (arguments.size() >= 255) {
          error(peek(), "Can't have more than 255 arguments.");
        }
        arguments.add(expression());
      } while (match(TokenType.COMMA));
    }

    Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");

    return new Call(callee, paren, arguments);
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

  private Stmt ifStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
    Expr condition = expression();
    consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

    Stmt thenBranch = statement();
    Stmt elseBranch = null;

    // If we see an 'else', aggressively bind it to this 'if'
    if (match(TokenType.ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private Stmt whileStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
    Expr condition = expression();
    consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");

    Stmt body = statement();

    return new Stmt.While(condition, body);
  }

  private Stmt forStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");

    // 1. Grab the initializer (var i = 0;)
    Stmt initializer;
    if (match(TokenType.SEMICOLON)) {
      initializer = null;
    } else if (match(TokenType.VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    // 2. Grab the condition (i < 10;)
    Expr condition = null;
    if (!check(TokenType.SEMICOLON)) {
      condition = expression();
    }
    consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

    // 3. Grab the increment (i = i + 1)
    Expr increment = null;
    if (!check(TokenType.RIGHT_PAREN)) {
      increment = expression();
    }
    consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");

    // 4. Grab the body statement
    Stmt body = statement();

    // --- DESUGARING (Translating to While) ---

    // A. If there is an increment, append it to the end of the body block
    if (increment != null) {
      body = new Stmt.Block(
          java.util.Arrays.asList(body, new Stmt.Expression(increment)));
    }

    // B. Wrap the body in a standard While loop (defaulting to 'true' if no
    // condition)
    if (condition == null)
      condition = new Literal(true);
    body = new Stmt.While(condition, body);

    // C. If there is an initializer, wrap everything in an outer block so the
    // initializer runs once first
    if (initializer != null) {
      body = new Stmt.Block(java.util.Arrays.asList(initializer, body));
    }

    return body; // The Interpreter will just see a Block containing a While loop!
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