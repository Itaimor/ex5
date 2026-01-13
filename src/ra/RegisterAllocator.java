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
import temp.*;
import ir.*;
import dfa.*;

public class RegisterAllocator {
    private static final int NUM_REGISTERS = 10;
    private static final String[] PHYSICAL_REGISTERS = {
            "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$t8", "$t9"
    };

    private Map<Temp, String> allocation = new HashMap<>();
    private InterferenceGraph graph;
    private Stack<Temp> stack = new Stack<>();
    private Set<Temp> removed = new HashSet<>();

    public RegisterAllocator(List<IrCommand> commands, LivenessAnalyzer liveness) {
        this.graph = new InterferenceGraph(commands, liveness);
        allocate();
    }

    private void allocate() {
        // Step 1: Simplify
        Set<Temp> worklist = new HashSet<>(graph.getAllTemps());
        while (!worklist.isEmpty()) {
            Temp toRemove = null;
            for (Temp t : worklist) {
                if (getEffectiveDegree(t) < NUM_REGISTERS) {
                    toRemove = t;
                    break;
                }
            }

            if (toRemove == null) {
                System.out.println("Register Allocation Failed");
                System.exit(0);
            }

            stack.push(toRemove);
            removed.add(toRemove);
            worklist.remove(toRemove);
        }

        // Step 2: Select
        while (!stack.isEmpty()) {
            Temp t = stack.pop();
            removed.remove(t);

            Set<String> usedColors = new HashSet<>();
            for (Temp neighbor : graph.getNeighbors(t)) {
                if (allocation.containsKey(neighbor)) {
                    usedColors.add(allocation.get(neighbor));
                }
            }

            for (String reg : PHYSICAL_REGISTERS) {
                if (!usedColors.contains(reg)) {
                    allocation.put(t, reg);
                    break;
                }
            }
        }
    }

    private int getEffectiveDegree(Temp t) {
        int degree = 0;
        for (Temp neighbor : graph.getNeighbors(t)) {
            if (!removed.contains(neighbor)) {
                degree++;
            }
        }
        return degree;
    }

    public Map<Temp, String> getAllocation() {
        return allocation;
    }

    public String getRegister(Temp t) {
        return allocation.get(t);
    }
}
