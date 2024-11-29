import myenum.TokenType;
import utils.StringUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static myenum.StringEnum.*;

public class XMLCompilationEngine implements ICompilationEngine {
    private BufferedWriter bw;
    private JackTokenizer jackTokenizer;

    public XMLCompilationEngine(JackTokenizer jackTokenizer, String test) {
        String outputPath = jackTokenizer.getFilePath();
        try {
            outputPath = outputPath.replace(".jack", "T.xml");
            bw = new BufferedWriter(new FileWriter(outputPath));
            write("<tokens>");
            this.jackTokenizer = jackTokenizer;
            jackTokenizer.advance();
            while (jackTokenizer.hasMoreTokens()) {
                eat(jackTokenizer.tokenType());
            }
            write("</tokens>");
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public XMLCompilationEngine(JackTokenizer jackTokenizer) {
        String outputPath = jackTokenizer.getFilePath();
        try {
            outputPath = outputPath.replace(".jack", ".xml");
            bw = new BufferedWriter(new FileWriter(outputPath));
            this.jackTokenizer = jackTokenizer;
            while (jackTokenizer.hasMoreTokens()) {
                jackTokenizer.advance();
                compileClass();
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void compileClass() {
        // Write the opening <class> tag
        write("<class>");

        // Consume the 'class' keyword
        eat("class");

        // Consume the class name (identifier)
        eat(TokenType.IDENTIFIER);

        // Consume the opening '{' symbol
        eat("{");

        // Compile class variable declarations
        while (JackTokenizer.tokenType() == TokenType.KEYWORD && isClassVarDecKeyword(jackTokenizer.getThisToken())) {
            compileClassVarDec();
        }

        // Compile subroutine declarations
        while (JackTokenizer.tokenType() == TokenType.KEYWORD && isSubroutineDecKeyword(jackTokenizer.getThisToken())) {
            compileSubroutine();
        }

        // Consume the closing '}' symbol
        eat("}");

        // Write the closing </class> tag
        write("</class>");
    }

    @Override
    public void compileClassVarDec() {
        // 1. Write the opening <classVarDec> tag
        write("<classVarDec>");

        // 2. Use eat() to consume the keyword (static or field)
        eat(TokenType.KEYWORD);

        // 3. Call compileType() to handle the type
        compileType();

        // 4. Use eat() to consume the variable name (identifier)
        eat(TokenType.IDENTIFIER);

        // 5. Use a while loop to handle multiple variable names separated by commas
        while (jackTokenizer.getThisToken().equals(",")) {
            eat(","); // Consume the comma
            eat(TokenType.IDENTIFIER); // Consume the variable name
        }

        // 6. Use eat() to consume the closing ';' symbol
        eat(";");

        // 7. Write the closing </classVarDec> tag
        write("</classVarDec>");
    }

    @Override
    public void compileVarDec() {
        // 1. Write the opening <varDec> tag
        write("<varDec>");

        // 2. Use eat() to consume the 'var' keyword
        eat("var");

        // 3. Call compileType() to handle the type
        compileType();

        // 4. Use eat() to consume the variable name (identifier)
        eat(TokenType.IDENTIFIER);

        // 5. Use a while loop to handle multiple variable names separated by commas
        while (jackTokenizer.getThisToken().equals(",")) {
            eat(","); // Consume the comma
            eat(TokenType.IDENTIFIER); // Consume the next variable name
        }

        // 6. Use eat() to consume the closing ';' symbol
        eat(";");

        // 7. Write the closing </varDec> tag
        write("</varDec>");
    }

    @Override
    public String compileType() {
        // 1. Check if the current token is a primitive type (int, char, boolean) or an identifier (class name)
        String token = jackTokenizer.getThisToken();

        if (token.equals("int") || token.equals("char") || token.equals("boolean")) {
            // 2. Use eat() to consume the primitive type token
            eat(token);
            return token;
        } else if (jackTokenizer.tokenType() == TokenType.IDENTIFIER) {
            // If it's an identifier (class name)
            eat(TokenType.IDENTIFIER);
            return token;
        } else {
            throw new RuntimeException("Invalid type: " + token);
        }
    }

    @Override
    public void compileSubroutine() {
        // 1. Write the opening <subroutineDec> tag
        write("<subroutineDec>");

        // 2. Use eat() to consume the subroutine keyword (constructor, function, method)
        String subroutineKeyword = jackTokenizer.getThisToken();
        if (subroutineKeyword.equals("constructor") || subroutineKeyword.equals("function") || subroutineKeyword.equals("method")) {
            eat(subroutineKeyword);
        } else {
            throw new RuntimeException("Expecting subroutine keyword, but found " + subroutineKeyword);
        }

        // 3. Call compileType() to handle the return type or use eat() to consume 'void' if no return type
        String returnType = compileType();  // This will consume the type or 'void'

        // 4. Use eat() to consume the subroutine name (identifier)
        eat(TokenType.IDENTIFIER);  // The subroutine name is an identifier (e.g., "main")

        // 5. Use eat() to consume the opening '(' symbol
        eat("(");

        // 6. Call compileParameterList() to handle the parameter list
        compileParameterList();

        // 7. Use eat() to consume the closing ')' symbol
        eat(")");

        // 8. Call compileSubroutineBody() to handle the subroutine body
        compileSubroutineBody();

        // 9. Write the closing </subroutineDec> tag
        write("</subroutineDec>");
    }

    @Override
    public void compileParameterList() {
        // 1. Write the opening <parameterList> tag.
        write("<parameterList>");

        // 2. Check if the parameter list is empty (it can be if there are no parameters).
        if (jackTokenizer.getThisToken().equals(")")) {
            // If the next token is ')' (i.e., no parameters), just return
            write("</parameterList>");
            return;
        }

        // 3. Use a while loop to handle multiple parameters separated by commas.
        boolean first = true;
        while (true) {
            if (!first) {
                // Consume the comma if it's not the first parameter
                eat(",");
            }
            first = false;

            // 3. For each parameter, call compileType() to handle the type.
            compileType();

            // 4. Use eat() to consume the parameter name (identifier).
            eat(TokenType.IDENTIFIER);

            // 5. Check if the next token is a closing parenthesis; if so, stop processing parameters.
            if (jackTokenizer.getThisToken().equals(")")) {
                break;
            }
        }

        // 6. Write the closing </parameterList> tag.
        write("</parameterList>");
    }

    @Override
    public void compileSubroutineBody() {
        // 1. Write the opening <subroutineBody> tag.
        write("<subroutineBody>");

        // 2. Use eat() to consume the opening '{' symbol.
        eat("{");

        // 3. Use a while loop to handle variable declarations (compileVarDec()).
        while (jackTokenizer.tokenType() == TokenType.KEYWORD && jackTokenizer.getThisToken().equals("var")) {
            compileVarDec();
        }

        // 4. Call compileStatements() to handle the statements.
        compileStatements();

        // 5. Use eat() to consume the closing '}' symbol.
        eat("}");

        // 6. Write the closing </subroutineBody> tag.
        write("</subroutineBody>");
    }

    @Override
    public void compileStatements() {
        // 1. Write the opening <statements> tag.
        write("<statements>");

        // 2. Use a while loop to handle different types of statements (let, if, while, do, return).
        while (jackTokenizer.tokenType() == TokenType.KEYWORD &&
                (jackTokenizer.getThisToken().equals("let") ||
                        jackTokenizer.getThisToken().equals("if") ||
                        jackTokenizer.getThisToken().equals("while") ||
                        jackTokenizer.getThisToken().equals("do") ||
                        jackTokenizer.getThisToken().equals("return"))) {

            // 3. For each statement type, call the corresponding compile method.
            if (jackTokenizer.getThisToken().equals("let")) {
                compileLet();
            } else if (jackTokenizer.getThisToken().equals("if")) {
                compileIf();
            } else if (jackTokenizer.getThisToken().equals("while")) {
                compileWhile();
            } else if (jackTokenizer.getThisToken().equals("do")) {
                compileDo();
            } else if (jackTokenizer.getThisToken().equals("return")) {
                compileReturn();
            }
        }

        // 4. Write the closing </statements> tag.
        write("</statements>");
    }

    @Override
    public void compileLet() {
        // 1. Write the opening <letStatement> tag.
        write("<letStatement>");

        // 2. Use eat() to consume the 'let' keyword.
        eat("let");

        // 3. Use eat() to consume the variable name (identifier).
        eat(TokenType.IDENTIFIER);

        // 4. If the next token is '[', handle array indexing by calling compileExpression().
        if (jackTokenizer.getThisToken().equals("[")) {
            eat("[");
            compileExpression();
            eat("]");
        }

        // 5. Use eat() to consume the '=' symbol.
        eat("=");

        // 6. Call compileExpression() to handle the expression.
        compileExpression();

        // 7. Use eat() to consume the closing ';' symbol.
        eat(";");

        // 8. Write the closing </letStatement> tag.
        write("</letStatement>");
    }

    @Override
    public void compileDo() {
        // 1. Write the opening <doStatement> tag.
        write("<doStatement>");

        // 2. Use eat() to consume the 'do' keyword.
        eat("do");

        // 3. Use eat() to consume the subroutine call (identifier).
        eat(TokenType.IDENTIFIER);

        // 4. If the next token is '(', handle the subroutine call by calling compileExpressionList().
        if (jackTokenizer.getThisToken().equals("(")) {
            eat("(");
            compileExpressionList();
            eat(")");
        }
        // 5. If the next token is '.', handle the method call by consuming the class name, '.', and subroutine name,
        //    then call compileExpressionList().
        else if (jackTokenizer.getThisToken().equals(".")) {
            eat(".");
            eat(TokenType.IDENTIFIER);  // subroutine name
            eat("(");
            compileExpressionList();
            eat(")");
        }

        // 6. Use eat() to consume the closing ';' symbol.
        eat(";");

        // 7. Write the closing </doStatement> tag.
        write("</doStatement>");
    }

    @Override
    public int compileExpressionList() {
        // 1. Write the opening <expressionList> tag.
        write("<expressionList>");

        int expressionCount = 0;

        // 2. Use a while loop to handle multiple expressions separated by commas.
        while (!jackTokenizer.getThisToken().equals(")")) {
            // 3. For each expression, call compileExpression().
            compileExpression();
            expressionCount++;

            // Check if there's a comma, and if so, consume it and continue.
            if (jackTokenizer.getThisToken().equals(",")) {
                eat(",");
            }
        }

        // 4. Write the closing </expressionList> tag.
        write("</expressionList>");

        // 5. Return the number of expressions in the list.
        return expressionCount;
    }

    @Override
    public void compileWhile() {
        // 1. Write the opening <whileStatement> tag.
        write("<whileStatement>");

        // 2. Use eat() to consume the 'while' keyword.
        eat("while");

        // 3. Use eat() to consume the opening '(' symbol.
        eat("(");

        // 4. Call compileExpression() to handle the condition expression.
        compileExpression();

        // 5. Use eat() to consume the closing ')' symbol.
        eat(")");

        // 6. Use eat() to consume the opening '{' symbol.
        eat("{");

        // 7. Call compileStatements() to handle the statements inside the while loop.
        compileStatements();

        // 8. Use eat() to consume the closing '}' symbol.
        eat("}");

        // 9. Write the closing </whileStatement> tag.
        write("</whileStatement>");
    }

    @Override
    public void compileReturn() {
        // 1. Write the opening <returnStatement> tag.
        write("<returnStatement>");

        // 2. Use eat() to consume the 'return' keyword.
        eat("return");

        // 3. If the next token is not ';', call compileExpression() to handle the return expression.
        if (!JackTokenizer.getThisToken().equals(";")) {
            compileExpression();
        }

        // 4. Use eat() to consume the closing ';' symbol.
        eat(";");

        // 5. Write the closing </returnStatement> tag.
        write("</returnStatement>");
    }

    @Override
    public void compileIf() {
        // 1. Write the opening <ifStatement> tag.
        write("<ifStatement>");

        // 2. Use eat() to consume the 'if' keyword.
        eat("if");

        // 3. Use eat() to consume the opening '(' symbol.
        eat("(");

        // 4. Call compileExpression() to handle the condition expression.
        compileExpression();

        // 5. Use eat() to consume the closing ')' symbol.
        eat(")");

        // 6. Use eat() to consume the opening '{' symbol.
        eat("{");

        // 7. Call compileStatements() to handle the statements inside the if block.
        compileStatements();

        // 8. Use eat() to consume the closing '}' symbol.
        eat("}");

        // 9. If the next token is 'else', handle the else block by consuming 'else', '{', calling compileStatements(), and '}'.
        if (JackTokenizer.getThisToken().equals("else")) {
            eat("else"); // Consume the 'else' keyword
            eat("{"); // Consume the opening '{' for else block
            compileStatements(); // Compile statements inside else block
            eat("}"); // Consume the closing '}'
        }

        // 10. Write the closing </ifStatement> tag.
        write("</ifStatement>");
    }

    @Override
    public void compileExpression() {
        // 1. Write the opening <expression> tag.
        write("<expression>");

        // 2. Call compileTerm() to handle the first term.
        compileTerm();

        // 3. Use a while loop to handle multiple terms separated by operators.
        while (JackTokenizer.isOp()) {
            // 4. For each operator, use eat() to consume the operator.
            String operator = JackTokenizer.getThisToken();
            eat(operator);  // Consume the operator

            // 5. Call compileTerm() to handle the next term.
            compileTerm();
        }

        // 5. Write the closing </expression> tag.
        write("</expression>");
    }

    @Override
    public void compileTerm() {
        // 1. Write the opening <term> tag.
        write("<term>");

        // 2. Check the type of the current token and handle accordingly:
        TokenType tokenType = JackTokenizer.tokenType();

        // - If the token is an integer constant, use eat() to consume it.
        if (tokenType == TokenType.INT_CONSTANT) {
            write("<integerConstant>");  // Start tag for integer constant
            write(JackTokenizer.getThisToken());
            eat(JackTokenizer.getThisToken());  // Consume the integer constant
            write("</integerConstant>");  // End tag for integer constant
        }
        // - If the token is a string constant, use eat() to consume it.
        else if (tokenType == TokenType.STRING_CONSTANT) {
            write("<stringConstant>");
            write(JackTokenizer.getThisToken());
            eat(JackTokenizer.getThisToken());  // Consume the string constant
            write("</stringConstant>");
        }
        // - If the token is a keyword constant, use eat() to consume it.
        else if (tokenType == TokenType.KEYWORD && JackTokenizer.isKeywordConstant()) {
            write("<keywordConstant>");
            write(JackTokenizer.getThisToken());
            eat(JackTokenizer.getThisToken());  // Consume the keyword constant (true, false, null, this)
            write("</keywordConstant>");
        }
        // - If the token is an identifier, handle variable, array, or subroutine calls.
        else if (tokenType == TokenType.IDENTIFIER) {
            String identifier = JackTokenizer.getThisToken();
            eat(identifier);  // Consume the identifier

            // Check if the next token is a '[' indicating array access
            if (JackTokenizer.getThisToken().equals("[")) {
                write("<arrayAccess>");
                write("<identifier>");
                write(identifier);
                write("</identifier>");
                eat("[");  // Consume the '['
                compileExpression();  // Compile the expression for the index
                eat("]");  // Consume the ']'
                write("</arrayAccess>");
            }
            // Check if the next token is '(' or '.' for subroutine calls
            else if (JackTokenizer.getThisToken().equals("(")) {
                write("<subroutineCall>");
                write("<identifier>");
                write(identifier);
                write("</identifier>");
                eat("(");  // Consume the '('
                compileExpressionList();  // Compile the expression list for the subroutine
                eat(")");  // Consume the ')'
                write("</subroutineCall>");
            }
            else if (JackTokenizer.getThisToken().equals(".")) {
                // Handle method calls (object.method or class.method)
                write("<subroutineCall>");
                write("<identifier>");
                write(identifier);
                write("</identifier>");
                eat(".");  // Consume the '.'
                write("<identifier>");
                write(JackTokenizer.getThisToken());  // Method name
                write("</identifier>");
                eat(JackTokenizer.getThisToken());  // Consume the method name
                eat("(");  // Consume the '('
                compileExpressionList();  // Compile the expression list for the method
                eat(")");  // Consume the ')'
                write("</subroutineCall>");
            }
            // Handle simple variable access (identifier without array or subroutine call)
            else {
                write("<identifier>");
                write(identifier);
                write("</identifier>");
            }
        }
        // - If the token is '(', handle the expression inside parentheses by calling compileExpression().
        else if (JackTokenizer.getThisToken().equals("(")) {
            eat("(");  // Consume '('
            compileExpression();  // Compile the expression inside parentheses
            eat(")");  // Consume ')'
        }
        // - If the token is a unary operator, use eat() to consume it and call compileTerm() to handle the term.
        else if (JackTokenizer.isUnaryOp()) {
            write("<unaryOp>");
            write(JackTokenizer.getThisToken());
            eat(JackTokenizer.getThisToken());  // Consume the unary operator
            compileTerm();  // Compile the term following the unary operator
            write("</unaryOp>");
        }

        // 3. Write the closing </term> tag.
        write("</term>");
    }


    private boolean isClassVarDecKeyword(String token) {
        return token.equals("static") || token.equals("field");
    }

    private boolean isSubroutineDecKeyword(String token) {
        return token.equals("constructor") || token.equals("function") || token.equals("method");
    }

    private void advance() {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance();
        }
    }

    private void eat(String str) {
        if (jackTokenizer.getThisToken().equals(str)) {
            // writes <tokenType> str </tokenType>
            write(jackTokenizer.getThisTokenAsTag());
        } else {
            throw new RuntimeException("expect " + str + " but get " + jackTokenizer.getThisToken());
        }
        advance();
    }

    private void eat(TokenType tokenType) {
        if (JackTokenizer.tokenType() == tokenType) {
            // writes <tokenType> val </tokenType>
            write(jackTokenizer.getThisTokenAsTag());
        } else {
            throw new RuntimeException("expect " + StringUtils.getTokenType(tokenType) + " but get " + StringUtils.getTokenType(jackTokenizer.tokenType()));
        }
        advance();
    }

    private void write(String str) {
        try {
            bw.write(str);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
