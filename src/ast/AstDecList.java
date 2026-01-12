package ast;

import types.*;
import semantic.SemanticException;
import temp.*;

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

		// if (tail != null) System.out.print("====================== decList -> dec decList\n");
		// if (tail == null) System.out.print("====================== decList -> dec\n");
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

		// PASS 1: Generate IR for ALL global variable initializations
		AstDecList curr = this;
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
