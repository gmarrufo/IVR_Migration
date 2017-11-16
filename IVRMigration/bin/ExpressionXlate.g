grammar ExpressionXlate;

options {
  language = Java;
  ASTLabelType=CommonTree;
  output=AST; 
  backtrack=true;
  
  //buildAST=true;
}

@header {
  package com.ibm.ivr.eus.migration;
//  package com.ibm.ivr.framework.expressions.parser;
//  import com.ibm.ivr.framework.expressions.*;
}

@lexer::header { 
  package com.ibm.ivr.eus.migration;
// package com.ibm.ivr.framework.expressions.parser;  
//  import com.ibm.ivr.framework.expressions.*;
}


@members {
    	public static String parse(String line)  throws Exception {
    			ExpressionXlateLexer lexer = new ExpressionXlateLexer(new ANTLRStringStream(line));
    			TokenStream tokens = new CommonTokenStream(lexer);
    			ExpressionXlateParser parser = new ExpressionXlateParser(tokens);
    			ExpressionXlateParser.list_return ret = parser.list();
//    			CommonTree root = (CommonTree)ret.getTree();
    			return ret.value;
    	}
}

list returns [String value]: list1 {$value = $list1.value;} EOF;

list1 returns [String value]: e1 = expression {$value = ($e1.value == null) ? "" : $e1.value;} (';' e2 = expression {$value += ";" + $e2.value;})* ;

expression returns [String value]:
//(	e1=identifier '=' e2=expression	{	$value = $e1.text + '=' + $e2.value; })
//|
(
(
	'PARSE' 'VAR' i1=identifier {$value = "PARSE_VAR(" + ((i1 != null) ? $i1.text+",'" : "''");}
			(  (e2=identifier {$value+= ((e2 != null) ? ","+$e2.text+"" : "");}) 
			 | (e3=number {$value+= ((e3 != null) ? ","+$e3.text : "");}) 
			 | (e4=literal {$value+= ((e4 != null) ? ","+$e4.text.replaceAll("'","`").replaceAll("\"","`") +"": "");}))+ 
	//l1=term i2=identifier l2=term {$value = "PARSE_VAR("+$i1.text+","+$l1.value+",'"+$i2.text+"',"+$l2.value+")";}
)
	{ $value += "')"; System.err.println("expression:"+$value);}
|
(	r1 = relation {$value = ($r1.value == null) ? "" : $r1.value;} 
  (
    (    ('&' | AND) {	$value = $value + " AND "; }
		   | ('|' | OR)  {	$value = $value + " OR "; }
		   | ('&&' | XOR)  {	$value = $value + " XOR "; }
		)
 	  r2 = relation {	$value = $value + $r2.value; }
	  
	)*
	
)

{System.err.println("expression:"+$value);}
);

relation returns [String value]:	
	r1=add {$value = $r1.value;} (
		op=('=' | '==' | '!=' | '<>' | '<' | '<=' | '>=' | '>' | 'EQ' | 'NE' | 'LT' | 'LTEQ' | 'GTEQ' | 'GT') r2=add  {	
			$value = "(" + $value + (
				($op.text.equals("<>") || $op.text.equals("NE") || $op.text.equals("!=")) ? " != " : 
				($op.text.equals("<") || $op.text.equals("LT"))? " LT " : 
				($op.text.equals(">") || $op.text.equals("GT"))? " GT " : 
				($op.text.equals(">=") || $op.text.equals("GTEQ"))? " GTEQ " : 
				($op.text.equals("<=") || $op.text.equals("LTEQ")) ? " LTEQ " : 
				($op.text.equals("=") || $op.text.equals("EQ")|| $op.text.equals("==")) ? "==" : $op.text) + $r2.value + ")";	 
		}
	)*;

add returns [String value]: 
	r1=mult {$value = $r1.value;} (
		(op=('||' | '+' | '-'))? r2=mult  {	$value = $value + ("-".equals($op.text) ? "-" : "+") + $r2.value;	 }
	)*;
	
mult returns [String value]:	
	r1=unary {$value = $r1.value;} (
		op=('*' | '/' | DOUBLESLASH) r2=unary {	$value = $value + $op.text + $r2.value;	 }
	)*;

unary returns [String value]: term {
	$value = ""+ $term.value; 
	System.err.println("unary:"+$value);
};

//term:	 number | identifier | function | '(' expression ')' |	literal;
function returns [String value]: 
	identifier ('('	actualParameters? ')') {	
		$value = $identifier.text + "(" +  ($actualParameters.value  == null ? "" : $actualParameters.value) + ")";	 
}; 

actualParameters returns [String value]: 
		e1=expression {$value = ($e1.value == null ? "" : $e1.value);}
		(',' e2=expression {$value+= ((e2 != null) ? ","+$e2.value : "");})* 
		
		{
			System.err.println("actualParameters:"+$value);
		}
;

literal: STRING_LITERAL | CHAR_LITERAL;
identifier: IDENTIFIER;
number: NUMBER;

term returns [String value]:
	{
	//StringBuffer b = new StringBuffer();
		System.err.println("term??");
	}
		
	( number {$value = $number.text;}
	|	literal {$value = $literal.text; }//.replace('"', '\'')
	| TREXX  '('
			({ $value = "TREXX('"; }
			(	c=~(')' | '\r' | '\n')	{ $value += $c.text.replaceAll("'","`");}	)*
			 {$value += "')";})
	 ')'
	| function {$value = $function.value;} 
	| ('(' expression ')'){$value = $expression.value;}
	| identifier {$value = $identifier.text;}
	)
	{
		System.err.println("term:"+$value);
	}
;

	//: (function | literal | ident {my.Test1.addIdent($ident.text);} |  ',' | number)+ ;
	
	
//function:   
//ident ('(' actualParameters? ')') {my.Test1.add($ident.text); } ; 
//('(' .* ')')


//literal: STRING_LITERAL | CHAR_LITERAL;

//ident: IDENT;

//number: NUMBER;


//negation
//	:	'not'* term
//	;
	
//	:	('+' | '-')* negation
	

	

logic_op: RELATION_OPERATOR;
	
	
 
XOR: ' XOR ';
AND: ' AND ';
OR: ' OR ';
IF: 'IF' | 'if';
THEN: 'THEN' | 'then';
LINEOUT: 'LINEOUT';
TREXX: ('TREXX' | 'trexx');

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
RELATION_OPERATOR: ('&&' | '&' | '||' | DOUBLESLASH | '|');
COLON: ':'; 
SEMI: ';';
NUMBER: DIGIT+ ('.' DIGIT+)?;
//INTEGER : DIGIT+ ;
IDENTIFIER : LETTER (LETTER | DIGIT | '_')*;

//LETTER (LETTER | DIGIT | '_')*

NL: '\r'? '\n'; 
WS : (' ' | '\t' | '\r' | '\f')+ {$channel = HIDDEN;};
COMMENT : DOUBLESLASH .* ('\n'|'\r') {$channel = HIDDEN;};
