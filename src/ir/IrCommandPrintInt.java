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

public class IrCommandPrintInt extends IrCommand
{
	Temp t;
	
	public IrCommandPrintInt(Temp t)
	{
		this.t = t;
	}
	
	public Temp getTemp() { return t; }
	
	@Override
	public String toString()
	{
		return String.format("PrintInt(Temp_%d)", t.getSerialNumber());
	}
}
