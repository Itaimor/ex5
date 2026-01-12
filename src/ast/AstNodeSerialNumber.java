package ast;

public class AstNodeSerialNumber
{
	/*******************************************/
	/* The serial number is for debug purposes */
	/* In particular, it can help in creating  */
	/* a graphviz dot format of the AST ...    */
	/*******************************************/
	public int SerialNumber;
	
	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static AstNodeSerialNumber instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected AstNodeSerialNumber() {}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	private static AstNodeSerialNumber getInstance()
	{
		if (instance == null)
		{
			instance = new AstNodeSerialNumber();
			instance.SerialNumber = 0;
			
		}
		return instance;
	}

	/**********************************/
	/* GET A UNIQUE SERIAL NUMBER ... */
	/**********************************/
	public int get()
	{
		return SerialNumber++;
	}

	/**********************************/
	/* GET A UNIQUE SERIAL NUMBER ... */
	/**********************************/
	public static int getFresh()
	{
		return AstNodeSerialNumber.getInstance().get();
	}
}
