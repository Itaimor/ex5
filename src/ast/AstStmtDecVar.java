package ast;

import types.*;
import semantic.SemanticException;
import temp.*;

public class AstStmtDecVar extends AstStmt
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstDecVar var;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstStmtDecVar(AstDecVar var)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		this.var = var;
	}
	
	public void printMe()
	{
		var.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("STMT\nDEC\nVAR"));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		return var.semantMe();
	}

	@Override
	public Temp irMe()
	{
		// Delegate to AstDecVar irMe()
		// Only generate IR if variable has initializer
		if (var != null) {
			var.irMe();
		}
		return null;
	}
}
