
import java.util.ArrayList;
import java.util.List;

class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!this.isAtEnd()) {
            start = current;
            this.scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));

        return tokens;
    }

    private boolean isAtEnd() {
        return this.current >= this.source.length();
    }

    private void scanToken() {
        char c = this.advance();

        if (this.isDigit(c)) {
            this.number();
            return;
        } else if (this.isAlpha(c)) {
            this.identifier();
            return;
        }

        switch (c) {
            case '(' ->
                this.addToken(TokenType.LEFT_PAREN);
            case ')' ->
                this.addToken(TokenType.RIGHT_PAREN);
            case '{' ->
                this.addToken(TokenType.LEFT_BRACE);
            case '}' ->
                this.addToken(TokenType.RIGHT_BRACE);
            case ',' ->
                this.addToken(TokenType.COMMA);
            case '.' ->
                this.addToken(TokenType.DOT);
            case '-' ->
                this.addToken(TokenType.MINUS);
            case '+' ->
                this.addToken(TokenType.PLUS);
            case ';' ->
                this.addToken(TokenType.SEMICOLON);
            case '*' ->
                this.addToken(TokenType.STAR);
            case '!' ->
                this.addToken(this.match() ? TokenType.BANG_EQUAL : TokenType.BANG);
            case '=' ->
                this.addToken(this.match() ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '<' ->
                this.addToken(this.match() ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '>' ->
                this.addToken(this.match() ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '/' -> {
                if (this.peek() == '/') {
                    while (this.peek() != '\n' && !this.isAtEnd()) {
                        this.advance();
                    }
                } else {
                    this.addToken(TokenType.SLASH);
                }
            }
            case '"' ->
                this.string();
            //Need a case to handle numbers
            case ' ', '\r', '\t' -> {
            }
            case '\n' ->
                this.line++;
            default ->
                Main.error(this.line, "Unexpected character: " + c);
        }
    }

    private void identifier() {
        while (this.AlphaNumeric(this.peek())) {
            this.advance();
        }

        String text = this.source.substring(this.start, this.current);
        TokenType type = switch (text) {
            case "and" ->
                TokenType.AND;
            case "class" ->
                TokenType.CLASS;
            case "else" ->
                TokenType.ELSE;
            case "false" ->
                TokenType.FALSE;
            case "for" ->
                TokenType.FOR;
            case "fun" ->
                TokenType.FUN;
            case "if" ->
                TokenType.IF;
            case "nil" ->
                TokenType.NIL;
            case "or" ->
                TokenType.OR;
            case "print" ->
                TokenType.PRINT;
            case "return" ->
                TokenType.RETURN;
            case "super" ->
                TokenType.SUPER;
            case "this" ->
                TokenType.THIS;
            case "true" ->
                TokenType.TRUE;
            case "var" ->
                TokenType.VAR;
            case "while" ->
                TokenType.WHILE;
            default ->
                TokenType.IDENTIFIER;
        };
        this.addToken(type);
    }

    private boolean AlphaNumeric(char peek) {
        return Character.isAlphabetic(peek) || Character.isDigit(peek) || peek == '_';
    }

    private boolean isAlpha(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_';
    }

    private void number() {
        while (this.isDigit(this.peek())) {
            this.advance();
        }

        if (this.peek() == '.' && this.isDigit(this.peekNext())) {
            this.advance();

            while (this.isDigit(this.peek())) {
                this.advance();
            }
        }

        String numberString = this.source.substring(this.start, this.current);
        this.addToken(TokenType.NUMBER, Double.valueOf(numberString));
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private char peekNext() {
        if (this.current + 1 >= this.source.length()) {
            return '\0';
        }
        return this.source.charAt(this.current + 1);
    }

    private void string() {
        while (this.peek() != '"' && !this.isAtEnd()) {
            if (this.peek() == '\n') {
                this.line++;
            }
            this.advance();
        }

        if (this.isAtEnd()) {
            Main.error(this.line, "Unterminated string.");
            return;
        }
        this.advance();
        String value = this.source.substring(this.start + 1, this.current - 1);
        this.addToken(TokenType.STRING, value);
    }

    private char peek() {
        if (this.isAtEnd()) {
            return '\0';
        }
        return this.source.charAt(this.current);
    }

    private boolean match() {
        if (this.isAtEnd()) {
            return false;
        }
        if (this.source.charAt(this.current) != '=') {
            return false;
        }

        this.current++;
        return true;
    }

    private char advance() {
        return this.source.charAt(this.current++);
    }

    private void addToken(TokenType type) {
        this.addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
