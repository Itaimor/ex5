package ast;

import types.*;
import semantic.SemanticException;
import temp.*;
import ir.*;

public class AstExpInt extends AstExp
{
	public int value;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpInt(int value)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();
		this.value = value;
	}

	/************************************************/
	/* The printing message for an INT EXP AST node */
	/************************************************/
	public void printMe()
	{
		/*******************************/
		/* AST NODE TYPE = AST INT EXP */
		/*******************************/
		System.out.format("AST NODE INT( %d )\n",value);

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("INT(%d)",value));
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		return TypeInt.getInstance();
	}

	@Override
	public Integer getConstantValue() {
		return value;
	}

	@Override
	public Temp irMe()
	{
		Temp t = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IRcommandConstInt(t, value));
		return t;
	}
}
