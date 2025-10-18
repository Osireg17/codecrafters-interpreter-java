
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    static boolean hadError = false;

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
                run(fileContents);
            case "parse" ->
                parse(fileContents);
            case "evaluate" ->
                evaluate(fileContents);
            default -> {
                System.err.println("Unknown command: " + command);
                System.exit(1);
            }
        }

        if (hadError) {
            System.exit(65);
        }
    }

    public static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    public static void parse(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        if (hadError) {
            return;
        }

        AstPrinter printer = new AstPrinter();
        System.out.println(printer.print(expression));
    }

    public static void evaluate(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        if (hadError) {
            return;
        }

        Interpreter interpreter = new Interpreter();
        try {
            Object value = interpreter.interpret(expression);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            runtimeError(error);
        }
    }

    private static String stringify(Object object) {
        if (object == null) {
            return "nil";
        }

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
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
