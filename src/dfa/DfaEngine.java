package dfa;

import cfg.*;
import ir.*;
import java.util.*;

/**
 * DfaEngine performs Data Flow Analysis on a Control Flow Graph.
 * Specifically, it implements a Forward "Must" Analysis used for
 * Definite Assignment (checking if variables are guaranteed to be initialized).
 */
public class DfaEngine {
    // The Control Flow Graph to analyze
    private ControlFlowGraph cfg;
    // Maps each BasicBlock to the set of variables guaranteed to be initialized at its entry
    private Map<BasicBlock, Set<String>> inSets;
    // Maps each BasicBlock to the set of variables guaranteed to be initialized at its exit
    private Map<BasicBlock, Set<String>> outSets;

    public DfaEngine(ControlFlowGraph cfg) {
        this.cfg = cfg;
        this.inSets = new HashMap<>();
        this.outSets = new HashMap<>();
        initialize();
    }

    /**
     * Initializes the analysis sets.
     * For a "Must" analysis, we start with the "Top" value (all variables) for all blocks,
     * except the entry block which starts with an empty set.
     */
    private void initialize() {
        // Collect all variables and temporaries used in the CFG
        Set<String> allVars = new HashSet<>();
        for (BasicBlock block : cfg.getBlocks()) {
            for (IrCommand cmd : block.getInstructions()) {
                allVars.addAll(getDefinedVars(cmd));
                allVars.addAll(getUsedVars(cmd));
            }
        }
        
        // Initialize all blocks to the "Top" state (optimistic assumption)
        for (BasicBlock block : cfg.getBlocks()) {
            inSets.put(block, new HashSet<>(allVars));
            outSets.put(block, new HashSet<>(allVars));
        }

        // The entry block starts with no variables initialized
        if (cfg.getEntryBlock() != null) {
            inSets.put(cfg.getEntryBlock(), new HashSet<>());
        }
    }

