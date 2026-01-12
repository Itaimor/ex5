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

public class IrCommandLabel extends IrCommand
{
	String labelName;
	
	public IrCommandLabel(String labelName)
	{
		this.labelName = labelName;
	}
	
	public String getLabelName() { return labelName; }
	
	@Override
	public String toString()
	{
		return String.format("%s:", labelName);
	}
}
