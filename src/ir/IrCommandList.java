/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/

public class IrCommandList
{
	public IrCommand head;
	public IrCommandList tail;

	IrCommandList(IrCommand head, IrCommandList tail)
	{
		this.head = head;
		this.tail = tail;
	}
}
