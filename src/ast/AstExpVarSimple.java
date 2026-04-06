package ast;

import types.*;
import symboltable.*;
import semantic.SemanticException;
import temp.*;
import ir.*;

public class AstExpVarSimple extends AstExpVar
{
	/************************/
	/* simple variable name */
	/************************/
	public String name;
	
	/*****************************************************/
	/* Symbol table entry (saved during semantic analysis) */
	/*****************************************************/
	public SymbolTableEntry entry;
	
	public String getUniqueName()
	{
		if (entry != null) {
			return name + "_" + entry.getOffset();
		}
		return name;
	}

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpVarSimple(String name)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();
		this.name = name;
	}

	/**************************************************/
	/* The printing message for a simple var AST node */
	/**************************************************/
	public void printMe()
	{
		/**********************************/
		/* AST NODE TYPE = AST SIMPLE VAR */
		/**********************************/
		System.out.format("AST NODE SIMPLE VAR( %s )\n",name);

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("SIMPLE\nVAR\n(%s)",name));
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		Type t = SymbolTable.getInstance().find(name);
		if (t == null)
			throw new SemanticException(lineNumber, "variable '" + name + "' is not declared");
		
		entry = SymbolTable.getInstance().findEntry(name);
		
		resolvedType = t;
		return resolvedType;
	}

	@Override
	public Temp irMe()
	{
		if (AstDecFunc.currentMethodOwner != null)
		{
			int fieldOffset = ClassLayout.getInstance().getFieldOffset(
				AstDecFunc.currentMethodOwner, this.name);
			if (fieldOffset >= 0 && (entry == null || entry.scopeDepth <= 1))
			{
				Temp thisTemp = TempFactory.getInstance().getFreshTemp();
				Ir.getInstance().AddIrCommand(new IrCommandLoad(thisTemp, "__this"));
				Temp result = TempFactory.getInstance().getFreshTemp();
				Ir.getInstance().AddIrCommand(new IrCommandLoadField(result, thisTemp, fieldOffset));
				return result;
			}
		}
		Temp t = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandLoad(t, getUniqueName()));
		return t;
	}
}
