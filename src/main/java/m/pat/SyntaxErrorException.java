package m.pat;


/**
 * Syntax Error Exception
 */
public class SyntaxErrorException extends Exception {
    public SyntaxErrorException(String message){
        super(message);
    }

    public SyntaxErrorException(Token token){
        super("Unexpected token: " + token.toString());
    }
}