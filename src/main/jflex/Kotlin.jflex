package com.scriptrunner.lexer;

import com.scriptrunner.lexer.TokenType;

%%

%public
%class KotlinLexer
%unicode
%line
%column
%char
%type TokenType

%{
    private int tokenStart;
    private int tokenLine;
    private int tokenColumn;
    private int stateBeforeEof = YYINITIAL;

    public int getTokenStart() { return tokenStart; }
    public int getTokenEnd() { return (int) yychar + yylength(); }
    public int getTokenLine() { return tokenLine + 1; }
    public int getTokenColumn() { return tokenColumn + 1; }
    public String getTokenText() { return yytext(); }
    public int getCurrentState() { return zzLexicalState; }
    public int getStateBeforeEof() { return stateBeforeEof; }

    private void markTokenStart() {
        tokenStart = (int) yychar;
        tokenLine = yyline;
        tokenColumn = yycolumn;
    }

    private void saveStateBeforeEof() {
        stateBeforeEof = zzLexicalState;
    }
%}

%state STRING
%state MULTILINE_STRING
%state LINE_COMMENT
%state BLOCK_COMMENT

// Macros
LineTerminator = \r|\n|\r\n
WhiteSpace = [ \t\f]
Digit = [0-9]
HexDigit = [0-9a-fA-F]
Letter = [a-zA-Z_]
IdentifierChar = {Letter} | {Digit}

Identifier = {Letter} {IdentifierChar}*
IntegerLiteral = {Digit}+ [lL]?
HexLiteral = 0[xX] {HexDigit}+ [lL]?
FloatLiteral = {Digit}+ \. {Digit}* ([eE] [+-]? {Digit}+)? [fFdD]?
             | {Digit}+ [eE] [+-]? {Digit}+ [fFdD]?
             | {Digit}+ [fFdD]

%%

