package ast;

import types.*;
import semantic.SemanticException;
import symboltable.SymbolTable;
import temp.*;
import ir.*;
import java.util.ArrayList;
import java.util.List;

public class AstExpCall extends AstExp
{
	public AstExpVar var;      // null for global function call, non-null for method call
	public String funcName;
	public AstExpList args;

	public AstExpCall(AstExpVar var, String funcName, AstExpList args)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.var = var;
		this.funcName = funcName;
		this.args = args;
	}

	public void printMe()
	{
		System.out.format("CALL(%s)\n", funcName);
		if (var != null) var.printMe();
		if (args != null) args.printMe();
		
		AstGraphviz.getInstance().logNode(serialNumber,
			String.format("CALL\n%s", funcName));
		if (var != null) AstGraphviz.getInstance().logEdge(serialNumber, var.serialNumber);
		if (args != null) AstGraphviz.getInstance().logEdge(serialNumber, args.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		TypeFunction funcType = null;

		if (var == null) {
			// Global function call: funcName(args)
			Type t = SymbolTable.getInstance().find(funcName);
			if (t == null)
				throw new SemanticException(lineNumber, "function '" + funcName + "' not found");
			if (!(t instanceof TypeFunction))
				throw new SemanticException(lineNumber, "'" + funcName + "' is not a function");
			funcType = (TypeFunction) t;
		} else {
			// Method call: var.funcName(args)
			Type varType = var.semantMe();
			if (!varType.isClass())
				throw new SemanticException(lineNumber, "method call on non-class type");
			
			TypeClass classType = (TypeClass) varType;
			TypeClassVarDec member = classType.findMemberInHierarchy(funcName);
			if (member != null) {
				if (!(member.t instanceof TypeFunction))
					throw new SemanticException(lineNumber, "'" + funcName + "' is not a method");
				funcType = (TypeFunction) member.t;
			} else {
				SymbolTable st = SymbolTable.getInstance();
				TypeClass curClass = st.getCurClass();
				TypeFunction curFunc = st.getCurrFunc();
				if (curClass != null && classType == curClass
						&& curFunc != null && funcName.equals(curFunc.name)) {
					funcType = curFunc;
				} else {
					throw new SemanticException(lineNumber, "method '" + funcName + "' not found in class");
				}
			}
		}

		// Check argument count and types
		TypeList expectedParams = funcType.params;
		AstExpList actualArgs = args;

		while (expectedParams != null && actualArgs != null) {
			Type expectedType = expectedParams.head;
			Type actualType = actualArgs.head.semantMe();
			
			if (!TypeUtils.canAssignTo(actualType, expectedType))
				throw new SemanticException(lineNumber, "argument type mismatch in call to '" + funcName + "'");
			
			expectedParams = expectedParams.tail;
			actualArgs = actualArgs.tail;
		}

		// Check same number of arguments
		if (expectedParams != null || actualArgs != null)
			throw new SemanticException(lineNumber, "wrong number of arguments in call to '" + funcName + "'");

		resolvedType = funcType.returnType;
		return resolvedType;
	}

	@Override
	public Temp irMe()
	{
		if (funcName.equals("PrintInt") && var == null) {
			if (args != null) {
				Temp argTemp = args.head.irMe();
				Ir.getInstance().AddIrCommand(new IrCommandPrintInt(argTemp));
			}
			return null;
		}

		if (funcName.equals("PrintString") && var == null) {
			if (args != null) {
				Temp argTemp = args.head.irMe();
				Ir.getInstance().AddIrCommand(new IrCommandPrintString(argTemp));
			}
			return null;
		}

		if (var == null) {
			if (AstDecFunc.currentMethodOwner != null)
			{
				int methodIdx = ClassLayout.getInstance().getMethodIndex(
					AstDecFunc.currentMethodOwner, funcName);
				if (methodIdx >= 0)
				{
					Temp thisTemp = TempFactory.getInstance().getFreshTemp();
					Ir.getInstance().AddIrCommand(new IrCommandLoad(thisTemp, "__this"));
					List<Temp> argTemps = new ArrayList<>();
					argTemps.add(thisTemp);
					for (AstExpList cur = args; cur != null; cur = cur.tail)
						argTemps.add(cur.head.irMe());
					Temp dst = (resolvedType != null && !resolvedType.isVoid())
						? TempFactory.getInstance().getFreshTemp() : null;
					Ir.getInstance().AddIrCommand(
						new IrCommandVirtualCall(dst, thisTemp, methodIdx, argTemps));
					return dst;
				}
			}
			List<Temp> argTemps = new ArrayList<>();
			for (AstExpList cur = args; cur != null; cur = cur.tail)
				argTemps.add(cur.head.irMe());
			String funcLabel = "func_" + funcName;
			Temp dst = (resolvedType != null && !resolvedType.isVoid())
				? TempFactory.getInstance().getFreshTemp() : null;
			Ir.getInstance().AddIrCommand(new IrCommandCall(dst, funcLabel, argTemps));
			return dst;
		} else {
			Temp baseTemp = var.irMe();
			Ir.getInstance().AddIrCommand(
				new IrCommandJumpIfEqToZero(baseTemp, "label_error_null_deref"));
			List<Temp> argTemps = new ArrayList<>();
			argTemps.add(baseTemp);
			for (AstExpList cur = args; cur != null; cur = cur.tail)
				argTemps.add(cur.head.irMe());
			int methodIdx = ClassLayout.getInstance().getMethodIndex(
				var.resolvedType.name, funcName);
			Temp dst = (resolvedType != null && !resolvedType.isVoid())
				? TempFactory.getInstance().getFreshTemp() : null;
			Ir.getInstance().AddIrCommand(
				new IrCommandVirtualCall(dst, baseTemp, methodIdx, argTemps));
			return dst;
		}
	}
}
