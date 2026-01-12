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

public class IrCommandAllocate extends IrCommand
{
	String varName;
	
	public IrCommandAllocate(String varName)
	{
		this.varName = varName;
	}
	
	public String getVarName() { return varName; }
	
	@Override
	public String toString()
	{
		return String.format("%s := Allocate()", varName);
	}
}
