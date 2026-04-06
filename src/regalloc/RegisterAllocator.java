package regalloc;

import java.util.*;

public class RegisterAllocator
{
	private static final String[] REGISTERS = {
		"$t0", "$t1", "$t2", "$t3", "$t4",
		"$t5", "$t6", "$t7", "$t8", "$t9"
	};
	private static final int K = REGISTERS.length;

	public static Map<Integer, String> allocate(InterferenceGraph graph)
	{
		if (graph.getNodes().isEmpty()) return new HashMap<>();

		Map<Integer, Set<Integer>> adj = graph.getAdjListCopy();
		Set<Integer> remaining = new HashSet<>(graph.getNodes());
		Deque<Integer> stack = new ArrayDeque<>();

		while (!remaining.isEmpty())
		{
			int toRemove = -1;
			for (int node : remaining)
			{
				int degree = 0;
				for (int neighbor : adj.getOrDefault(node, Collections.emptySet()))
					if (remaining.contains(neighbor)) degree++;
				if (degree < K)
				{
					toRemove = node;
					break;
				}
			}

			if (toRemove == -1) return null;

			stack.push(toRemove);
			remaining.remove(toRemove);
		}

		Map<Integer, String> coloring = new HashMap<>();
		while (!stack.isEmpty())
		{
			int node = stack.pop();
			Set<String> usedColors = new HashSet<>();
			for (int neighbor : adj.getOrDefault(node, Collections.emptySet()))
				if (coloring.containsKey(neighbor))
					usedColors.add(coloring.get(neighbor));

			String assigned = null;
			for (String reg : REGISTERS)
			{
				if (!usedColors.contains(reg))
				{
					assigned = reg;
					break;
				}
			}

			if (assigned == null) return null;
			coloring.put(node, assigned);
		}

		return coloring;
	}
}
