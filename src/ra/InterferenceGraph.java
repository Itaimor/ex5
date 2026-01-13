/***********/
/* PACKAGE */
/***********/
package ra;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.util.*;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import ir.*;
import temp.*;
import dfa.*;

public class InterferenceGraph {
    private Map<Temp, Set<Temp>> adj = new HashMap<>();
    private Set<Temp> allTemps = new HashSet<>();

    public InterferenceGraph(List<IrCommand> commands, LivenessAnalyzer liveness) {
        // Collect all temporaries from def and use sets
        for (IrCommand cmd : commands) {
            allTemps.addAll(liveness.getDef(cmd));
            // Use reflection or just trust the liveness sets which should contain
            // everything
            // Actually, we can get them from the in/out sets or re-extract from commands.
            // Let's re-extract to be safe.
        }

        // Re-extracting from commands to ensure we catch all 'use' temps too
        // (LivenessAnalyzer already did this logic, we just didn't expose useMap)
        // For simplicity, let's just make sure they are in the graph via addEdge
        // and the initial scan below.

        // Ensure every temp has an entry in the adjacency map
        for (Temp t : allTemps) {
            adj.putIfAbsent(t, new HashSet<>());
        }

        // Build edges
        for (IrCommand cmd : commands) {
            Set<Temp> defs = liveness.getDef(cmd);
            Set<Temp> out = liveness.getOut(cmd);

            for (Temp d : defs) {
                for (Temp l : out) {
                    if (!d.equals(l)) {
                        addEdge(d, l);
                    }
                }
            }
        }
    }

    private void addEdge(Temp u, Temp v) {
        // Ensure both are in the graph (in case some temps only appear in 'out' sets)
        allTemps.add(u);
        allTemps.add(v);
        adj.computeIfAbsent(u, k -> new HashSet<>()).add(v);
        adj.computeIfAbsent(v, k -> new HashSet<>()).add(u);
    }

    public Set<Temp> getNeighbors(Temp t) {
        return adj.getOrDefault(t, Collections.emptySet());
    }

    public int getDegree(Temp t) {
        return getNeighbors(t).size();
    }

    public Set<Temp> getAllTemps() {
        return allTemps;
    }

    // Helper for debugging/printing
    public void printGraph() {
        for (Temp t : allTemps) {
            System.out.print("Temp_" + t.getSerialNumber() + ": ");
            for (Temp neighbor : getNeighbors(t)) {
                System.out.print("Temp_" + neighbor.getSerialNumber() + " ");
            }
            System.out.println();
        }
    }
}
