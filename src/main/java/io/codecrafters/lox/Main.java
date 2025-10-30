package io.codecrafters.lox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: ./your_program.sh <command> <filename>");
            System.exit(1);
        }

        String command = args[0];
        String filename = args[1];

        String fileContents = "";
        try {
            fileContents = Files.readString(Path.of(filename));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }

        switch (command) {
            case "tokenize" ->
                tokenize(fileContents);
            case "parse" ->
                parse(fileContents);
            case "evaluate" ->
                evaluate(fileContents);
            case "run" ->
                run(fileContents);
            default -> {
                System.err.println("Unknown command: " + command);
                System.exit(1);
            }
        }

        if (hadRuntimeError) {
            System.exit(70);
        } else if (hadError) {
            System.exit(65);
        }
    }

    public static void tokenize(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    public static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError) {
            return;
        }

        Interpreter interpreter = new Interpreter();
        try {
            Resolver resolver = new Resolver(interpreter);
            resolver.resolve(statements);

            if (hadError) {
                return;
            }

            interpreter.interpret(statements);
        } catch (RuntimeError error) {
            System.err.println(error.getMessage());
            System.err.println("[line " + error.token.line + "]");
            hadRuntimeError = true;
        }
    }

    public static void parse(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Stmt statement = parser.parseExpressionStatement();

        if (hadError || statement == null) {
            return;
        }

        AstPrinter printer = new AstPrinter();
        Stmt.Expression exprStmt = (Stmt.Expression) statement;
        System.out.println(printer.print(exprStmt.expression));
    }

    public static void evaluate(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Stmt statement = parser.parseExpressionStatement();

        if (hadError || statement == null) {
            return;
        }

        Interpreter interpreter = new Interpreter();
        try {
            Stmt.Expression exprStmt = (Stmt.Expression) statement;
            Object result = interpreter.evaluate(exprStmt.expression);
            System.out.println(interpreter.stringify(result));
        } catch (RuntimeError error) {
            runtimeError(error);
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage());
        System.err.println("[line " + error.token.line + "]");
        System.exit(70);
    }

    private static void report(int line, String where,
            String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
