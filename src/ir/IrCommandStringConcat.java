package ir;

import temp.*;

public class IrCommandStringConcat extends IrCommand
{
	Temp dst;
	Temp str1;
	Temp str2;

	public IrCommandStringConcat(Temp dst, Temp str1, Temp str2)
	{
		this.dst  = dst;
		this.str1 = str1;
		this.str2 = str2;
	}

	public Temp getDst()  { return dst; }
	public Temp getStr1() { return str1; }
	public Temp getStr2() { return str2; }

	@Override
	public String toString()
	{
		return String.format("Temp_%d := Temp_%d ++ Temp_%d",
			dst.getSerialNumber(), str1.getSerialNumber(), str2.getSerialNumber());
	}
}
