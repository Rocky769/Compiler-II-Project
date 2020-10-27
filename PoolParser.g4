parser grammar PoolParser;
options { tokenVocab=PoolLexer; }


/*program : test assignexpression test EOF
		| test EOF
		| expression SEMICOLON EOF
		;*/



program : (import_stat | alias_stat | classblock)+;

import_stat : IMPORT (TYPEID | OBJECTID) (AS OBJECTID)? SEMICOLON;

alias_stat : USING (TYPEID | OBJECTID) AS (TYPEID | OBJECTID);

classblock : CLASS TYPEID (INHERITS TYPEID)? LBRACE inClass RBRACE SEMICOLON;

inClass : (classblock | attribute | method)+;//what about private public?

attribute : TYPEID OBJECTID (ASSIGN expression)? (COMMA OBJECTID (ASSIGN expression)?)* SEMICOLON;
//Int a := 5, b := 7; 


method : (TYPEID | VOID) OBJECTID (LPAREN RPAREN | LPAREN parameters RPAREN)  LBRACE inMethod RBRACE SEMICOLON;


parameters : TYPEID OBJECTID (COMMA TYPEID OBJECTID)* ;

inMethod : attribute* statements ;

//attributes :
/**COOL GRAMMAR
inClass : insideClass* ;
insideClass
   : TYPEID OBJECTID '(' (formal (',' formal)*)? ')'  '{' (expression SEMICOLON)* '}' SEMICOLON # method
   | TYPEID OBJECTID (ASSIGN expression)? SEMICOLON # property
   ;

formal
   : TYPEID OBJECTID 
   ;
// method argument 
   
   
expression
   : expression ('@' TYPEID)? '.' OBJECTID '(' (expression (',' expression)*)? ')' # methodCall
   | OBJECTID '(' (expression (',' expression)*)? ')' # ownMethodCall
   | IF expression THEN expression ELSE expression FI # if
   | WHILE expression LOOP expression POOL # while
   | 
   | 
   | NEW TYPEID # new
   | INTEGER_NEGATIVE expression # negative
   | 
   | 
   | relational_expr
   | NOT expression # boolNot
   | '(' expression ')' # parentheses
   | OBJECTID # id
   | INT # int
   | STRING # string
   | TRUE # true
   | FALSE # false
   | OBJECTID ASSIGNMENT expression # assignment
   ;*/

//statements : 
statement:
	(
		expressionStatement
		| compoundStatement
		| selectionStatement
		| iterationStatement
		| jumpStatement
		| tryBlock
	)
	| declarationStatement;

expressionStatement: expression? SEMICOLON;

compoundStatement: LBRACE statementSeq? RBRACE;

statementSeq: statement+;

//Selection statement
selectionstatement:
	IF LPAREN expression RPAREN compoundStatement (ELIF LPAREN expression RPAREN compoundStatement)* (ELSE compoundStatement)?;
//iteration statement
iterationStatement:
	WHILE LPAREN logical_or_expr RPAREN statement
	| FOR LPAREN ( expressionStatement  condition? SEMICOLON expression? ) RPAREN statement;


//jump block
jumpStatement:
	(BREAK | CONTINUE | RETURN (expression )? ) SEMICOLON;


//try block
tryBlock: TRY compoundStatement EXCEPT compoundStatement;


//declaration statement




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
				| STR_CONST
				| LPAREN expression RPAREN
				;

num :	INT_CONST | FLOAT_CONST; 


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

