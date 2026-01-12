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

public class IrCommandLoad extends IrCommand
{
	Temp dst;
	String varName;
	
	public IrCommandLoad(Temp dst, String varName)
	{
		this.dst      = dst;
		this.varName = varName;
	}
	
	public Temp getDst() { return dst; }
	public String getVarName() { return varName; }
	
	@Override
	public String toString()
	{
		return String.format("Temp_%d := %s", dst.getSerialNumber(), varName);
	}
}
