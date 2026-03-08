package ast;

import types.*;
import semantic.SemanticException;
import temp.*;
import ir.*;

public class AstExpBinop extends AstExp
{
	// Operator constants
	public static final int OP_PLUS = 0;
	public static final int OP_MINUS = 1;
	public static final int OP_TIMES = 2;
	public static final int OP_DIVIDE = 3;
	public static final int OP_LT = 4;
	public static final int OP_GT = 5;
	public static final int OP_EQ = 6;

	public int op;
	public AstExp left;
	public AstExp right;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpBinop(AstExp left, AstExp right, int op)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();

		/*******************************/
		/* COPY INPUT DATA MENBERS ... */
		/*******************************/
		this.left = left;
		this.right = right;
		this.op = op;
	}
	
	/*************************************************/
	/* The printing message for a binop exp AST node */
	/*************************************************/
	public void printMe()
	{
		String sop = getOpSymbol();

		/**********************************/
		/* AST NODE TYPE = AST BINOP EXP */
		/*********************************/
		System.out.print("AST NODE BINOP EXP\n");
		System.out.format("BINOP EXP(%s)\n",sop);

		/**************************************/
		/* RECURSIVELY PRINT left + right ... */
		/**************************************/
		if (left != null) left.printMe();
		if (right != null) right.printMe();

		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("BINOP(%s)",sop));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (left  != null) AstGraphviz.getInstance().logEdge(serialNumber,left.serialNumber);
		if (right != null) AstGraphviz.getInstance().logEdge(serialNumber,right.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		Type t1 = left.semantMe();
		Type t2 = right.semantMe();
		
		if (op == OP_PLUS)
		{
			if (t1.isInt() && t2.isInt())
			{ resolvedType = TypeInt.getInstance(); return resolvedType; }
			if (t1.isString() && t2.isString())
			{ resolvedType = TypeString.getInstance(); return resolvedType; }
			throw new SemanticException(lineNumber, "'+' requires two ints or two strings");
		}
		else if (op == OP_MINUS || op == OP_TIMES || op == OP_DIVIDE)
		{
			if (!t1.isInt() || !t2.isInt())
				throw new SemanticException(lineNumber, "arithmetic operator requires int operands");
			if (op == OP_DIVIDE && right instanceof AstExpInt) {
				int val = ((AstExpInt) right).value;
				if (val == 0)
					throw new SemanticException(lineNumber, "division by zero");
			}
			resolvedType = TypeInt.getInstance();
			return resolvedType;
		}
		else if (op == OP_LT || op == OP_GT)
		{
			if (!t1.isInt() || !t2.isInt())
				throw new SemanticException(lineNumber, "comparison operator requires int operands");
			resolvedType = TypeInt.getInstance();
			return resolvedType;
		}
		else if (op == OP_EQ)
		{
			if (!TypeUtils.canCompareEquality(t1, t2))
				throw new SemanticException(lineNumber, "cannot compare these types for equality");
			resolvedType = TypeInt.getInstance();
			return resolvedType;
		}
		
		throw new SemanticException(lineNumber, "unknown binary operator");
	}

	@Override
	public Integer getConstantValue() {
		Integer leftVal = left.getConstantValue();
		Integer rightVal = right.getConstantValue();
		if (leftVal == null || rightVal == null) return null;
		
		switch (op) {
			case OP_PLUS:   return leftVal + rightVal;
			case OP_MINUS:  return leftVal - rightVal;
			case OP_TIMES:  return leftVal * rightVal;
			case OP_DIVIDE: return rightVal == 0 ? null : leftVal / rightVal;
			default: return null;
		}
	}

	/** Get the symbol string for this operator */
	private String getOpSymbol() {
		switch (op) {
			case OP_PLUS:   return "+";
			case OP_MINUS:  return "-";
			case OP_TIMES:  return "*";
			case OP_DIVIDE: return "/";
			case OP_LT:     return "<";
			case OP_GT:     return ">";
			case OP_EQ:     return "=";
			default:        return "?";
		}
	}

	@Override
	public Temp irMe()
	{
		Temp t1 = left.irMe();
		Temp t2 = right.irMe();
		Temp result = TempFactory.getInstance().getFreshTemp();

		switch (op) {
			case OP_PLUS:
				if (left.resolvedType != null && left.resolvedType.isString())
					Ir.getInstance().AddIrCommand(new IrCommandStringConcat(result, t1, t2));
				else
					Ir.getInstance().AddIrCommand(new IrCommandBinopAddIntegers(result, t1, t2));
				break;
			case OP_MINUS:
				Ir.getInstance().AddIrCommand(new IrCommandBinopSubIntegers(result, t1, t2));
				break;
			case OP_TIMES:
				Ir.getInstance().AddIrCommand(new IrCommandBinopMulIntegers(result, t1, t2));
				break;
			case OP_DIVIDE:
				Ir.getInstance().AddIrCommand(
					new IrCommandJumpIfEqToZero(t2, "label_error_div_by_zero"));
				Ir.getInstance().AddIrCommand(new IrCommandBinopDivIntegers(result, t1, t2));
				break;
			case OP_LT:
				Ir.getInstance().AddIrCommand(new IrCommandBinopLtIntegers(result, t1, t2));
				break;
			case OP_GT:
				Ir.getInstance().AddIrCommand(new IrCommandBinopGtIntegers(result, t1, t2));
				break;
			case OP_EQ:
				if (left.resolvedType != null && left.resolvedType.isString())
					Ir.getInstance().AddIrCommand(new IrCommandStringEq(result, t1, t2));
				else
					Ir.getInstance().AddIrCommand(new IrCommandBinopEqIntegers(result, t1, t2));
				break;
		}
		return result;
	}
}
