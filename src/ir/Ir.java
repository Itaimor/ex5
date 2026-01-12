/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;

/*******************/
/* PROJECT IMPORTS */
/*******************/

public class Ir
{
	private IrCommand head=null;
	private IrCommandList tail=null;

	/******************/
	/* Add Ir command */
	/******************/
	public void AddIrCommand(IrCommand cmd)
	{
		if ((head == null) && (tail == null))
		{
			this.head = cmd;
		}
		else if ((head != null) && (tail == null))
		{
			this.tail = new IrCommandList(cmd,null);
		}
		else
		{
			IrCommandList it = tail;
			while ((it != null) && (it.tail != null))
			{
				it = it.tail;
			}
			it.tail = new IrCommandList(cmd,null);
		}
	}

	/********************************/
	/* Get all commands as a List   */
	/********************************/
	public List<IrCommand> getAllCommands()
	{
		List<IrCommand> result = new ArrayList<>();
		if (head != null) result.add(head);
		IrCommandList curr = tail;
		while (curr != null)
		{
			result.add(curr.head);
			curr = curr.tail;
		}
		return result;
	}

	/********************************/
	/* Print all IR to writer       */
	/********************************/
	public void printIR(PrintWriter writer)
	{
		if (head != null) writer.println(head.toString());
		IrCommandList curr = tail;
		while (curr != null)
		{
			writer.println(curr.head.toString());
			curr = curr.tail;
		}
	}

	/********************************/
	/* Reset for testing            */
	/********************************/
	public void clear()
	{
		head = null;
		tail = null;
	}

	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static Ir instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected Ir() {}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static Ir getInstance()
	{
		if (instance == null)
		{
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new Ir();
		}
		return instance;
	}
}
