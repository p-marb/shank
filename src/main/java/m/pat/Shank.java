package m.pat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Shank {

    public static boolean DEBUG = true;

    // Shank Lexer Main
    public static void main(String[] args){
        // Check commandline arguments.
        if(args.length != 1){
            System.err.println("Error: you can only have 1 commandline argument.");
        } else {
            System.out.println("Trying to access file: " + args[0]);
            File file = new File(args[0]);
            // Check if file actually exists.
            if (!file.exists()) {
                System.err.println("Error: the file (" + file.getAbsolutePath() + ") does not exist.");
            } else {
                System.out.println("File found. Attempting to lex...");
                long lexStartTime = System.currentTimeMillis();
                try {
                    // Lex each line in the file.
                    List<String> fileLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                    Lexer lexer = new Lexer();
                    Lexer.initialize();
                    int errorCount = 0;
                    for (String line : fileLines) {
                        try{
                            lexer.lex(line);
                        } catch (LexerException e) {
                            errorCount++; // Keep track of any errors.
                            e.printStackTrace();
                        }
                    }
                    List<Token> tokenList = new ArrayList<>(lexer.getTokens());
                    long lexEndTime = System.currentTimeMillis();
                    System.out.println("Finished lexical analysis (" + (lexEndTime - lexStartTime) + " milliseconds)");
                    System.out.println("Total tokens - " + tokenList.size() + " - Total errors - " + errorCount);
                    long parseStartTime = System.currentTimeMillis();
                    if(errorCount == 0){
                        // Only move on if there are no errors in the lexical analysis.
                        Parser parser = new Parser(tokenList);
                        ProgramNode programNode;
                        try{
                            programNode = (ProgramNode) parser.parse();
                            if(programNode != null){
                                long parseEndTime = System.currentTimeMillis();
                                System.out.println("Finished parsing (" + (parseEndTime - parseStartTime) + " milliseconds)");
                                System.out.println("ProgramNode: " + programNode);
                            }
                        } catch(SyntaxErrorException e){
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error reading all lines from file: ");
                    e.printStackTrace();
                }
            }
        }
    }
}
