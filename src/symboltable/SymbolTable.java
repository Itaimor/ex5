package symboltable;

import java.io.PrintWriter;
import types.*;

public class SymbolTable
{
	private int hashArraySize = 13;
	private SymbolTableEntry[] table = new SymbolTableEntry[hashArraySize];
	private SymbolTableEntry top;
	private int topIndex = 0;
	
	private int curScopeDepth = 0;
	private TypeClass curClass = null;
	private TypeFunction currFunc = null;
	
	private int hash(String s)
	{
		return Math.abs(s.hashCode()) % hashArraySize;
	}

	public void enter(String name, Type t)
	{
		int hashValue = hash(name);
		SymbolTableEntry next = table[hashValue];
		SymbolTableEntry e = new SymbolTableEntry(name, t, hashValue, next, curScopeDepth, top, topIndex++);
		top = e;
		table[hashValue] = e;
		// printMe();
	}

	public Type find(String name)
	{
		// Step 1: Search local scopes (not global)
		for (SymbolTableEntry e = table[hash(name)]; e != null; e = e.next)
			if (name.equals(e.name) && e.scopeDepth > 0)
				return e.type;
		
		// Step 2: If inside a class, search class hierarchy
		if (curClass != null) {
			TypeClassVarDec member = curClass.findMemberInHierarchy(name);
			if (member != null)
				return member.t;
		}
		
		// Step 3: Search global scope
		for (SymbolTableEntry e = table[hash(name)]; e != null; e = e.next)
			if (name.equals(e.name) && e.scopeDepth == 0)
				return e.type;
		
		return null;
	}

	public SymbolTableEntry findEntry(String name)
	{
		// Step 1: Search local scopes (not global)
		for (SymbolTableEntry e = table[hash(name)]; e != null; e = e.next)
			if (name.equals(e.name) && e.scopeDepth > 0)
				return e;
		
		// Step 2: Search global scope
		for (SymbolTableEntry e = table[hash(name)]; e != null; e = e.next)
			if (name.equals(e.name) && e.scopeDepth == 0)
				return e;
		
		return null;
	}

	public void beginScope()
	{
		enter("SCOPE-BOUNDARY", TypeForScopeBoundaries.getInstance());
		curScopeDepth++;
		// printMe();
	}

	public void endScope()
	{
		while (!top.name.equals("SCOPE-BOUNDARY")) {
			table[top.index] = top.next;
			topIndex--;
			top = top.prevtop;
		}
		table[top.index] = top.next;
		topIndex--;
		top = top.prevtop;
		curScopeDepth--;
		// printMe();
	}

	public boolean isGlobalScope() { return curScopeDepth == 0; }

	public Type findInCurrentScope(String name)
	{
		for (SymbolTableEntry e = top; e != null; e = e.prevtop) {
			if (e.name.equals("SCOPE-BOUNDARY")) return null;
			if (e.name.equals(name)) return e.type;
		}
		return null;
	}

	public void beginClassScope(TypeClass classType)
	{
		beginScope();
		this.curClass = classType;
	}

	public void endClassScope()
	{
		endScope();
		this.curClass = null;
	}

	public void beginFuncScope(TypeFunction func)
	{
		beginScope();
		this.currFunc = func;
	}

	public void endFuncScope()
	{
		endScope();
		this.currFunc = null;
	}

	public Type getReturnType() { return currFunc != null ? currFunc.returnType : null; }
	
	public boolean insideFunction() { return currFunc != null; }

	public static int n=0;
	
	public void printMe()
	{
		String dirname="./output/";
		String filename=String.format("SYMBOL_TABLE_%d_IN_GRAPHVIZ_DOT_FORMAT.txt",n++);
		try {
			PrintWriter fileWriter = new PrintWriter(dirname+filename);
			if (fileWriter != null) {
				fileWriter.print("digraph structs {\nrankdir = LR\nnode [shape=record];\n");
				fileWriter.print("hashTable [label=\"");
				for (int i=0;i<hashArraySize-1;i++) fileWriter.format("<f%d>\n%d\n|",i,i);
				fileWriter.format("<f%d>\n%d\n\"];\n",hashArraySize-1,hashArraySize-1);
				for (int i=0;i<hashArraySize;i++) {
					if (table[i] != null)
						fileWriter.format("hashTable:f%d -> node_%d_0:f0;\n",i,i);
					int j=0;
					for (SymbolTableEntry it = table[i]; it!=null; it=it.next) {
						fileWriter.format("node_%d_%d [label=\"<f0>%s|<f1>%s|<f2>prevtop=%d|<f3>next\"];\n",
							i,j,it.name,it.type.name,it.prevtopIndex);
						if (it.next != null) {
							fileWriter.format("node_%d_%d -> node_%d_%d [style=invis,weight=10];\n",i,j,i,j+1);
							fileWriter.format("node_%d_%d:f3 -> node_%d_%d:f0;\n",i,j,i,j+1);
						}
						j++;
					}
				}
				fileWriter.print("}\n");
				fileWriter.close();
			}
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	private static SymbolTable instance = null;
	protected SymbolTable() {}

	public static SymbolTable getInstance()
	{
		if (instance == null) {
			instance = new SymbolTable();
			instance.enter("int", TypeInt.getInstance());
			instance.enter("string", TypeString.getInstance());
			instance.enter("void", TypeVoid.getInstance());
			instance.enter("PrintInt", new TypeFunction(TypeVoid.getInstance(), "PrintInt",
				new TypeList(TypeInt.getInstance(), null)));
			instance.enter("PrintString", new TypeFunction(TypeVoid.getInstance(), "PrintString",
				new TypeList(TypeString.getInstance(), null)));
		}
		return instance;
	}
}
