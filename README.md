# Compiler-II-Project
Group Project
---
## Language: POOL
---
## Team: 

_Revanth_  : @rocky769

_Vishal Siva Kumar_  : @gvishalin

_Chaitanya Janakie_  : @chaitanya3699

_Prajwal_ : @Tyler-Durden-official

_Dheekshitha_ : @bdheekshitha1210

_Krishna Prashanth_ : @krishnaprashanth246

---

We have written Lexer and Parser using ANTLR V4.5 .

##Building Lexer and Parser:

We need ANTLR 4.5 for building the lexer and parser.

We have added a makefile and a test script which installs ANTLR if not present.

The makefile tests whether _antlr-4.5-complete.jar_ is present in _/usr/local/lib/_.

The makefile makes use of testscript and builds the lexer and parser automatically.

Command format:

	$ make INPUT_FILE=<testfile>  		# Here INPUT_FILE is a variable for the makefile which has the input file name with relative path

When make is invoked, Java, Tokens and class files are generated, followed by testing the lexer and parser on the input file given.

For testing, we have used ANTLR's TestRig package. It prints out all the tokens and the parse tree onto stdout 
as well as shows the GUI representation of parse tree as a picture.

The running of script stops after closing the gui.

removes all the .class, .tokens, and .java files generated:

	$make clean 
	
If any syntax errors occur,the inbuilt ANTLR itself raises the error and is reflected in AST.
If no syntax error occurs,No error is shown.
