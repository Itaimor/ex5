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

public class IrCommandJumpIfEqToZero extends IrCommand
{
	Temp t;
	String labelName;
	
	public IrCommandJumpIfEqToZero(Temp t, String labelName)
	{
		this.t          = t;
		this.labelName = labelName;
	}
	
	public Temp getTemp() { return t; }
	public String getLabelName() { return labelName; }
	
	@Override
	public String toString()
	{
		return String.format("if Temp_%d == 0 goto %s", t.getSerialNumber(), labelName);
	}
}
