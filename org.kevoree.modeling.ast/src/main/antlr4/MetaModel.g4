grammar MetaModel;

fragment ESC :   '\\' (["\\/bfnrt] | UNICODE) ;
fragment UNICODE : 'u' HEX HEX HEX HEX ;
fragment HEX : [0-9a-fA-F] ;

STRING :  '"' (ESC | ~["\\])* '"' ;

IDENT : [0-9a-zA-Z_]+;

NUMBER : [\-]?[0-9]+[.]?[0-9]*;

TYPE_NAME : [0-9a-zA-Z_]+[0-9a-zA-Z_.]*;

metamodel: decl*;

decl : versionDeclr
           | kmfVersionDeclr
           | enumDeclr
           | classDeclr
           ;

versionDeclr : 'version' STRING;

kmfVersionDeclr : 'kmfVersion' STRING;

enumDeclr :
    'enum' TYPE_NAME '{' IDENT* '}';

classDeclr :
    'class' TYPE_NAME '{' (attributeDeclaration | referenceDeclaration | dependencyDeclaration | inputDeclaration | outputDeclaration)* '}' classParentDeclr?;

classParentDeclr :
    ':' TYPE_NAME (',' TYPE_NAME );

attributeDeclaration : 'att' IDENT ':' attributeType ('precision' NUMBER)?;

attributeType : 'String' | 'Double' | 'Long' | 'Continuous' | 'Int' | 'Bool' | TYPE_NAME;

referenceDeclaration : ('ref' | 'ref*') IDENT ':' TYPE_NAME ('oppositeOf' IDENT)?;

dependencyDeclaration : 'dependency' IDENT ':' TYPE_NAME;

inputDeclaration : 'input' IDENT STRING;

outputDeclaration : 'output' IDENT ':' attributeType;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines
