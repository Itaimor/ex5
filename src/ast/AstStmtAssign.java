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
		// Generate IR for RHS expression
		Temp rhsTemp = exp.irMe();
		
		// Handle different LHS types
		if (var instanceof AstExpVarSimple) {
			// Simple assignment: x := exp
			AstExpVarSimple simpleVar = (AstExpVarSimple) var;
			Ir.getInstance().AddIrCommand(new IrCommandStore(simpleVar.getUniqueName(), rhsTemp));
		}
		else if (var instanceof AstExpVarSubscript) {
			// Array assignment: a[i] := exp
			// For now, simplified - would need proper array address calculation
			Ir.getInstance().AddIrCommand(new IrCommandStore("array_element", rhsTemp));
		}
		else if (var instanceof AstExpVarField) {
			// Field assignment: obj.field := exp
			// For now, simplified - would need proper field offset calculation
			Ir.getInstance().AddIrCommand(new IrCommandStore("field", rhsTemp));
		}
		
		return null;
	}
}
