package ir;

import java.util.*;

public class StringRegistry
{
	private static StringRegistry instance = null;
	private Map<String, String> valueToLabel = new LinkedHashMap<>();
	private int counter = 0;

	protected StringRegistry() {}

	public static StringRegistry getInstance()
	{
		if (instance == null) instance = new StringRegistry();
		return instance;
	}

	public String getOrRegister(String value)
	{
		return valueToLabel.computeIfAbsent(value, v -> "str_const_" + counter++);
	}

	public Map<String, String> getAll()
	{
		return valueToLabel;
	}

	public static void reset() { instance = null; }
}
