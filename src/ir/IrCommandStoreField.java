package ir;

import temp.*;

public class IrCommandStoreField extends IrCommand
{
	Temp base;
	int offset;
	Temp src;

	public IrCommandStoreField(Temp base, int offset, Temp src)
	{
		this.base   = base;
		this.offset = offset;
		this.src    = src;
	}

	public Temp getBase()   { return base; }
	public int  getOffset() { return offset; }
	public Temp getSrc()    { return src; }

	@Override
	public String toString()
	{
		return String.format("Temp_%d[%d] := Temp_%d",
			base.getSerialNumber(), offset, src.getSerialNumber());
	}
}
