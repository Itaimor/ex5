package ir;

import temp.*;

public class IrCommandStoreArray extends IrCommand
{
	Temp base;
	Temp index;
	Temp src;

	public IrCommandStoreArray(Temp base, Temp index, Temp src)
	{
		this.base  = base;
		this.index = index;
		this.src   = src;
	}

	public Temp getBase()  { return base; }
	public Temp getIndex() { return index; }
	public Temp getSrc()   { return src; }

	@Override
	public String toString()
	{
		return String.format("Temp_%d[Temp_%d] := Temp_%d",
			base.getSerialNumber(), index.getSerialNumber(), src.getSerialNumber());
	}
}
