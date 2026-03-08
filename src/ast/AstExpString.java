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
		resolvedType = TypeString.getInstance();
		return resolvedType;
	}

	@Override
	public Temp irMe()
	{
		String label = StringRegistry.getInstance().getOrRegister(value);
		Temp t = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandLoadAddress(t, label));
		return t;
	}
}
