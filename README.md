# shank

Shank is a Computer Science project written for my college class.
It is an intrepreted language and syntactically very similar to
Python and Java. 

## Usage

Currently this example of Shank only supports interpreting one file, this may change since writing this! To use this on a file,
simply run the program with your text file as the 1st argument. Shank will lex the file and catch any bad tokens, then it will
pass the list of tokens to the parser to generate AST nodes and finally it will be interpreted. 

## Notes

Shank doesn't use any third party libraries to run Shank code. That means no lex library, no parse library, no interpreter library, etc. 
This was the goal of this project as it was supposed to teach very thouroughly the ins and outs of programming language design. 
