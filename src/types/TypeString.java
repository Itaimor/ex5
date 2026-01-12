package types;

public class TypeString extends Type
{
	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static TypeString instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected TypeString() { super("string"); }

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static TypeString getInstance()
	{
		if (instance == null)
		{
			instance = new TypeString();
		}
		return instance;
	}

	/*==================================================================*/
    /*                    TYPE CHECKS                                   */
    /*==================================================================*/
	@Override
	public boolean isString() { return true; }
	
}
