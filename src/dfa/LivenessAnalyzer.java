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

            // === Binops (T6a: replaced reflection with direct instanceof) ===
            if (cmd instanceof IrCommandBinopAddIntegers) {
                IrCommandBinopAddIntegers c = (IrCommandBinopAddIntegers) cmd;
                use.add(c.getT1()); use.add(c.getT2()); def.add(c.getDst());
            } else if (cmd instanceof IrCommandBinopSubIntegers) {
                IrCommandBinopSubIntegers c = (IrCommandBinopSubIntegers) cmd;
                use.add(c.getT1()); use.add(c.getT2()); def.add(c.getDst());
            } else if (cmd instanceof IrCommandBinopMulIntegers) {
                IrCommandBinopMulIntegers c = (IrCommandBinopMulIntegers) cmd;
                use.add(c.getT1()); use.add(c.getT2()); def.add(c.getDst());
            } else if (cmd instanceof IrCommandBinopDivIntegers) {
                IrCommandBinopDivIntegers c = (IrCommandBinopDivIntegers) cmd;
                use.add(c.getT1()); use.add(c.getT2()); def.add(c.getDst());
            } else if (cmd instanceof IrCommandBinopEqIntegers) {
                IrCommandBinopEqIntegers c = (IrCommandBinopEqIntegers) cmd;
                use.add(c.getT1()); use.add(c.getT2()); def.add(c.getDst());
            } else if (cmd instanceof IrCommandBinopLtIntegers) {
                IrCommandBinopLtIntegers c = (IrCommandBinopLtIntegers) cmd;
                use.add(c.getT1()); use.add(c.getT2()); def.add(c.getDst());
            } else if (cmd instanceof IrCommandBinopGtIntegers) {
                IrCommandBinopGtIntegers c = (IrCommandBinopGtIntegers) cmd;
                use.add(c.getT1()); use.add(c.getT2()); def.add(c.getDst());
            }
            // === Original EX4 commands ===
            else if (cmd instanceof IRcommandConstInt) {
                def.add(((IRcommandConstInt) cmd).getDst());
            } else if (cmd instanceof IrCommandLoad) {
                def.add(((IrCommandLoad) cmd).getDst());
            } else if (cmd instanceof IrCommandStore) {
                use.add(((IrCommandStore) cmd).getSrc());
            } else if (cmd instanceof IrCommandPrintInt) {
                use.add(((IrCommandPrintInt) cmd).getTemp());
            } else if (cmd instanceof IrCommandReturn) {
                Temp ret = ((IrCommandReturn) cmd).getReturnValue();
                if (ret != null) use.add(ret);
            } else if (cmd instanceof IrCommandJumpIfEqToZero) {
                use.add(((IrCommandJumpIfEqToZero) cmd).getTemp());
                String label = ((IrCommandJumpIfEqToZero) cmd).getLabelName();
                if (labelToCommand.containsKey(label))
                    succs.add(labelToCommand.get(label));
            }
            // === 12 new EX5 commands (T6b) ===
            else if (cmd instanceof IrCommandMalloc) {
                IrCommandMalloc c = (IrCommandMalloc) cmd;
                def.add(c.getDst()); use.add(c.getSize());
            } else if (cmd instanceof IrCommandLoadField) {
                IrCommandLoadField c = (IrCommandLoadField) cmd;
                def.add(c.getDst()); use.add(c.getBase());
            } else if (cmd instanceof IrCommandStoreField) {
                IrCommandStoreField c = (IrCommandStoreField) cmd;
                use.add(c.getBase()); use.add(c.getSrc());
            } else if (cmd instanceof IrCommandLoadArray) {
                IrCommandLoadArray c = (IrCommandLoadArray) cmd;
                def.add(c.getDst()); use.add(c.getBase()); use.add(c.getIndex());
            } else if (cmd instanceof IrCommandStoreArray) {
                IrCommandStoreArray c = (IrCommandStoreArray) cmd;
                use.add(c.getBase()); use.add(c.getIndex()); use.add(c.getSrc());
            } else if (cmd instanceof IrCommandLoadAddress) {
                def.add(((IrCommandLoadAddress) cmd).getDst());
            } else if (cmd instanceof IrCommandCall) {
                IrCommandCall c = (IrCommandCall) cmd;
                if (c.getDst() != null) def.add(c.getDst());
                use.addAll(c.getArgs());
            } else if (cmd instanceof IrCommandVirtualCall) {
                IrCommandVirtualCall c = (IrCommandVirtualCall) cmd;
                if (c.getDst() != null) def.add(c.getDst());
                use.add(c.getBaseObj()); use.addAll(c.getArgs());
            } else if (cmd instanceof IrCommandLoadParam) {
                def.add(((IrCommandLoadParam) cmd).getDst());
            } else if (cmd instanceof IrCommandPrintString) {
                use.add(((IrCommandPrintString) cmd).getStrAddr());
            } else if (cmd instanceof IrCommandStringConcat) {
                IrCommandStringConcat c = (IrCommandStringConcat) cmd;
                def.add(c.getDst()); use.add(c.getStr1()); use.add(c.getStr2());
            } else if (cmd instanceof IrCommandStringEq) {
                IrCommandStringEq c = (IrCommandStringEq) cmd;
                def.add(c.getDst()); use.add(c.getStr1()); use.add(c.getStr2());
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
