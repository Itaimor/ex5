package ir;

import temp.*;
import java.util.List;

public class IrCommandVirtualCall extends IrCommand
{
	Temp dst;
	Temp baseObj;
	int methodIndex;
	List<Temp> args;

	public IrCommandVirtualCall(Temp dst, Temp baseObj, int methodIndex, List<Temp> args)
	{
		this.dst         = dst;
		this.baseObj     = baseObj;
		this.methodIndex = methodIndex;
		this.args        = args;
	}

	public Temp getDst()          { return dst; }
	public Temp getBaseObj()      { return baseObj; }
	public int  getMethodIndex()  { return methodIndex; }
	public List<Temp> getArgs()   { return args; }

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (dst != null)
			sb.append(String.format("Temp_%d := ", dst.getSerialNumber()));
		sb.append(String.format("vcall Temp_%d[%d](",
			baseObj.getSerialNumber(), methodIndex));
		for (int i = 0; i < args.size(); i++)
		{
			if (i > 0) sb.append(", ");
			sb.append(String.format("Temp_%d", args.get(i).getSerialNumber()));
		}
		sb.append(")");
		return sb.toString();
	}
}
