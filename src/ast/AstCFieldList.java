package ast;

import types.Type;
import semantic.SemanticException;
import temp.*;

public class AstCFieldList extends AstNode {
	public AstCField head;
	public AstCFieldList tail;

	public AstCFieldList(AstCField head, AstCFieldList tail) {
		serialNumber = AstNodeSerialNumber.getFresh();
		this.head = head;
		this.tail = tail;
	}

	public void printMe() {
		System.out.println("CLASS FIELD LIST");
		if (head != null)
			head.printMe();
		if (tail != null)
			tail.printMe();

		AstGraphviz.getInstance().logNode(serialNumber, "CFIELD\nLIST");
		if (head != null)
			AstGraphviz.getInstance().logEdge(serialNumber, head.serialNumber);
		if (tail != null)
			AstGraphviz.getInstance().logEdge(serialNumber, tail.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException {
		if (head != null)
			head.semantMe();
		if (tail != null)
			tail.semantMe();
		return null;
	}

	@Override
	public Temp irMe()
	{
		// Generate IR for head field (methods only)
		if (head != null) {
			head.irMe();
		}
		
		// Recursively generate for tail
		if (tail != null) {
			tail.irMe();
		}
		
		return null;
	}
}
