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
     * Returns the current indentation level.
     * @return integer representation of the indentation level
     */
    private int getIndentLevel(){
        return this.indentLevel;
    }

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

            if(Shank.DEBUG) System.out.println("matchAndRemove() | " + Thread.currentThread().getStackTrace()[2].getMethodName() + "(): Removed " + tokenType.name() + "(" + tok.getValue() + ") index " + currentIndex + "/" + tokens.size());
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
                assert  token != null;

                if (token.getTokenType() == tokenType) {
                    expectsToken(tokenType);
                } else {
                    return token;

                }
            } else {

                throw new SyntaxErrorException("Expected " + tokenType.name() + " token, found: " + token);
            }
        } else {
            throw new SyntaxErrorException("Expected " + tokenType.name() + " token, found nothing. ");
        }
        return null;
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
                node = function();
            }
        }
        if(functions.size() > 0){
            programNode = new ProgramNode(functions);
            return programNode;
        }
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
                token = peek(0);
                varName = token.getValue();
                matchAndRemove(Token.TokenType.IDENTIFIER);

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
                if(Shank.DEBUG) System.out.println("boolCompare(): Found left " + left + ", " + comparisonType + ", " + right);
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
                    if(Shank.DEBUG) System.out.println("function (" + functionName + "): Finished processing function parameters (" + parameters.size() + " parameters)");


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
                                if(Shank.DEBUG) System.out.println("function(): (" + functionName + "): Finished processing constants and variables (" + constantsAndVariables.size() + ")");

                                expectsEndOfLine();
                                break;
                            }

                            token = peek(0);

                            // Expect either INDENT and statements or nothing else.
                            if(token.getTokenType() == Token.TokenType.INDENT){
                                // Expect statements...
                                //matchAndRemove(Token.TokenType.INDENT);
                                List<StatementNode> statements = statements();

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
     * Expects INDENT, then keeps gathering statement() while the
     * getIndentLevel() > 0. Expects DEDENT after each statement() call.
     * @return the list of statements found
     * @throws SyntaxErrorException if there was an error parsing statements
     */
    public List<StatementNode> statements() throws SyntaxErrorException {
        List<StatementNode> statementNodes = new ArrayList<>();
        Token token = peek(0); // Used to track whether the next token is ENDOFLINE or DEDENT.


        expectsToken(Token.TokenType.INDENT);
        StatementNode statementNode;
        while((statementNode = statement()) != null){
            if(Shank.DEBUG) System.out.println("statements(): Got statement: " + statementNode);
            statementNodes.add(statementNode);
            if(getIndentLevel() != 0){
                if(Shank.DEBUG && statementNode instanceof IfNode) System.out.println("statements(): ADDED IfNode: " + statementNode);
                if(Shank.DEBUG && statementNode instanceof WhileNode) System.out.println("statements(): ADDED WhileNode: " + statementNode);
                if(Shank.DEBUG && statementNode instanceof ForNode) System.out.println("statements(): ADDED ForNode: " + statementNode);


                // Remove any DEDENT or ENDOFLINE
                token = peek(0);
                if(token == null) return statementNodes;
                while(token != null && token.getTokenType() == Token.TokenType.DEDENT || token.getTokenType() == Token.TokenType.ENDOFLINE){
                    switch (token.getTokenType()){
                        case DEDENT -> {
                            expectsToken(Token.TokenType.DEDENT);
                            token = peek(0);
                        }
                        case ENDOFLINE -> {
                            expectsEndOfLine();
                            token = peek(0);
                        }
                        default -> {
                            statementNode = statement();
                        }
                    }
                }

            }

        }
        if(Shank.DEBUG) System.out.println("statements(): Found " + statementNodes.size() + " statements.");
        return statementNodes;
    }

    /**
     * Parses an individual statement.
     * Expects assignment(), parseFunctionCalls(), parseIf(), parseFor() or parseWhile()
     * Expects indentation to be dealt with in statements()
     * @return a StatementNode of the node being assigned.
     * @throws SyntaxErrorException if there was an error parsing the statement
     */
    public StatementNode statement() throws SyntaxErrorException {
        Token token = peek(0);
        if(token == null){
            return null;
        }

        switch (token.getTokenType()){
            case IDENTIFIER -> {
                if(peek(1) != null && peek(1).getTokenType() == Token.TokenType.ASSIGNER){
                    return assignment();
                } else {
                    return parseFunctionCalls();
                }
            }
            case IF -> {
                IfNode ifNode = parseIf();
                if(ifNode != null) System.out.println("statement(): RETURNING IFNODE: " + ifNode);
                return ifNode;
            }
            case WHILE -> {
                return parseWhile();
            }
            case FOR -> {
                return parseFor();
            }
            default -> { return null; }
        }

    }

    //TODO: Process type limits (i.e variables numberOfCards : integer from 0 to 52)
    /**
     * Processes a list of variable declarations.
     * Expected that function() already deals with VARIABLES/CONSTANTS token
     * before using this function to process any declarations.
     * Expects IDENTIFIER [,IDENTIFIER] COLON  [FROM factor() TO factor()]
     * @param isConstants whether the examined declarations are constant or not
     * @return collection of VariableNodes that were found
     */
    public Collection<VariableNode> processDeclarations(boolean isConstants) throws SyntaxErrorException {
        // format for variable declarations:
        // () = optional, [] = required.
        // (var) [identifier] (comma identifier...) [colon/equal] [type/value] (from 0/0.0 to 2/2.0) (semicolon, repeat...)

        Collection<VariableNode> declarations = new ArrayList<>();
        Token token;
        token = peek(0);

        // Use variableNode to build a variable node and then add it to declarations.
        VariableNode variableNode = new VariableNode(null, null, false);
        // Use preprocessed list of VariableNode to build up multiple variables of the same type (i.e a, b, c : integer)
        Collection<VariableNode> preDeclarations = new ArrayList<>();
        while(token != null){
            switch (token.getTokenType()) {

                // Process (VAR) and [IDENTIFIER] (COMMA IDENTIFIER)'s
                case VAR -> {
                    matchAndRemove(Token.TokenType.VAR);
                    variableNode.setConstant(true);
                    token = peek(0);
                }
                case IDENTIFIER -> {
                    // Add to preprocessed list.
                    preDeclarations.add(new VariableNode(null, token.getValue(), false));
                    matchAndRemove(Token.TokenType.IDENTIFIER);
                    // Add to list.
                    token = peek(0);
                }
                case COMMA -> {
                    matchAndRemove(Token.TokenType.COMMA);
                    token = peek(0);
                }
                case COLON -> {
                    matchAndRemove(Token.TokenType.COLON);
                    token = peek(0);
                }

                // Process types.
                case INTEGER -> {
                    matchAndRemove(Token.TokenType.INTEGER);
                    for (VariableNode preDec : preDeclarations) {
                        // We aren't aware of ranges yet, so just create a regular IntegerNode for now.
                        preDec.setType(new IntegerNode(0));
                    }
                    token = peek(0);
                }

                case FLOAT -> {
                    matchAndRemove(Token.TokenType.FLOAT);
                    for (VariableNode preDec : preDeclarations) {
                        preDec.setType(new FloatNode(0.0f));
                    }
                    token = peek(0);
                }

                case STRING -> {
                    matchAndRemove(Token.TokenType.STRING);
                    for (VariableNode preDec : preDeclarations) {
                        preDec.setType(new StringNode(""));
                    }
                    token = peek(0);
                }

                // Process ranges.

                case FROM -> {
                    matchAndRemove(Token.TokenType.FROM);
                    Token fromRange = matchAndRemove(Token.TokenType.NUMBER);
                    assert fromRange != null;
                    matchAndRemove(Token.TokenType.TO);
                    Token toRange = matchAndRemove(Token.TokenType.NUMBER);
                    assert toRange != null;
                    for (VariableNode preDec : preDeclarations) {
                        // Check if the predeclarations are of the right type (IntegerNode, FloatNode or StringNode)
                        if (preDec.getType() instanceof IntegerNode) {
                            preDec.setType(new IntegerNode(0, Integer.parseInt(fromRange.getValue()), Integer.parseInt(toRange.getValue())));
                        } else if (preDec.getType() instanceof FloatNode) {
                            preDec.setType(new FloatNode(0.0f, Float.parseFloat(fromRange.getValue()), Float.parseFloat(toRange.getValue())));
                        } else if (preDec.getType() instanceof StringNode) {
                            preDec.setType(new StringNode("", Integer.parseInt(fromRange.getValue()), Integer.parseInt(toRange.getValue())));
                        }
                        if (Shank.DEBUG) System.out.println("processDeclarations(): processed range " + preDec);
                    }
                    token = peek(0);
                }

                case SEMICOLON -> {
                    matchAndRemove(Token.TokenType.SEMICOLON);
                    // When SEMICOLON is reached, add preDeclarations to declarations and clear preDeclarations to
                    // build any more parameters.
                    declarations.addAll(preDeclarations);
                    preDeclarations.clear();
                    token = peek(0);
                }

                case PARENTHESIS_R, ENDOFLINE -> {
                    declarations.addAll(preDeclarations);
                    if(Shank.DEBUG) System.out.println("processDeclarations(): processed " + declarations.size() + " declarations: " + declarations );
                    return declarations;
                }

            }
        }


        // To keep track of multiple variable names, whether it is a var and value.
        List<String> variableNames = new ArrayList<>();
        boolean var = false;
        String value;
        // integer, string
        int fromRange;
        int toRange;
        // float/real
        float realFromRange;
        float realToRange;
        Node variableType;
/**
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
                case FROM:
                    matchAndRemove(Token.TokenType.FROM);
                    Node fRange = factor();
                    matchAndRemove(Token.TokenType.TO);
                    Node tRange = factor();


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

                case FLOAT:

                    break;
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
 **/
        return declarations;
    }

    /**
     * Processes a for loop statement.
     * Expects tokens FOR, IDENTIFIER, FROM, expression(), to, expression(), ENDOFLINE, statements()
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
                        expectsEndOfLine();
                        Collection<StatementNode> statements = statements();
                        if(Shank.DEBUG) System.out.println("parseFor(): Collected  " + statements.size() + " statements.");
                        ForNode forNode = new ForNode(variableReferenceNode, from, to, statements);
                        if(Shank.DEBUG) System.out.println("parseFor(): Built " + forNode);
                        return forNode;
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
     * Expects tokens WHILE, boolCompare() ENDOFLINE, statements()
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
            expectsEndOfLine();
            if(condition instanceof BooleanCompareNode) {
                Collection<StatementNode> statements = statements();
                WhileNode whileNode = new WhileNode((BooleanCompareNode) condition, statements  );
                if(Shank.DEBUG) System.out.println("parseWhile(): Built " + whileNode);
                return whileNode;
            } else {
                throw new SyntaxErrorException("Expected a condition for while loop, found: " + token);
            }
        } else {
            throw  new SyntaxErrorException("Expected a while loop statement, found: " + token);
        }
    }

    /**
     * Parses an if statement.
     * Expects IF, boolCompare(), THEN, ENDOFLINE, statements()
     * Optionally: ELSIF, ELSE
     *
     * @return a linked list chain of IfNode with potential nextIf nodes.
     * @throws SyntaxErrorException if there was an error processing the IfNode statements.
     */
    public IfNode parseIf() throws SyntaxErrorException {
        Token token = peek(0);
        Node ifNode = new IfNode(null, null, null); //ifNode will be used to build the chain of linked IfNode statements.
        if(token != null){
            if(token.getTokenType() == Token.TokenType.IF || token.getTokenType() == Token.TokenType.ELSIF){
                // Remove IF, ELSIF
                matchAndRemove(token.getTokenType());
                // Gather comparison.
                Node comparison = boolCompare();
                if(comparison instanceof BooleanCompareNode){
                     ((IfNode) ifNode).setCondition((BooleanCompareNode) comparison);
                } else {
                    throw new SyntaxErrorException("Expected a boolean comparison for if statement, found " + comparison);
                }

                if(Shank.DEBUG) System.out.println("parseIf() Found comparison " + ifNode);

                // Gather statements.
                expectsToken(Token.TokenType.THEN);
                expectsEndOfLine();
                ((IfNode) ifNode).setStatements(statements());
                if(Shank.DEBUG) System.out.println("parseIf(): Found statements " + ifNode);

                token = peek(0);
                // Check for more IfNodes.
                if(token != null){
                    // If there are more ELSIF, recursively set ifNode.nextIf to parseIf()
                    if(token.getTokenType() == Token.TokenType.ELSIF){
                        if(Shank.DEBUG) System.out.println("parseIf(): Building ESLIF node -------");
                        ((IfNode) ifNode).setNextIf(parseIf());
                    } else {
                        // If no ELSIF, we're done processing the IfNode.
                        System.out.println(Thread.currentThread().getStackTrace()[2].getMethodName() + "() -> parseIf(): Built ifNode: " + ifNode);

                        return (IfNode) ifNode;
                    }
                }

            }
        }




        return (IfNode) ifNode;
    }

    /**
     * Parses a repeat statement.
     * Expects REPEAT, UNTIL, boolCompare(), ENDOFLINE, statements()
     * @return
     * @throws SyntaxErrorException
     */
    public RepeatNode parseRepeat() throws SyntaxErrorException {
        Token token = peek(0);
        if(token != null){
            matchAndRemove(Token.TokenType.REPEAT);
            matchAndRemove(Token.TokenType.UNTIL);
            Node condition = boolCompare();
            expectsEndOfLine();
            List<StatementNode> statements = statements();
            if(condition instanceof BooleanCompareNode) {
                return new RepeatNode((BooleanCompareNode) condition, statements);
            } else {
                System.err.println("Got non boolean comparison expression: " + condition);
                return null;
            }
        }

        return null;
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
                    if(token.getTokenType() == Token.TokenType.VAR || token.getTokenType() == Token.TokenType.IDENTIFIER){
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
                                    ParameterNode parameter = new ParameterNode(expression());
                                    parameters.add(parameter);
                                    matchAndRemove(Token.TokenType.IDENTIFIER);
                                    token = peek(0);
                                }

                            }

                        }

                        // We've finished processing any parameters.
                        expectsEndOfLine();
                        return new FunctionCallNode(functionName, parameters);

                    } else if(token.getTokenType() == Token.TokenType.ENDOFLINE){
                        // No parameters.
                        return new FunctionCallNode(functionName, null);
                    } else {
                        // Process expression() -> expect expression() COMMA expression() COMMA....
                        //Node parameter = boolCompare();
                        Collection<ParameterNode> parameters = new ArrayList<>();
                        // Already processed function name, so now time to process parameters.
                        parameters.add(new ParameterNode(expression()));
                        while(token != null){
                            if(token.getTokenType() != Token.TokenType.ENDOFLINE){
                                switch (token.getTokenType()){
                                    case COMMA -> {
                                        matchAndRemove(Token.TokenType.COMMA);
                                        parameters.add(new ParameterNode(expression()));
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
