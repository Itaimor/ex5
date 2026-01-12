package ast;

import types.*;
import semantic.SemanticException;
import temp.*;
import ir.*;

public class AstExpNil extends AstExp
{
	public AstExpNil()
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		// Debug disabled: 0
	}

	public void printMe()
	{
		System.out.print("AST NODE NIL\n");
		AstGraphviz.getInstance().logNode(serialNumber, "NIL");
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		return TypeNil.getInstance();
	}

	@Override
	public Temp irMe()
	{
		// Nil is represented as 0 (null pointer)
		Temp t = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IRcommandConstInt(t, 0));
		return t;
	}
}

