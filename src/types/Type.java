package types;

public abstract class Type
{
	/******************************/
	/*  Every type has a name ... */
	/******************************/
	public String name;

    /*  constructor */
    protected Type(String name) 
    {
        this.name = name;
    }

	/*==================================================================*/
    /*                    TYPE CHECKS                                   */
    /*==================================================================*/
	public boolean isInt() { return false; }
    public boolean isString() { return false; }
    public boolean isClass() { return false; }
    public boolean isArray() { return false; }
    public boolean isFunction() { return false; }
    public boolean isVoid() { return false; }
    public boolean isNil() { return false; }



    /*  toString */
    @Override
    public String toString() 
    {
        return name;
    }
	
}
