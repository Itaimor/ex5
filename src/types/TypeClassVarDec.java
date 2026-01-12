package types;

public class TypeClassVarDec
{
	public Type t;
	public String name;
	public boolean inherited;
	
    public TypeClassVarDec(Type t, String name)
    {
        this.t = t;
        this.name = name;
        this.inherited = false;
    }

    public TypeClassVarDec(Type t, String name, boolean inherited)
    {
        this.t = t;
        this.name = name;
        this.inherited = inherited;
    }
}
