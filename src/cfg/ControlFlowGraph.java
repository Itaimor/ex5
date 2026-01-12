/***********/
/* PACKAGE */
/***********/
package cfg;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import ir.*;

/**
 * ControlFlowGraph - Manages the complete control flow graph
 * 
 * Based on lecture materials (1.md lines 2862-3096):
 * - CFG is a directed graph G = (V, E)
 * - V = set of basic blocks
 * - E = control flow edges between blocks
 */
public class ControlFlowGraph
{
	/****************/
	/* DATA MEMBERS */
	/****************/
	private List<BasicBlock> blocks;
	private BasicBlock entryBlock;
	private BasicBlock exitBlock;
	
	// Map from label names to blocks that start with that label
	private Map<String, BasicBlock> labelToBlock;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public ControlFlowGraph()
	{
		this.blocks = new ArrayList<>();
		this.labelToBlock = new HashMap<>();
	}
	
	/******************/
	/* ADD BLOCK      */
	/******************/
	public void addBlock(BasicBlock block)
	{
		blocks.add(block);
	}
	
	/******************/
	/* GETTERS        */
	/******************/
	public List<BasicBlock> getBlocks() { return blocks; }
	public BasicBlock getEntryBlock() { return entryBlock; }
	public BasicBlock getExitBlock() { return exitBlock; }
	
	public void setEntryBlock(BasicBlock entry) { this.entryBlock = entry; }
	public void setExitBlock(BasicBlock exit) { this.exitBlock = exit; }
	
	public Map<String, BasicBlock> getLabelToBlock() { return labelToBlock; }
	
	/*******************************************/
	/* BUILD CFG FROM IR COMMAND LIST         */
	/* Based on lecture 1.md lines 2990-3010  */
	/*******************************************/
	public static ControlFlowGraph build(List<IrCommand> irCommands)
	{
		ControlFlowGraph cfg = new ControlFlowGraph();
		
		if (irCommands == null || irCommands.isEmpty()) {
			return cfg;
		}
		
		/************************************/
		/* STEP 1: Identify Leaders         */
		/* (per lecture 1.md lines 2999-3005) */
		/************************************/
		List<Integer> leaders = identifyLeaders(irCommands);
		
		/************************************/
		/* STEP 2: Partition into Blocks    */
		/************************************/
		List<BasicBlock> blocks = partitionIntoBlocks(irCommands, leaders);
		cfg.blocks = blocks;
		
		/************************************/
		/* STEP 3: Build Label Map          */
		/************************************/
		cfg.buildLabelMap(irCommands, blocks, leaders);
		
		/************************************/
		/* STEP 4: Connect Blocks           */
		/************************************/
		cfg.connectBlocks(irCommands, leaders);
		
		/************************************/
		/* STEP 5: Set Entry/Exit          */
		/************************************/
		if (!blocks.isEmpty()) {
			cfg.entryBlock = blocks.get(0);
			cfg.entryBlock.setEntry(true);
			
			// Exit block is the last block (or blocks with no successors)
			cfg.exitBlock = blocks.get(blocks.size() - 1);
			cfg.exitBlock.setExit(true);
		}
		
		return cfg;
	}
	
	/************************************/
	/* IDENTIFY LEADERS                 */
	/* Per lecture 1.md lines 2999-3005:*/
	/* - First instruction is a leader  */
	/* - Target of jump is a leader     */
	/* - Instruction after jump is leader*/
	/************************************/
	private static List<Integer> identifyLeaders(List<IrCommand> irCommands)
	{
		List<Integer> leaders = new ArrayList<>();
		
		// Rule 1: First instruction is always a leader
		if (!irCommands.isEmpty()) {
			leaders.add(0);
		}
		
		for (int i = 0; i < irCommands.size(); i++) {
			IrCommand cmd = irCommands.get(i);
			
			// Rule 2: Any instruction that is the target of a jump is a leader
			if (cmd instanceof IrCommandLabel) {
				if (!leaders.contains(i)) {
					leaders.add(i);
				}
			}
			
			// Rule 3: Any instruction immediately following a jump is a leader
			if (cmd instanceof IrCommandJumpLabel || 
			    cmd instanceof IrCommandJumpIfEqToZero ||
			    cmd instanceof IrCommandReturn) {
				if (i + 1 < irCommands.size() && !leaders.contains(i + 1)) {
					leaders.add(i + 1);
				}
			}
		}
		
		// Sort leaders for easy processing
		leaders.sort(Integer::compareTo);
		
		return leaders;
	}
	
	/************************************/
	/* PARTITION INTO BASIC BLOCKS      */
	/************************************/
	private static List<BasicBlock> partitionIntoBlocks(List<IrCommand> irCommands, List<Integer> leaders)
	{
		List<BasicBlock> blocks = new ArrayList<>();
		
		for (int i = 0; i < leaders.size(); i++) {
			int start = leaders.get(i);
			int end = (i + 1 < leaders.size()) ? leaders.get(i + 1) : irCommands.size();
			
			// Create a new basic block
			BasicBlock block = new BasicBlock(i);
			
			// Add all instructions from start to end (exclusive)
			for (int j = start; j < end; j++) {
				block.addInstruction(irCommands.get(j));
			}
			
			blocks.add(block);
		}
		
		return blocks;
	}
	
