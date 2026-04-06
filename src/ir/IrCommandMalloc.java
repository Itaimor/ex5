package ir;

import temp.*;

public class IrCommandMalloc extends IrCommand
{
	Temp dst;
	Temp size;

	public IrCommandMalloc(Temp dst, Temp size)
	{
		this.dst  = dst;
		this.size = size;
	}

	public Temp getDst()  { return dst; }
	public Temp getSize() { return size; }

	@Override
	public String toString()
	{
		return String.format("Temp_%d := malloc(Temp_%d)",
			dst.getSerialNumber(), size.getSerialNumber());
	}
}
