grammar GrammarExpr
;

prog : computeExprs;

@@KEYWORD :
	 K_AND     'and'
	 K_OR      'or'
	 K_TRUE    'true'
	 K_FALSE   'false'
	 K_NULL    'null'
	 K_MAX     'max'
	 K_MIN     'min'
	 K_COUNT   'count'
	 K_SUM     'sum'
	 k_AVG     'avg'
	 K_DIVIDE    'divide'
	 K_MULTIPLY  'multiply'
	 K_COMPUTE   'compute'
	 K_FILTER    'filter'
	 K_THIS      'this'
	 
;
computeExprs : (computeExpr ';')*
;

computeExpr: left (':'|'!:') right
;

left : variable (',' variable)*
;

right :expr
;

expr : expr ('*'|'/') expr
     | expr ('+'|'-') expr
     | '!' expr
     | '-' INT
     | 'if'  expr 'then'  expr ('else' expr)?
     | expr ('='|'!='|'>='|'<='|'>'|'<') expr
     | expr (K_AND|K_OR) expr
     | valueParam ('.' valueParam)*
     | '(' expr ')'
     | STRING
     | DATE
     | BOOLEAN
     | NULL
;
  
BOOLEAN : K_TRUE
		  | K_FALSE
;

fun: ID '(' ( params )?  ')' 
;

params: param (',' param)*;

param:  expr
;

NULL : K_NULL
;

arrayScale: INT '...' (INT)?
     | '...' INT
;
valueParam :   ID 
	   |  ID  ('[' arrayScale ']')?
	   |  INT
	   | fun
;

variable :  element ('.' element)*
	   | 'error('  STRING ',' STRING ')'
;

element : ID ('[' arrayScale ']')? 
	| '(' ID (',' ID)* ')' 
	| ID '{' (INT)? '}'
;


@ID : #STRING ;
@INT : #NUMBER ;
@STRING : '\'' #STRING '\'';
@DATE : '\'' #STRING '\'';
WS  : [ \t\r\n];