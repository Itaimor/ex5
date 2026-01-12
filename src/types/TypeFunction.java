package types;

public class TypeFunction extends Type
{
    public Type returnType;
    public TypeList params;
    
    public TypeFunction(Type returnType, String name, TypeList params)
    {
        super(name);
        this.returnType = returnType;
        this.params = params;
    }

    /*==================================================================*/
    /*                    TYPE CHECK                                    */
    /*==================================================================*/
    @Override
    public boolean isFunction() { return true; }


    /** Check if signatures match (for method override) */
    public boolean signatureMatches(TypeFunction other)
    {
        if (this.returnType != other.returnType)
        {
            return false;
        }
        if (this.paramCount() != other.paramCount())
        {
            return false;
        }
        TypeList p1 = this.params;
        TypeList p2 = other.params;
        while (p1 != null && p2 != null)
        {
            if (p1.head != p2.head)
            {
                return false;
            }
            p1 = p1.tail;
            p2 = p2.tail;
        }
        return true;
    }

    /** Get number of parameters */
    public int paramCount()
    {
        return params != null ? params.size() : 0;
    }
}
