package ast;

import types.*;
import semantic.SemanticException;
import symboltable.SymbolTable;
import temp.*;
import ir.*;

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
			if (member == null)
				throw new SemanticException(lineNumber, "method '" + funcName + "' not found in class");
			if (!(member.t instanceof TypeFunction))
				throw new SemanticException(lineNumber, "'" + funcName + "' is not a method");
			funcType = (TypeFunction) member.t;
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

		return funcType.returnType;
	}

	@Override
	public Temp irMe()
	{
		// Handle PrintInt specially (library function)
		if (funcName.equals("PrintInt")) {
			// Get the first argument temp
			if (args != null) {
				Temp argTemp = args.head.irMe();
				Ir.getInstance().AddIrCommand(new IrCommandPrintInt(argTemp));
			}
			return null;  // PrintInt returns void
		}

		// Generate IR for all arguments first
		AstExpList currArg = args;
		while (currArg != null) {
			currArg.head.irMe();
			currArg = currArg.tail;
		}
		
		// For other functions, we would emit a call instruction
		// For now, return a fresh temp for non-void functions
		Temp result = TempFactory.getInstance().getFreshTemp();
		return result;
	}
}