<YYINITIAL> {
    // Keywords
    "fun"           { markTokenStart(); return TokenType.KEYWORD; }
    "val"           { markTokenStart(); return TokenType.KEYWORD; }
    "var"           { markTokenStart(); return TokenType.KEYWORD; }
    "if"            { markTokenStart(); return TokenType.KEYWORD; }
    "else"          { markTokenStart(); return TokenType.KEYWORD; }
    "when"          { markTokenStart(); return TokenType.KEYWORD; }
    "for"           { markTokenStart(); return TokenType.KEYWORD; }
    "while"         { markTokenStart(); return TokenType.KEYWORD; }
    "do"            { markTokenStart(); return TokenType.KEYWORD; }
    "class"         { markTokenStart(); return TokenType.KEYWORD; }
    "object"        { markTokenStart(); return TokenType.KEYWORD; }
    "interface"     { markTokenStart(); return TokenType.KEYWORD; }
    "enum"          { markTokenStart(); return TokenType.KEYWORD; }
    "sealed"        { markTokenStart(); return TokenType.KEYWORD; }
    "data"          { markTokenStart(); return TokenType.KEYWORD; }
    "abstract"      { markTokenStart(); return TokenType.KEYWORD; }
    "open"          { markTokenStart(); return TokenType.KEYWORD; }
    "return"        { markTokenStart(); return TokenType.KEYWORD; }
    "break"         { markTokenStart(); return TokenType.KEYWORD; }
    "continue"      { markTokenStart(); return TokenType.KEYWORD; }
    "throw"         { markTokenStart(); return TokenType.KEYWORD; }
    "try"           { markTokenStart(); return TokenType.KEYWORD; }
    "catch"         { markTokenStart(); return TokenType.KEYWORD; }
    "finally"       { markTokenStart(); return TokenType.KEYWORD; }
    "null"          { markTokenStart(); return TokenType.KEYWORD; }
    "true"          { markTokenStart(); return TokenType.KEYWORD; }
    "false"         { markTokenStart(); return TokenType.KEYWORD; }
    "is"            { markTokenStart(); return TokenType.KEYWORD; }
    "as"            { markTokenStart(); return TokenType.KEYWORD; }
    "in"            { markTokenStart(); return TokenType.KEYWORD; }
    "out"           { markTokenStart(); return TokenType.KEYWORD; }
    "private"       { markTokenStart(); return TokenType.KEYWORD; }
    "public"        { markTokenStart(); return TokenType.KEYWORD; }
    "protected"     { markTokenStart(); return TokenType.KEYWORD; }
    "internal"      { markTokenStart(); return TokenType.KEYWORD; }
    "override"      { markTokenStart(); return TokenType.KEYWORD; }
    "suspend"       { markTokenStart(); return TokenType.KEYWORD; }
    "import"        { markTokenStart(); return TokenType.KEYWORD; }
    "package"       { markTokenStart(); return TokenType.KEYWORD; }
    "this"          { markTokenStart(); return TokenType.KEYWORD; }
    "super"         { markTokenStart(); return TokenType.KEYWORD; }
    "companion"     { markTokenStart(); return TokenType.KEYWORD; }
    "init"          { markTokenStart(); return TokenType.KEYWORD; }
    "constructor"   { markTokenStart(); return TokenType.KEYWORD; }
    "typealias"     { markTokenStart(); return TokenType.KEYWORD; }
    "inline"        { markTokenStart(); return TokenType.KEYWORD; }
    "reified"       { markTokenStart(); return TokenType.KEYWORD; }
    "crossinline"   { markTokenStart(); return TokenType.KEYWORD; }
    "noinline"      { markTokenStart(); return TokenType.KEYWORD; }
    "lateinit"      { markTokenStart(); return TokenType.KEYWORD; }
    "by"            { markTokenStart(); return TokenType.KEYWORD; }
    "where"         { markTokenStart(); return TokenType.KEYWORD; }
    "get"           { markTokenStart(); return TokenType.KEYWORD; }
    "set"           { markTokenStart(); return TokenType.KEYWORD; }

    // Comments
    "//"            { markTokenStart(); yybegin(LINE_COMMENT); }
    "/*"            { markTokenStart(); yybegin(BLOCK_COMMENT); }

    // Strings
    "\"\"\""        { markTokenStart(); yybegin(MULTILINE_STRING); }
    \"              { markTokenStart(); yybegin(STRING); }

    // Character literal
    \' ([^\\\'\r\n] | \\.) \'  { markTokenStart(); return TokenType.CHAR; }

    // Numbers
    {HexLiteral}    { markTokenStart(); return TokenType.NUMBER; }
    {FloatLiteral}  { markTokenStart(); return TokenType.NUMBER; }
    {IntegerLiteral} { markTokenStart(); return TokenType.NUMBER; }

    // Brackets
    "("             { markTokenStart(); return TokenType.LPAREN; }
    ")"             { markTokenStart(); return TokenType.RPAREN; }
    "{"             { markTokenStart(); return TokenType.LBRACE; }
    "}"             { markTokenStart(); return TokenType.RBRACE; }
    "["             { markTokenStart(); return TokenType.LBRACKET; }
    "]"             { markTokenStart(); return TokenType.RBRACKET; }
    "<"             { markTokenStart(); return TokenType.LT; }
    ">"             { markTokenStart(); return TokenType.GT; }

    // Operators (longer patterns first)
    ".." | "::" | "->" | "++" | "--" |
    "+=" | "-=" | "*=" | "/=" | "%=" |
    "==" | "!=" | "<=" | ">=" |
    "&&" | "||" | "?:" | "?." | "!!" |
    "+"  | "-"  | "*"  | "/"  | "%" |
    "="  | "!"  | ":" | ";" | "," | "." | "@" | "?" |
    "&"  | "|"  | "^"  | "~"
        { markTokenStart(); return TokenType.OPERATOR; }

    // Identifier
    {Identifier}    { markTokenStart(); return TokenType.IDENTIFIER; }

    // Whitespace
    {WhiteSpace}+   { markTokenStart(); return TokenType.WHITESPACE; }
    {LineTerminator} { markTokenStart(); return TokenType.NEWLINE; }

    // Any other character
    .               { markTokenStart(); return TokenType.ERROR; }
}

<STRING> {
    \"              { yybegin(YYINITIAL); return TokenType.STRING; }
    \\.             { }
    [^\\\"\r\n]+    { }
    {LineTerminator} { yybegin(YYINITIAL); return TokenType.STRING; }
    <<EOF>>         { yybegin(YYINITIAL); return TokenType.STRING; }
}

<MULTILINE_STRING> {
    "\"\"\""        { yybegin(YYINITIAL); return TokenType.STRING; }
    [^\"]+          { }
    \"              { }
    <<EOF>>         { saveStateBeforeEof(); yybegin(YYINITIAL); return TokenType.STRING; }
}

<LINE_COMMENT> {
    {LineTerminator} { yybegin(YYINITIAL); return TokenType.COMMENT; }
    [^\r\n]+        { }
    <<EOF>>         { yybegin(YYINITIAL); return TokenType.COMMENT; }
}

<BLOCK_COMMENT> {
    "*/"            { yybegin(YYINITIAL); return TokenType.COMMENT; }
    [^*]+           { }
    "*"             { }
    <<EOF>>         { saveStateBeforeEof(); yybegin(YYINITIAL); return TokenType.COMMENT; }
}
