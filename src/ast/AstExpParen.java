package ast;

import types.*;
import semantic.SemanticException;
import temp.*;

public class AstExpParen extends AstExp
{
	public AstExp exp;

	public AstExpParen(AstExp exp)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.exp = exp;
	}

	public void printMe()
	{
		System.out.print("AST NODE PAREN EXP\n");
		if (exp != null) exp.printMe();
		AstGraphviz.getInstance().logNode(serialNumber, "(EXP)");
		if (exp != null) AstGraphviz.getInstance().logEdge(serialNumber, exp.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		resolvedType = exp.semantMe();
		return resolvedType;
	}

	@Override
	public Integer getConstantValue() {
		return exp.getConstantValue();
	}

	@Override
	public Temp irMe()
	{
		// Parentheses don't change semantics, just delegate to inner expression
		return exp.irMe();
	}
}

