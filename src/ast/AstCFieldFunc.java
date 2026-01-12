package ast;

import types.Type;
import semantic.SemanticException;
import temp.*;

public class AstCFieldFunc extends AstCField {
	public AstDecFunc funcDec;

	public AstCFieldFunc(AstDecFunc funcDec) {
		serialNumber = AstNodeSerialNumber.getFresh();
		this.funcDec = funcDec;
		// Use funcDec's line number for better error reporting
		this.lineNumber = funcDec.lineNumber;
	}

	public void printMe() {
		System.out.println("CLASS FIELD FUNC");
		if (funcDec != null)
			funcDec.printMe();

		AstGraphviz.getInstance().logNode(serialNumber, "CFIELD\nFUNC");
		if (funcDec != null)
			AstGraphviz.getInstance().logEdge(serialNumber, funcDec.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException {
		return funcDec.semantMe();
	}

	@Override
	public Temp irMe()
	{
		// Generate IR for method body
		if (funcDec != null) {
			funcDec.irMe();
		}
		return null;
	}
}

