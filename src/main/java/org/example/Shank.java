package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class Shank {

    // Shank Lexer Main
    public static void main(String[] args){
        // Check commandline arguments.
        System.out.println(args.length);
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
                try {
                    // Lex each line in the file.
                    List<String> fileLines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
                    Lexer lexer;
                    for (String line : fileLines) {
                        try{
                            lexer = new Lexer(line);
                            lexer.lex();
                        } catch (Lexer.LexerException e) {
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
