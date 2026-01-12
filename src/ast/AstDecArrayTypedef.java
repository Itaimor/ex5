package ast;

import types.*;
import semantic.SemanticException;
import symboltable.SymbolTable;
import temp.*;

public class AstDecArrayTypedef extends AstDec
{
	public String name;
	public String elementTypeName;

	public AstDecArrayTypedef(String name, String elementTypeName)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.name = name;
		this.elementTypeName = elementTypeName;
	}

	public void printMe()
	{
		System.out.format("AST NODE ARRAY TYPEDEF: %s = %s[]\n", name, elementTypeName);
		AstGraphviz.getInstance().logNode(serialNumber, 
			String.format("ARRAY\n%s=%s[]", name, elementTypeName));
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		// PDF 2.1: Array definitions may appear only in global scope
		if (!SymbolTable.getInstance().isGlobalScope())
			throw new SemanticException(lineNumber, "array typedef must be at global scope");

		// Check name not already used in current scope (PDF 2.7)
		if (SymbolTable.getInstance().findInCurrentScope(name) != null)
			throw new SemanticException(lineNumber, "type '" + name + "' already defined");

		// Check element type exists and is not void
		Type elementType = SymbolTable.getInstance().find(elementTypeName);
		if (elementType == null)
			throw new SemanticException(lineNumber, "element type '" + elementTypeName + "' not found");
		if (elementType.isVoid())
			throw new SemanticException(lineNumber, "array cannot have void element type");

		// Create and enter array type
		TypeArray arrayType = new TypeArray(name, elementType);
		SymbolTable.getInstance().enter(name, arrayType);

		return null;
	}

	@Override
	public Temp irMe()
	{
		// Type declarations don't generate IR
		return null;
	}
}

