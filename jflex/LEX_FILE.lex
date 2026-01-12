/***************************/
/* FILE NAME: LEX_FILE.lex */
/***************************/

/*************/
/* USER CODE */
/*************/

import java_cup.runtime.*;

/******************************/
/* DOLLAR DOLLAR - DON'T TOUCH! */
/******************************/

%%

/************************************/
/* OPTIONS AND DECLARATIONS SECTION */
/************************************/
   
/*****************************************************/ 
/* Lexer is the name of the class JFlex will create. */
/* The code will be written to the file Lexer.java.  */
/*****************************************************/ 
%class Lexer

/********************************************************************/
/* The current line number can be accessed with the variable yyline */
/* and the current column number with the variable yycolumn.        */
/********************************************************************/
%line
%column

/*******************************************************************************/
/* Note that this has to be the EXACT same name of the class the CUP generates */
/*******************************************************************************/
%cupsym TokenNames

/******************************************************************/
/* CUP compatibility mode interfaces with a CUP generated parser. */
/******************************************************************/
%cup

/****************/
/* DECLARATIONS */
/****************/
/*****************************************************************************/   
/* Code between %{ and %}, both of which must be at the beginning of a line, */
/* will be copied verbatim (letter to letter) into the Lexer class code.     */
/* Here you declare member variables and functions that are used inside the  */
/* scanner actions.                                                          */  
/*****************************************************************************/   
%{
	/*********************************************************************************/
	/* Create a new java_cup.runtime.Symbol with information about the current token */
	/*********************************************************************************/
	private Symbol symbol(int type)               {return new Symbol(type, yyline, yycolumn);}
	private Symbol symbol(int type, Object value) {return new Symbol(type, yyline, yycolumn, value);}

	/*******************************************/
	/* Enable line number extraction from main */
	/*******************************************/
	public int getLine() { return yyline + 1; } 

	/**********************************************/
	/* Enable token position extraction from main */
	/**********************************************/
	public int getTokenStartPosition() { return yycolumn + 1; } 

	/************************************************/
	/* Get token name string from token type number */
	/************************************************/
	public String getTokenName(int tokenType) {
		switch(tokenType) {
			case TokenNames.EOF: return "EOF";
			case TokenNames.PLUS: return "PLUS";
			case TokenNames.MINUS: return "MINUS";
			case TokenNames.TIMES: return "TIMES";
			case TokenNames.DIVIDE: return "DIVIDE";
			case TokenNames.LPAREN: return "LPAREN";
			case TokenNames.RPAREN: return "RPAREN";
			case TokenNames.LBRACK: return "LBRACK";
			case TokenNames.RBRACK: return "RBRACK";
			case TokenNames.LBRACE: return "LBRACE";
			case TokenNames.RBRACE: return "RBRACE";
			case TokenNames.COMMA: return "COMMA";
			case TokenNames.DOT: return "DOT";
			case TokenNames.SEMICOLON: return "SEMICOLON";
			case TokenNames.ASSIGN: return "ASSIGN";
			case TokenNames.EQ: return "EQ";
			case TokenNames.LT: return "LT";
			case TokenNames.GT: return "GT";
			case TokenNames.ARRAY: return "ARRAY";
			case TokenNames.CLASS: return "CLASS";
			case TokenNames.RETURN: return "RETURN";
			case TokenNames.WHILE: return "WHILE";
			case TokenNames.IF: return "IF";
			case TokenNames.ELSE: return "ELSE";
			case TokenNames.NEW: return "NEW";
			case TokenNames.EXTENDS: return "EXTENDS";
			case TokenNames.NIL: return "NIL";
			case TokenNames.TYPE_INT: return "TYPE_INT";
			case TokenNames.TYPE_STRING: return "TYPE_STRING";
			case TokenNames.TYPE_VOID: return "TYPE_VOID";
			case TokenNames.INT: return "INT";
			case TokenNames.STRING: return "STRING";
			case TokenNames.ID: return "ID";
			case TokenNames.ERROR: return "ERROR";
			default: return "UNKNOWN";
		}
	}

	/**********************************************/
	/* Checks that Integers dont lead with zeros, and are in the 16-bit range.
	if they are return lexical error*/
	/**********************************************/
	private Symbol checkInt(String text)
	{
		try {
			int num = Integer.parseInt(text);
			if(num < 0 || num > 32767)
			{
				return symbol(TokenNames.ERROR);
			}
			return symbol(TokenNames.INT, num);
		} catch (NumberFormatException e) {
			// Number is too large or invalid format
			return symbol(TokenNames.ERROR);
		}
	}

%}

