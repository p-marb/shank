package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Lexer {

    List<Token> tokenList = new ArrayList<>();
    HashMap<String, Token.TokenType> knownWords = new HashMap<String, Token.TokenType>();
    private String input;
    private int index = 0;

    /**
     * Instantiates a Lexer.
     * @param input the line of text to lex
     */
    public Lexer(String input){
        this.input = input;
        initialize();
        //this.knownWords.put("while")
    }

    /**
     * Helper function for initializing tokens and known words.
     */
    public void initialize(){
        knownWords.put("variables", Token.TokenType.VARIABLES);
        knownWords.put("constants", Token.TokenType.CONSTANTS);

        knownWords.put("if", Token.TokenType.IF);
        knownWords.put("while", Token.TokenType.WHILE);
        knownWords.put("for", Token.TokenType.FOR);
        knownWords.put("from", Token.TokenType.FROM);
        knownWords.put("to", Token.TokenType.TO);
        knownWords.put("repeat", Token.TokenType.REPEAT);
        knownWords.put("block", Token.TokenType.BLOCK);
        knownWords.put("then", Token.TokenType.THEN);
        knownWords.put("until", Token.TokenType.UNTIL);

        knownWords.put("write", Token.TokenType.WRITE);
        knownWords.put("{", Token.TokenType.COMMENT_BLOCK_L);
        knownWords.put("}", Token.TokenType.COMMENT_BLOCK_R);

        knownWords.put("+", Token.TokenType.PLUS);
        knownWords.put("-", Token.TokenType.MINUS);
        knownWords.put("*", Token.TokenType.MULTIPLY);
        knownWords.put("/", Token.TokenType.DIVIDE);
        knownWords.put("mod", Token.TokenType.MODULUS);
        knownWords.put(":=", Token.TokenType.ASSIGNER);
        knownWords.put("=", Token.TokenType.EQUALS);
        knownWords.put("<>", Token.TokenType.NOT_EQUAL);
        knownWords.put("<", Token.TokenType.LESS_THAN);
        knownWords.put("<=", Token.TokenType.LESS_OR_EQUAL);
        knownWords.put(">", Token.TokenType.GREATER_THAN);
        knownWords.put(">=", Token.TokenType.GREATER_OR_EQUAL);
    }

    /**
     * State of the lexer through lexical analysis.
     */
    enum LexState {
        NONE,
        WORD,
        NUMBER,
        OPERATOR,
        STRINGLITERAL, // Strings "this is a string"
        CHARACTERLITERAL // Characters 'c'
    }

    public void lex() throws LexerException {
        LexState currentState = LexState.NONE;
        // Loop through each character
        while(index < input.length()) {
            char c = input.charAt(index);
            switch (currentState) {
                default:
                case NONE:
                    // We're at the beginning of a token.
                    if(Character.isLetter(c))
                        // If the character is a letter
                        currentState = LexState.WORD;
                    else if(Character.isDigit(c))
                        // If the character is a number
                        currentState = LexState.NUMBER;
                    else if(c == '"')
                        // If the character is a string literal ("")
                        currentState = LexState.STRINGLITERAL;
                    else if(c == '\'')
                        // If the character is a character literal ('')
                        currentState = LexState.CHARACTERLITERAL;

                    else
                        index++;
                    break;
                case WORD:
                    // Words can contain both numbers and letters.
                    //TODO: Add a method for known words (for, mod, while, etc.)
                    if(Character.isLetterOrDigit(c)) {

                        // Read the word fully, then try and match it to a token.
                        String value = readWord();
                        Token.TokenType tokenType = matchWord(value);

                        // Check to see if the word matched any tokens, if not, assign it as an IDENTIFIER.
                        if(tokenType != null) {
                            addToken(new Token(tokenType, value));
                        } else {
                            addToken(new Token(Token.TokenType.IDENTIFIER, value));
                        }
                        currentState = LexState.NONE;
                    }

                    break;
                case NUMBER:
                    if(Character.isDigit(c)){
                        int start = index;

                        String value = input.substring(start, index);
                        addToken(new Token(Token.TokenType.NUMBER, readNumber()));

                        currentState = LexState.NONE;

                    }

                    break;
                case OPERATOR:
                    /**
                    Token tok;
                    if((tok = readOperatorFully(c)) != null){
                        addToken(tok);

                        currentState = LexState.NONE;
                    } else {
                        System.err.println("Error reading operator: " + c );
                    }
                     **/
                    break;

            }
        }
    }

    /**
     * Add a token to the list of tokens lexed.
     * @param token the token to add
     */
    public void addToken(Token token){
        tokenList.add(token);
        System.out.println("New token: " + token.toString());
    }

    /**
     * Attempts to match a word to a token.
     * @param word the word to try and match
     * @return null if no token type was found
     */
    private Token.TokenType matchWord(String word){
        // Loop through all the tokens that are defined.
        for(String knownWord : knownWords.keySet()){
            // Check to see if word matches any of the known words (tokens).
            if(Objects.equals(knownWord, word)){
                // Return the token type.
                return knownWords.get(knownWord);
            }
        }
        // No token could be matched.
        return null;
    }

    /**
     * Helper method to read the rest of a word, given it is alphanumeric.
     * @return the word that was read
     */
    private String readWord() {
        int start = index;
        while (index < input.length() && isAlphanumeric(input.charAt(index))) {
            index++;
        }
        return input.substring(start, index);
    }

    /**
     * Helper method to read digits.
     * @return the number read from digits
     * @throws LexerException if the digit parsed is not valid
     */
    private String readNumber() throws LexerException {
        int start = index;
        while (index < input.length() && (Character.isDigit(input.charAt(index)) || input.charAt(index) == '.')) {
            index++;
        }
        String number = input.substring(start, index);
        try {
            Double.parseDouble(number);
        } catch (NumberFormatException e) {
            throw new LexerException("Invalid number format: " + number);
        }
        return number;
    }
    /**
     * Whether the character is alphanumeric or not.
     * @param c the character to analyze
     * @return
     */
    private boolean isAlphanumeric(char c){
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || Character.isDigit(c);
    }

    private boolean isOperator(char c){
        return (c == ':');
    }

    private Token.TokenType matchOperator(char c){
        // First, we must see if the character is >, < or :
        for(String knownWord : knownWords.keySet()){
            if(knownWord.length() == 1){
                // We must check the next character in the stream, to correctly validate whether we are matching just
                // > or >=, <>, etc.
            }
        }
        return null;
    }

    /**
     * LexerException class to handle any exceptions during lexical analysis.
     */
    public static class LexerException extends Exception {
        public LexerException(String message){
            super(message);
        }
        public LexerException(Token badToken){
            super("Unexpected token: " + badToken.toString());
        }
    }
}
