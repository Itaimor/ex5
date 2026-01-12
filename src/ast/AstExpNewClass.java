package ast;

import types.*;
import semantic.SemanticException;
import symboltable.SymbolTable;
import temp.*;
import ir.*;

public class AstExpNewClass extends AstExp
{
	public String className;

	public AstExpNewClass(String className)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.className = className;
	}

	public void printMe()
	{
		System.out.format("AST NODE NEW CLASS: %s\n", className);
		AstGraphviz.getInstance().logNode(serialNumber, 
			String.format("NEW\n%s", className));
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		// PDF 2.2: new T - T must be a previously defined class
		Type t = SymbolTable.getInstance().find(className);
		if (t == null)
			throw new SemanticException(lineNumber, "class '" + className + "' not found");
		if (!t.isClass())
			throw new SemanticException(lineNumber, "'" + className + "' is not a class type");

		return t;
	}

	@Override
	public Temp irMe()
	{
		// Allocate object (simplified)
		Temp objectAddr = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandAllocate(String.format("object_%s", className)));
		
		return objectAddr;
	}
}

