package ir;

import temp.*;

public class IrCommandLoadField extends IrCommand
{
	Temp dst;
	Temp base;
	int offset;

	public IrCommandLoadField(Temp dst, Temp base, int offset)
	{
		this.dst    = dst;
		this.base   = base;
		this.offset = offset;
	}

	public Temp getDst()    { return dst; }
	public Temp getBase()   { return base; }
	public int  getOffset() { return offset; }

	@Override
	public String toString()
	{
		return String.format("Temp_%d := Temp_%d[%d]",
			dst.getSerialNumber(), base.getSerialNumber(), offset);
	}
}
