package ast;

import types.*;
import semantic.SemanticException;
import symboltable.SymbolTable;
import temp.*;
import ir.*;

public class AstExpNewArray extends AstExp
{
	public String typeName;
	public AstExp size;

	public AstExpNewArray(String typeName, AstExp size)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.typeName = typeName;
		this.size = size;
	}

	public void printMe()
	{
		System.out.format("AST NODE NEW ARRAY: %s[]\n", typeName);
		if (size != null) size.printMe();
		AstGraphviz.getInstance().logNode(serialNumber, 
			String.format("NEW\n%s[]", typeName));
		if (size != null) AstGraphviz.getInstance().logEdge(serialNumber, size.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		// PDF 2.3: new T[e] - T must be a previously declared type
		Type elementType = SymbolTable.getInstance().find(typeName);
		if (elementType == null)
			throw new SemanticException(lineNumber, "type '" + typeName + "' not found");
		
		// Element type cannot be void
		if (elementType.isVoid())
			throw new SemanticException(lineNumber, "array cannot have void element type");

		// Size expression must be int
		Type sizeType = size.semantMe();
		if (!sizeType.isInt())
			throw new SemanticException(lineNumber, "array size must be int");

		// PDF 2.3: If size is constant expression, must be > 0
		Integer constVal = size.getConstantValue();
		if (constVal != null && constVal <= 0)
			throw new SemanticException(size.lineNumber, "array size must be positive");

		// Return anonymous array type (no name)
		return new TypeArray(elementType);
	}

	@Override
	public Temp irMe()
	{
		// Generate IR for size expression
		//Temp sizeTemp = size.irMe();
		
		// Allocate array (simplified - allocate returns array address)
		Temp arrayAddr = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandAllocate(String.format("array_%s", typeName)));
		
		return arrayAddr;
	}
}

