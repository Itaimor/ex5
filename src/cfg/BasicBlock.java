/***********/
/* PACKAGE */
/***********/
package cfg;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.util.ArrayList;
import java.util.List;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import ir.*;

/**
 * BasicBlock - Represents a basic block in the control flow graph
 * 
 * A basic block is a maximal sequence of instructions with:
 * - Single entry point (only the first instruction can be entered)
 * - Single exit point (only the last instruction can exit)
 * - No jumps into the middle of the block
 * - No jumps out except from the last instruction
 */
public class BasicBlock
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	private int id;
	private List<IrCommand> instructions;
	private List<BasicBlock> predecessors;
	private List<BasicBlock> successors;
	
	private boolean isEntry = false;
	private boolean isExit = false;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public BasicBlock(int id)
	{
		this.id = id;
		this.instructions = new ArrayList<>();
		this.predecessors = new ArrayList<>();
		this.successors = new ArrayList<>();
	}
	
	/******************/
	/* ADD INSTRUCTION */
	/******************/
	public void addInstruction(IrCommand cmd)
	{
		instructions.add(cmd);
	}
	
	/*********************/
	/* ADD PREDECESSOR   */
	/*********************/
	public void addPredecessor(BasicBlock pred)
	{
		if (!predecessors.contains(pred)) {
			predecessors.add(pred);
		}
	}
	
	/******************/
	/* ADD SUCCESSOR  */
	/******************/
	public void addSuccessor(BasicBlock succ)
	{
		if (!successors.contains(succ)) {
			successors.add(succ);
		}
	}
	
	/******************/
	/* GETTERS        */
	/******************/
	public int getId() { return id; }
	public List<IrCommand> getInstructions() { return instructions; }
	public List<BasicBlock> getPredecessors() { return predecessors; }
	public List<BasicBlock> getSuccessors() { return successors; }
	
	public boolean isEntry() { return isEntry; }
	public boolean isExit() { return isExit; }
	
	public void setEntry(boolean entry) { this.isEntry = entry; }
	public void setExit(boolean exit) { this.isExit = exit; }
	
	/******************/
	/* FIRST/LAST CMD */
	/******************/
	public IrCommand getFirstInstruction()
	{
		return instructions.isEmpty() ? null : instructions.get(0);
	}
	
	public IrCommand getLastInstruction()
	{
		return instructions.isEmpty() ? null : instructions.get(instructions.size() - 1);
	}
	
	/******************/
	/* DEBUG OUTPUT   */
	/******************/
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("BasicBlock %d:\n", id));
		for (IrCommand cmd : instructions) {
			sb.append("  ").append(cmd.toString()).append("\n");
		}
		return sb.toString();
	}
}

