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

    switch (c) {
      case '(':
        this.addToken(TokenType.LEFT_PAREN);
        break;
      case ')':
        this.addToken(TokenType.RIGHT_PAREN);
        break;
      case '{':
        this.addToken(TokenType.LEFT_BRACE);
        break;
      case '}':
        this.addToken(TokenType.RIGHT_BRACE);
        break;
      case ',':
        this.addToken(TokenType.COMMA);
        break;
      case '.':
        this.addToken(TokenType.DOT);
        break;
      case '-':
        this.addToken(TokenType.MINUS);
        break;
      case '+':
        this.addToken(TokenType.PLUS);
        break;
      case ';':
        this.addToken(TokenType.SEMICOLON);
        break;
      case '*':
        this.addToken(TokenType.STAR);
        break;
      case '!':
        this.addToken(this.match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
        break;
      case '=':
        this.addToken(this.match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
        break;
      case '<':
        this.addToken(this.match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
        break;
      case '>':
        this.addToken(this.match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
        break;
      case ' ':
      case '\r':
      case '\t':
        break;
      case '\n':
        this.line++;
        break;
      default:
        Main.error(this.line, "Unexpected character: " + c);
        break;
    }
  }

  private boolean match(char c) {
    if (this.isAtEnd()) return false;
    if (this.source.charAt(this.current) != c) return false;

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