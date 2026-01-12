package types;

public class TypeArray extends Type
{
    public Type elementType;

    public TypeArray(Type elementType)
    {
        super("array");
        this.elementType = elementType;
    }

    public TypeArray(String name, Type elementType)
    {
        super(name);
        this.elementType = elementType;
    }

    /*==================================================================*/
    /*                    TYPE CHECKS                                   */
    /*==================================================================*/
    @Override
    public boolean isArray() { return true; }

}
