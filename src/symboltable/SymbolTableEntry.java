/***********/
/* PACKAGE */
/***********/
package symboltable;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import types.*;

/**********************/
/* SYMBOL TABLE ENTRY */
/**********************/
public class SymbolTableEntry
{
	/*********/
	/* index */
	/*********/
	int index;
	
	/********/
	/* name */
	/********/
	public String name;

	/******************/
	/* TYPE value ... */
	/******************/
	public Type type;

	/*********************************************/
	/* prevtop and next symbol table entries ... */
	/*********************************************/
	public SymbolTableEntry prevtop;
	public SymbolTableEntry next;

	/****************************************************/
	/* The prevtopIndex is just for debug purposes ... */
	/****************************************************/
	public int prevtopIndex;

	/****************************************************/
	/* Scope depth - at which nesting level declared    */
	/****************************************************/
	public int scopeDepth;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public SymbolTableEntry(
		String name,
		Type type,
		int index,
		SymbolTableEntry next,
		int scopeDepth,
		SymbolTableEntry prevtop,
		int prevtopIndex)
	{
		this.index = index;
		this.name = name;
		this.type = type;
		this.next = next;
		this.scopeDepth = scopeDepth;
		this.prevtop = prevtop;
		this.prevtopIndex = prevtopIndex;
	}
	
	/*****************************************************/
	/* Get unique offset for this symbol (for IR names) */
	/*****************************************************/
	public int getOffset()
	{
		return prevtopIndex;
	}
}
