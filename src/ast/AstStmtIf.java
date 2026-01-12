package ast;

import types.*;
import semantic.SemanticException;
import symboltable.*;
import temp.*;
import ir.*;

public class AstStmtIf extends AstStmt
{
	public AstExp cond;
	public AstStmtList thenBody;
	public AstStmtList elseBody;  // null if no else

	public AstStmtIf(AstExp cond, AstStmtList thenBody, AstStmtList elseBody)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.cond = cond;
		this.thenBody = thenBody;
		this.elseBody = elseBody;
	}

	public void printMe()
	{
		System.out.print("AST NODE STMT IF\n");
		if (cond != null) cond.printMe();
		if (thenBody != null) thenBody.printMe();
		if (elseBody != null) elseBody.printMe();

		AstGraphviz.getInstance().logNode(serialNumber, "IF");
		if (cond != null) AstGraphviz.getInstance().logEdge(serialNumber, cond.serialNumber);
		if (thenBody != null) AstGraphviz.getInstance().logEdge(serialNumber, thenBody.serialNumber);
		if (elseBody != null) AstGraphviz.getInstance().logEdge(serialNumber, elseBody.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		// PDF 2.5: condition must be int
		Type condType = cond.semantMe();
		if (!condType.isInt())
			throw new SemanticException(cond.lineNumber, "if condition must be int");
		
		// Then branch scope
		SymbolTable.getInstance().beginScope();
		if (thenBody != null)
			thenBody.semantMe();
		SymbolTable.getInstance().endScope();

		// Else branch scope (if exists)
		if (elseBody != null) {
			SymbolTable.getInstance().beginScope();
			elseBody.semantMe();
			SymbolTable.getInstance().endScope();
		}

		return null;		
	}

	@Override
	public Temp irMe()
	{
		// Generate IR for condition
		Temp condTemp = cond.irMe();
		
		if (elseBody == null) {
			// Simple if-then (no else)
			String afterLabel = IrCommand.getFreshLabel("if_after");
			
			// If condition is 0 (false), jump to after
			Ir.getInstance().AddIrCommand(new IrCommandJumpIfEqToZero(condTemp, afterLabel));
			
			// Generate IR for then body
			if (thenBody != null) {
				thenBody.irMe();
			}
			
			// Emit after label
			Ir.getInstance().AddIrCommand(new IrCommandLabel(afterLabel));
		} else {
			// If-then-else
			String elseLabel = IrCommand.getFreshLabel("if_else");
			String afterLabel = IrCommand.getFreshLabel("if_after");
			
			// If condition is 0 (false), jump to else
			Ir.getInstance().AddIrCommand(new IrCommandJumpIfEqToZero(condTemp, elseLabel));
			
			// Generate IR for then body
			if (thenBody != null) {
				thenBody.irMe();
			}
			
			// Jump to after (skip else)
			Ir.getInstance().AddIrCommand(new IrCommandJumpLabel(afterLabel));
			
			// Emit else label and body
			Ir.getInstance().AddIrCommand(new IrCommandLabel(elseLabel));
			if (elseBody != null) {
				elseBody.irMe();
			}
			
			// Emit after label
			Ir.getInstance().AddIrCommand(new IrCommandLabel(afterLabel));
		}
		
		return null;
	}	
}
