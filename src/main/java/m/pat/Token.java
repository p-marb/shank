package m.pat;

public class Token {
    enum TokenType {
        /* Very primitive types */
        IDENTIFIER,
        NUMBER,
        DEFINE,
        ENDOFLINE,

        /* Definitions */
        VARIABLES,
        CONSTANTS,

        /* Loops */
        WHILE,
        FOR,
        FROM,
        TO,
        REPEAT,
        BLOCK,

        WRITE,

        /* Operators */
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        MODULUS,
        EQUALS,
        NOT_EQUAL,
        GREATER_THAN,
        LESS_THAN,
        GREATER_OR_EQUAL,
        LESS_OR_EQUAL,
        ASSIGNER,
        INDEX_L, //[
        INDEX_R, //]

        /* Boolean Logic */
        PARENTHESIS_L, //(
        PARENTHESIS_R, //)
        IF,
        ELSIF,
        ELSE,
        AND,
        OR,
        NOT,
        THEN,
        UNTIL,
        TRUE,
        FALSE,

        STRINGLITERAL,
        CHARACTERLITERAL,
        COMMENTBLOCK_L,
        COMMENTBLOCK_R,
        COLON,
        SEMICOLON,
        VAR,
        INTEGER,
        FLOAT,
        CHARACTER,
        STRING,
        BOOLEAN,
        COMMA,
        INDENT,
        DEDENT,
    }

    private final TokenType tokenType;
    private final String value;

    /**
     * Instantiates a Token to be used in lexing.
     * @param tokenType the type of token
     * @param value the value of the token
     */
    public Token(TokenType tokenType, String value){
        this.tokenType = tokenType;
        this.value = value;
    }

    /**
     * The type of token.
     * @return the type of token
     */
    public TokenType getTokenType(){
        return this.tokenType;
    }

    public boolean is(TokenType tokenType){
        return this.tokenType == tokenType;
    }

    /**
     * The value of the token
     * @return value of token
     */
    public String getValue(){
        return this.value;
    }

    /**
     * Helper function to determine whether token is a
     * comparison token or not.
     * @return BooleanComparison of compare type or null
     */
    public BooleanComparison isComparisonNode(){
        switch(this.tokenType){
            case EQUALS:
                return BooleanComparison.EQUALS;
            case NOT_EQUAL:
                return BooleanComparison.NOT_EQUALS;
            case GREATER_THAN:
                return BooleanComparison.GREATER_THAN;
            case GREATER_OR_EQUAL:
                return BooleanComparison.GREATER_OR_EQUAL;
            case LESS_THAN:
                return BooleanComparison.LESS_THAN;
            case LESS_OR_EQUAL:
                return BooleanComparison.LESS_OR_EQUAL;
            default:
                return null;
        }
    }

    public boolean isType(){
        switch (tokenType){
            case INTEGER:
            case FLOAT:
            case STRING:
            case BOOLEAN:
            case CHARACTER:
                return true;
        }
        return false;
    }

    public String toString(){
        return getTokenType().name() + "(" + getValue() + ")";
    }

}
