parser grammar PoolParser;
options { tokenVocab=PoolLexer; }




program : (import_stat | alias_stat | classblock)+;

import_stat : IMPORT (TYPEID | OBJECTID) (AS OBJECTID)? SEMICOLON;

alias_stat : USING (TYPEID | OBJECTID) AS (TYPEID | OBJECTID);

classblock : CLASS TYPEID (INHERITS TYPEID)? LBRACE inClass RBRACE SEMICOLON;

inClass : (access_specifier? attribute | method)+;

attribute : TYPEID OBJECTID (ASSIGN expression)? (COMMA OBJECTID (ASSIGN expression)?)* SEMICOLON;

access_specifier : PUBLIC | PRIVATE;

method : access_specifier (TYPEID | VOID) OBJECTID (LPAREN RPAREN | LPAREN parameters RPAREN)  LBRACE inMethod RBRACE SEMICOLON;


parameters : TYPEID OBJECTID (COMMA TYPEID OBJECTID)* ;

inMethod : (attribute | statement)* ;


statement:
	expressionStatement
	| attribute
	| compoundStatement
	| selectionStatement
	| iterationStatement
	| jumpStatement
	| tryBlock
	;

expressionStatement: expression? SEMICOLON;

compoundStatement: LBRACE statementSeq? RBRACE ;

statementSeq: statement+;

//Selection statement
selectionStatement:
	IF LPAREN expression RPAREN compoundStatement (ELIF LPAREN expression RPAREN compoundStatement)* (ELSE compoundStatement)? SEMICOLON;

//iteration statement
iterationStatement:
	WHILE LPAREN logical_or_expr RPAREN compoundStatement SEMICOLON
	| FOR LPAREN (expressionStatement | attribute) expression SEMICOLON expression? RPAREN compoundStatement SEMICOLON ;


//jump block
jumpStatement:
	(BREAK | CONTINUE | RETURN (expression | SELF)? ) SEMICOLON;


//try block
tryBlock: TRY compoundStatement EXCEPT LPAREN (TYPEID OBJECTID)? RPAREN compoundStatement SEMICOLON;



assignexpression : OBJECTID ASSIGN OBJECTID;

test 	: 	CLASS LPAREN OBJECTID RPAREN;


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
			 | postfix_expr LPAREN (assignment_expr (COMMA assignment_expr)*)? RPAREN
			 | postfix_expr DOT OBJECTID
			 | postfix_expr INCRE_OP
			 | postfix_expr DECRE_OP
             | RAISE LPAREN OBJECTID RPAREN
             ;

unary_operator : PLUS | MINUS | NOT | STAR | BITAND;

primary_expr 	: OBJECTID | INT_CONST | FLOAT_CONST | STR_CONST | BOOL_CONST
				| LPAREN expression RPAREN
				;


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
								| equality_expr TEQUALS relational_expr
								| equality_expr NOTEQUAL relational_expr
        				| equality_expr NOT_TEQUAL relational_expr
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
        | mult_expr POWER unary_expr
		;
