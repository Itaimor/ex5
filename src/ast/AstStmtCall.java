package ast;

import types.*;
import semantic.SemanticException;
import temp.*;

public class AstStmtCall extends AstStmt
{
	public AstExpCall callExp;
	
	public AstStmtCall(AstExpCall callExp)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.callExp = callExp;
	}
	
	public void printMe()
	{
		if (callExp != null) callExp.printMe();

		AstGraphviz.getInstance().logNode(serialNumber, "STMT\nCALL");
		if (callExp != null) AstGraphviz.getInstance().logEdge(serialNumber, callExp.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		// Just delegate to the call expression
		if (callExp != null)
			callExp.semantMe();
		return null;
	}

	@Override
	public Temp irMe()
	{
		// Delegate to call expression (discard return value)
		if (callExp != null) {
			callExp.irMe();
		}
		return null;
	}
}
