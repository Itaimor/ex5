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

		return member.t;
	}

	@Override
	public Temp irMe()
	{
		// Generate IR for base variable
		//Temp baseTemp = var.irMe();
		
		// Calculate field offset from class type
		// For now, we'll use a simplified approach - load field symbolically
		Temp result = TempFactory.getInstance().getFreshTemp();
		String fieldAccess = String.format("field_%s", fieldName);
		Ir.getInstance().AddIrCommand(new IrCommandLoad(result, fieldAccess));
		
		return result;
	}
}
