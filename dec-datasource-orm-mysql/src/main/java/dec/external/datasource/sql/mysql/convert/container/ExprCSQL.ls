grammar ExprSQL
;
prog : sqlExpr;

@@KEYWORD :
	 K_SELECT  'select'
	 K_FROM    'from'
	 k_WHERE   'where'
	 K_AS      'as'
	 K_LEFT    'left'
	 K_RIGHT   'right'
	 K_INNER   'inner'
	 K_JOIN    'join'
	 K_CROSS   'cross'
	 K_NATURAL 'natural'
	 K_OUTER   'outer'
	 K_FULL    'full'
	 K_ON      'on'
	 K_AND     'and'
	 K_OR      'or'
	 K_LIKE    'like'
	 K_L       '\'%\''
	 K_UNION   'union'
	 K_ALL     'all'
	 K_EXCEPT  'except'
	 K_DISTINCT 'distinct'
	 K_NULL     'null'
	 K_GROUP    'group'
	 K_BY       'by'
	 K_HAVING   'having'
	 K_ORDER    'order'
	 K_OFFSET   'offset'
	 K_LIMIT    'limit'
	 K_BETWEEN  'between'
	 K_NOT      'not'
	 K_IN       'in'
	 K_IS       'is'
	 K_DELETE   'delete'
	 K_INSERT   'insert'
	 K_INTO     'into'
	 K_UPDATE   'update'
	 K_SET      'set'
	 K_EXISTS   'exists'
	 K_TRUE     'true'
	 K_FALSE    'false'
;

sqlExpr: selectExpr
		 | deleteExpr
		 | insertExpr
		 | updateExpr
;

selectExpr : K_SELECT columnInfo K_FROM tableInfo (condition)? (groupInfo)? (pageInfo)? (queryInfo)*
;

insertExpr : K_INSERT K_INTO ID '(' iTargetInfo ')' 'values' '(' ivalueInfo ')'
;

iTargetInfo: expr (',' expr)*
;

ivalueInfo : expr (',' expr)*
;

updateExpr : K_UPDATE tableElement K_SET uValueInfo (condition)?
;
uValueInfo: signleValueInfo (',' signleValueInfo)*
;

signleValueInfo : columnElement '=' expr
;
deleteExpr : K_DELETE K_FROM tableInfo (condition)?
;

condition: k_WHERE sqlCondition
;

groupInfo: K_GROUP K_BY columnElement (',' columnElement)*  (nextInfo)?
;

nextInfo:orderInfo
	| havingInfo
;

queryInfo : (K_UNION|K_EXCEPT) (K_ALL|K_DISTINCT)? selectExpr
;

orderInfo: K_ORDER K_BY columnElement (',' columnElement)*
;

havingInfo: K_HAVING (sqlCondition)+
;

columnInfo : column (',' column)*
;
column : '*'
		 | ID '.*'
		 | ID 'as' ID
		 | ID '.' ID ('as' ID)?
		 | ID (ID)?
		 | '(' selectExpr ')' ('as') ID
		 | fun
;

colElementCon : ID ('.' ID)?
;

columnElement : fun
	 | '(' selectExpr ')'
	 | colElementCon ('as' ID)?
;

tableInfo : table (',' table)*
;

table : tableElement (JOIN_INFO)*
;

tableElement : ID 'as' ID
             | ID (ID)?
             | '(' selectExpr ')' ('as') ID
;
tableTailElement :ID
                 | 'as' ID
;
JOIN_INFO : (K_FULL|K_LEFT|K_RIGHT|K_INNER) (K_OUTER)? K_JOIN table K_ON expr
		 | K_CROSS K_JOIN table
		 | K_UNION K_JOIN table
;

sqlCondition : expr
;

expr : expr ('*'|'/') expr
     | expr ('+'|'-') expr
     | expr ('='|'!='|'>='|'<='|'>'|'<') expr
     | expr K_LIKE likeExpr
     | expr (K_AND|K_OR) expr
     | K_NULL
     | isInfo
     | betweenInfo
     | inInfo
     | existsInfo
     | fun
     | colElementCon
     | cparam
     | INT
     | '(' exprElement ')'
     | STRINGTEXT
     | BOOLEAN
;

BOOLEAN : (K_TRUE|FALSE)
;

isInfo :expr K_IS (K_NOT)? K_NULL
;

cparam : '#' ID ('.' ID)*
;

betweenInfo:expr (K_NOT)? K_BETWEEN expr K_AND expr
;

inInfo :expr (K_NOT)? K_IN '(' inValue ')'
;

existsInfo : (K_NOT)? K_EXISTS '(' selectExpr ')'
;

inValue : expr (',' expr)*
		| selectExpr
;

likeExpr : K_L expr K_L
		 | expr K_L
;

exprElement : expr
	        |selectExpr
;


fun: ID '(' (array)? ')'
;

array: param (',' param)*
;

param: ID
	   | INT
       | expr
       | '*'
;

pageInfo : (K_OFFSET INT)? K_LIMIT INT
;

@ID : #STRING;
@INT : #NUMBER;
@STRINGTEXT : '\'' #STRING '\'';
WS  : [ \t\r\n];