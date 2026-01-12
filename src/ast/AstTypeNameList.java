package ast;

import types.*;
import semantic.SemanticException;

public class AstTypeNameList extends AstNode
{
	public AstTypeName head;
	public AstTypeNameList tail;
	
	public AstTypeNameList(AstTypeName head, AstTypeNameList tail)
	{
		serialNumber = AstNodeSerialNumber.getFresh();
		this.head = head;
		this.tail = tail;
	}

	public void printMe()
	{
		System.out.print("AST TYPE NAME LIST\n");
		if (head != null) head.printMe();
		if (tail != null) tail.printMe();

		AstGraphviz.getInstance().logNode(serialNumber, "TYPE-NAME\nLIST\n");
		if (head != null) AstGraphviz.getInstance().logEdge(serialNumber, head.serialNumber);
		if (tail != null) AstGraphviz.getInstance().logEdge(serialNumber, tail.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException
	{
		if (head != null) head.semantMe();
		if (tail != null) tail.semantMe();
		return null;
	}

	/** Build a TypeList from this list (for function params) */
	public TypeList toTypeList() throws SemanticException
	{
		if (tail == null) {
			return new TypeList(head.semantMe(), null);
		} else {
			return new TypeList(head.semantMe(), tail.toTypeList());
		}
	}
}
