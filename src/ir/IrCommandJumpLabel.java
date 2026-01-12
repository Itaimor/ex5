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

public class IrCommandJumpLabel extends IrCommand
{
	String labelName;
	
	public IrCommandJumpLabel(String labelName)
	{
		this.labelName = labelName;
	}
	
	public String getLabelName() { return labelName; }
	
	@Override
	public String toString()
	{
		return String.format("goto %s", labelName);
	}
}
