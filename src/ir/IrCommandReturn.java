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

public class IrCommandReturn extends IrCommand
{
	public Temp returnValue;
	
	public IrCommandReturn(Temp returnValue)
	{
		this.returnValue = returnValue;
	}
	
	public Temp getReturnValue() { return returnValue; }
	
	@Override
	public String toString()
	{
		if (returnValue != null) {
			return String.format("return Temp_%d", returnValue.getSerialNumber());
		}
		return "return";
	}
}

