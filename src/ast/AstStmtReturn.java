package ast;

import types.*;
import semantic.SemanticException;
import symboltable.SymbolTable;
import temp.*;
import ir.*;

public class AstStmtReturn extends AstStmt
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstExp exp;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtReturn(AstExp exp)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.exp = exp;
	}

	/********************************************************/
	/* The printing message for a return statement AST node */
	/********************************************************/
	public void printMe()
	{
		/***********************************/
		/* AST NODE TYPE = AST RETURN STMT */
		/***********************************/
		System.out.print("AST NODE STMT RETURN\n");

		/*****************************/
		/* RECURSIVELY PRINT exp ... */
		/*****************************/
		if (exp != null) exp.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			"RETURN");

		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (exp != null) AstGraphviz.getInstance().logEdge(serialNumber,exp.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		// PDF 2.5: Return statements only inside functions
		if (!SymbolTable.getInstance().insideFunction())
			throw new SemanticException(lineNumber, "return statement outside of function");
		
		Type expectedReturn = SymbolTable.getInstance().getReturnType();

		if (exp == null) {
			// return; - must be void function
			if (!expectedReturn.isVoid())
				throw new SemanticException(lineNumber, "non-void function must return a value");
		} else {
			// return exp; - must match return type
			Type actualReturn = exp.semantMe();
			if (expectedReturn.isVoid())
				throw new SemanticException(lineNumber, "void function cannot return a value");
			if (!TypeUtils.canAssignTo(actualReturn, expectedReturn))
				throw new SemanticException(lineNumber, "return type mismatch");
		}

		return null;
	}

	@Override
	public Temp irMe()
	{
		if (exp != null) {
			Temp retTemp = exp.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandReturn(retTemp));
		} else {
			Ir.getInstance().AddIrCommand(new IrCommandReturn(null));
		}
		return null;
	}
}
