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

public class IrCommandStore extends IrCommand
{
	String varName;
	Temp src;
	
	public IrCommandStore(String varName, Temp src)
	{
		this.src      = src;
		this.varName = varName;
	}
	
	public Temp getSrc() { return src; }
	public String getVarName() { return varName; }
	
	@Override
	public String toString()
	{
		return String.format("%s := Temp_%d", varName, src.getSerialNumber());
	}
}
