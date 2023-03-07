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
        Node node = function();
        while(node != null){
            if(node instanceof FunctionNode){
                String name = ((FunctionNode) node).getName();
                if(functions.containsKey(name)){
                    System.out.println("Duplicate function name! (" + name + ")");
                }
                functions.put(((FunctionNode) node).getName(), ((FunctionNode) node));
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

    /**
     * Factor can be either an IntegerNode/FloatNode or MathOpNode.
     * @return IntegerNode/FloatNode or MathOpNode
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
        }
        return null;
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
                                token = peek(0);
                            }

                            List<StatementNode> statements = processStatements();

                            FunctionNode functionNode = new FunctionNode(functionName, parameters, constantsAndVariables, statements);
                            return functionNode;

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
     * Processes list of statements.
     * For now will read until indent level is back to zero,
     * indicating that the function has ended.
     * @return list of statements or null
     * @throws SyntaxErrorException if processing statements failed
     */
    public List<StatementNode> processStatements() throws SyntaxErrorException {
        Token token;
        matchAndRemove(Token.TokenType.INDENT);
        token = peek(0);
        while(token != null && indentLevel > 0){
            matchAndRemove(token.getTokenType());
            token = peek(0);
        }
        return null;
    }

}
