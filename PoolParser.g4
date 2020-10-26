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


//statements : 



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
		| ADD_ASSIGN | SUB_ASSIGN | MOD_ASSIGN
		| AND_ASSIGN | OR_ASSIGN | XOR_ASSIGN
		| LSHIFT_ASSIGN | RSHIFT_ASSIGN
		;

const_expr : logical_or_expr
		   ;

unary_expr : primary_expr
			| unary_operator unary_expr
			| postfix_expr
			| (INCRE_OP | DECRE_OP) unary_expr
			;

postfix_expr : primary_expr
			 | postfix_expr LSQUARE expression RSQUARE
			 | postfix_expr // write argument assignment list 
			 | postfix_expr DOT OBJECTID
			 | postfix_expr INCRE_OP
			 | postfix_expr DECRE_OP

unary_operator : PLUS | MINUS | NOT | STAR | BITAND;

primary_expr 	: OBJECTID
				| num 
				| LPAREN expression RPAREN
				;

num :	INT_CONST | FLOAT_CONST; //what about char const and string 


logical_or_expr : logical_and_expr
				| logical_or_expr OR logical_and_expr
				; 

logical_and_expr: or_expr
				| logical_and_expr AND or_expr
				;

or_expr : xor_expr
		| or_expr BITOR xor_expr
		;

xor_expr : and_expr
		 | xor_expr BITXOR and_expr
		 ;

and_expr : equality_expr
		 | and_expr BITAND equality_expr
		 ;

equality_expr 	: relational_expr
				| equality_expr EQUALS relational_expr
				| equality_expr NOTEQUAL relational_expr
				;

relational_expr : shift_expr
				| relational_expr LT shift_expr
				| relational_expr GT shift_expr
				| relational_expr LE shift_expr
				| relational_expr GE shift_expr
				;

shift_expr  : add_expr
			| shift_expr LSHIFT add_expr
			| shift_expr RSHIFT add_expr
			;

add_expr : mult_expr
			| add_expr PLUS mult_expr
			| add_expr MINUS mult_expr
			;

mult_expr : unary_expr
		| mult_expr STAR unary_expr
		| mult_expr SLASH unary_expr
		| mult_expr MOD unary_expr
		;

