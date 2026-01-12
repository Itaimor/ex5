package types;

public class TypeForScopeBoundaries extends Type
{
	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static TypeForScopeBoundaries instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	private TypeForScopeBoundaries()
	{
		super("SCOPE-BOUNDARY");
	}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static TypeForScopeBoundaries getInstance()
	{
		if (instance == null)
		{
			instance = new TypeForScopeBoundaries();
		}
		return instance;
	}
}
