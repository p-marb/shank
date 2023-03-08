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

## Sample code

Here is a tiny example of Shank code, illustrating two different methods one being recursive the other being iterative. 
``` python
{ Recursive }
define add (x,y : integer; var sum : integer)
variables yMinusOne, xPlusOne, newSum : integer
	{ in here, x and y are constant }
	if y = 0 then
		sum := x
	else
		xPlusOne := x + 1
		yMinusOne := y - 1
		add xPlusOne, yMinusOne, var newSum
		sum := newSum

{ Iterative }
define add (x,y:integer, var sum: integer)
variables counter : integer
	counter := y
	sum := x
	while counter > 0
		counter := counter - 1
		sum := sum + 1


{ To call one of the adds: }
define start ()
variables a,b,c : integer
	a := 5
	b := 6
	add a,b,var c
	{ in here, a & b are the original values, but c is whatever add sets sum to }
```
