package ir;

import temp.*;

public class IrCommandLoadArray extends IrCommand
{
	Temp dst;
	Temp base;
	Temp index;

	public IrCommandLoadArray(Temp dst, Temp base, Temp index)
	{
		this.dst   = dst;
		this.base  = base;
		this.index = index;
	}

	public Temp getDst()   { return dst; }
	public Temp getBase()  { return base; }
	public Temp getIndex() { return index; }

	@Override
	public String toString()
	{
		return String.format("Temp_%d := Temp_%d[Temp_%d]",
			dst.getSerialNumber(), base.getSerialNumber(), index.getSerialNumber());
	}
}
