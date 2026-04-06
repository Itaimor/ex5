package ir;

import temp.*;

public class IrCommandPrintString extends IrCommand
{
	Temp strAddr;

	public IrCommandPrintString(Temp strAddr)
	{
		this.strAddr = strAddr;
	}

	public Temp getStrAddr() { return strAddr; }

	@Override
	public String toString()
	{
		return String.format("PrintString(Temp_%d)", strAddr.getSerialNumber());
	}
}
