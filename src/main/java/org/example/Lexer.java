package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Lexer {

    List<Token> tokenList = new ArrayList<>();
    HashMap<Token.TokenType, String> knownWords = new HashMap<>();
    private String input;
    private int index = 0;
    private static int lineCounter;

    /**
     * Instantiates a Lexer.
     * @param input the line of text to lex
     */
    public Lexer(String input){
        this.input = input;
        lineCounter++;
        initialize();
    }

    /**
     * Helper function for initializing tokens and known words.
     */
    public void initialize(){
        knownWords.put(Token.TokenType.DEFINE, "define");
        knownWords.put(Token.TokenType.VARIABLES, "variables");
        knownWords.put(Token.TokenType.CONSTANTS, "constants");

        knownWords.put(Token.TokenType.IF, "if");
        knownWords.put(Token.TokenType.WHILE, "while");
        knownWords.put(Token.TokenType.FOR, "for");
        knownWords.put(Token.TokenType.FROM, "from");
        knownWords.put(Token.TokenType.TO, "to");
        knownWords.put(Token.TokenType.REPEAT, "repeat");
        knownWords.put(Token.TokenType.BLOCK, "block");
        knownWords.put(Token.TokenType.THEN, "then");
        knownWords.put(Token.TokenType.UNTIL, "until");
        knownWords.put(Token.TokenType.NOT, "not");
        knownWords.put(Token.TokenType.VAR, "var");
        knownWords.put(Token.TokenType.INTEGER, "integer");

        knownWords.put(Token.TokenType.WRITE, "write");

        knownWords.put(Token.TokenType.STRINGLITERAL, "\"");
        knownWords.put(Token.TokenType.CHARACTERLITERAL, "\'");
        knownWords.put(Token.TokenType.COMMA, ",");
        knownWords.put(Token.TokenType.PARENTHESIS_R, ")");
        knownWords.put(Token.TokenType.PARENTHESIS_L, "(");
        knownWords.put(Token.TokenType.COMMENTBLOCK_L, "{");
        knownWords.put(Token.TokenType.COMMENTBLOCK_R, "}");

        knownWords.put(Token.TokenType.PLUS, "+");
        knownWords.put(Token.TokenType.MINUS, "-");
        knownWords.put(Token.TokenType.MULTIPLY, "*");
        knownWords.put(Token.TokenType.DIVIDE, "/");
        knownWords.put(Token.TokenType.MODULUS, "mod");
        knownWords.put(Token.TokenType.ASSIGNER, ":=");
        knownWords.put(Token.TokenType.NOT_EQUAL, "<>");
        knownWords.put(Token.TokenType.LESS_OR_EQUAL, "<=");
        knownWords.put(Token.TokenType.GREATER_OR_EQUAL, ">=");
        knownWords.put(Token.TokenType.EQUALS, "=");
        knownWords.put(Token.TokenType.LESS_THAN, "<");
        knownWords.put(Token.TokenType.GREATER_THAN, ">");
        knownWords.put(Token.TokenType.COLON, ":");
        knownWords.put(Token.TokenType.SEMICOLON, ";");


    }

    /**
     * State of the lexer through lexical analysis.
     */
    enum LexState {
        NONE,
        WORD,
        NUMBER,
        STRINGLITERAL, // Strings "this is a string"
        CHARACTERLITERAL, // Characters 'c'
        COMMENT
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
                    else if(c == knownWords.get(Token.TokenType.STRINGLITERAL).charAt(0))
                        // If the character is a string literal ("")
                        currentState = LexState.STRINGLITERAL;
                    else if(c == knownWords.get(Token.TokenType.CHARACTERLITERAL).charAt(0))
                        // If the character is a character literal ('')
                        currentState = LexState.CHARACTERLITERAL;
                    else if(c == knownWords.get(Token.TokenType.COMMENTBLOCK_L).charAt(0))
                        currentState = LexState.COMMENT;
                    else if (tryMatch(c))
                        currentState = LexState.NONE;
                    else if (Character.isWhitespace(c))
                        index++;
                    else
                        throw new LexerException(errorMessage(c, index));
                    break;
                case WORD:
                    // Words can contain both numbers and letters.
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
                case STRINGLITERAL:
                    // Read until the next " is found
                    String string = readUntil('\"', false);
                    addToken(new Token(Token.TokenType.STRINGLITERAL, string));
                    currentState = LexState.NONE;
                    break;
                case CHARACTERLITERAL:
                    String character = readUntil('\'', false);
                    addToken(new Token(Token.TokenType.CHARACTERLITERAL, character));
                    currentState = LexState.NONE;
                    break;
                case COMMENT:
                    addToken(new Token(Token.TokenType.COMMENTBLOCK_L, ""));
                    String comment = readUntil('}', false);
                    addToken(new Token(Token.TokenType.COMMENT, comment));
                    currentState = LexState.NONE;
                    break;

            }
        }
        addToken(new Token(Token.TokenType.ENDOFLINE, ""));
    }

    /**
     * Generates an error message pointing to the problematic character.
     * @param c the character to point to
     * @param i the current index
     * @return a string of the message
     */
    public String errorMessage(char c, int i){
        String message = "Unknown symbol at line " + lineCounter + " index " + i + ":\n" +
                input + "\n"; // Print the entire input line.
        for(int j = 0; j < input.length(); j++) {
            // Loop through until we find the problem char.
            if (j == i) {
                message += "^";
            } else if(j > i){
                message += "~";
            }
            message += " ";
        }
        message += "\n";
        return message;
    }

    /**
     * Helper function to try and match a token once all other available methods have been used.
     * @param c the character to start matching from
     * @return true if a match was found
     */
    public boolean tryMatch(char c){
        // For each known word in the key set
        int i = 0;
            for (String knownWord : knownWords.values()) {
                // Take length and see if the current input at index until index + length equals the known word
                int len = knownWord.length();
                // Make sure the word can fit before checking
                if (index + len <= input.length()) {
                    String str = input.substring(index, index + len);
                    if (str.equals(knownWord)) {
                        addToken(new Token(
                                (Token.TokenType) knownWords.keySet().toArray()[i], str));
                        index = index + len;
                        return true;
                    }
                }
                i++;
            }

        return false;
    }

    /**
     * Helper function to read until a certain character is met.
     * @param c - the character to read until
     * @param include - whether to include the surrounding characters.
     * @return - a string of what was read
     */
    public String readUntil(char c, boolean include){
        int start = index;
        while(index < input.length() && (input.charAt(index) != c)){
            index++;
        }
        String read;
        if(include)
         read = input.substring(start, index+1);
        else
            read = input.substring(start+1, index);
        return read;
    }

    /**
     * Add a token to the list of tokens lexed.
     * @param token the token to add
     */
    public void addToken(Token token){
        tokenList.add(token);
        System.out.println("New Token (L:" + lineCounter + " I:" + index + ") " + token.toString());
    }

    /**
     * Attempts to match a word to a token.
     * @param word the word to try and match
     * @return null if no token type was found
     */
    private Token.TokenType matchWord(String word){
        // Loop through all the tokens that are defined.
        int i = 0;
        for(String knownWord : knownWords.values()){
            // Check to see if word matches any of the known words (tokens).
            if(Objects.equals(knownWord, word)){
                // Return the token type.

                return
                        (Token.TokenType) knownWords.keySet().toArray()[i];
            }
            i++;
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
