package ast;

import types.Type;
import semantic.SemanticException;
import temp.*;

public class AstCFieldVar extends AstCField {
	public AstDecVar varDec;

	public AstCFieldVar(AstDecVar varDec) {
		serialNumber = AstNodeSerialNumber.getFresh();
		this.varDec = varDec;
		// Use varDec's line number for better error reporting
		this.lineNumber = varDec.lineNumber;
	}

	public void printMe() {
		System.out.println("CLASS FIELD VAR");
		if (varDec != null)
			varDec.printMe();

		AstGraphviz.getInstance().logNode(serialNumber, "CFIELD\nVAR");
		if (varDec != null)
			AstGraphviz.getInstance().logEdge(serialNumber, varDec.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException {
		return varDec.semantMe();
	}

	@Override
	public Temp irMe()
	{
		// Field variable declarations don't generate IR (only methods do)
		return null;
	}
}