    /**
     * Executes the fixed-point iteration algorithm.
     * The sets are updated until no further changes occur.
     */
    public void run() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (BasicBlock block : cfg.getBlocks()) {
                Set<String> newIn;
                if (block.isEntry()) {
                    // Entry block IN set is always empty
                    newIn = new HashSet<>();
                } else {
                    // IN[B] = Intersection of OUT[P] for all predecessors P of B
                    newIn = null;
                    for (BasicBlock pred : block.getPredecessors()) {
                        if (newIn == null) {
                            newIn = new HashSet<>(outSets.get(pred));
                        } else {
                            newIn.retainAll(outSets.get(pred));
                        }
                    }
                    // Handle case with no predecessors (shouldn't happen except for entry)
                    if (newIn == null) newIn = new HashSet<>();
                }

                // If IN set changed, mark as changed and update
                if (!newIn.equals(inSets.get(block))) {
                    inSets.put(block, newIn);
                    changed = true;
                }

                // OUT[B] = TransferFunction(B, IN[B])
                Set<String> newOut = transferFunction(block, newIn);
                // If OUT set changed, mark as changed and update
                if (!newOut.equals(outSets.get(block))) {
                    outSets.put(block, newOut);
                    changed = true;
                }
            }
        }
    }

    /**
     * The transfer function defines how a block's IN set is transformed into its OUT set.
     * It simulates the execution of each instruction in the block.
     * 
     * @param block The basic block to process
     * @param in The set of variables initialized at the start of the block
     * @return The set of variables initialized at the end of the block
     */
    private Set<String> transferFunction(BasicBlock block, Set<String> in) {
        Set<String> current = new HashSet<>(in);
        for (IrCommand cmd : block.getInstructions()) {
            Set<String> defs = getDefinedVars(cmd);
            Set<String> uses = getUsedVars(cmd);

            // Check if all variables used by this command are already initialized
            boolean allUsesInitialized = current.containsAll(uses);
            
            for (String d : defs) {
                // Remove the defined variable from the set first (resetting its status)
                current.remove(d);
                // If all its inputs were initialized, then the result of this command is also initialized
                if (allUsesInitialized) {
                    current.add(d);
                }
            }
        }
        return current;
    }

    /**
     * Gets the set of variables initialized at the entry of a block.
     */
    public Set<String> getInSet(BasicBlock block) {
        return inSets.get(block);
    }

    /**
     * Identifies which variables/temporaries are READ by a given IR command.
     */
    public static Set<String> getUsedVars(IrCommand cmd) {
        // Variable load: reads the variable being loaded
        if (cmd instanceof IrCommandLoad) {
            return Collections.singleton(((IrCommandLoad) cmd).getVarName());
        }
        // Store: reads the temporary source
        if (cmd instanceof IrCommandStore) {
            return Collections.singleton("Temp_" + ((IrCommandStore) cmd).getSrc().getSerialNumber());
        }
        // Binary operations: read two temporary operands
        if (cmd instanceof IrCommandBinopAddIntegers) {
            IrCommandBinopAddIntegers c = (IrCommandBinopAddIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        if (cmd instanceof IrCommandBinopSubIntegers) {
            IrCommandBinopSubIntegers c = (IrCommandBinopSubIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        if (cmd instanceof IrCommandBinopMulIntegers) {
            IrCommandBinopMulIntegers c = (IrCommandBinopMulIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        if (cmd instanceof IrCommandBinopDivIntegers) {
            IrCommandBinopDivIntegers c = (IrCommandBinopDivIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        if (cmd instanceof IrCommandBinopEqIntegers) {
            IrCommandBinopEqIntegers c = (IrCommandBinopEqIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        if (cmd instanceof IrCommandBinopLtIntegers) {
            IrCommandBinopLtIntegers c = (IrCommandBinopLtIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        if (cmd instanceof IrCommandBinopGtIntegers) {
            IrCommandBinopGtIntegers c = (IrCommandBinopGtIntegers) cmd;
            return new HashSet<>(Arrays.asList("Temp_" + c.getT1().getSerialNumber(), "Temp_" + c.getT2().getSerialNumber()));
        }
        // Jump: reads the temporary condition
        if (cmd instanceof IrCommandJumpIfEqToZero) {
            return Collections.singleton("Temp_" + ((IrCommandJumpIfEqToZero) cmd).getTemp().getSerialNumber());
        }
        // Print: reads the temporary being printed
        if (cmd instanceof IrCommandPrintInt) {
            return Collections.singleton("Temp_" + ((IrCommandPrintInt) cmd).getTemp().getSerialNumber());
        }
        // Return: reads the temporary return value
        if (cmd instanceof IrCommandReturn) {
            IrCommandReturn c = (IrCommandReturn) cmd;
            if (c.getReturnValue() != null) {
                return Collections.singleton("Temp_" + c.getReturnValue().getSerialNumber());
            }
        }
        return Collections.emptySet();
    }

    /**
     * Identifies which variable/temporary is WRITTEN to by a given IR command.
     */
    public static Set<String> getDefinedVars(IrCommand cmd) {
        // Variable load: writes to a destination temporary
        if (cmd instanceof IrCommandLoad) {
            return Collections.singleton("Temp_" + ((IrCommandLoad) cmd).getDst().getSerialNumber());
        }
        // Store: writes to a named variable
        if (cmd instanceof IrCommandStore) {
            return Collections.singleton(((IrCommandStore) cmd).getVarName());
        }
        // Constant: writes to a destination temporary
        if (cmd instanceof IRcommandConstInt) {
            return Collections.singleton("Temp_" + ((IRcommandConstInt) cmd).getDst().getSerialNumber());
        }
        // Binary operations: write to a destination temporary
        if (cmd instanceof IrCommandBinopAddIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopAddIntegers) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopSubIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopSubIntegers) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopMulIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopMulIntegers) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopDivIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopDivIntegers) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopEqIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopEqIntegers) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopLtIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopLtIntegers) cmd).getDst().getSerialNumber());
        }
        if (cmd instanceof IrCommandBinopGtIntegers) {
            return Collections.singleton("Temp_" + ((IrCommandBinopGtIntegers) cmd).getDst().getSerialNumber());
        }
        // Allocate: defines a new named variable (allocated on stack/heap)
        if (cmd instanceof IrCommandAllocate) {
            return Collections.singleton(((IrCommandAllocate) cmd).getVarName());
        }
        return Collections.emptySet();
    }
}
