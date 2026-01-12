package ast;

import types.*;
import semantic.SemanticException;
import temp.*;
import ir.*;

public class AstExpString extends AstExp
{
	public String value;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpString(String value)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();
		this.value = value;
	}

	/******************************************************/
	/* The printing message for a STRING EXP AST node */
	/******************************************************/
	public void printMe()
	{
		/*******************************/
		/* AST NODE TYPE = AST STRING EXP */
		/*******************************/
		System.out.format("AST NODE STRING( %s )\n",value);

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("STRING\n%s",value.replace('"','\'')));
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		return TypeString.getInstance();
	}

	@Override
	public Temp irMe()
	{
		// For now, strings are represented as constants in IR
		// In a real compiler, we'd load a string constant address
		Temp t = TempFactory.getInstance().getFreshTemp();
		// Store string value symbolically - for dataflow analysis this doesn't matter
		// as we only analyze int variables in EX4
		Ir.getInstance().AddIrCommand(new IrCommandLoad(t, "STRING_" + value.hashCode()));
		return t;
	}
}
