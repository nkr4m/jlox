package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import jlox.ast.Expr;

/**
 * Command-line entry point for the Lox interpreter.
 */
public class Lox {
  // --- THE GLOBAL INTERPRETER INSTANCE ---
  private static final Interpreter interpreter = new Interpreter();
  
  static boolean hadError = false;
  static boolean hadRuntimeError = false; // Tracks if the code crashed while running

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64); 
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
    
    // Syntax error exit code
    if (hadError) System.exit(65);
    // Runtime error exit code
    if (hadRuntimeError) System.exit(70);
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) { 
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) break;
      run(line);
      
      // Reset BOTH error flags in the interactive loop so it doesn't kill the session.
      hadError = false;
      hadRuntimeError = false;
    }
  }

  private static void run(String source) {
	    // 1. SCANNER: Text -> Tokens
	    Scanner scanner = new Scanner(source);
	    List<Token> tokens = scanner.scanTokens();
	    
	    System.out.println("--- TOKENS ---");
	    for(Token t : tokens) {
	      System.out.println(t);
	    }

	    // 2. PARSER: Tokens -> AST
	    Parser parser = new Parser(tokens);
	    Expr expression = parser.parse();

	    if (hadError) return;

	    System.out.println("\n--- AST TREE ---");
	    // (Assuming you still have the AstPrinter class from Chapter 5)
	    System.out.println(new AstPrinter().print(expression));

	    // 3. INTERPRETER: AST -> Final Value
	    System.out.println("\n--- RESULT ---");
	    interpreter.interpret(expression);
	  }

  // --- ERROR HANDLING ---

  // Used by the Scanner
  static void error(int line, String message) {
    report(line, "", message);
  }

  // Used by the Parser
  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }

  // Used by the Interpreter
  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() + "\n[line " + error.operator.line + "]");
    hadRuntimeError = true;
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
}