package ast;

import types.*;
import semantic.SemanticException;
import symboltable.SymbolTable;
import temp.*;
import ir.ClassLayout;

public class AstDecList extends AstNode
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	public AstDec head;
	public AstDecList tail;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstDecList(AstDec head, AstDecList tail)
	{
		/******************************/
		/* SET A UNIQUE SERIAL NUMBER */
		/******************************/
		serialNumber = AstNodeSerialNumber.getFresh();
		this.head = head;
		this.tail = tail;
	}

	/********************************************************/
	/* The printing message for a declaration list AST node */
	/********************************************************/
	public void printMe()
	{
		/********************************/
		/* AST NODE TYPE = AST DEC LIST */
		/********************************/
		System.out.print("AST NODE DEC LIST\n");

		/*************************************/
		/* RECURSIVELY PRINT HEAD + TAIL ... */
		/*************************************/
		if (head != null) head.printMe();
		if (tail != null) tail.printMe();

		/**********************************/
		/* PRINT to AST GRAPHVIZ DOT file */
		/**********************************/
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"DEC\nLIST\n");
				
		/****************************************/
		/* PRINT Edges to AST GRAPHVIZ DOT file */
		/****************************************/
		if (head != null) AstGraphviz.getInstance().logEdge(serialNumber,head.serialNumber);
		if (tail != null) AstGraphviz.getInstance().logEdge(serialNumber,tail.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		/*************************************/
		/* RECURSIVELY SEMANT HEAD + TAIL ... */
		/*************************************/
		if (head != null) head.semantMe();
		if (tail != null) tail.semantMe();

		return null;
	}

	@Override
	public Temp irMe()
	{
		// PASS 0: Build class layouts + set ownerClassName on methods
		AstDecList curr = this;
		while (curr != null) {
			if (curr.head instanceof AstDecClass) {
				AstDecClass decClass = (AstDecClass) curr.head;
				Type t = SymbolTable.getInstance().find(decClass.name);
				if (t != null && t instanceof TypeClass) {
					ClassLayout.getInstance().addClass((TypeClass) t);
					for (AstCFieldList cl = decClass.dataMembers; cl != null; cl = cl.tail) {
						if (cl.head instanceof AstCFieldVar) {
							AstDecVar varDec = ((AstCFieldVar) cl.head).varDec;
							if (varDec.initialValue != null) {
								int offset = ClassLayout.getInstance().getFieldOffset(decClass.name, varDec.name);
								if (offset > 0) {
									if (varDec.initialValue instanceof AstExpInt)
										ClassLayout.getInstance().addFieldInit(decClass.name, offset,
											ClassLayout.INIT_INT, ((AstExpInt) varDec.initialValue).value, null);
									else if (varDec.initialValue instanceof AstExpString)
										ClassLayout.getInstance().addFieldInit(decClass.name, offset,
											ClassLayout.INIT_STRING, 0, ((AstExpString) varDec.initialValue).value);
									else if (varDec.initialValue instanceof AstExpNil)
										ClassLayout.getInstance().addFieldInit(decClass.name, offset,
											ClassLayout.INIT_NIL, 0, null);
								}
							}
						}
						if (cl.head instanceof AstCFieldFunc)
							((AstCFieldFunc) cl.head).funcDec.ownerClassName = decClass.name;
					}
				}
			}
			curr = curr.tail;
		}

		// PASS 1: Generate IR for ALL global variable initializations
		curr = this;
		while (curr != null) {
			if (curr.head instanceof AstDecVar) {
				curr.head.irMe();
			}
			curr = curr.tail;
		}
		
		// PASS 2: Generate IR for main function
		curr = this;
		while (curr != null) {
			if (curr.head instanceof AstDecFunc) {
				AstDecFunc func = (AstDecFunc) curr.head;
				if (func.name.equals("main")) {
					func.irMe();
				}
			}
			curr = curr.tail;
		}
		
		// PASS 3: Generate IR for other declarations (classes, other functions, typedefs)
		curr = this;
		while (curr != null) {
			if (!(curr.head instanceof AstDecVar)) {
				if (curr.head instanceof AstDecFunc) {
					AstDecFunc func = (AstDecFunc) curr.head;
					if (!func.name.equals("main")) {
						curr.head.irMe();
					}
				} else {
					// Classes, typedefs, etc.
					curr.head.irMe();
				}
			}
			curr = curr.tail;
		}
		
		return null;
	}
}
