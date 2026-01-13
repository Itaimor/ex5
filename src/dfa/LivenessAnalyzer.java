/***********/
/* PACKAGE */
/***********/
package dfa;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.util.*;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import ir.*;
import temp.*;

public class LivenessAnalyzer {
    private List<IrCommand> commands;
    private Map<IrCommand, Set<Temp>> useMap = new HashMap<>();
    private Map<IrCommand, Set<Temp>> defMap = new HashMap<>();
    private Map<IrCommand, Set<IrCommand>> succMap = new HashMap<>();
    private Map<IrCommand, Set<Temp>> inMap = new HashMap<>();
    private Map<IrCommand, Set<Temp>> outMap = new HashMap<>();

    public LivenessAnalyzer(List<IrCommand> commands) {
        this.commands = commands;
        buildAnalysisData();
        computeLiveness();
    }

    private void buildAnalysisData() {
        Map<String, IrCommand> labelToCommand = new HashMap<>();

        // Pass 1: Find all labels
        for (IrCommand cmd : commands) {
            if (cmd instanceof IrCommandLabel) {
                labelToCommand.put(((IrCommandLabel) cmd).getLabelName(), cmd);
            }
        }

        // Pass 2: Build def/use and successors
        for (int i = 0; i < commands.size(); i++) {
            IrCommand cmd = commands.get(i);
            Set<Temp> use = new HashSet<>();
            Set<Temp> def = new HashSet<>();
            Set<IrCommand> succs = new HashSet<>();

            // Calculate def/use
            if (cmd instanceof IrCommandBinopAddIntegers ||
                    cmd instanceof IrCommandBinopSubIntegers ||
                    cmd instanceof IrCommandBinopMulIntegers ||
                    cmd instanceof IrCommandBinopDivIntegers ||
                    cmd instanceof IrCommandBinopEqIntegers ||
                    cmd instanceof IrCommandBinopGtIntegers ||
                    cmd instanceof IrCommandBinopLtIntegers) {
                try {
                    use.add((Temp) cmd.getClass().getMethod("getT1").invoke(cmd));
                    use.add((Temp) cmd.getClass().getMethod("getT2").invoke(cmd));
                    def.add((Temp) cmd.getClass().getMethod("getDst").invoke(cmd));
                } catch (Exception e) {
                    // Fallback to field access if methods are missing
                    try {
                        use.add((Temp) cmd.getClass().getField("t1").get(cmd));
                        use.add((Temp) cmd.getClass().getField("t2").get(cmd));
                        def.add((Temp) cmd.getClass().getField("dst").get(cmd));
                    } catch (Exception e2) {
                    }
                }
            } else if (cmd instanceof IRcommandConstInt) {
                def.add(((IRcommandConstInt) cmd).getDst());
            } else if (cmd instanceof IrCommandLoad) {
                def.add(((IrCommandLoad) cmd).getDst());
            } else if (cmd instanceof IrCommandStore) {
                use.add(((IrCommandStore) cmd).getSrc());
            } else if (cmd instanceof IrCommandPrintInt) {
                use.add(((IrCommandPrintInt) cmd).getTemp());
            } else if (cmd instanceof IrCommandReturn) {
                Temp ret = ((IrCommandReturn) cmd).getReturnValue();
                if (ret != null)
                    use.add(ret);
            } else if (cmd instanceof IrCommandJumpIfEqToZero) {
                use.add(((IrCommandJumpIfEqToZero) cmd).getTemp());
                String label = ((IrCommandJumpIfEqToZero) cmd).getLabelName();
                if (labelToCommand.containsKey(label)) {
                    succs.add(labelToCommand.get(label));
                }
            }

            // Calculate successors (default: next instruction)
            if (!(cmd instanceof IrCommandJumpLabel) && !(cmd instanceof IrCommandReturn)) {
                if (i + 1 < commands.size()) {
                    succs.add(commands.get(i + 1));
                }
            }
            if (cmd instanceof IrCommandJumpLabel) {
                String label = ((IrCommandJumpLabel) cmd).getLabelName();
                if (labelToCommand.containsKey(label)) {
                    succs.add(labelToCommand.get(label));
                }
            }

            useMap.put(cmd, use);
            defMap.put(cmd, def);
            succMap.put(cmd, succs);
            inMap.put(cmd, new HashSet<>());
            outMap.put(cmd, new HashSet<>());
        }
    }

    private void computeLiveness() {
        boolean changed = true;
        while (changed) {
            changed = false;
            // Iterate backwards for faster convergence in liveness
            for (int i = commands.size() - 1; i >= 0; i--) {
                IrCommand cmd = commands.get(i);

                Set<Temp> oldIn = new HashSet<>(inMap.get(cmd));
                Set<Temp> oldOut = new HashSet<>(outMap.get(cmd));

                // out[n] = U_{s in succ[n]} in[s]
                Set<Temp> newOut = new HashSet<>();
                for (IrCommand succ : succMap.get(cmd)) {
                    newOut.addAll(inMap.get(succ));
                }
                outMap.put(cmd, newOut);

                // in[n] = use[n] U (out[n] - def[n])
                Set<Temp> newIn = new HashSet<>(useMap.get(cmd));
                Set<Temp> outMinusDef = new HashSet<>(newOut);
                outMinusDef.removeAll(defMap.get(cmd));
                newIn.addAll(outMinusDef);
                inMap.put(cmd, newIn);

                if (!newIn.equals(oldIn) || !newOut.equals(oldOut)) {
                    changed = true;
                }
            }
        }
    }

    public Set<Temp> getIn(IrCommand cmd) {
        return inMap.get(cmd);
    }

    public Set<Temp> getOut(IrCommand cmd) {
        return outMap.get(cmd);
    }

    public Set<Temp> getDef(IrCommand cmd) {
        return defMap.get(cmd);
    }
}
