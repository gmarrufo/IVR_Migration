grammar IniFile;

options {
  language = Java;
  ASTLabelType=CommonTree;
  output=AST; 
  //buildAST=true;
  
}


@header {
  package com.ibm.ivr.eus.migration;
}

@lexer::header { 
  package com.ibm.ivr.eus.migration;  
}

@members {
	public static CommonTree parse(String line)  throws Exception {
		IniFileLexer lexer = new IniFileLexer(new ANTLRStringStream(line));
		TokenStream tokens = new CommonTokenStream(lexer);
		IniFileParser parser = new IniFileParser(tokens);
		IniFileParser.program_return ret = parser.program();
		CommonTree root = (CommonTree)ret.getTree();
		//	if((parser.failed() || parser.getNumberOfSyntaxErrors() > 0) && line.trim().length() > 0)
		//		throw new RuntimeException("Failed to parse expression:"+line); 
			
		return root;
	}
}

//program: program3 {System.out.println(""+$program3.tree.getType());};

//program3 : (IDENT) => (ident '=' expression) -> ^(FOO ident '=' expression)
//			| (IF) => IF^ expression THEN statement	;

program: nl* ((statement -> ^(STATEMENT statement)) (nl+ | EOF))+;

label:	'[' ident ']';

nl: NL;

statement: 
	  label 
	| (IDENT '=' 'esmrt_logger') => loggerExpression 
	| (IF) => ifStatement -> ^(IF ifStatement)
	|	('say' | 'Say' | 'SAY') => sayStatement -> ^(SAY sayStatement)
	|	('CALL' 'LINEOUT') => lineoutStatement -> ^(LINEOUT lineoutStatement) 
	//{MigrationDebug.doStats=false;} e1=expression (',' expression)* {MigrationDebug.doStats=true;}{ System.out.println("&&&CALL "+$e1.text);} 
	|	('Call' 'SYSSLEEP') => 'Call' 'SYSSLEEP' number {/* System.out.println("&&&CALL SYSSLEEP "+$number.text);*/} 
	| ((NUMBER | IDENT | IDENT2 | PHONE) (';' | '|')) => dtmfTableEntry -> ^(CHOICE dtmfTableEntry)
	| (IDENT '=') => assignStatement -> ^(ASSIGN assignStatement) 
	| expression;
	
loggerExpression: ident '=' 'esmrt_logger' '(' 
//{MigrationDebug.doStats=false;} 
expression 
//{MigrationDebug.doStats=true;} 
')'; 

ifStatement	:	IF e1=expression THEN statement	-> IF $e1 THEN statement;

sayStatement : ('say' | 'Say' | 'SAY') expression;

lineoutStatement: ('CALL' 'LINEOUT') ident expression ',' expression;
	 
assignStatement : 
		ident '=' expression
		;

//main_1;XFER_918773014686
dtmfTableEntry : menu1=ident ';' handler1=ident (';' parms1=expression)+ /* menu1 ';' handler1 ';' parms1 */
| (number | ident2 | phone) (';' | '|') .*;
	
actualParameters
	: (term | ',')+
//	: (function | literal | ident {MigrationDebug.addIdent($ident.text);} |  ',' | number)+
	;
 
term:
		number
	| (IDENT ~'(') => i2=ident 
	// {MigrationDebug.addIdent($i2.text);}  {System.out.println("here1:"+$ident.text+","+$i2.text);}
	| (IDENT '(') =>	function // {System.out.println("here2:"+$function.text);}  
	| ('(') => ('(' expression ')')// 	 {System.out.println("here3:"+$expression.text);}
	|	literal
	| ident
	;
	
	
function:   
ident ('(' actualParameters? ')') 
//{MigrationDebug.add($ident.text); } 
; 
//('(' .* ')')


literal: STRING_LITERAL | CHAR_LITERAL;

ident: IDENT;

ident2: IDENT2;

number: NUMBER;

phone: PHONE;

negation
	:	'not'* term
	;
	
unary: term;
//	:	('+' | '-')* negation
	

mult
	:	unary (('*' | '/' | '|' | DOUBLESLASH) unary)*
	;
	
add
	:	mult (('+' | '-') mult)*
	;

relation
	:	add (('==' | '/=' | '<>' | '<' | '<=' | '>=' | '>' | '=') add)*
	;
	
expression:
		relation (r_oper? relation)* 
	;
	
r_oper: RELATION_OPERATOR;
 

IF: 'IF' | 'if';
THEN: 'THEN' | 'then';
LINEOUT: 'LINEOUT';

fragment SQ
	:	'\''
;	

fragment DQ
	:	'"'
;	

MULTILINE_COMMENT : '/*' .* '*/' {$channel = HIDDEN;} ;

STRING_LITERAL
	:	DQ .*
//		{ StringBuilder b = new StringBuilder(); }
//		(	'"' '"'				{ b.appendCodePoint('"');}
//|	c=~('"'|'\r'|'\n')	{ b.appendCodePoint(c);}
//		)*
		DQ 
//		{ setText(b.toString()); }
	;

CHAR_LITERAL
	:	SQ .*
//		{ StringBuilder b = new StringBuilder(); }
//		(	'\'' '\''				{ b.appendCodePoint('\'');}
//		|	c=~(SQ | '\r' | '\n')	{ b.appendCodePoint(c);}
//		)*
		SQ
//		{ setText(b.toString()); }
	;

//fragment OR: '|';
fragment DOUBLESLASH: '//';
fragment LETTER : ('a'..'z' | 'A'..'Z') ;
fragment DIGIT : '0'..'9';
ASSIGN: 'ASSIGN';
STATEMENT: 'STATEMENT';
CHOICE: 'CHOICE';
SAY: 'SAY';
RELATION_OPERATOR: ('&&' | '&' | '||' | DOUBLESLASH | '|' | '.');
COLON: ':'; 
SEMI: ';';
NUMBER: DIGIT+ ('.' DIGIT+)?;
PHONE : (DIGIT | '*') (DIGIT | '*' | '#' | '.' | '-')*;
//INTEGER : DIGIT+ ;
IDENT : LETTER (LETTER | DIGIT | '_' | '*' | '#' )*;
DIR: LETTER (LETTER | DIGIT | ':' | '\\' | '_' | '.')+;
IDENT2: (LETTER | DIGIT) (LETTER | DIGIT | '_' | '*' | '#' | '.')*;
//ARGS : '(' .* ')';

NL: '\r'? '\n'; 
WS : (' ' | '\t' | '\r' | '\f')+ {$channel = HIDDEN;};
COMMENT : DOUBLESLASH .* ('\n'|'\r') {$channel = HIDDEN;};
