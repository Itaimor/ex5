package ir;

import types.*;
import java.util.*;

public class ClassLayout
{
	private static ClassLayout instance = null;

	private Map<String, List<TypeClassVarDec>> classFields  = new LinkedHashMap<>();
	private Map<String, List<MethodEntry>>     classMethods = new LinkedHashMap<>();
	private Map<String, Integer>               objectSize   = new HashMap<>();
	private Map<String, String>                vtableLabels = new HashMap<>();
	private Map<String, List<FieldInit>>       classFieldInits = new LinkedHashMap<>();

	public static final int INIT_INT = 0;
	public static final int INIT_STRING = 1;
	public static final int INIT_NIL = 2;

	public static class FieldInit
	{
		public int offset;
		public int kind;
		public int intValue;
		public String stringValue;

		public FieldInit(int offset, int kind, int intValue, String stringValue)
		{
			this.offset      = offset;
			this.kind        = kind;
			this.intValue    = intValue;
			this.stringValue = stringValue;
		}
	}

	public static class MethodEntry
	{
		public String methodName;
		public String funcLabel;

		public MethodEntry(String methodName, String funcLabel)
		{
			this.methodName = methodName;
			this.funcLabel  = funcLabel;
		}
	}

	protected ClassLayout() {}

	public static ClassLayout getInstance()
	{
		if (instance == null) instance = new ClassLayout();
		return instance;
	}

	public void addClass(TypeClass tc)
	{
		if (classFields.containsKey(tc.name)) return;

		List<TypeClassVarDec> fields  = new ArrayList<>();
		List<MethodEntry>     methods = new ArrayList<>();

		if (tc.father != null)
		{
			addClass(tc.father);
			fields.addAll(classFields.get(tc.father.name));
			for (MethodEntry me : classMethods.get(tc.father.name))
				methods.add(new MethodEntry(me.methodName, me.funcLabel));
			List<FieldInit> parentInits = classFieldInits.get(tc.father.name);
			if (parentInits != null)
				classFieldInits.put(tc.name, new ArrayList<>(parentInits));
		}

		for (TypeClassVarDecList it = tc.dataMembers; it != null; it = it.tail)
		{
			TypeClassVarDec member = it.head;
			if (member == null || member.inherited) continue;

			if (member.t instanceof TypeFunction)
			{
				int existingIdx = -1;
				for (int i = 0; i < methods.size(); i++)
					if (methods.get(i).methodName.equals(member.name))
					{ existingIdx = i; break; }

				String label = tc.name + "_" + member.name;
				if (existingIdx >= 0)
					methods.set(existingIdx, new MethodEntry(member.name, label));
				else
					methods.add(new MethodEntry(member.name, label));
			}
			else
			{
				fields.add(member);
			}
		}

		classFields.put(tc.name, fields);
		classMethods.put(tc.name, methods);
		objectSize.put(tc.name, (fields.size() + 1) * 4);
		vtableLabels.put(tc.name, "vtable_" + tc.name);
	}

	public int getFieldOffset(String className, String fieldName)
	{
		List<TypeClassVarDec> fields = classFields.get(className);
		if (fields == null) return -1;
		for (int i = 0; i < fields.size(); i++)
			if (fields.get(i).name.equals(fieldName))
				return (i + 1) * 4;
		return -1;
	}

	public int getMethodIndex(String className, String methodName)
	{
		List<MethodEntry> methods = classMethods.get(className);
		if (methods == null) return -1;
		for (int i = 0; i < methods.size(); i++)
			if (methods.get(i).methodName.equals(methodName))
				return i;
		return -1;
	}

	public int getObjectSize(String className)
	{
		return objectSize.getOrDefault(className, 4);
	}

	public String getVtableLabel(String className)
	{
		return vtableLabels.getOrDefault(className, "vtable_" + className);
	}

	public String getMethodLabel(String className, String methodName)
	{
		List<MethodEntry> methods = classMethods.get(className);
		if (methods == null) return null;
		for (MethodEntry m : methods)
			if (m.methodName.equals(methodName))
				return m.funcLabel;
		return null;
	}

	public List<String> getVtableEntries(String className)
	{
		List<MethodEntry> methods = classMethods.get(className);
		List<String> labels = new ArrayList<>();
		if (methods != null)
			for (MethodEntry m : methods)
				labels.add(m.funcLabel);
		return labels;
	}

	public List<TypeClassVarDec> getFields(String className)
	{
		return classFields.getOrDefault(className, new ArrayList<>());
	}

	public void addFieldInit(String className, int offset, int kind, int intValue, String stringValue)
	{
		classFieldInits.computeIfAbsent(className, k -> new ArrayList<>())
			.add(new FieldInit(offset, kind, intValue, stringValue));
	}

	public List<FieldInit> getFieldInits(String className)
	{
		return classFieldInits.getOrDefault(className, new ArrayList<>());
	}

	public Set<String> getAllClassNames()
	{
		return classFields.keySet();
	}

	public static void reset() { instance = null; }
}
