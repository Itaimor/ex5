package ast;

import types.Type;
import semantic.SemanticException;
import temp.Temp;

public abstract class AstNode {
	public int serialNumber;
	public static int staticLine = 0;  // Set by parser on each token
	public int lineNumber = staticLine;

	public AstNode() {
	}

	public AstNode(int lineNumber) {
		this.lineNumber = lineNumber;
	}

	/***********************************************/
	/* The default message for an unknown AST node */
	/***********************************************/
	public void printMe() {
		System.out.print("AST NODE UNKNOWN\n");
	}

	public abstract Type semantMe() throws SemanticException;
	
	/*****************************************/
	/* The default IR action for an AST node */
	/*****************************************/
	public Temp irMe()
	{
		return null;
	}
}
