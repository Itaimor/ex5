package ir;

import temp.*;
import java.util.List;

public class IrCommandCall extends IrCommand
{
	Temp dst;
	String funcLabel;
	List<Temp> args;

	public IrCommandCall(Temp dst, String funcLabel, List<Temp> args)
	{
		this.dst       = dst;
		this.funcLabel = funcLabel;
		this.args      = args;
	}

	public Temp getDst()          { return dst; }
	public String getFuncLabel()  { return funcLabel; }
	public List<Temp> getArgs()   { return args; }

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		if (dst != null)
			sb.append(String.format("Temp_%d := ", dst.getSerialNumber()));
		sb.append(String.format("call %s(", funcLabel));
		for (int i = 0; i < args.size(); i++)
		{
			if (i > 0) sb.append(", ");
			sb.append(String.format("Temp_%d", args.get(i).getSerialNumber()));
		}
		sb.append(")");
		return sb.toString();
	}
}
