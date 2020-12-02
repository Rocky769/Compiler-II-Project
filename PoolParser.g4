parser grammar PoolParser;
options { tokenVocab=PoolLexer; }


program : (import_stat | alias_stat | classblock)+;

import_stat : IMPORT (TYPEID | OBJECTID) (AS OBJECTID)? SEMICOLON;

alias_stat : USING (TYPEID | OBJECTID) AS (TYPEID | OBJECTID);

classblock : CLASS TYPEID (INHERITS TYPEID)? LBRACE inClass RBRACE SEMICOLON;

inClass : (access_specifier? declaration | method)+;

declaration: TYPEID init_declarator_list SEMICOLON ;

declaration_list : declaration+ ;

init_declarator_list
	: init_declarator
	| init_declarator_list COMMA init_declarator
	;

init_declarator
	: declarator
	| declarator ASSIGN initializer
	;

initializer
	: assignment_expr
	| LBRACE initializer_list RBRACE
	;

initializer_list
	: initializer
	| initializer_list COMMA initializer
	;

declarator
	: pointer direct_declarator
	| direct_declarator
	;

direct_declarator
	: OBJECTID
	| LPAREN declarator RPAREN
	| direct_declarator LSQUARE const_expr? RSQUARE
	| direct_declarator LPAREN (parameter_list | identifier_list)? RPAREN
	;

pointer
	: STAR
	| STAR pointer
	;

parameter_list
	: parameter_declaration
	| parameter_list COMMA parameter_declaration
	;

parameter_declaration
	: TYPEID declarator
	| TYPEID abstract_declarator
	| TYPEID
	;

identifier_list
	: OBJECTID
	| identifier_list COMMA OBJECTID
	;

type_name
	: TYPEID
	| TYPEID abstract_declarator
	;

abstract_declarator
	: pointer
	| direct_abstract_declarator
	| pointer direct_abstract_declarator
	;

direct_abstract_declarator
	: LPAREN abstract_declarator RPAREN
	| LSQUARE const_expr? RSQUARE
	| direct_abstract_declarator LSQUARE const_expr? RSQUARE
	| LPAREN parameter_list? RPAREN
	| direct_abstract_declarator LPAREN parameter_list? RPAREN
	;

access_specifier : PUBLIC | PRIVATE;

method : access_specifier (TYPEID | VOID) OBJECTID (LPAREN RPAREN | LPAREN parameters RPAREN)  LBRACE inMethod RBRACE SEMICOLON;

parameters : TYPEID OBJECTID (COMMA TYPEID OBJECTID)* ;

inMethod : (declaration | statement)* ;

statement:
	expressionStatement
	| compoundStatement
	| selectionStatement
	| iterationStatement
	| jumpStatement
	| tryBlock
	;

expressionStatement: expression? SEMICOLON;

compoundStatement: LBRACE  declaration_list? statementSeq? RBRACE ;

statementSeq: statement+;

//Selection statement
selectionStatement:
	IF LPAREN expression RPAREN compoundStatement (ELIF LPAREN expression RPAREN compoundStatement)* (ELSE compoundStatement)? SEMICOLON;

//iteration statement
iterationStatement:
	WHILE LPAREN logical_or_expr RPAREN compoundStatement SEMICOLON
	| FOR LPAREN (expressionStatement | declaration) expression SEMICOLON expression? RPAREN compoundStatement SEMICOLON ;

//jump block
jumpStatement:
	(BREAK | CONTINUE | RETURN (expression | SELF)? ) SEMICOLON;

//try block
tryBlock: TRY compoundStatement EXCEPT LPAREN (TYPEID OBJECTID)? RPAREN compoundStatement SEMICOLON;

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

const_expr : logical_or_expr ;

new_expr :
	NEW TYPEID (LSQUARE const_expr RSQUARE)? (LPAREN initializer_list? RPAREN)? ;

del_expr:
	DELETE (LSQUARE RSQUARE)? cast_expr;

unary_expr : postfix_expr
			| (INCRE_OP | DECRE_OP) unary_expr
			| unary_operator cast_expr
			| new_expr
			| del_expr
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

primary_expr 	: OBJECTID | INT_CONST | FLOAT_CONST | STR_CONST | BOOL_CONST | CHAR_CONST
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

add_expr 	: mult_expr
			| add_expr PLUS mult_expr
			| add_expr MINUS mult_expr
			;

mult_expr 	: cast_expr
			| mult_expr STAR cast_expr
			| mult_expr SLASH cast_expr
			| mult_expr MOD cast_expr
        	| mult_expr POWER cast_expr
			;
cast_expr 	: unary_expr
			| LPAREN type_name RPAREN cast_expr
			;
