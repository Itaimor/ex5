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
import temp.*;

public class IRcommandConstInt extends IrCommand
{
	Temp t;
	int value;
	
	public IRcommandConstInt(Temp t, int value)
	{
		this.t = t;
		this.value = value;
	}
	
	public Temp getDst() { return t; }
	public int getValue() { return value; }
	
	@Override
	public String toString()
	{
		return String.format("Temp_%d := %d", t.getSerialNumber(), value);
	}
}
