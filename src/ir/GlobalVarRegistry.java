package ir;

import java.util.*;

public class GlobalVarRegistry
{
	private static GlobalVarRegistry instance = null;
	private Set<String> globalVarNames = new LinkedHashSet<>();

	protected GlobalVarRegistry() {}

	public static GlobalVarRegistry getInstance()
	{
		if (instance == null) instance = new GlobalVarRegistry();
		return instance;
	}

	public void register(String uniqueName)
	{
		globalVarNames.add(uniqueName);
	}

	public Set<String> getAll()
	{
		return globalVarNames;
	}

	public static void reset() { instance = null; }
}
