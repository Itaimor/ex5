package ir;

import temp.*;

public class IrCommandLoadParam extends IrCommand
{
	Temp dst;
	int paramIndex;

	public IrCommandLoadParam(Temp dst, int paramIndex)
	{
		this.dst        = dst;
		this.paramIndex = paramIndex;
	}

	public Temp getDst()      { return dst; }
	public int getParamIndex() { return paramIndex; }

	@Override
	public String toString()
	{
		return String.format("Temp_%d := param[%d]", dst.getSerialNumber(), paramIndex);
	}
}
