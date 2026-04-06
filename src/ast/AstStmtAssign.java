package ast;

import types.*;
import semantic.SemanticException;
import temp.*;
import ir.*;

public class AstStmtAssign extends AstStmt
{
	/***************/
	/*  var := exp */
	/***************/
	public AstExpVar var;
	public AstExp exp;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtAssign(AstExpVar var, AstExp exp)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/*******************************/
		/* COPY INPUT DATA MENBERS ... */
		/*******************************/
		this.var = var;
		this.exp = exp;
	}

	/*********************************************************/
	/* The printing message for an assign statement AST node */
	/*********************************************************/
	public void printMe()
	{
		/********************************************/
		/* AST NODE TYPE = AST ASSIGNMENT STATEMENT */
		/********************************************/
		System.out.print("AST NODE ASSIGN STMT\n");

		/***********************************/
		/* RECURSIVELY PRINT VAR + EXP ... */
		/***********************************/
		if (var != null) var.printMe();
		if (exp != null) exp.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			"ASSIGN\nleft := right\n");
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
		AstGraphviz.getInstance().logEdge(serialNumber,exp.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		// PDF 2.4: x := e - type of e must be compatible with type of x
		Type varType = var.semantMe();
		Type expType = exp.semantMe();
		
		if (!TypeUtils.canAssignTo(expType, varType))
			throw new SemanticException(lineNumber, "type mismatch in assignment");

		return null;
	}

	@Override
	public Temp irMe()
	{
		if (var instanceof AstExpVarSimple) {
			AstExpVarSimple sv = (AstExpVarSimple) var;
			if (AstDecFunc.currentMethodOwner != null)
			{
				int fieldOffset = ClassLayout.getInstance().getFieldOffset(
					AstDecFunc.currentMethodOwner, sv.name);
				if (fieldOffset >= 0 && (sv.entry == null || sv.entry.scopeDepth <= 1))
				{
					Temp thisTemp = TempFactory.getInstance().getFreshTemp();
					Ir.getInstance().AddIrCommand(new IrCommandLoad(thisTemp, "__this"));
					Temp rhsTemp = exp.irMe();
					Ir.getInstance().AddIrCommand(new IrCommandStoreField(thisTemp, fieldOffset, rhsTemp));
					return null;
				}
			}
			Temp rhsTemp = exp.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandStore(sv.getUniqueName(), rhsTemp));
		}
		else if (var instanceof AstExpVarSubscript) {
			AstExpVarSubscript sub = (AstExpVarSubscript) var;
			Temp baseTemp  = sub.var.irMe();
			Temp indexTemp = sub.subscript.irMe();
			Temp rhsTemp   = exp.irMe();
			Ir.getInstance().AddIrCommand(
				new IrCommandJumpIfEqToZero(baseTemp, "label_error_null_deref"));
			AstExpVarSubscript.emitBoundsCheck(baseTemp, indexTemp);
			Ir.getInstance().AddIrCommand(new IrCommandStoreArray(baseTemp, indexTemp, rhsTemp));
		}
		else if (var instanceof AstExpVarField) {
			AstExpVarField fld = (AstExpVarField) var;
			Temp baseTemp = fld.var.irMe();
			Temp rhsTemp  = exp.irMe();
			Ir.getInstance().AddIrCommand(
				new IrCommandJumpIfEqToZero(baseTemp, "label_error_null_deref"));
			int offset = ClassLayout.getInstance().getFieldOffset(
				fld.var.resolvedType.name, fld.fieldName);
			Ir.getInstance().AddIrCommand(new IrCommandStoreField(baseTemp, offset, rhsTemp));
		}
		return null;
	}
}
