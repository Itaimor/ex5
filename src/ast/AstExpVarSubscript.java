package ast;

import types.*;
import semantic.SemanticException;
import temp.*;
import ir.*;

public class AstExpVarSubscript extends AstExpVar
{
	public AstExpVar var;
	public AstExp subscript;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpVarSubscript(AstExpVar var, AstExp subscript)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.var = var;
		this.subscript = subscript;
	}

	/*****************************************************/
	/* The printing message for a subscript var AST node */
	/*****************************************************/
	public void printMe()
	{
		/*************************************/
		/* AST NODE TYPE = AST SUBSCRIPT VAR */
		/*************************************/
		System.out.print("AST NODE SUBSCRIPT VAR\n");

		/****************************************/
		/* RECURSIVELY PRINT VAR + SUBSRIPT ... */
		/****************************************/
		if (var != null) var.printMe();
		if (subscript != null) subscript.printMe();

		AstGraphviz.getInstance().logNode(serialNumber, "SUBSCRIPT\nVAR");
		if (var != null) AstGraphviz.getInstance().logEdge(serialNumber, var.serialNumber);
		if (subscript != null) AstGraphviz.getInstance().logEdge(serialNumber, subscript.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		// PDF 2.3: v[e] - v must be array type, e must be int
		Type varType = var.semantMe();
		Type subscriptType = subscript.semantMe();

		// Check var is array type
		if (!varType.isArray())
			throw new SemanticException(lineNumber, "subscript access on non-array type");

		// Check subscript is int
		if (!subscriptType.isInt())
			throw new SemanticException(lineNumber, "array subscript must be int");

		// PDF 2.3: If subscript is constant expression, must be >= 0
		Integer constVal = subscript.getConstantValue();
		if (constVal != null && constVal < 0)
			throw new SemanticException(subscript.lineNumber, "array subscript cannot be negative constant");

		TypeArray arrType = (TypeArray) varType;
		resolvedType = arrType.elementType;
		return resolvedType;
	}

	@Override
	public Temp irMe()
	{
		Temp baseTemp = var.irMe();
		Temp indexTemp = subscript.irMe();

		Ir.getInstance().AddIrCommand(
			new IrCommandJumpIfEqToZero(baseTemp, "label_error_null_deref"));

		emitBoundsCheck(baseTemp, indexTemp);

		Temp result = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandLoadArray(result, baseTemp, indexTemp));
		return result;
	}

	public static void emitBoundsCheck(Temp baseTemp, Temp indexTemp)
	{
		Temp zeroTemp = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IRcommandConstInt(zeroTemp, 0));
		Temp isNeg = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandBinopLtIntegers(isNeg, indexTemp, zeroTemp));
		String skipNeg = IrCommand.getFreshLabel("bounds_ok_neg");
		Ir.getInstance().AddIrCommand(new IrCommandJumpIfEqToZero(isNeg, skipNeg));
		Ir.getInstance().AddIrCommand(new IrCommandJumpLabel("label_error_access_violation"));
		Ir.getInstance().AddIrCommand(new IrCommandLabel(skipNeg));

		Temp lengthTemp = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandLoadField(lengthTemp, baseTemp, 0));
		Temp inBounds = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandBinopLtIntegers(inBounds, indexTemp, lengthTemp));
		Ir.getInstance().AddIrCommand(
			new IrCommandJumpIfEqToZero(inBounds, "label_error_access_violation"));
	}
}
