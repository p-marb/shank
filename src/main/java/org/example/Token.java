package org.example;

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
        AND,
        OR,
        NOT,
        THEN,
        UNTIL,

        STRINGLITERAL,
        CHARACTERLITERAL,
        COMMENTBLOCK_L,
        COMMENT, // to house the body of the comment
        COMMENTBLOCK_R,
        COLON,
        SEMICOLON,
        VAR,
        INTEGER,
        COMMA,
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

    /**
     * The value of the token
     * @return value of token
     */
    public String getValue(){
        return this.value;
    }

    public String toString(){
        return getTokenType().name() + "(" + getValue() + ")";
    }

}
