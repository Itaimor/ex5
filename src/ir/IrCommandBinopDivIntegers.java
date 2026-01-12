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

public class IrCommandBinopDivIntegers extends IrCommand
{
	public Temp t1;
	public Temp t2;
	public Temp dst;
	
	public IrCommandBinopDivIntegers(Temp dst, Temp t1, Temp t2)
	{
		this.dst = dst;
		this.t1 = t1;
		this.t2 = t2;
	}
	
	public Temp getDst() { return dst; }
	public Temp getT1() { return t1; }
	public Temp getT2() { return t2; }
	
	@Override
	public String toString()
	{
		return String.format("Temp_%d := Temp_%d / Temp_%d", 
			dst.getSerialNumber(), t1.getSerialNumber(), t2.getSerialNumber());
	}
}

