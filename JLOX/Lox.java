package jlox;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox{
    static boolean hadError = false;
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadRuntimeError = false;

    static boolean showsyntax = false;
    static boolean showtokens = false;
    static boolean allowedExpression;
    static boolean foundExpression = false;
public static void main(String args[]) throws IOException
{
    String scriptPath = null;
    for (String arg : args) {
        if (!arg.startsWith("--")) {
            if (scriptPath == null) {
                scriptPath = arg; // Assume the first non-flag argument is the script path
            } else {
                System.out.println("Multiple script files provided. Only one is allowed.");
                System.exit(64);
            }
        } else {
            switch (arg) {
                case "--showsyntax":
                    showsyntax = true;
                    break;
                case "--showtokens":
                    showtokens = true;
                    break;
                default:
                    System.out.println("Unknown flag: " + arg);
                    System.exit(64);
            }
        }
    }

    if (scriptPath != null) {
        runFile(scriptPath);
    } else {
        runPrompt();
        System.out.println("Usage: jlox [script] [flags]");
        System.out.println("optional flags --showsyntax --showtokens");
    }
}

    private static void runFile(String path ) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes,Charset.defaultCharset()));
        if(hadError) System.exit(65);//exit code 65 indicates that file is not well 
        if (hadRuntimeError) System.exit(70);

    }

    private static void runPrompt() throws IOException {
    
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);
    
    for (;;) { 
      hadError = false;
      System.out.print("> ");
      String line = reader.readLine();
      if(line==null){
        break;
      }
      Scanner scanner = new Scanner(line);
      
      List<Token> tokens = scanner.ScanTokens();
      if(hadError) continue;
      if(showtokens){
        System.out.println("printing tokens");
        for (Token token : tokens) {
          System.out.println(token);
        }
      }

      Parser parser = new Parser(tokens);
      Object syntax = parser.ParseRepl();
      
      if (hadError) continue;
      if(showsyntax){
        System.out.println("printing syntax in reverse polish notation");
        System.out.println(new RpnPrinter().print(syntax));
        System.out.println("printing syntax parenthesized");
        System.out.println(new AstPrinter().print(syntax));
      }
      if (syntax instanceof List) {
     // System.out.println(new RpnPrinter().print((List<Stmt>)syntax));
     // System.out.println(new AstPrinter().print((List<Stmt>)syntax));
      Resolver resolver = new Resolver(interpreter);
      resolver.resolve((List<Stmt>)syntax);
      if(hadError) continue;
      interpreter.interpret((List<Stmt>)syntax);
      } 
      else if (syntax instanceof Expr) 
      {
      //System.out.println(new RpnPrinter().print((Expr)syntax));
      //System.out.println(new AstPrinter().print((Expr)syntax));
      String result = interpreter.interpret((Expr)syntax);
      if (result != null) {
        System.out.println("= " + result);
      }

      }
  }
  
  }
    
    private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.ScanTokens();
   if(showtokens){
      System.out.println("printing tokens");
    for (Token token : tokens) {
      System.out.println(token);
    }
   }

    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();

    if(hadError) return;
    if(showsyntax){
      System.out.println("printing syntax in reverse polish notation");
      System.out.println(new RpnPrinter().print(statements));
      System.out.println("printing syntax parenthesized");
      System.out.println(new AstPrinter().print(statements));
    }
    Resolver resolver = new Resolver(interpreter);
    resolver.resolve(statements);
    if(hadError) return;
    //System.out.println(new RpnPrinter().print(statements));
    //System.out.println(new AstPrinter().print(statements));
    interpreter.interpret(statements);
  }

  static void error(Token token, String message) {
    if(token.type == TokenType.EOF){
      report(token.line,"at the end",message);
    }
    else{
      report(token.line,"at '" + token.lexeme,message);
    }
  }

  static void runtimeError(RunTimeError error) {
    System.err.println(error.getMessage() +
        "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }

  static void error(int line ,String message){
    report(line," ",message);
  }
  private static void report(int line, String where,
                             String message) {
    System.err.println(
        "[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }
}