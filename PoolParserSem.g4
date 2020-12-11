parser grammar PoolParser;
options { tokenVocab=PoolLexer; }

@header{
    import java.util.List;
}

@members{
    String filename;
    public void setFilename(String f){
        filename = f;
    }
}

// program : (import_stat | alias_stat)* (classblock)+;

program returns [AST.program value]
locals [ArrayList<AST.import_stat> a;
	ArrayList<AST.alias_stat>b;
	ArrayList<AST.classblock> c; 
	int temp_line_no = -1;]
		@init {
			$a = new ArrayList<>();
			$b = new ArrayList<>();
			$c = new ArrayList<>();
			//$temp_line_no=-1;
		}
		:(imp=import_stat{$a.add($imp.value);if($temp_line_no==-1){$temp_line_no=$imp.value.get(0).lineNo;}}
		 | alias=alias_stat{$b.add($alias.value);if($temp_line_no==-1){$temp_line_no=$alias.value.get(0).lineNo;}})*
		 (cl=classblock{$c.add($cl.value);if($temp_line_no==-1){$temp_line_no=$cl.value.get(0).lineNo;}})+{
		 		$value= new AST.program($cl.value, $imp.value, $alias.value, $temp_line_no);
		 };

import_stat returns [AST.import_stat value]
locals [String obj_alias, lib;
		int temp_line_no =-1;]
@init {
	$obj_alias = "";
	$lib = "";
}
: IMPORT ( tp=TYPEID { $lib = tp.getText();if($temp_line_no==-1){$temp_line_no=$tp.value.get(0).lineNo;}}
		| ob=OBJECTID {$lib = ob.getText();if($temp_line_no==-1){$temp_line_no=$ob.value.get(0).lineNo;}}) 	 
		(AS obj=OBJECTID{ $obj_alias = obj.getText(); })? 
		SEMICOLON
		{
			if($obj_alias == "") {
				$obj_alias = $lib;
			}
			$value = new AST.import_stat($lib, $obj_alias, $temp_line_no);
		};


alias_stat returns [AST.alias_stat value]
locals [String obj_alias, obj;
		int temp_line_no =-1;]
@init {
	$obj_alias = "";
	$obj = "";
}
: USING (tp1 = TYPEID { $obj = tp1.getText();if($temp_line_no==-1){$temp_line_no=$tp1.value.get(0).lineNo;}}
		| ob1 = OBJECTID { $obj = ob1.getText();if($temp_line_no==-1){$temp_line_no=$ob1.value.get(0).lineNo;}}
		) 
		AS (tp2 = TYPEID { $obj_alias = tp2.getText();}
			| ob2 = OBJECTID { $obj_alias = ob2.getText();}
			)
		{
			$value = new AST.alias_stat($obj, $obj_alias, $temp_line_no);
		};

classblock returns [AST.classblock value]
locals [String name,filename,parent;
	ArrayList<AST.feature> a;
	int temp_line_no=-1;]
@init{
	$name="";
	$filename="";
	$parent="";
	$a=new ArrayList<>();
}
: (cl = CLASS{ $name = cl.getText();if($temp_line_no==-1){$temp_line_no=$cl.value.get(0).lineNo;}})
  (tp1 = TYPEID{ $filename = tp1.getText();if($temp_line_no==-1){$temp_line_no=$tp1.value.get(0).lineNo;}})
  (INHERITS (tp2 = TYPEID{$parent=tp2.getText();}))? 
  LBRACE 
  (inc = inClass{$a.add($inc.value);if($temp_line_no==-1){$temp_line_no=$a.value.get(0).lineNo;}}) 
  RBRACE 
  SEMICOLON
  {
  	$value= new AST.classblock($name,$filename,$parent,$inc.value,$temp_line_no);
  };

inClass returns [AST.inclass value]
locals[ArrayList<AST.access_specifier> a;
	ArrayList<AST.declaration>b;
	ArrayList<AST.method> c; 
	int temp_line_no = -1;]
@init{
	$a = new ArrayList<>();
	$b = new ArrayList<>();
        $c = new ArrayList<>();
}
: ((acc = access_specifier{$a.add($acc.value);if($temp_line_no==-1){$temp_line_no=$acc.value.get(0).lineNo;}})? 
    de = declaration{$b.add($de.value);if($temp_line_no==-1){$temp_line_no=$de.value.get(0).lineNo;}} 
    | me = method{$c.add($me.value);if($temp_line_no==-1){$temp_line_no=$me.value.get(0).lineNo;}})+;
    {
    	$value=new AST.inClass($acc.value, $de.value, $me.value, $temp_line_no)
    };

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