	/************************************/
	/* BUILD LABEL TO BLOCK MAP         */
	/************************************/
	private void buildLabelMap(List<IrCommand> irCommands, List<BasicBlock> blocks, List<Integer> leaders)
	{
		// Map each label to the block that contains it
		for (int i = 0; i < leaders.size(); i++) {
			int leaderIndex = leaders.get(i);
			IrCommand cmd = irCommands.get(leaderIndex);
			
			if (cmd instanceof IrCommandLabel) {
				String labelName = ((IrCommandLabel) cmd).getLabelName();
				labelToBlock.put(labelName, blocks.get(i));
			}
		}
		
		// Also need to map labels that appear within blocks (though rare)
		for (int blockIdx = 0; blockIdx < blocks.size(); blockIdx++) {
			BasicBlock block = blocks.get(blockIdx);
			for (IrCommand cmd : block.getInstructions()) {
				if (cmd instanceof IrCommandLabel) {
					String labelName = ((IrCommandLabel) cmd).getLabelName();
					if (!labelToBlock.containsKey(labelName)) {
						labelToBlock.put(labelName, block);
					}
				}
			}
		}
	}
	
	/************************************/
	/* CONNECT BLOCKS                   */
	/* Add edges based on control flow  */
	/************************************/
	private void connectBlocks(List<IrCommand> irCommands, List<Integer> leaders)
	{
		for (int i = 0; i < blocks.size(); i++) {
			BasicBlock block = blocks.get(i);
			IrCommand lastCmd = block.getLastInstruction();
			
			if (lastCmd == null) continue;
			
			// Handle different types of control flow
			if (lastCmd instanceof IrCommandJumpLabel) {
				// Unconditional jump: goto L
				String targetLabel = ((IrCommandJumpLabel) lastCmd).getLabelName();
				BasicBlock targetBlock = labelToBlock.get(targetLabel);
				if (targetBlock != null) {
					block.addSuccessor(targetBlock);
					targetBlock.addPredecessor(block);
				}
			}
			else if (lastCmd instanceof IrCommandJumpIfEqToZero) {
				// Conditional jump: if t == 0 goto L
				String targetLabel = ((IrCommandJumpIfEqToZero) lastCmd).getLabelName();
				BasicBlock targetBlock = labelToBlock.get(targetLabel);
				
				// Edge to target (false branch)
				if (targetBlock != null) {
					block.addSuccessor(targetBlock);
					targetBlock.addPredecessor(block);
				}
				
				// Edge to next block (true branch - fall through)
				if (i + 1 < blocks.size()) {
					BasicBlock nextBlock = blocks.get(i + 1);
					block.addSuccessor(nextBlock);
					nextBlock.addPredecessor(block);
				}
			}
			else if (lastCmd instanceof IrCommandReturn) {
				// Return statement: no successors (ends function)
				// No edges added
			}
			else {
				// Regular instruction: fall through to next block
				if (i + 1 < blocks.size()) {
					BasicBlock nextBlock = blocks.get(i + 1);
					block.addSuccessor(nextBlock);
					nextBlock.addPredecessor(block);
				}
			}
		}
	}
	
	/******************/
	/* DEBUG OUTPUT   */
	/******************/
	public void printCFG()
	{
		System.out.println("=== Control Flow Graph ===");
		System.out.println("Entry Block: " + (entryBlock != null ? entryBlock.getId() : "none"));
		System.out.println("Exit Block: " + (exitBlock != null ? exitBlock.getId() : "none"));
		System.out.println();
		
		for (BasicBlock block : blocks) {
			System.out.println(block.toString());
			System.out.print("  Predecessors: ");
			for (BasicBlock pred : block.getPredecessors()) {
				System.out.print(pred.getId() + " ");
			}
			System.out.println();
			
			System.out.print("  Successors: ");
			for (BasicBlock succ : block.getSuccessors()) {
				System.out.print(succ.getId() + " ");
			}
			System.out.println();
			System.out.println();
		}
	}
	
	/******************/
	/* TO DOT FORMAT  */
	/******************/
	public String toDot()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("digraph CFG {\n");
		sb.append("  node [shape=box];\n");
		
		// Add nodes
		for (BasicBlock block : blocks) {
			sb.append(String.format("  block%d [label=\"Block %d", block.getId(), block.getId()));
			if (block.isEntry()) sb.append("\\n[ENTRY]");
			if (block.isExit()) sb.append("\\n[EXIT]");
			sb.append("\\n---\\n");
			
			// Add first few instructions as label
			int count = 0;
			for (IrCommand cmd : block.getInstructions()) {
				if (count++ >= 3) {
					sb.append("...\\n");
					break;
				}
				sb.append(cmd.toString().replace("\"", "\\\"")).append("\\n");
			}
			sb.append("\"];\n");
		}
		
		// Add edges
		for (BasicBlock block : blocks) {
			for (BasicBlock succ : block.getSuccessors()) {
				sb.append(String.format("  block%d -> block%d;\n", block.getId(), succ.getId()));
			}
		}
		
		sb.append("}\n");
		return sb.toString();
	}
}

