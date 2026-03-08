package ir;

import temp.*;

public class IrCommandLoadAddress extends IrCommand
{
	Temp dst;
	String label;

	public IrCommandLoadAddress(Temp dst, String label)
	{
		this.dst   = dst;
		this.label = label;
	}

	public Temp getDst()     { return dst; }
	public String getLabel() { return label; }

	@Override
	public String toString()
	{
		return String.format("Temp_%d := &%s", dst.getSerialNumber(), label);
	}
}
