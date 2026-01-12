package dfa;

import cfg.*;
import ir.*;
import java.util.*;

/**
 * UninitializedAnalyzer identifies variables that may be used before they are initialized.
 * It uses Data Flow Analysis (DfaEngine) to track the initialization state of variables
 * across all possible execution paths in a Control Flow Graph.
 */
public class UninitializedAnalyzer {
    private ControlFlowGraph cfg;
    // Set to store names of variables found to be potentially uninitialized
    private Set<String> uninitializedVariables;

    /**
     * Constructs a new analyzer for the given Control Flow Graph.
     * @param cfg The Control Flow Graph to analyze.
     */
    public UninitializedAnalyzer(ControlFlowGraph cfg) {
        this.cfg = cfg;
        this.uninitializedVariables = new TreeSet<>();
    }

    /**
     * Performs the uninitialized variable analysis.
     * @return A set of variable names that may be used without being initialized.
     */
    public Set<String> analyze() {
        // Step 1: Run the Data Flow Analysis engine to compute IN/OUT sets for each block.
        // The engine performs a "Must" analysis for definite assignment.
        DfaEngine engine = new DfaEngine(cfg);
        engine.run();

        // Step 2: Iterate through each basic block and its instructions to find violations.
        for (BasicBlock block : cfg.getBlocks()) {
            // Start with the set of variables guaranteed to be initialized at the block's entry.
            Set<String> currentInitialized = new HashSet<>(engine.getInSet(block));
            
            for (IrCommand cmd : block.getInstructions()) {
                // Check all variables used (read) by this instruction.
                Set<String> uses = DfaEngine.getUsedVars(cmd);
                
                for (String use : uses) {
                    // If a variable is used but not in the initialized set, it's a potential error.
                    if (!currentInitialized.contains(use)) {
                        String baseName = getBaseName(use);
                        if (baseName != null) {
                            uninitializedVariables.add(baseName);
                        }
                    }
                }
                
                // Update the initialized set based on what this instruction defines (writes).
                Set<String> defs = DfaEngine.getDefinedVars(cmd);
                // A definition only results in an initialized variable if all its inputs were initialized.
                boolean allUsesInitialized = currentInitialized.containsAll(uses);
                
                for (String d : defs) {
                    // Remove the defined variable first to handle re-assignments.
                    currentInitialized.remove(d);
                    if (allUsesInitialized) {
                        currentInitialized.add(d);
                    }
                }
            }
        }
        
        return uninitializedVariables;
    }

    /**
     * Normalizes a unique variable name back to its base name.
     * Internal compiler temporaries (Temp_N) are ignored.
     * Unique suffixes (like _1 in x_1) are stripped to return the original variable name.
     * 
     * @param uniqueName The unique name used in the IR.
     * @return The original variable name, or null if it's a temporary.
     */
    private String getBaseName(String uniqueName) {
        if (uniqueName.startsWith("Temp_")) {
            return null;
        }
        
        int lastUnderscore = uniqueName.lastIndexOf('_');
        if (lastUnderscore != -1) {
            String suffix = uniqueName.substring(lastUnderscore + 1);
            try {
                Integer.parseInt(suffix);
                return uniqueName.substring(0, lastUnderscore);
            } catch (NumberFormatException e) {
                return uniqueName;
            }
        }
        
        return uniqueName;
    }
}