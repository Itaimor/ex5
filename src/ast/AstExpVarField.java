package ast;

import types.*;
import semantic.SemanticException;
import temp.*;
import ir.*;

public class AstExpVarField extends AstExpVar
{
	public AstExpVar var;
	public String fieldName;
	
	public AstExpVarField(AstExpVar var, String fieldName)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.var = var;
		this.fieldName = fieldName;
	}

	public void printMe()
	{
		System.out.format("FIELD VAR: .%s\n", fieldName);
		if (var != null) var.printMe();

		AstGraphviz.getInstance().logNode(serialNumber,
			String.format("FIELD\n.%s", fieldName));
		if (var != null) AstGraphviz.getInstance().logEdge(serialNumber, var.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		// PDF 2.2: v.f - v must be class type, f must be member
		Type varType = var.semantMe();

		if (!varType.isClass())
			throw new SemanticException(lineNumber, "field access on non-class type");

		TypeClass classType = (TypeClass) varType;
		TypeClassVarDec member = classType.findMemberInHierarchy(fieldName);

		if (member == null)
			throw new SemanticException(lineNumber, "field '" + fieldName + "' not found in class");

		resolvedType = member.t;
		return resolvedType;
	}

	@Override
	public Temp irMe()
	{
		Temp baseTemp = var.irMe();

		Ir.getInstance().AddIrCommand(
			new IrCommandJumpIfEqToZero(baseTemp, "label_error_null_deref"));

		int offset = ClassLayout.getInstance().getFieldOffset(
			var.resolvedType.name, fieldName);
		Temp result = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IrCommandLoadField(result, baseTemp, offset));
		return result;
	}
}
