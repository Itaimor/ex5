package types;

public class TypeVoid extends Type
{
	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static TypeVoid instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected TypeVoid() { super("void"); }	
	@Override
	public boolean isVoid() {return true;}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static TypeVoid getInstance()
	{
		if (instance == null)
		{
			instance = new TypeVoid();
		}
		return instance;
	}


}
