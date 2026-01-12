package ast;

import types.*;
import semantic.SemanticException;
import symboltable.SymbolTable;
import temp.*;
import ir.*;

public class AstStmtWhile extends AstStmt
{
	public AstExp cond;
	public AstStmtList body;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtWhile(AstExp cond, AstStmtList body)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.cond = cond;
		this.body = body;
	}

	public void printMe()
	{
		System.out.print("AST NODE STMT WHILE\n");
		if (cond != null) cond.printMe();
		if (body != null) body.printMe();

		AstGraphviz.getInstance().logNode(serialNumber, "WHILE");
		if (cond != null) AstGraphviz.getInstance().logEdge(serialNumber, cond.serialNumber);
		if (body != null) AstGraphviz.getInstance().logEdge(serialNumber, body.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		// PDF 2.5: condition must be int
		Type condType = cond.semantMe();
		if (!condType.isInt())
			throw new SemanticException(cond.lineNumber, "while condition must be int");

		// Begin scope for while body
		SymbolTable.getInstance().beginScope();

		if (body != null)
			body.semantMe();

		SymbolTable.getInstance().endScope();

		return null;
	}

	@Override
	public Temp irMe()
	{
		String beginLabel = IrCommand.getFreshLabel("while_begin");
		String afterLabel = IrCommand.getFreshLabel("while_after");
		
		// Emit begin label
		Ir.getInstance().AddIrCommand(new IrCommandLabel(beginLabel));
		
		// Generate condition
		Temp condTemp = cond.irMe();
		
		// If false (0), exit loop
		Ir.getInstance().AddIrCommand(new IrCommandJumpIfEqToZero(condTemp, afterLabel));
		
		// Generate body
		if (body != null) {
			body.irMe();
		}
		
		// Jump back to begin
		Ir.getInstance().AddIrCommand(new IrCommandJumpLabel(beginLabel));
		
		// Emit after label
		Ir.getInstance().AddIrCommand(new IrCommandLabel(afterLabel));
		
		return null;
	}
}
