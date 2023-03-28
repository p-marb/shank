package m.pat;

import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class Parser {

    private List<Token> tokens;
    private int currentIndex;
    private Token currentToken;
    private int indentLevel;

    /**
     * Instantiates the parser with a list of tokens.
     * The list of tokens acts like a queue, matching and
     * removing expected tokens.
     * @param tokens the list of tokens to parse
     */
    Parser(List<Token> tokens){
        this.tokens = tokens;
        this.currentIndex = 0;
        this.currentToken = tokens.get(0);
        this.indentLevel = 0;
    }

    // CORE PARSE METHODS

    /**
     * Accepts a token type, if the token type matches the next token type
     * token is removed from list and returned. If not a match, returns null.
     * @param tokenType the type of token to check for
     * @return null if next token is not a match, or the token that was removed.
     */
    private Token matchAndRemove(Token.TokenType tokenType){
        Token tok = peek(0); // Peek the next token.
        if(tok.getTokenType() == tokenType){
            // Keep track of indent level.
            if(tok.getTokenType() == Token.TokenType.INDENT) indentLevel++;
            if(tok.getTokenType() == Token.TokenType.DEDENT) indentLevel--;
            // Token is  match, remove and return it.
            if(Shank.DEBUG) System.out.println("Parser: Removed " + tokenType.name() + "(" + tok.getValue() + ") index " + currentIndex + "/" + tokens.size());
            tokens.remove(currentIndex);
            //currentIndex++;
            return tok;
        }
        return null;
    }

    private Token expectsToken(Token.TokenType tokenType) throws SyntaxErrorException{
        Token token = peek(0);
        if(token != null) {
            if (token.getTokenType() == tokenType) {
                matchAndRemove(tokenType);
                token = peek(0);

                if (token != null && token.getTokenType() == tokenType) {
                    expectsToken(tokenType);
                }
                return token;
            }
        } else {
            return null;
        }
        throw new SyntaxErrorException("Expected " + tokenType.name() + " token not found. Found: " + token);
    }

    /**
     * Checks if the next token is an end of line.
     * Returns ENDOFLINE token or
     * If not, throws a SyntaxErrorException
     * @throws SyntaxErrorException if the next token type is not end of line.
     */
    private Token expectsEndOfLine() throws SyntaxErrorException {
        Token token = peek(0);
        if(token != null) {
            if (token.getTokenType() == Token.TokenType.ENDOFLINE) {
                matchAndRemove(Token.TokenType.ENDOFLINE);
                token = peek(0);

                if (token != null && token.getTokenType() == Token.TokenType.ENDOFLINE) {
                    expectsEndOfLine();
                }
                return token;
            }
        } else {
            return null;
        }
        throw new SyntaxErrorException("Expected end of line token not found. Found: " + token);
    }

    /**
     * Looks ahead a given amount, returns null if it can't.
     * @param index the index (currentIndex+index) to peek
     * @return null if nothing found, token if found.
     */
    private Token peek(int index){
        if((currentIndex + index) < tokens.size()){
            return tokens.get(currentIndex + index);
        }
        return null;
    }

    /**
     * Main parsing method. Returns a ProgramNode if code
     * parses correctly, null if not.
     * @return ProgramNode or null
     * @throws SyntaxErrorException if parsing fails
     */
    public Node parse() throws SyntaxErrorException {
        System.out.println("Beginning parsing...");
        ProgramNode programNode;
        HashMap<String, FunctionNode> functions = new HashMap<>();

//        // Clear up any ENDOFLINE's that are present before code tokens.
//        expectsEndOfLine();
        Node node = function();
        while(node != null){
            if(node instanceof FunctionNode){
                String name = ((FunctionNode) node).getName();
                if(functions.containsKey(name)){
                    System.out.println("Duplicate function name! (" + name + ")");
                    functions.replace(((FunctionNode) node).getName(), ((FunctionNode) node));
                } else {
                    functions.put(((FunctionNode) node).getName(), ((FunctionNode) node));
                }
                if(Shank.DEBUG) System.out.println("Found function: " + node);
                expectsEndOfLine();
                node = function();
            }
        }
        if(functions.size() > 0){
            programNode = new ProgramNode(functions);
            return programNode;
        }
//        while((node = expression()) != null){
//
//            try{
//                expectsEndOfLine();
//                if (node instanceof MathOpNode) {
//                    System.out.println("MathOpNode("
//                            + ((MathOpNode) node).getOperation() + ", "
//                            + ((MathOpNode) node).getLeft().toString() + ", " +
//                            ((MathOpNode) node).getRight().toString() + ")");
//                }
//
//            } catch(SyntaxErrorException e){
//                e.printStackTrace();
//            }
//        }
        throw new SyntaxErrorException("Parsing found non matching token: " + tokens.get(currentIndex));
    }

    // MATH OPERATIONS

    /**
     * Expression is a MathOpNode, with left and right term and a math operation
     * of either plus or minus.
     * @return the math operation
     */
    public Node expression(){
        // Build an expression = term { (+ or -) term}

        Node left = term();
        while (true) {
            Token token = peek(0);
            if (token != null && (token.getTokenType() == Token.TokenType.PLUS || token.getTokenType() == Token.TokenType.MINUS)) {
                matchAndRemove(token.getTokenType());
                Node right = term();
                left = new MathOpNode(token.getTokenType() == Token.TokenType.PLUS ? MathOp.PLUS : MathOp.MINUS, left, right);
            } else {
                break;
            }
        }
        return left;
    }

    /**
     * Term looks for factor on left hand side, factor on right hand side
     * and is used for multiplication, division and modulus.
     * @return two-sided term with left and right hand factors
     */
    public Node term(){
        // term = factor {(times or divide or mod) factor}
        Node left = factor();
        while (true) {
            Token token = peek(0);
            if (token != null && token.getTokenType() == Token.TokenType.MULTIPLY) {
                matchAndRemove(Token.TokenType.MULTIPLY);
                Node right = factor();
                left = new MathOpNode(MathOp.TIMES, left, right);
            } else if (token != null && token.getTokenType() == Token.TokenType.DIVIDE) {
                matchAndRemove(Token.TokenType.DIVIDE);
                Node right = factor();
                left = new MathOpNode(MathOp.DIVIDE, left, right);
            } else if (token != null && token.getTokenType() == Token.TokenType.MODULUS) {
                matchAndRemove(Token.TokenType.MODULUS);
                Node right = factor();
                left = new MathOpNode(MathOp.MOD, left, right);
            } else {
                break;
            }
        }
        return left;
    }

    //TODO: Support true, false, StringLiteral, CharLiteral
    /**
     * Factor can be either an IntegerNode/FloatNode/VariableReference or call
     * expression() to build a MathOpNode.
     * @return IntegerNode/FloatNode or MathOpNode
     * @throws SyntaxErrorException if VariableReference name is not found
     */
    public Node factor() {
        Token token = peek(0);

        // Base case check parens/groupings.
        switch(token.getTokenType()){
            // P
            case PARENTHESIS_L -> {
                matchAndRemove(Token.TokenType.PARENTHESIS_L);
                Node expression = expression();
                matchAndRemove(Token.TokenType.PARENTHESIS_R);
                return expression;
            }
            // ..EMDA
            case NUMBER -> {
                matchAndRemove(Token.TokenType.NUMBER);
                if(token.getValue().contains(".")){
                    return new FloatNode(Float.parseFloat(token.getValue()));
                } else {
                    return new IntegerNode(Integer.parseInt(token.getValue()));
                }
            }
            // ..S
            case MINUS -> {
                matchAndRemove(Token.TokenType.MINUS);
                token = peek(0);
                if(token.getValue().contains(".")){
                    matchAndRemove(Token.TokenType.NUMBER);
                    return new FloatNode(-1 * Float.parseFloat(token.getValue()));
                } else {
                    matchAndRemove(Token.TokenType.NUMBER);
                    return new IntegerNode(-1 * Integer.parseInt(token.getValue()));
                }
            }
            // VariableReference
            case IDENTIFIER -> {
                String varName;
                varName = matchAndRemove(Token.TokenType.IDENTIFIER).getValue();
                if(varName != null){
                    token = peek(0);
                    if(token.getTokenType() == Token.TokenType.INDEX_L){
                        matchAndRemove(Token.TokenType.INDEX_L);
                        Node index = expression();
                        matchAndRemove(Token.TokenType.INDEX_R);
                        return new VariableReferenceNode(varName, index);
                    } else {
                        return new VariableReferenceNode(varName);
                    }
                } else {
                    System.err.println("factor(): variable reference found null name, wrong token?");
                }
            }
        }
        return null;
    }


    /**
     * Evaluates a boolean comparison operation.
     * @return a BooleanCompareNode of the comparison type with left and right nodes
     * @throws SyntaxErrorException if there was an unexpected comparison type found
     */
    public Node boolCompare() throws SyntaxErrorException {
        Node left = expression();
        Token token = peek(0);
        BooleanComparison comparisonType;
        if(token != null){
            if((comparisonType = token.isComparisonNode()) != null){
                // Expect another expression,
                matchAndRemove(token.getTokenType());
                Node right = expression();
                return new BooleanCompareNode(comparisonType, left, right);
            } else {
                // No other expression.
                return left;
            }
        } else {
            throw new SyntaxErrorException("Expected an expression or comparison, found null.");
        }
    }

    // FUNCTION AND VARIABLE METHODS

    /**
     * Returns a FunctionNode.
     * @return function node
     */
    public Node function() throws SyntaxErrorException {
        // function() expects a DEFINE, IDENTIFIER, LEFT_PAREN, LIST_OF_VARS, RIGHT_PAREN, ENDOFLINE
        // then constants and variables, then an indent, then statements, then a dedent. return FunctionNode or null.

        Token token;
        token = peek(0);
        if(token == null) return null;
        if(token.getTokenType() == Token.TokenType.DEFINE){
            matchAndRemove(Token.TokenType.DEFINE);
            // Expect an identifier.
            token = peek(0);
            if(token.getTokenType() == Token.TokenType.IDENTIFIER){
                // Read function name.
                String functionName = token.getValue();
                matchAndRemove(Token.TokenType.IDENTIFIER);
                token = peek(0);
                if(token.getTokenType() == Token.TokenType.PARENTHESIS_L){
                    // We're processing the function parameters.
                    matchAndRemove(Token.TokenType.PARENTHESIS_L);
                    // Call processDeclarations to collect any function parameters.
                    Collection<VariableNode> parameters = processDeclarations(false);


                    // variableNodes can be empty, expect PAREN_R
                    token = peek(0);
                    if(token.getTokenType() == Token.TokenType.PARENTHESIS_R){
                        matchAndRemove(Token.TokenType.PARENTHESIS_R);
                        // Need ENDOFLINE to seal the deal.
                        // TODO: Replace with expectsEndOfLine();
                        token = peek(0);
                        if(token.getTokenType() == Token.TokenType.ENDOFLINE){
                            // We have processed define xyz (a,b,c : integer...) endofline
                            matchAndRemove(Token.TokenType.ENDOFLINE);
                            token = peek(0);
                            // Process the constants and variables that are defined for the function
                            Collection<VariableNode> constantsAndVariables = new ArrayList<>();
                            while(token.getTokenType() == Token.TokenType.CONSTANTS
                            || token.getTokenType() == Token.TokenType.VARIABLES){
                                if(token.getTokenType() == Token.TokenType.CONSTANTS){
                                    matchAndRemove(Token.TokenType.CONSTANTS);

                                    constantsAndVariables.addAll( processDeclarations(true));
                                } else {
                                    matchAndRemove(Token.TokenType.VARIABLES);

                                    constantsAndVariables.addAll( processDeclarations(false));
                                }

                                expectsEndOfLine();
                            }

                            token = peek(0);

                            System.out.println("Processing statements for function " + functionName + " debug: " + token);

                            // Expect either INDENT and statements or nothing else.
                            if(token.getTokenType() == Token.TokenType.INDENT){
                                // Expect statements...
                                matchAndRemove(Token.TokenType.INDENT);
                                List<StatementNode> statements = statements();
                                // Remove any leftover DEDENT
                                expectsToken(Token.TokenType.DEDENT);

                                System.out.println("Function(" + functionName + "): " + statements.size() + " statements.");

                                return new FunctionNode(functionName, parameters, constantsAndVariables, statements);
                            } else {
                                // No statements for function.
                                List<StatementNode> statements = new ArrayList<>();
                                System.out.println("Function(" + functionName + "): no statements.");
                                return new FunctionNode(functionName, parameters, constantsAndVariables, statements);
                            }

                        }
                    } else {
                        throw new SyntaxErrorException("Expected a right parenthesis, found: " + token);
                    }

                } else {
                    throw new SyntaxErrorException("Expected a left parenthesis, found: " + token);
                }

            } else {
                throw new SyntaxErrorException("Expected IDENTIFIER token, found: " + token);
            }
        } else {
            throw new SyntaxErrorException("Expected DEFINE token, found: " + token);
        }
        throw new SyntaxErrorException("Unexpected token: " + token);
    }

    /**
     * Assesses an assignment, expects an identifier
     * with possible number of array indexes followed by
     * an assignment operator(:=) followed by a
     * boolCompare()
     * @return assignment node
     */
    public AssignmentNode assignment() throws SyntaxErrorException {
        Token token = peek(0);
        // Check if token is an identifier.
        if(token.getTokenType() != Token.TokenType.IDENTIFIER){
            throw new SyntaxErrorException("Expected identifier in assignment statement. Found: " + token);
        }
        // Create a variable reference node with the name of the identifier.
        VariableReferenceNode variableRefNode = new VariableReferenceNode(token.getValue());
        matchAndRemove(Token.TokenType.IDENTIFIER);
        token = peek(0);
        if(token.getTokenType() == Token.TokenType.INDEX_L){

            matchAndRemove(Token.TokenType.INDEX_L);

            // Parse any array indices present.
            int indexCounter = 0;
            while(token.getTokenType() != Token.TokenType.ASSIGNER ){
                switch(token.getTokenType()){
                    case IDENTIFIER -> {
                        // Nested array identifier.
                        variableRefNode.setName(token.getValue());
                        matchAndRemove(Token.TokenType.IDENTIFIER);
                        token = peek(0);
                    }
                    case NUMBER -> {
                        // Array index.
                        // Check if number is floating point.
                        if(token.getValue().contains(".")){
                            throw new SyntaxErrorException("Array index cannot be a floating point.");
                        } else {
                            // Set the variable reference node index to the array index specified.
                            variableRefNode.setIndex(new IntegerNode(Integer.parseInt(token.getValue())));
                            matchAndRemove(Token.TokenType.NUMBER);
                            token = peek(0);
                        }
                    }
                    case INDEX_L -> {
                        indexCounter++;
                        matchAndRemove(Token.TokenType.INDEX_L);
                        token = peek(0);
                    }
                    case INDEX_R -> {
                        indexCounter--;
                        matchAndRemove(Token.TokenType.INDEX_R);
                        token = peek(0);
                    }
                    default -> throw new SyntaxErrorException("Unexpected token found while processing array: " + token);
                }
                //Node index = parseArrayIndex();
                //variableRefNode.setIndex(index);
            }
        }

        // Make sure next token is assign token.
        token = peek(0);
        if(token.getTokenType() != Token.TokenType.ASSIGNER){
            throw new SyntaxErrorException("Expected an assigner operator :=, found: " + token);
        }
        matchAndRemove(Token.TokenType.ASSIGNER);

        Node rightOperation = boolCompare();

        AssignmentNode assignmentNode = new AssignmentNode(variableRefNode, rightOperation);
        System.out.println("Got assignment: " + assignmentNode);
        expectsEndOfLine();

        return assignmentNode;
    }

    /**
     * Parses a list of statements.
     * @return the list of statements found
     * @throws SyntaxErrorException if there was an error parsing statements
     */
    public List<StatementNode> statements() throws SyntaxErrorException {
        List<StatementNode> statementNodes = new ArrayList<>();
        while(peek(0) != null && !peek(0).getTokenType().equals(Token.TokenType.DEDENT)){
            StatementNode statementNode = statement();
            if(statementNode != null){
                statementNodes.add(statementNode);
            }
        }
        return statementNodes;
    }

    /**
     * Parses an individual statement.
     * @return a StatementNode of the node being assigned.
     * @throws SyntaxErrorException if there was an error parsing the statement
     */
    public StatementNode statement() throws SyntaxErrorException {
        Token token = peek(0);
        if(token == null){
            return null;
        }
        if(token.getTokenType() == Token.TokenType.IDENTIFIER){
            // Check if this is going to be an assignment or function call.
            String identifierName = token.getValue();
            if(peek(1).getTokenType() == Token.TokenType.ASSIGNER){
                return assignment();
            } else {
                return parseFunctionCalls();
            }
        } else if(token.getTokenType() == Token.TokenType.IF){
            return parseIf();
        }

        throw new SyntaxErrorException("Unexpected token, found: " + token);
    }

    //TODO: Process type limits (i.e variables numberOfCards : integer from 0 to 52)
    /**
     * Processes a list of variable declarations.
     * Expected that function() already deals with VARIABLES token
     * before using this function to process any declarations.
     * The format is always the same, "a,b,c : integer"
     * @param isConstants whether the examined declarations are constant or not
     * @return collection of VariableNodes that were found
     */
    public Collection<VariableNode> processDeclarations(boolean isConstants) throws SyntaxErrorException {
        // format for variable declarations:
        // () = optional, [] = required.
        // (var) [identifier] (comma identifier...) [colon/equal] [type/value] (semicolon, repeat...)

        Collection<VariableNode> declarations = new ArrayList<>();
        Token token;
        token = peek(0);

        // To keep track of multiple variable names, whether it is a var and value.
        List<String> variableNames = new ArrayList<>();
        boolean var = false;
        String value;

        // Scan until we reach ENDOFLINE or ).
        while(token.getTokenType() != Token.TokenType.ENDOFLINE && token.getTokenType() != Token.TokenType.PARENTHESIS_R) {
            switch (token.getTokenType()) {
                case VAR:
                    var = true;
                    matchAndRemove(Token.TokenType.VAR);
                    token = peek(0);
                    break;
                case IDENTIFIER:
                    // Identifier can either have a comma or colon after it.
                    variableNames.add(token.getValue());
                    matchAndRemove(Token.TokenType.IDENTIFIER);
                    token = peek(0);
                    break;
                case COMMA:
                    matchAndRemove(Token.TokenType.COMMA);
                    token = peek(0);
                    break;
                case COLON:
                    matchAndRemove(Token.TokenType.COLON);
                    token = peek(0);
                    break;
                case SEMICOLON:
                    matchAndRemove(Token.TokenType.SEMICOLON);
                    variableNames.clear();
                    var = false;
                    token = peek(0);
                    break;
                case EQUALS:
                    if(isConstants){
                        matchAndRemove(Token.TokenType.EQUALS);
                        token = peek(0);
                    } else {
                        throw new SyntaxErrorException("EQUALS token expected only in constants declaration.");
                    }
                    break;
                case NUMBER:
                    // Expect to have a list or at least one name for constant declaration.
                    if(variableNames.size() > 0){
                        if(isConstants){
                            if(token.getValue().contains(".")){
                                for(String varName: variableNames){
                                    declarations.add(new VariableNode(new FloatNode(Float.parseFloat(token.getValue())), varName, true ));
                                }
                            } else {
                                for(String varName: variableNames){
                                    declarations.add(new VariableNode(new IntegerNode(Integer.parseInt(token.getValue())), varName, true ));
                                }
                            }
                            matchAndRemove(Token.TokenType.NUMBER);
                            token = peek(0);
                        } else {
                            throw new SyntaxErrorException("Expected numbers to be constants. Found: " + token);
                        }
                    } else {
                        throw new SyntaxErrorException("Expected list of 1 or more constant variables, found 0.");
                    }
                    break;

                case STRINGLITERAL:
                    if(variableNames.size() > 0){
                        if(isConstants){
                            for(String varName: variableNames){
                                declarations.add(new VariableNode(new StringNode(token.getValue()), varName, true));
                            }
                            matchAndRemove(Token.TokenType.STRINGLITERAL);
                            token = peek(0);
                        } else {
                            throw new SyntaxErrorException("Expected string literal to be constant. Found: " + token);
                        }
                    } else {
                        throw new SyntaxErrorException("Expected list of 1 or more constant variables, found 0.");
                    }
                    break;
                case CHARACTERLITERAL:
                    if(variableNames.size() > 0){
                        if(isConstants){
                            for(String varName: variableNames){
                                declarations.add(new VariableNode(new CharacterNode(token.getValue().charAt(0)), varName, true));
                            }
                            matchAndRemove(Token.TokenType.STRINGLITERAL);
                            token = peek(0);
                        } else {
                            throw new SyntaxErrorException("Expected character literal to be constant. Found: " + token);
                        }
                    } else {
                        throw new SyntaxErrorException("Expected list of 1 or more constant variables, found 0.");
                    }
                    break;



                    // TYPES (x : string, y : integer, z : char, etc.)

                case STRING:
                    if(variableNames.size() > 0){
                        for(String varName : variableNames){
                            declarations.add(new VariableNode(new StringNode(""), varName, var));
                        }
                        matchAndRemove(Token.TokenType.STRING);
                        token = peek(0);
                    } else {
                        throw new SyntaxErrorException("No variable names were found for type STRING.");
                    }
                    break;

                case CHARACTER:
                    if(variableNames.size() > 0){
                        for(String varName : variableNames){
                            declarations.add(new VariableNode(new CharacterNode('\0'), varName, var));
                        }
                        matchAndRemove(Token.TokenType.CHARACTER);
                        token = peek(0);
                    } else {
                        throw new SyntaxErrorException("No variable names were found for type CHAR.");
                    }
                    break;
                case BOOLEAN:
                    if(variableNames.size() > 0){
                        for(String varName : variableNames){
                            declarations.add(new VariableNode(new BooleanNode(false), varName, var));
                        }
                        matchAndRemove(Token.TokenType.BOOLEAN);
                        token = peek(0);
                    } else {
                        throw new SyntaxErrorException("No variable names were found for type BOOLEAN.");
                    }
                    break;
                case INTEGER:
                    // Expect to have a list of names built up.
                    if(variableNames.size() > 0){
                        for(String varName : variableNames){
                            declarations.add(new VariableNode(new IntegerNode(0), varName, var));
                        }
                        matchAndRemove(Token.TokenType.INTEGER);
                        token = peek(0);
                    } else {
                        throw new SyntaxErrorException("No variable names were found for type INTEGER.");
                    }
                    break;


            }
        }
        // Return can be null if no variables found.
        return declarations;
    }

    /**
     * Processes a for loop statement.
     * Expects tokens FOR, IDENTIFIER, FROM, expression(), to, expression()
     * @return a ForNode with the from and to nodes.
     * @throws SyntaxErrorException if there were any unexpected tokens found or logic.
     */
    public ForNode parseFor() throws SyntaxErrorException {
        Token token = peek(0);
        if(token.getTokenType() == Token.TokenType.FOR){
            matchAndRemove(Token.TokenType.FOR);
            token = peek(0);
            if(token.getTokenType() == Token.TokenType.IDENTIFIER){
                VariableReferenceNode variableReferenceNode =  new VariableReferenceNode(token.getValue());
                matchAndRemove(Token.TokenType.IDENTIFIER);
                token = peek(0);
                if(token.getTokenType() == Token.TokenType.FROM){
                    matchAndRemove(Token.TokenType.FROM);
                    Node from = expression();
                    token = peek(0);
                    if(token.getTokenType() == Token.TokenType.TO){
                        matchAndRemove(Token.TokenType.TO);
                        Node to = expression();
                        return new ForNode(from, to);
                    } else {
                        throw new SyntaxErrorException("Expected to token, found: " + token);
                    }
                } else {
                    throw  new SyntaxErrorException("Expected from token, found: " + token);
                }
            } else {
                throw new SyntaxErrorException("Expected an identifier, found: " + token);
            }
        } else {
            throw new SyntaxErrorException("Expected a for loop statement, found: " + token);
        }
    }

    /**
     * Parses a while loop statement.
     * Expects tokens WHILE, boolCompare(), statements()
     * If there isn't any comparison condition, throws an
     * error.
     * @return WhileNode of statement parsed with the condition and statements.
     * @throws SyntaxErrorException if no condition found or unexpected token
     */
    public WhileNode parseWhile() throws SyntaxErrorException {
        Token token = peek(0);
        if(token.getTokenType() == Token.TokenType.WHILE){
            matchAndRemove(Token.TokenType.WHILE);
            token = peek(0);
            Node condition = boolCompare();
            if(condition instanceof BooleanCompareNode) {
                Collection<StatementNode> statements = statements();
                return new WhileNode((BooleanCompareNode) condition, statements);
            } else {
                throw new SyntaxErrorException("Expected a condition for while loop, found: " + token);
            }
        } else {
            throw  new SyntaxErrorException("Expected a while loop statement, found: " + token);
        }
    }

    /**
     * Parses an if statement.
     *
     * @return
     * @throws SyntaxErrorException
     */
    public IfNode parseIf() throws SyntaxErrorException {
        Token token = peek(0);
        if(token.getTokenType() == Token.TokenType.IF) {
            matchAndRemove(Token.TokenType.IF);
            // Expect boolCompare(), then token, statements
            Node condition = boolCompare();
            // Make sure condition is a BooleanCompareNode.
            if(condition instanceof BooleanCompareNode) {
                token = peek(0);
                if(token.getTokenType() == Token.TokenType.THEN) {
                    Collection<StatementNode> statements = statements();
                    IfNode nextIfNode = null;
                    token = peek(0);
                    switch(token.getTokenType()) {
                        case ELSIF -> {
                            matchAndRemove(Token.TokenType.ELSIF);
                            nextIfNode = parseIf();
                            return new IfNode((BooleanCompareNode) condition, statements, nextIfNode);
                        }
                        case ELSE -> {
                            matchAndRemove(Token.TokenType.ELSE);
                            token = peek(0);
                            if(token.getTokenType() == Token.TokenType.IF){
                                matchAndRemove(Token.TokenType.IF);
                                statements = statements();
                                nextIfNode = parseIf();
                                return new IfNode((BooleanCompareNode) condition, statements, nextIfNode);
                            }
                        }
                    }
                    nextIfNode = parseIf(); // Recursively parse the next IfNode.
                    return new IfNode((BooleanCompareNode) condition, statements, nextIfNode);
                } else {
                    throw new SyntaxErrorException("Expected then token, found: " + token);
                }
            } else {
                // Condition is null, end of IfNode chain.
                return null;
            }
        } else {
            // No more if statements, return null.
            return null;
        }
    }


    /**
     * Processes a function call with optional amount of parameters.
     * Example: functionName var parameter1, 1+1
     * @return returns a FunctionCallNode with the name of the function and optional list of parameters.
     * @throws SyntaxErrorException if there is an error while processing the function call.
     */
    public FunctionCallNode parseFunctionCalls() throws SyntaxErrorException {
        // FunctionCall expects [] = optional, {} = 0 or more:
        // IDENTIFIER [PARAMETER] {,PARAMETER}
        // Parameter is either VAR IDENTIFIER or boolCompare()
        Token token = peek(0);
        if(token != null){
            if(token.getTokenType() == Token.TokenType.IDENTIFIER){
                String functionName = token.getValue();
                // Found function call name, process parameters...
                matchAndRemove(Token.TokenType.IDENTIFIER);
                token = peek(0);
                if(token != null){
                    if(token.getTokenType() == Token.TokenType.VAR){
                        // Process parameters...
                        Collection<ParameterNode> parameters = new ArrayList<>();
                        while(token != null && token.getTokenType() != Token.TokenType.ENDOFLINE){
                            // Keep processing until we reach ENDOFLINE.
                            switch (token.getTokenType()){
                                case VAR ->{
                                    matchAndRemove(Token.TokenType.VAR);
                                    token = peek(0);
                                }
                                case COMMA -> {
                                    matchAndRemove(Token.TokenType.COMMA);
                                    token = peek(0);
                                }
                                case IDENTIFIER -> {
                                    ParameterNode parameter = new ParameterNode(new VariableReferenceNode(token.getValue()));
                                    parameters.add(parameter);
                                    matchAndRemove(Token.TokenType.IDENTIFIER);
                                    token = peek(0);
                                }
                                default -> throw new SyntaxErrorException("Unexpected token while processing function call parameters: " + token);

                            }

                        }

                        // We've finished processing any parameters.
                        return new FunctionCallNode(functionName, parameters);

                    } else if(token.getTokenType() == Token.TokenType.ENDOFLINE){
                        // No parameters.
                        return new FunctionCallNode(functionName, null);
                    } else {
                        // Process boolCompare() -> expect boolCompare() COMMA boolCompare() COMMA....
                        //Node parameter = boolCompare();
                        Collection<ParameterNode> parameters = new ArrayList<>();
                        // Already processed function name, so now time to process parameters.
                        parameters.add(new ParameterNode(boolCompare()));
                        while(token != null){
                            if(token.getTokenType() != Token.TokenType.ENDOFLINE){
                                switch (token.getTokenType()){
                                    case COMMA -> {
                                        matchAndRemove(Token.TokenType.COMMA);
                                        parameters.add(new ParameterNode(boolCompare()));
                                    }
                                    default -> throw new SyntaxErrorException("Unexpected token while processing function call parameters: " + token);
                                }
                            } else {
                                break;
                            }
                        }

                        return new FunctionCallNode(functionName, parameters);
                    }
                } else {
                    // No more tokens and no parameters.
                    return new FunctionCallNode(functionName, null);
                }
            } else {
                throw new SyntaxErrorException("Expected to find an IDENTIFIER for function call, found: " + token);
            }
        }
        throw new SyntaxErrorException("Expected to find function call, found nothing.");
    }


}
