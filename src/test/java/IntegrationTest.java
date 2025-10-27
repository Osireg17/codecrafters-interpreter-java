import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IntegrationTest {

    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @BeforeEach
    void setUpStreams() {
        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        originalOut = System.out;
        originalErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        Main.hadError = false;
        Main.hadRuntimeError = false;
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    private Object evaluateExpression(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Stmt statement = parser.parseExpressionStatement();

        if (statement == null) {
            return null;
        }

        Interpreter interpreter = new Interpreter();
        Stmt.Expression exprStmt = (Stmt.Expression) statement;
        return interpreter.evaluate(exprStmt.expression);
    }

    @Test
    void itShouldHandlePrintStatementWithExpression(){
        Main.run(
            """
            print "the expression below is invalid";
            49 + "baz";
            print "this should not be printed";
            """
        );
        String[] lines = outContent.toString().split("\n");
        assertThat(lines[0].trim()).isEqualTo("the expression below is invalid");
        assertThat(errContent.toString()).contains("Operands must be two numbers or two strings.");
        assertThat(errContent.toString()).contains("[line 2]");
        assertThat(lines.length).isEqualTo(1);
    }

    @Test
    void itShouldHandleExpressionStatements(){
        Main.run("""
                 (63 + 37 - 52) > (29 - 63) * 2;
                 print !true;
                 "quz" + "bar" + "baz" == "quzbarbaz";
                 print !true;
                 """);
        String[] lines = outContent.toString().split("\n");
        assertThat(lines[0].trim()).isEqualTo("false");
        assertThat(lines[1].trim()).isEqualTo("false");
        assertThat(lines.length).isEqualTo(2);
    }

    @Test
    void itShouldHandleExpressionStatmentsOperandNumbers(){
        Main.run(
            """
            print "79" + "baz";
            print false * (18 + 84);
            """
        );
        String[] lines = outContent.toString().split("\n");
        assertThat(lines[0].trim()).isEqualTo("79baz");
        assertThat(errContent.toString()).contains("Operands must be numbers.");
        assertThat(errContent.toString()).contains("[line 2]");
    }

    @Test
    void itShouldHandleMultilineStringWithEmptyLineAndNonAscii() {
        Main.run("print false != false;\n\n" +
                "// multi-line strings should be supported\n" +
                "print \"83\n27\n58\n\";\n\n" +
                "print \"There should be an empty line above this.\";\n\n" +
                "print \"(\" + \"\" + \")\";\n\n" +
                "// non-ascii characters should be supported\n" +
                "print \"non-ascii: ॐ\";");
        String[] lines = outContent.toString().split("\n");
        assertThat(lines[0].trim()).isEqualTo("false");
        assertThat(lines[1].trim()).isEqualTo("83");
        assertThat(lines[2].trim()).isEqualTo("27");
        assertThat(lines[3].trim()).isEqualTo("58");
        assertThat(lines[4].trim()).isEqualTo("");
        assertThat(lines[5].trim()).isEqualTo("There should be an empty line above this.");
        assertThat(lines[6].trim()).isEqualTo("()");
        assertThat(lines[7].trim()).isEqualTo("non-ascii: ॐ");
    }

    //Write a test case to handle multiple statements
    @Test
    void itShouldPrintMultipleStatements() {
        Main.run("print \"world\" + \"baz\" + \"bar\"; print 27 - 26; print \"bar\" == \"quz\";");
        String[] lines = outContent.toString().split("\n");
        assertThat(lines[0].trim()).isEqualTo("worldbazbar");
        assertThat(lines[1].trim()).isEqualTo("1");
        assertThat(lines[2].trim()).isEqualTo("false");
    }

    @Test
    void itShouldPrintMultipleArithemticStatements() {
        Main.run("print 81; print 81 + 46; print 81 + 46 + 19;");
        String[] lines = outContent.toString().split("\n");
        assertThat(lines[0].trim()).isEqualTo("81");
        assertThat(lines[1].trim()).isEqualTo("127");
        assertThat(lines[2].trim()).isEqualTo("146");
    }

    @Test
    void itShouldPrintBooleanAndMultilineStringWithEmptyLine() {
        Main.run("print true != true;\n\nprint \"36\n10\n78\n\";\n\nprint \"There should be an empty line above this.\";");
        String[] lines = outContent.toString().split("\n");
        assertThat(lines[0].trim()).isEqualTo("false");
        assertThat(lines[1].trim()).isEqualTo("36");
        assertThat(lines[2].trim()).isEqualTo("10");
        assertThat(lines[3].trim()).isEqualTo("78");
        assertThat(lines[4].trim()).isEqualTo("");
        assertThat(lines[5].trim()).isEqualTo("There should be an empty line above this.");
    }

    @Test
    void itShouldRunPrintStatementWithBoolean() {
        Main.run("print true;");
        assertThat(outContent.toString().trim()).isEqualTo("true");
    }

    @Test
    void itShouldRunStringConcatenation() {
        Main.run("print \"baz\" + \"hello\" + \"world\";");
        assertThat(outContent.toString().trim()).isEqualTo("bazhelloworld");
    }

    @Test
    void itShouldRunArithmeticExpression() {
        Main.run("print (61 * 2 + 58 * 2) / 2;");
        assertThat(outContent.toString().trim()).isEqualTo("119");
    }

    @Test
    void itShouldHandlePrintWithoutExpression() {
        Main.run("print;");
        assertThat(errContent.toString()).contains("[line 1] Error");
        assertThat(errContent.toString()).contains("Expect expression");
        assertThat(Main.hadError).isTrue();
    }

    @Test
    void itShouldThrowRuntimeErrorForRelationalOperatorWithWrongTypes() {
        assertThatThrownBy(() -> evaluateExpression("\"world\" < true"))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operands must be numbers.");
    }

    @Test
    void itShouldThrowRuntimeErrorForMixedTypeRelationalOperator() {
        assertThatThrownBy(() -> evaluateExpression("false <= (63 + 32)"))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operands must be numbers.");
    }

    @Test
    void itShouldThrowRuntimeErrorForNumberVsStringComparison() {
        assertThatThrownBy(() -> evaluateExpression("50 > (\"baz\" + \"world\")"))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operands must be numbers.");
    }

    @Test
    void itShouldThrowRuntimeErrorForBooleanComparison() {
        assertThatThrownBy(() -> evaluateExpression("true >= false"))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operands must be numbers.");
    }

    @Test
    void itShouldThrowRuntimeErrorForMixedTypeAddition() {
        assertThatThrownBy(() -> evaluateExpression("\"hello\" + true"))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operands must be two numbers or two strings.");
    }

    @Test
    void itShouldThrowRuntimeErrorForNumberPlusString() {
        assertThatThrownBy(() -> evaluateExpression("43 + \"world\" + 88"))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operands must be two numbers or two strings.");
    }

    @Test
    void itShouldThrowRuntimeErrorForSubtractionWithBoolean() {
        assertThatThrownBy(() -> evaluateExpression("21 - false"))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operands must be numbers.");
    }

    @Test
    void itShouldThrowRuntimeErrorForBooleanMinusString() {
        assertThatThrownBy(() -> evaluateExpression("true - (\"hello\" + \"bar\")"))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operands must be numbers.");
    }

    @Test
    void itShouldThrowRuntimeErrorForMultiplicationWithBoolean() {
        assertThatThrownBy(() -> evaluateExpression("false * 35"))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operands must be numbers.");
    }

    @Test
    void itShouldThrowRuntimeErrorForStringTimesBoolean() {
        assertThatThrownBy(() -> evaluateExpression("\"world\" * true"))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operands must be numbers.");
    }

    @Test
    void itShouldThrowRuntimeErrorForDivisionWithString() {
        assertThatThrownBy(() -> evaluateExpression("25 / \"hello\""))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operands must be numbers.");
    }

    @Test
    void itShouldThrowRuntimeErrorForBooleanDivision() {
        assertThatThrownBy(() -> evaluateExpression("true / false"))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operands must be numbers.");
    }

    @Test
    void itShouldThrowRuntimeErrorForUnaryMinusWithString() {
        assertThatThrownBy(() -> evaluateExpression("-\"hello\""))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operand must be a number.");
    }

    @Test
    void itShouldThrowRuntimeErrorForUnaryMinusWithBoolean() {
        assertThatThrownBy(() -> evaluateExpression("-true"))
                .isInstanceOf(RuntimeError.class)
                .hasMessageContaining("Operand must be a number.");
    }

    @Test
    void itShouldEvaluateValidArithmetic() {
        Object result = evaluateExpression("2 + 3");
        assertThat(result).isEqualTo(5.0);
    }

    @Test
    void itShouldEvaluateValidComparison() {
        Object result = evaluateExpression("5 > 3");
        assertThat(result).isEqualTo(true);
    }

    @Test
    void itShouldEvaluateValidEquality() {
        Object result = evaluateExpression("true == true");
        assertThat(result).isEqualTo(true);
    }

    @Test
    void itShouldEvaluateValidStringConcatenation() {
        Object result = evaluateExpression("\"hello\" + \"world\"");
        assertThat(result).isEqualTo("helloworld");
    }

    @Test
    void itShouldEvaluateComplexValidExpression() {
        Object result = evaluateExpression("(10 + 20) * 2 / 3");
        assertThat(result).isEqualTo(20.0);
    }

    @Test
    void itShouldEvaluateNegation() {
        Object result = evaluateExpression("!true");
        assertThat(result).isEqualTo(false);
    }

    @Test
    void itShouldEvaluateDoubleNegation() {
        Object result = evaluateExpression("!!false");
        assertThat(result).isEqualTo(false);
    }

    @Test
    void itShouldEvaluateNegativeNumbers() {
        Object result = evaluateExpression("-123");
        assertThat(result).isEqualTo(-123.0);
    }

    @Test
    void itShouldParseAndPrintLiteral() {
        Main.parse("123");
        assertThat(outContent.toString().trim()).isEqualTo("123.0");
    }

    @Test
    void itShouldParseAndPrintBinaryExpression() {
        Main.parse("1 + 2");
        assertThat(outContent.toString().trim()).isEqualTo("(+ 1.0 2.0)");
    }

    @Test
    void itShouldParseAndPrintGrouping() {
        Main.parse("(1 + 2) * 3");
        assertThat(outContent.toString().trim()).isEqualTo("(* (group (+ 1.0 2.0)) 3.0)");
    }

    @Test
    void itShouldParseAndPrintUnary() {
        Main.parse("-123");
        assertThat(outContent.toString().trim()).isEqualTo("(- 123.0)");
    }

    @Test
    void itShouldTokenizeSimpleExpression() {
        Main.tokenize("123");
        String output = outContent.toString();
        assertThat(output).contains("NUMBER 123 123.0");
        assertThat(output).contains("EOF  null");
    }

    @Test
    void itShouldTokenizeOperators() {
        Main.tokenize("+ - * /");
        String output = outContent.toString();
        assertThat(output).contains("PLUS +");
        assertThat(output).contains("MINUS -");
        assertThat(output).contains("STAR *");
        assertThat(output).contains("SLASH /");
    }

    @Test
    void itShouldTokenizeString() {
        Main.tokenize("\"hello\"");
        String output = outContent.toString();
        assertThat(output).contains("STRING \"hello\" hello");
    }

    @Test
    void itShouldTokenizeKeywords() {
        Main.tokenize("print true false nil");
        String output = outContent.toString();
        assertThat(output).contains("PRINT print");
        assertThat(output).contains("TRUE true");
        assertThat(output).contains("FALSE false");
        assertThat(output).contains("NIL nil");
    }

    @Test
    void itShouldRunMultiplePrintStatements() {
        Main.run("print 1; print 2; print 3;");
        String[] lines = outContent.toString().split("\n");
        assertThat(lines[0].trim()).isEqualTo("1");
        assertThat(lines[1].trim()).isEqualTo("2");
        assertThat(lines[2].trim()).isEqualTo("3");
    }

    @Test
    void itShouldRunPrintWithComplexExpression() {
        Main.run("print 2 + 3 * 4;");
        assertThat(outContent.toString().trim()).isEqualTo("14");
    }

    @Test
    void itShouldRunPrintWithComparison() {
        Main.run("print 5 > 3;");
        assertThat(outContent.toString().trim()).isEqualTo("true");
    }

    @Test
    void itShouldRunPrintWithEquality() {
        Main.run("print 1 == 1;");
        assertThat(outContent.toString().trim()).isEqualTo("true");
    }

    @Test
    void itShouldRunPrintWithNegation() {
        Main.run("print !false;");
        assertThat(outContent.toString().trim()).isEqualTo("true");
    }

    @Test
    void itShouldRunPrintWithNil() {
        Main.run("print nil;");
        assertThat(outContent.toString().trim()).isEqualTo("nil");
    }

    @Test
    void itShouldHandleCommentsInRun() {
        Main.run("// This is a comment\nprint 42;");
        assertThat(outContent.toString().trim()).isEqualTo("42");
    }

    @Test
    void itShouldHandleMultilineStringInTokenize() {
        Main.tokenize("\"hello\nworld\"");
        String output = outContent.toString();
        assertThat(output).contains("STRING");
        assertThat(output).contains("hello\nworld");
    }

    @Test
    void itShouldEvaluateNestedGrouping() {
        Object result = evaluateExpression("((1 + 2) * (3 + 4))");
        assertThat(result).isEqualTo(21.0);
    }

    @Test
    void itShouldEvaluateMixedOperations() {
        Object result = evaluateExpression("10 - 5 + 3 * 2");
        assertThat(result).isEqualTo(11.0);
    }

    @Test
    void itShouldEvaluateDivisionWithDecimals() {
        Object result = evaluateExpression("10 / 3");
        assertThat((Double) result).isCloseTo(3.333, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    void itShouldEvaluateInequalityWithStrings() {
        Object result = evaluateExpression("\"hello\" != \"world\"");
        assertThat(result).isEqualTo(true);
    }

    @Test
    void itShouldEvaluateInequalityWithSameStrings() {
        Object result = evaluateExpression("\"hello\" != \"hello\"");
        assertThat(result).isEqualTo(false);
    }

    @Test
    void itShouldEvaluateNilEquality() {
        Object result = evaluateExpression("nil == nil");
        assertThat(result).isEqualTo(true);
    }

    @Test
    void itShouldEvaluateNilInequality() {
        Object result = evaluateExpression("nil != false");
        assertThat(result).isEqualTo(true);
    }

    @Test
    void itShouldHandleParseErrorWithMissingParenthesis() {
        Main.parse("(1 + 2");
        assertThat(errContent.toString()).contains("Error");
        assertThat(errContent.toString()).contains("Expect ')' after expression");
        assertThat(Main.hadError).isTrue();
    }

    @Test
    void itShouldHandleParseErrorWithInvalidExpression() {
        Main.parse("+");
        assertThat(errContent.toString()).contains("Error");
        assertThat(errContent.toString()).contains("Expect expression");
        assertThat(Main.hadError).isTrue();
    }

    @Test
    void itShouldHandleVariableReassignmentAndPrinting() {
        Main.run(
            "var quz;\n" +
            "quz = 1;\n" +
            "print quz;\n" +
            "print quz = 2;\n" +
            "print quz;\n"
        );
        String[] lines = outContent.toString().split("\n");
        assertThat(lines[0].trim()).isEqualTo("1");
        assertThat(lines[1].trim()).isEqualTo("2");
        assertThat(lines[2].trim()).isEqualTo("2");
        assertThat(lines.length).isEqualTo(3);
    }

    @Test
    void itShouldHandleChainedVariableAssignment() {
        Main.run(
            "var hello = 93;\n" +
            "var bar = 93;\n" +
            "bar = hello;\n" +
            "hello = bar;\n" +
            "print hello + bar;\n"
        );
        assertThat(outContent.toString().trim()).isEqualTo("186");
    }

    @Test
    void itShouldHandleComplexChainedAssignment() {
        Main.run(
            "var quz;\n" +
            "var hello;\n" +
            "quz = hello = 16 + 34 * 92;\n" +
            "print quz;\n" +
            "print hello;\n"
        );
        String[] lines = outContent.toString().split("\n");
        assertThat(lines[0].trim()).isEqualTo("3144");
        assertThat(lines[1].trim()).isEqualTo("3144");
        assertThat(lines.length).isEqualTo(2);
    }

    @Test
    void itShouldHandleSingleBlock() {
        Main.run(
            """
            {
                var hello = "baz";
                print hello;
            }
            """
        );
        assertThat(outContent.toString().trim()).isEqualTo("baz");
    }

    @Test
    void itShouldHandleMultipleBlocks() {
        Main.run(
            """
            {
                var world = "before";
                print world;
            }
            {
                var world = "after";
                print world;
            }
            """
        );
        String[] lines = outContent.toString().split("\n");
        assertThat(lines[0].trim()).isEqualTo("before");
        assertThat(lines[1].trim()).isEqualTo("after");
    }

    @Test
    void itShouldHandleNestedBlocks() {
        Main.run(
            """
            {
                var hello = 88;
                {
                    var foo = 88;
                    print foo;
                }
                print hello;
            }
            """
        );
        String[] lines = outContent.toString().split("\n");
        assertThat(lines[0].trim()).isEqualTo("88");
        assertThat(lines[1].trim()).isEqualTo("88");
    }

    @Test
    void itShouldHandleIfStatement() {
        Main.run("if (true) print \"bar\";");
        assertThat(outContent.toString().trim()).isEqualTo("bar");
    }

    @Test
    void itShouldHandleIfStatementWithBlock() {
        Main.run("if (true) { print \"block body\"; }");
        assertThat(outContent.toString().trim()).isEqualTo("block body");
    }

    @Test
    void itShouldHandleIfStatementWithAssignment() {
        Main.run("var a = false; if (a = true) { print (a == true); }");
        assertThat(outContent.toString().trim()).isEqualTo("true");
    }

    @Test
    void itShouldCallClockFunctionAndAddNumber() {
        Main.run("print clock() + 75;");
        String output = outContent.toString().trim();
        double result = Double.parseDouble(output);
        assertThat(result).isGreaterThan(75.0);
    }

    @Test
    void itShouldCallClockFunctionAndDivide() {
        Main.run("print clock() / 1000;");
        String output = outContent.toString().trim();
        double result = Double.parseDouble(output);
        assertThat(result).isGreaterThan(0.0);
    }

    @Test
    void itShouldHandleClockWithVariableAndLogicalOperator() {
        Main.run(
            "var startTime = clock();\n" +
            "var timeoutSeconds = 2;\n" +
            "\n" +
            "if ((clock() >= startTime) and (clock() <= (startTime + timeoutSeconds))) {\n" +
            "  print \"Operation in progress...\";\n" +
            "} else {\n" +
            "  print \"Operation timed out!\";\n" +
            "}"
        );
        assertThat(outContent.toString().trim()).isEqualTo("Operation in progress...");
    }
}
