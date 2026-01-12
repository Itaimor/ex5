package ast;

import types.*;
import semantic.SemanticException;
import symboltable.*;
import temp.*;

public class AstDecClass extends AstDec {
	public String name;
	public String parentName;  // null if no extends
	public AstCFieldList dataMembers;

	public AstDecClass(String name, String parentName, AstCFieldList dataMembers, int line) {
		serialNumber = AstNodeSerialNumber.getFresh();
		this.lineNumber = line;  // Override the default staticLine
		this.name = name;
		this.parentName = parentName;
		this.dataMembers = dataMembers;
	}

	@Override
	public void printMe() {
		System.out.format("CLASS DEC = %s", name);
		if (parentName != null)
			System.out.format(" extends %s", parentName);
		System.out.println();
		
		if (dataMembers != null)
			dataMembers.printMe();

		AstGraphviz.getInstance().logNode(serialNumber,
			String.format("CLASS\n%s", name));
		if (dataMembers != null)
			AstGraphviz.getInstance().logEdge(serialNumber, dataMembers.serialNumber);
	}

	@Override
	public Type semantMe() throws SemanticException {
		// PDF 2.1: Class definitions may appear only in global scope
		if (!SymbolTable.getInstance().isGlobalScope())
			throw new SemanticException(lineNumber, "class can only be defined at global scope");

		// Check class is not already declared in current scope (PDF 2.7)
		if (SymbolTable.getInstance().findInCurrentScope(name) != null)
			throw new SemanticException(lineNumber, "class '" + name + "' already defined");

		// PDF 2.2: A class can extend only previously defined classes
		TypeClass parentType = null;
		TypeClassVarDecList membersList = new TypeClassVarDecList(null, null);
		
		if (parentName != null) {
			Type t = SymbolTable.getInstance().find(parentName);
			if (t == null || !t.isClass())
				throw new SemanticException(lineNumber, "parent class '" + parentName + "' not found");
			parentType = (TypeClass) t;
			// Inherit parent's members
			if (parentType.dataMembers != null)
				membersList = parentType.dataMembers.inherit();
		}

		// Create class type and enter BEFORE processing members (for self-reference)
		TypeClass classType = new TypeClass(parentType, name, membersList);
		SymbolTable.getInstance().enter(name, classType);

		// Begin class scope
		SymbolTable.getInstance().beginClassScope(classType);

		// Process each data member
		for (AstCFieldList cl = dataMembers; cl != null; cl = cl.tail) {
			if (cl.head == null) continue;
			
			Type memberType = cl.head.semantMe();
			if (memberType == null)
				throw new SemanticException(cl.head.lineNumber, "invalid class member");

			// Get member name
			String memberName;
			if (cl.head instanceof AstCFieldFunc)
				memberName = ((AstCFieldFunc) cl.head).funcDec.name;
			else if (cl.head instanceof AstCFieldVar)
				memberName = ((AstCFieldVar) cl.head).varDec.name;
			else
				throw new SemanticException(cl.head.lineNumber, "unknown class member type");

			// PDF 2.2: Check for redeclaration within same class (not inherited)
			TypeClassVarDec existing = membersList.find(memberName);
			if (existing != null && !existing.inherited)
				throw new SemanticException(cl.head.lineNumber, "'" + memberName + "' already declared in class");

			// PDF 2.2: Method overriding rules
			if (existing != null && existing.inherited) {
				boolean existingIsMethod = (existing.t instanceof TypeFunction);
				boolean newIsMethod = (memberType instanceof TypeFunction);
				
				// Field cannot shadow anything
				if (!newIsMethod)
					throw new SemanticException(cl.head.lineNumber, "field '" + memberName + "' cannot shadow inherited member");
				
				// Method can only override method with same signature
				if (newIsMethod && !existingIsMethod)
					throw new SemanticException(cl.head.lineNumber, "method '" + memberName + "' cannot shadow inherited field");
				
				if (newIsMethod && existingIsMethod) {
					// Check signature match for method override
					TypeFunction newFunc = (TypeFunction) memberType;
					TypeFunction oldFunc = (TypeFunction) existing.t;
					if (!newFunc.signatureMatches(oldFunc))
						throw new SemanticException(cl.head.lineNumber, "method '" + memberName + "' override signature mismatch");
				}
			}

			// Add new member
			TypeClassVarDec newMember = new TypeClassVarDec(memberType, memberName);
			membersList.insert(newMember);
		}

		// End class scope
		SymbolTable.getInstance().endClassScope();

		return null;
	}

	@Override
	public Temp irMe()
	{
		// Generate IR for methods in the class
		if (dataMembers != null) {
			dataMembers.irMe();
		}
		return null;
	}
}
