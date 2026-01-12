package ast;

import types.*;
import symboltable.*;
import semantic.SemanticException;
import temp.*;
import ir.*;

public class AstDecFunc extends AstDec
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public String returnTypeName;
	public String name;
	public AstTypeNameList params;
	public AstStmtList body;
	
	public AstDecFunc(
		String returnTypeName,
		String name,
		AstTypeNameList params,
		AstStmtList body,
		int line)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.lineNumber = line;  // Override default line number
		this.returnTypeName = returnTypeName;
		this.name = name;
		this.params = params;
		this.body = body;
	}

	/************************************************************/
	/* The printing message for a function declaration AST node */
	/************************************************************/
	public void printMe()
	{
		/*************************************************/
		/* AST NODE TYPE = AST NODE FUNCTION DECLARATION */
		/*************************************************/
		System.out.format("FUNC(%s):%s\n",name,returnTypeName);

		/***************************************/
		/* RECURSIVELY PRINT params + body ... */
		/***************************************/
		if (params != null) params.printMe();
		if (body   != null) body.printMe();
		
		/***************************************/
		/* PRINT Node to AST GRAPHVIZ DOT file */
		/***************************************/
		AstGraphviz.getInstance().logNode(
                serialNumber,
			String.format("FUNC(%s)\n:%s\n",name,returnTypeName));
		
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (params != null) AstGraphviz.getInstance().logEdge(serialNumber,params.serialNumber);
		if (body   != null) AstGraphviz.getInstance().logEdge(serialNumber,body.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		Type t;
		Type returnType = null;
		TypeList type_list = null;

		/************************************/
		/* [0] Check function name is unique */
		/************************************/
		if (SymbolTable.getInstance().findInCurrentScope(name) != null)
			throw new SemanticException(lineNumber, "function '" + name + "' already declared");

		/************************/
		/* [1] Check return type */
		/************************/
		returnType = SymbolTable.getInstance().find(returnTypeName);
		if (returnType == null)
			throw new SemanticException(lineNumber, "return type '" + returnTypeName + "' does not exist");

		/*************************************************/
		/* [2] Check for duplicate parameters and void types */
		/*************************************************/
		java.util.HashSet<String> paramNames = new java.util.HashSet<>();
		for (AstTypeNameList it = params; it != null; it = it.tail)
		{
			// Check for duplicate parameter names
			if (paramNames.contains(it.head.name))
				throw new SemanticException(it.head.lineNumber, "duplicate parameter name '" + it.head.name + "'");
			paramNames.add(it.head.name);
			
			// Check parameter type exists and is not void
			Type paramType = SymbolTable.getInstance().find(it.head.type);
			if (paramType == null)
				throw new SemanticException(it.head.lineNumber, "parameter type '" + it.head.type + "' does not exist");
			if (paramType.isVoid())
				throw new SemanticException(it.head.lineNumber, "parameter cannot have void type");
		}

		/*************************************************/
		/* [3] Build params type list                    */
		/*************************************************/
		if (params != null) {
			type_list = params.toTypeList();
		} else {
			type_list = null;
		}

		TypeFunction funcType = new TypeFunction(returnType, name, type_list);
		SymbolTable.getInstance().enter(name, funcType);

		/*******************************************/
		/* [4] Begin Function Scope (tracks return) */
		/*******************************************/
		SymbolTable.getInstance().beginFuncScope(funcType);

		/*************************************/
		/* [5] Enter params into function scope */
		/*************************************/
		for (AstTypeNameList it = params; it != null; it = it.tail)
		{
			t = SymbolTable.getInstance().find(it.head.type);
			SymbolTable.getInstance().enter(it.head.name, t);
		}

		/*******************/
		/* [6] Semant Body */
		/*******************/
		if (body != null)
			body.semantMe();

		/*****************/
		/* [7] End Scope */
		/*****************/
		SymbolTable.getInstance().endFuncScope();

		return funcType;		
	}

	@Override
	public Temp irMe()
	{
		// Emit function entry label
		String funcLabel = IrCommand.getFreshLabel("func_" + name);
		Ir.getInstance().AddIrCommand(new IrCommandLabel(funcLabel));
		
		// Generate IR for function body
		if (body != null) {
			body.irMe();
		}
		
		// Implicit return for void functions (if no explicit return at end)
		// For now, we'll let the function end naturally
		
		return null;
	}
}
