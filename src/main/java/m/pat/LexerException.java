package m.pat;


/**
 * LexerException class to handle any exceptions during lexical analysis.
 */
public class LexerException extends Exception {
    public LexerException(String message){
        super(message);
    }
    public LexerException(Token badToken){
        super("Unexpected token: " + badToken.toString());
    }
}