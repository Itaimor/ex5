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

		resolvedType = t;
		return resolvedType;
	}

	@Override
	public Temp irMe()
	{
		int objSize = ClassLayout.getInstance().getObjectSize(className);

		Temp sizeTemp = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IRcommandConstInt(sizeTemp, objSize));
		Temp addrTemp = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandMalloc(addrTemp, sizeTemp));

		Temp vtableAddr = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandLoadAddress(vtableAddr,
			ClassLayout.getInstance().getVtableLabel(className)));
		Ir.getInstance().AddIrCommand(new IrCommandStoreField(addrTemp, 0, vtableAddr));

		for (ClassLayout.FieldInit init : ClassLayout.getInstance().getFieldInits(className))
		{
			Temp initTemp = TempFactory.getInstance().getFreshTemp();
			if (init.kind == ClassLayout.INIT_INT)
				Ir.getInstance().AddIrCommand(new IRcommandConstInt(initTemp, init.intValue));
			else if (init.kind == ClassLayout.INIT_STRING)
				Ir.getInstance().AddIrCommand(new IrCommandLoadAddress(initTemp,
					StringRegistry.getInstance().getOrRegister(init.stringValue)));
			else if (init.kind == ClassLayout.INIT_NIL)
				Ir.getInstance().AddIrCommand(new IRcommandConstInt(initTemp, 0));
			Ir.getInstance().AddIrCommand(new IrCommandStoreField(addrTemp, init.offset, initTemp));
		}

		return addrTemp;
	}
}

