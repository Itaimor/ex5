package ast;

import types.*;
import semantic.SemanticException;
import symboltable.*;
import temp.*;
import ir.*;

public class AstDecVar extends AstDec {
	/****************/
	/* DATA MEMBERS */
	/****************/
	public String type;
	public String name;
	public AstExp initialValue;
	
	/*****************************************************/
	/* Symbol table entry (saved during semantic analysis) */
	/*****************************************************/
	private SymbolTableEntry entry;

	public String getUniqueName() {
		if (entry != null) {
			return name + "_" + entry.getOffset();
		}
		return name;
	}

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstDecVar(String type, String name, AstExp initialValue, int line) {
		serialNumber = AstNodeSerialNumber.getFresh();
		this.lineNumber = line;  // Override default line number
		this.type = type;
		this.name = name;
		this.initialValue = initialValue;
	}

	/************************************************************/
	/* The printing message for a variable declaration AST node */
	/************************************************************/
	public void printMe() {
		/****************************************/
		/* AST NODE TYPE = AST VAR DECLARATION */
		/***************************************/
		if (initialValue != null)
			System.out.format("VAR-DEC(%s):%s := initialValue\n", name, type);
		if (initialValue == null)
			System.out.format("VAR-DEC(%s):%s                \n", name, type);

		/**************************************/
		/* RECURSIVELY PRINT initialValue ... */
		/**************************************/
		if (initialValue != null)
			initialValue.printMe();

		/**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
				String.format("VAR\nDEC(%s)\n:%s", name, type));

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (initialValue != null)
			AstGraphviz.getInstance().logEdge(serialNumber, initialValue.serialNumber);

	}

	@Override
	public Type semantMe() throws SemanticException {
		// 1. Check type exists
		Type t = SymbolTable.getInstance().find(type);
		if (t == null)
			throw new SemanticException(lineNumber, "type '" + type + "' does not exist");

		// 2. Variables cannot be void (PDF 2.1)
		if (t.isVoid())
			throw new SemanticException(lineNumber, "variable cannot be declared with type void");

		// 2. Check name does not already exist in the current scope
		if (SymbolTable.getInstance().findInCurrentScope(name) != null)
			throw new SemanticException(lineNumber, "variable '" + name + "' already declared in this scope");

		// 3. Check initial value (if exists)
		if (initialValue != null) {
			Type initType = initialValue.semantMe();
			
			if (!TypeUtils.canAssignTo(initType, t))
				throw new SemanticException(lineNumber, "type mismatch in variable initialization");
		}

		// 5. Enter variable into symbol table
		SymbolTable.getInstance().enter(name, t);
		
		// Save entry for IR generation
		entry = SymbolTable.getInstance().findEntry(name);

		return t;
	}

	@Override
	public Temp irMe()
	{
		// Only generate IR if variable has initializer
		if (initialValue != null) {
			Temp initTemp = initialValue.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandStore(getUniqueName(), initTemp));
		}
		// No initializer → variable stays uninitialized (important for dataflow analysis!)
		return null;
	}

}
