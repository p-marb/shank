package m.pat;

import java.util.List;

public class Parser {

    //TODO: Change `tokens` to a Queue.

    private List<Token> tokens;
    private int currentIndex;
    private Token currentToken;

    Parser(List<Token> tokens){
        this.tokens = tokens;
        this.currentIndex = 0;
        this.currentToken = tokens.get(0);
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
            // Token is  match, remove and return it.
            //System.out.println("matchAndRemove(" + tokens.get(currentIndex) + ")");
            System.out.println("Parser: Removed " + tokenType.name() + "(" + tok.getValue() + ") index " + currentIndex + "/" + tokens.size());
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
        if(token.getTokenType() == Token.TokenType.ENDOFLINE){
            matchAndRemove(Token.TokenType.ENDOFLINE);
            token = peek(0);

            if(token != null && token.getTokenType() == Token.TokenType.ENDOFLINE){
                expectsEndOfLine();
            }
            return token;
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

    public Node parse() throws SyntaxErrorException {
        System.out.println("Beginning parsing...");
        // First thing we do is find the left expression.
        Node node;
        while((node = expression()) != null){
            try{
                if(expectsEndOfLine() != null){
                    if (node instanceof MathOpNode) {
                        System.out.println("MathOpNode("
                                + ((MathOpNode) node).getOperation() + ", "
                                + ((MathOpNode) node).getLeft().toString() + ", " +
                                ((MathOpNode) node).getRight().toString() + ")");
                    }
                }
            } catch(SyntaxErrorException e){
                e.printStackTrace();
            }
        }
        throw new SyntaxErrorException("Parsing found unidentified token: " + tokens.get(currentIndex));
    }

    /**
     * Expression is a MathOpNode, with left and right term and a math operation.
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
     * Term can be either an IntegerNode/RealNode or MathOpNode.
     * @return
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
}