/***********************/
/* MACRO DECLARATIONS */
/***********************/
LineTerminator	= \r|\n|\r\n
WhiteSpace		= {LineTerminator} | [ \t]
INTEGER			= 0 | [1-9][0-9]*
INVALID_INT		= 0[0-9]+
ANY_NUMBER_PATTERN= [0-9]+
LETTER 			= [a-zA-Z]
ID				= {LETTER}[a-zA-Z0-9]*
Anything        = . | {LineTerminator}
Quote           = \"
STRING          = {Quote}{LETTER}*{Quote}
AllowedComment      = {LETTER} | [0-9] | [ \t] | \( | \) | \[ | \] | \{ | \} | \! | \? | \+ | - | \* | \/ | \. | ;
SingleOpen          = \/\/
SINGLECOMMENT       = {SingleOpen}{AllowedComment}*{LineTerminator}
BADSINGLECOMMENT    = {SingleOpen}.*{LineTerminator}
AllowedMultiComment = {AllowedComment} | {LineTerminator}
MultiOpen           = \/\*
MultiClose          = \*\/
MULTICOMMENT        = {MultiOpen}{AllowedMultiComment}*{MultiClose}
BADMULTICOMMENT     = {MultiOpen} | {MultiOpen}{Anything}*{MultiClose}
INVALID_STRING_WITH_BAD_CHARS= {Quote}[^\"\r\n]*{Quote}
UNCLOSED_STRING= {Quote}[^\"\r\n]*

/******************************/
/* DOLLAR DOLLAR - DON'T TOUCH! */
/******************************/

%%

/************************************************************/
/* LEXER matches regular expressions to actions (Java code) */
/************************************************************/

/**************************************************************/
/* YYINITIAL is the state at which the lexer begins scanning. */
/* So these regular expressions will only be matched if the   */
/* scanner is in the start state YYINITIAL.                   */
/**************************************************************/

<YYINITIAL> {

{WhiteSpace}		{ /* just skip what was found, do nothing */ }
{SINGLECOMMENT}		{ /* just skip what was found, do nothing */ }
{BADSINGLECOMMENT}	{ return symbol(TokenNames.ERROR); }
{MULTICOMMENT}		{ yypushback(yylength() - yytext().indexOf("*/") - 2); }
{BADMULTICOMMENT}	{ return symbol(TokenNames.ERROR); }


/*keywords*/
"array"				{ return symbol(TokenNames.ARRAY);}
"class"				{ return symbol(TokenNames.CLASS);}
"return"			{ return symbol(TokenNames.RETURN);}
"while"				{ return symbol(TokenNames.WHILE);}
"if"				{ return symbol(TokenNames.IF);}
"else"				{ return symbol(TokenNames.ELSE);}
"new"				{ return symbol(TokenNames.NEW);}
"extends"			{ return symbol(TokenNames.EXTENDS);}
"nil"				{ return symbol(TokenNames.NIL);}
"int"				{ return symbol(TokenNames.TYPE_INT);}
"string"			{ return symbol(TokenNames.TYPE_STRING);}
"void"				{ return symbol(TokenNames.TYPE_VOID);}


/*brackets*/
"("					{ return symbol(TokenNames.LPAREN);}
")"					{ return symbol(TokenNames.RPAREN);}
"["					{ return symbol(TokenNames.LBRACK);}
"]"					{ return symbol(TokenNames.RBRACK);}
"{"					{ return symbol(TokenNames.LBRACE);}
"}"					{ return symbol(TokenNames.RBRACE);}

/*punctuation*/
","					{ return symbol(TokenNames.COMMA);}
"."					{ return symbol(TokenNames.DOT);}
";"					{ return symbol(TokenNames.SEMICOLON);}

/*operators*/
"+"					{ return symbol(TokenNames.PLUS);}
"-"					{ return symbol(TokenNames.MINUS);}
"*"					{ return symbol(TokenNames.TIMES);}
"/"					{ return symbol(TokenNames.DIVIDE);}
":="				{ return symbol(TokenNames.ASSIGN);}
"="			 		{ return symbol(TokenNames.EQ);}
"<"					{ return symbol(TokenNames.LT);}
">"					{ return symbol(TokenNames.GT);}

/*identifiers*/

{STRING}			{ return symbol(TokenNames.STRING, yytext());}
{INVALID_STRING_WITH_BAD_CHARS}	{ return symbol(TokenNames.ERROR); }
{UNCLOSED_STRING}		{ return symbol(TokenNames.ERROR); }
{INVALID_INT}		{ return symbol(TokenNames.ERROR); }
{INTEGER}			{ return checkInt(yytext());}
{ID}				{ return symbol(TokenNames.ID, yytext());}
{ANY_NUMBER_PATTERN}	{ return symbol(TokenNames.ERROR); }

<<EOF>>				{ return symbol(TokenNames.EOF);}

/* Error fallback - any unrecognized character */
[^]					{ return symbol(TokenNames.ERROR); }
}
