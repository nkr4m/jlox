package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Command-line entry point for the Lox interpreter.
 *
 * <p>This stage of the interpreter scans source code and prints its tokens;
 * parsing and evaluation are added by later stages.
 */
public class Lox {
  /** Tracks whether the current script run reported an error. */
  static boolean hadError = false;

  /**
   * Selects script execution or the interactive prompt from the arguments.
   *
   * @param args zero arguments to start the prompt, or one script path
   * @throws IOException if reading the script or prompt input fails
   */
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

  /** Reads and scans a script, using a nonzero exit status if scanning failed. */
  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
    
    // Indicate an error in the exit code.
    if (hadError) System.exit(65);
  }

  /**
   * Reads and scans one line at a time.
   *
   * <p>An error only affects the current line, so the prompt remains usable
   * after reporting it.
   */
  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) { 
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) break;
      run(line);
      
      // Reset the error flag in the interactive loop so it doesn't kill the entire session.
      hadError = false;
    }
  }

  /** Converts source text into tokens and prints them for inspection. */
  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    // For now, just print the tokens to verify the scanner works.
    for (Token token : tokens) {
      System.out.println(token);
    }
  }

  /** Reports an error that is not associated with a particular lexeme. */
  static void error(int line, String message) {
    report(line, "", message);
  }

  /** Prints a diagnostic and records that this run encountered an error. */
  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
}
