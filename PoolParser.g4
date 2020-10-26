parser grammar PoolParser;
options { tokenVocab=PoolLexer; }

/*program : test assignexpression test EOF
		| test EOF
		| expression SEMICOLON EOF
		;*/



program : (import_stat | classblock)+;

import_stat : IMPORT (TYPEID | OBJECTID) SEMICOLON;

classblock : CLASS TYPEID (INHERITS TYPEID)? LBRACE inClass RBRACE SEMICOLON;

inClass : (classblock | attribute | method)+;//what about private public?

attribute : TYPEID OBJECTID (ASSIGN expression)? (COMMA OBJECTID (ASSIGN expression)?)* SEMICOLON;
//Int a := 5, b := 7; 

method : (TYPEID | VOID) OBJECTID (LPAREN RPAREN | LPAREN parameters RPAREN)  LBRACE inMethod RBRACE SEMICOLON;

parameters : TYPEID OBJECTID (COMMA TYPEID OBJECTID)* ;

inMethod : attribute* statements ;

expression : 

statements : 





assignexpression : OBJECTID ASSIGN OBJECTID;
	
test 	: 	CLASS LPAREN OBJECTID RPAREN;
/*
declaration= 
TYPEID OBJECTID [ assignment ]; */

expression  : assignment_expr
			| expression COMMA assignment_expr
			;

assignment_expr : logical_or_expr
				| unary_expr assign_op assignment_expr
				;

assign_op :
		ASSIGN | MUL_ASSIGN | DIV_ASSIGN
		| ADD_ASSIGN | SUB_ASSIGN
		| AND_ASSIGN | OR_ASSIGN
		;

unary_expr : primary_expr
			| unary_operator unary_expr
			;

unary_operator : PLUS | MINUS | NOT ;

primary_expr 	: OBJECTID
				| num 
				| LPAREN expression RPAREN
				;
num :	INT_CONST | FLOAT_CONST;


logical_or_expr : logical_and_expr
				| logical_or_expr OR logical_and_expr
				;

logical_and_expr: equality_expr
				| logical_and_expr AND equality_expr
				;

equality_expr 	: relational_expr
				| equality_expr EQUALS relational_expr
				| equality_expr NOTEQUAL relational_expr
				;

relational_expr : add_expr
				| relational_expr LT add_expr
				| relational_expr GT add_expr
				| relational_expr LE add_expr
				| relational_expr GE add_expr
				;

add_expr : mult_expr
			| add_expr PLUS mult_expr
			| add_expr MINUS mult_expr
			;

mult_expr : unary_expr
		| mult_expr STAR unary_expr
		| mult_expr SLASH unary_expr
		;
