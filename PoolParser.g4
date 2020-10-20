parser grammar PoolParser;
options { tokenVocab=PoolLexer; }

program : test assignexpression test EOF
		| test EOF
		;

assignexpression : OBJECTID ASSIGN OBJECTID;
	
test 	: 	CLASS LPAREN OBJECTID RPAREN;