package regalloc;

import temp.*;
import ir.*;
import dfa.LivenessAnalyzer;
import java.util.*;

public class InterferenceGraph
{
	private Map<Integer, Set<Integer>> adjList;
	private Set<Integer> nodes;

	public InterferenceGraph()
	{
		adjList = new HashMap<>();
		nodes = new HashSet<>();
	}

	public void addNode(int tempSerial)
	{
		nodes.add(tempSerial);
		adjList.computeIfAbsent(tempSerial, k -> new HashSet<>());
	}

	public void addEdge(int t1, int t2)
	{
		if (t1 == t2) return;
		adjList.computeIfAbsent(t1, k -> new HashSet<>()).add(t2);
		adjList.computeIfAbsent(t2, k -> new HashSet<>()).add(t1);
	}

	public Set<Integer> getNodes() { return nodes; }

	public Set<Integer> getNeighbors(int node)
	{
		return adjList.getOrDefault(node, Collections.emptySet());
	}

	public static InterferenceGraph build(LivenessAnalyzer liveness, List<IrCommand> commands)
	{
		InterferenceGraph graph = new InterferenceGraph();

		for (IrCommand cmd : commands)
		{
			Set<Temp> defs = liveness.getDef(cmd);
			Set<Temp> outs = liveness.getOut(cmd);

			if (defs != null)
				for (Temp d : defs)
					graph.addNode(d.getSerialNumber());

			if (outs != null)
				for (Temp o : outs)
					graph.addNode(o.getSerialNumber());

			if (defs != null && outs != null)
			{
				for (Temp d : defs)
					for (Temp o : outs)
						if (d.getSerialNumber() != o.getSerialNumber())
							graph.addEdge(d.getSerialNumber(), o.getSerialNumber());
			}
		}

		return graph;
	}

	public Map<Integer, Set<Integer>> getAdjListCopy()
	{
		Map<Integer, Set<Integer>> copy = new HashMap<>();
		for (int node : nodes)
			copy.put(node, new HashSet<>(adjList.getOrDefault(node, Collections.emptySet())));
		return copy;
	}
}
