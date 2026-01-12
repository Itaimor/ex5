/***********/
/* PACKAGE */
/***********/
package ast;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import types.*;
import symboltable.*;
import semantic.SemanticException;

public class AstTypeName extends AstNode
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public String type;
	public String name;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstTypeName(String type, String name)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();
	
		this.type = type;
		this.name = name;
	}

	/*************************************************/
	/* The printing message for a type name AST node */
	/*************************************************/
	public void printMe()
	{
		/**************************************/
		/* AST NODE TYPE = AST TYPE NAME NODE */
		/**************************************/
		System.out.format("NAME(%s):TYPE(%s)\n",name,type);

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("NAME:TYPE\n%s:%s",name,type));
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		Type t = SymbolTable.getInstance().find(type);
		if (t == null)
			throw new SemanticException(lineNumber, "type '" + type + "' does not exist");

		if (t.isVoid())
			throw new SemanticException(lineNumber, "parameter/field cannot have void type");

		/*******************************************************/
		/* Enter var with name=name and type=t to symbol table */
		/*******************************************************/
		SymbolTable.getInstance().enter(name, t);

		/****************************/
		/* return (existing) type t */
		/****************************/
		return t;
	}	
}
