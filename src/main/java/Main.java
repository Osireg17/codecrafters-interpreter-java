import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        // You can use print statements as follows for debugging, they'll be visible when running tests.
        System.err.println("Logs from your program will appear here!");

        if (args.length < 2) {
            System.err.println("Usage: ./your_program.sh tokenize <filename>");
            System.exit(1);
        }

        String command = args[0];
        String filename = args[1];

        if (!command.equals("tokenize")) {
            System.err.println("Unknown command: " + command);
            System.exit(1);
        }

        String fileContents = "";
        try {
            fileContents = Files.readString(Path.of(filename));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }

        if (!fileContents.isEmpty()) {
            int line = 1;
            boolean hasError = false;
            for (char c : fileContents.toCharArray()) {
                switch (c) {
                    case '(':
                        System.out.println("LEFT_PAREN ( null");
                        break;
                    case ')':
                        System.out.println("RIGHT_PAREN ) null");
                        break;
                    case '{':
                        System.out.println("LEFT_BRACE { null");
                        break;
                    case '}':
                        System.out.println("RIGHT_BRACE } null");
                        break;
                    case ',':
                        System.out.println("COMMA , null");
                        break;
                    case '.':
                        System.out.println("DOT . null");
                        break;
                    case '-':
                        System.out.println("MINUS - null");
                        break;
                    case '+':
                        System.out.println("PLUS + null");
                        break;
                    case ';':
                        System.out.println("SEMICOLON ; null");
                        break;
                    case '*':
                        System.out.println("STAR * null");
                        break;
                    case ' ':
                    case '\t':
                    case '\r':
                        // Ignore whitespace
                        break;
                    case '\n':
                        line++;
                        break;
                    default:
                        System.err.println("[line " + line + "] Error: Unexpected character: " + c);
                        hasError = true;
                        break;
                }
            }
            System.out.println("EOF  null");
            if (hasError) {
                System.exit(65);
            }
        } else {
            System.out.println("EOF  null");
        }
    }
}
