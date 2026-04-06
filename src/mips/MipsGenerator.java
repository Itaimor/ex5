package mips;

import java.io.PrintWriter;
import java.util.*;
import ir.*;
import temp.*;

public class MipsGenerator
{
	private PrintWriter writer;
	private int labelCounter = 0;
	private Map<Integer, String> regMap;
	private Set<String> globalVars;
	private Map<String, Integer> localVarOffsets;
	private String currentFuncLabel;

	public MipsGenerator(PrintWriter writer)
	{
		this.writer = writer;
		this.globalVars = new HashSet<>();
	}

	private String freshLabel(String prefix)
	{
		return "__mips_" + prefix + "_" + (labelCounter++);
	}

	private String reg(Temp t)
	{
		if (t == null) return "$zero";
		String r = regMap.get(t.getSerialNumber());
		if (r == null) return "$zero";
		return r;
	}

	public void generate(
		List<IrCommand> initCommands,
		List<List<IrCommand>> functions,
		Map<Integer, String> initRegMap,
		List<Map<Integer, String>> funcRegMaps,
		Set<String> globalVarNames)
	{
		this.globalVars = globalVarNames;

		emitDataSection();
		writer.println(".text");

		emitEntryPoint(initCommands, initRegMap);
		emitRuntimeHandlers();
		emitStringHelpers();

		for (int i = 0; i < functions.size(); i++)
			emitFunction(functions.get(i), funcRegMaps.get(i));
	}

	private void emitDataSection()
	{
		writer.println(".data");
		writer.println("string_access_violation: .asciiz \"Access Violation\"");
		writer.println("string_illegal_div_by_0: .asciiz \"Illegal Division By Zero\"");
		writer.println("string_invalid_ptr_dref: .asciiz \"Invalid Pointer Dereference\"");

		Map<String, String> stringConsts = StringRegistry.getInstance().getAll();
		for (Map.Entry<String, String> entry : stringConsts.entrySet())
			writer.format("%s: .asciiz %s\n", entry.getValue(), entry.getKey());

		for (String gv : globalVars)
			writer.format("global_%s: .word 0\n", gv);

		ClassLayout cl = ClassLayout.getInstance();
		for (String className : cl.getAllClassNames())
		{
			List<String> entries = cl.getVtableEntries(className);
			if (!entries.isEmpty())
			{
				writer.format("%s: .word ", cl.getVtableLabel(className));
				for (int i = 0; i < entries.size(); i++)
				{
					if (i > 0) writer.print(", ");
					writer.print(entries.get(i));
				}
				writer.println();
			}
			else
			{
				writer.format("%s: .word 0\n", cl.getVtableLabel(className));
			}
		}
	}

	private void emitEntryPoint(List<IrCommand> initCommands, Map<Integer, String> initRegMap)
	{
		writer.println("main:");

		if (!initCommands.isEmpty())
		{
			this.regMap = initRegMap;
			this.currentFuncLabel = "main";
			this.localVarOffsets = buildLocalVarOffsets(initCommands);

			emitPrologue(localVarOffsets.size());

			for (IrCommand cmd : initCommands)
				translateCommand(cmd);
		}

		writer.println("\tjal user_main");
		writer.println("\tli $v0, 10");
		writer.println("\tsyscall");
	}

	private void emitRuntimeHandlers()
	{
		writer.println("label_error_div_by_zero:");
		writer.println("\tla $a0, string_illegal_div_by_0");
		writer.println("\tli $v0, 4");
		writer.println("\tsyscall");
		writer.println("\tli $v0, 10");
		writer.println("\tsyscall");

		writer.println("label_error_null_deref:");
		writer.println("\tla $a0, string_invalid_ptr_dref");
		writer.println("\tli $v0, 4");
		writer.println("\tsyscall");
		writer.println("\tli $v0, 10");
		writer.println("\tsyscall");

		writer.println("label_error_access_violation:");
		writer.println("\tla $a0, string_access_violation");
		writer.println("\tli $v0, 4");
		writer.println("\tsyscall");
		writer.println("\tli $v0, 10");
		writer.println("\tsyscall");
	}

	private void emitStringHelpers()
	{
		// __string_concat: $a0 = str1, $a1 = str2, returns new string in $v0
		writer.println("__string_concat:");
		writer.println("\tmove $s0, $a0");       // s0 = str1 (saved)
		writer.println("\tmove $s1, $a1");       // s1 = str2 (saved)
		// strlen(str1) -> s2
		writer.println("\tli $s2, 0");
		writer.println("__sc_len1:");
		writer.println("\tlb $s3, 0($a0)");
		writer.println("\tbeqz $s3, __sc_len1_done");
		writer.println("\taddiu $s2, $s2, 1");
		writer.println("\taddiu $a0, $a0, 1");
		writer.println("\tj __sc_len1");
		writer.println("__sc_len1_done:");
		// strlen(str2) -> s4
		writer.println("\tli $s4, 0");
		writer.println("\tmove $a0, $s1");
		writer.println("__sc_len2:");
		writer.println("\tlb $s3, 0($a0)");
		writer.println("\tbeqz $s3, __sc_len2_done");
		writer.println("\taddiu $s4, $s4, 1");
		writer.println("\taddiu $a0, $a0, 1");
		writer.println("\tj __sc_len2");
		writer.println("__sc_len2_done:");
		// malloc(len1 + len2 + 1)
		writer.println("\tadd $a0, $s2, $s4");
		writer.println("\taddiu $a0, $a0, 1");
		writer.println("\tli $v0, 9");
		writer.println("\tsyscall");
		writer.println("\tmove $s5, $v0");       // s5 = dest buffer
		// copy str1
		writer.println("\tmove $s6, $s5");       // s6 = write pointer
		writer.println("\tmove $s7, $s0");       // s7 = read pointer (str1)
		writer.println("__sc_copy1:");
		writer.println("\tlb $s3, 0($s7)");
		writer.println("\tbeqz $s3, __sc_copy1_done");
		writer.println("\tsb $s3, 0($s6)");
		writer.println("\taddiu $s7, $s7, 1");
		writer.println("\taddiu $s6, $s6, 1");
		writer.println("\tj __sc_copy1");
		writer.println("__sc_copy1_done:");
		// copy str2
		writer.println("\tmove $s7, $s1");       // s7 = read pointer (str2)
		writer.println("__sc_copy2:");
		writer.println("\tlb $s3, 0($s7)");
		writer.println("\tbeqz $s3, __sc_copy2_done");
		writer.println("\tsb $s3, 0($s6)");
		writer.println("\taddiu $s7, $s7, 1");
		writer.println("\taddiu $s6, $s6, 1");
		writer.println("\tj __sc_copy2");
		writer.println("__sc_copy2_done:");
		// null terminate
		writer.println("\tsb $zero, 0($s6)");
		writer.println("\tmove $v0, $s5");
		writer.println("\tjr $ra");

		// __string_eq: $a0 = str1, $a1 = str2, returns 0 or 1 in $v0
		writer.println("__string_eq:");
		writer.println("__se_loop:");
		writer.println("\tlb $s0, 0($a0)");
		writer.println("\tlb $s1, 0($a1)");
		writer.println("\tbne $s0, $s1, __se_not_equal");
		writer.println("\tbeqz $s0, __se_equal");
		writer.println("\taddiu $a0, $a0, 1");
		writer.println("\taddiu $a1, $a1, 1");
		writer.println("\tj __se_loop");
		writer.println("__se_not_equal:");
		writer.println("\tli $v0, 0");
		writer.println("\tjr $ra");
		writer.println("__se_equal:");
		writer.println("\tli $v0, 1");
		writer.println("\tjr $ra");
	}

	private void emitPrologue(int numLocals)
	{
		writer.println("\tsubu $sp, $sp, 4");
		writer.println("\tsw $ra, 0($sp)");
		writer.println("\tsubu $sp, $sp, 4");
		writer.println("\tsw $fp, 0($sp)");
		writer.println("\tmove $fp, $sp");
		for (int i = 0; i <= 9; i++)
		{
			writer.println("\tsubu $sp, $sp, 4");
			writer.format("\tsw $t%d, 0($sp)\n", i);
		}
		if (numLocals > 0)
			writer.format("\tsubu $sp, $sp, %d\n", numLocals * 4);
	}

	private void emitEpilogue()
	{
		writer.format("%s_epilogue:\n", currentFuncLabel);
		writer.println("\tmove $sp, $fp");
		for (int i = 0; i <= 9; i++)
			writer.format("\tlw $t%d, %d($sp)\n", i, -(i + 1) * 4);
		writer.println("\tlw $fp, 0($sp)");
		writer.println("\tlw $ra, 4($sp)");
		writer.println("\taddu $sp, $sp, 8");
		writer.println("\tjr $ra");
	}

	private void emitFunction(List<IrCommand> commands, Map<Integer, String> funcRegMap)
	{
		if (commands.isEmpty()) return;

		IrCommandLabel entryLabel = (IrCommandLabel) commands.get(0);
		this.currentFuncLabel = entryLabel.getLabelName();
		this.regMap = funcRegMap;
		this.localVarOffsets = buildLocalVarOffsets(commands);

		writer.format("%s:\n", currentFuncLabel);
		emitPrologue(localVarOffsets.size());

		for (int i = 1; i < commands.size(); i++)
			translateCommand(commands.get(i));

		emitEpilogue();
	}

	private Map<String, Integer> buildLocalVarOffsets(List<IrCommand> commands)
	{
		Map<String, Integer> offsets = new LinkedHashMap<>();
		for (IrCommand cmd : commands)
		{
			String varName = null;
			if (cmd instanceof IrCommandStore)
				varName = ((IrCommandStore) cmd).getVarName();
			else if (cmd instanceof IrCommandLoad)
				varName = ((IrCommandLoad) cmd).getVarName();

			if (varName != null && !globalVars.contains(varName) && !offsets.containsKey(varName))
				offsets.put(varName, -(44 + offsets.size() * 4));
		}
		return offsets;
	}

	private void translateCommand(IrCommand cmd)
	{
		if (cmd instanceof IRcommandConstInt)
		{
			IRcommandConstInt c = (IRcommandConstInt) cmd;
			writer.format("\tli %s, %d\n", reg(c.getDst()), c.getValue());
		}
		else if (cmd instanceof IrCommandLoad)
		{
			IrCommandLoad c = (IrCommandLoad) cmd;
			if (globalVars.contains(c.getVarName()))
				writer.format("\tlw %s, global_%s\n", reg(c.getDst()), c.getVarName());
			else
			{
				Integer offset = localVarOffsets.get(c.getVarName());
				if (offset != null)
					writer.format("\tlw %s, %d($fp)\n", reg(c.getDst()), offset);
				else
					writer.format("\tlw %s, global_%s\n", reg(c.getDst()), c.getVarName());
			}
		}
		else if (cmd instanceof IrCommandStore)
		{
			IrCommandStore c = (IrCommandStore) cmd;
			if (globalVars.contains(c.getVarName()))
				writer.format("\tsw %s, global_%s\n", reg(c.getSrc()), c.getVarName());
			else
			{
				Integer offset = localVarOffsets.get(c.getVarName());
				if (offset != null)
					writer.format("\tsw %s, %d($fp)\n", reg(c.getSrc()), offset);
				else
					writer.format("\tsw %s, global_%s\n", reg(c.getSrc()), c.getVarName());
			}
		}
		else if (cmd instanceof IrCommandLabel)
		{
			writer.format("%s:\n", ((IrCommandLabel) cmd).getLabelName());
		}
		else if (cmd instanceof IrCommandJumpLabel)
		{
			writer.format("\tj %s\n", ((IrCommandJumpLabel) cmd).getLabelName());
		}
		else if (cmd instanceof IrCommandJumpIfEqToZero)
		{
			IrCommandJumpIfEqToZero c = (IrCommandJumpIfEqToZero) cmd;
			writer.format("\tbeq %s, $zero, %s\n", reg(c.getTemp()), c.getLabelName());
		}
		else if (cmd instanceof IrCommandReturn)
		{
			IrCommandReturn c = (IrCommandReturn) cmd;
			if (c.getReturnValue() != null)
				writer.format("\tmove $v0, %s\n", reg(c.getReturnValue()));
			writer.format("\tj %s_epilogue\n", currentFuncLabel);
		}
		else if (cmd instanceof IrCommandPrintInt)
		{
			IrCommandPrintInt c = (IrCommandPrintInt) cmd;
			writer.format("\tmove $a0, %s\n", reg(c.getTemp()));
			writer.println("\tli $v0, 1");
			writer.println("\tsyscall");
			writer.println("\tli $a0, 32");
			writer.println("\tli $v0, 11");
			writer.println("\tsyscall");
		}
		else if (cmd instanceof IrCommandBinopAddIntegers)
		{
			IrCommandBinopAddIntegers c = (IrCommandBinopAddIntegers) cmd;
			writer.format("\tadd %s, %s, %s\n", reg(c.getDst()), reg(c.getT1()), reg(c.getT2()));
			emitClamp(reg(c.getDst()));
		}
		else if (cmd instanceof IrCommandBinopSubIntegers)
		{
			IrCommandBinopSubIntegers c = (IrCommandBinopSubIntegers) cmd;
			writer.format("\tsub %s, %s, %s\n", reg(c.getDst()), reg(c.getT1()), reg(c.getT2()));
			emitClamp(reg(c.getDst()));
		}
		else if (cmd instanceof IrCommandBinopMulIntegers)
		{
			IrCommandBinopMulIntegers c = (IrCommandBinopMulIntegers) cmd;
			writer.format("\tmul %s, %s, %s\n", reg(c.getDst()), reg(c.getT1()), reg(c.getT2()));
			emitClamp(reg(c.getDst()));
		}
		else if (cmd instanceof IrCommandBinopDivIntegers)
		{
			IrCommandBinopDivIntegers c = (IrCommandBinopDivIntegers) cmd;
			String doneLabel = freshLabel("div_done");
			writer.format("\tdiv %s, %s\n", reg(c.getT1()), reg(c.getT2()));
			writer.format("\tmflo %s\n", reg(c.getDst()));
			writer.println("\tmfhi $s0");
			writer.format("\tbeqz $s0, %s\n", doneLabel);
			writer.format("\txor $s1, %s, %s\n", reg(c.getT1()), reg(c.getT2()));
			writer.format("\tbgez $s1, %s\n", doneLabel);
			writer.format("\taddiu %s, %s, -1\n", reg(c.getDst()), reg(c.getDst()));
			writer.format("%s:\n", doneLabel);
			emitClamp(reg(c.getDst()));
		}
		else if (cmd instanceof IrCommandBinopEqIntegers)
		{
			IrCommandBinopEqIntegers c = (IrCommandBinopEqIntegers) cmd;
			String trueL = freshLabel("eq_t");
			String doneL = freshLabel("eq_d");
			writer.format("\tbeq %s, %s, %s\n", reg(c.getT1()), reg(c.getT2()), trueL);
			writer.format("\tli %s, 0\n", reg(c.getDst()));
			writer.format("\tj %s\n", doneL);
			writer.format("%s:\n", trueL);
			writer.format("\tli %s, 1\n", reg(c.getDst()));
			writer.format("%s:\n", doneL);
		}
		else if (cmd instanceof IrCommandBinopLtIntegers)
		{
			IrCommandBinopLtIntegers c = (IrCommandBinopLtIntegers) cmd;
			writer.format("\tslt %s, %s, %s\n", reg(c.getDst()), reg(c.getT1()), reg(c.getT2()));
		}
		else if (cmd instanceof IrCommandBinopGtIntegers)
		{
			IrCommandBinopGtIntegers c = (IrCommandBinopGtIntegers) cmd;
			writer.format("\tslt %s, %s, %s\n", reg(c.getDst()), reg(c.getT2()), reg(c.getT1()));
		}
		else if (cmd instanceof IrCommandMalloc)
		{
			IrCommandMalloc c = (IrCommandMalloc) cmd;
			writer.format("\tmove $a0, %s\n", reg(c.getSize()));
			writer.println("\tli $v0, 9");
			writer.println("\tsyscall");
			writer.format("\tmove %s, $v0\n", reg(c.getDst()));
		}
		else if (cmd instanceof IrCommandLoadAddress)
		{
			IrCommandLoadAddress c = (IrCommandLoadAddress) cmd;
			writer.format("\tla %s, %s\n", reg(c.getDst()), c.getLabel());
		}
		else if (cmd instanceof IrCommandLoadField)
		{
			IrCommandLoadField c = (IrCommandLoadField) cmd;
			writer.format("\tlw %s, %d(%s)\n", reg(c.getDst()), c.getOffset(), reg(c.getBase()));
		}
		else if (cmd instanceof IrCommandStoreField)
		{
			IrCommandStoreField c = (IrCommandStoreField) cmd;
			writer.format("\tsw %s, %d(%s)\n", reg(c.getSrc()), c.getOffset(), reg(c.getBase()));
		}
		else if (cmd instanceof IrCommandLoadArray)
		{
			IrCommandLoadArray c = (IrCommandLoadArray) cmd;
			writer.format("\taddiu $s0, %s, 1\n", reg(c.getIndex()));
			writer.println("\tsll $s0, $s0, 2");
			writer.format("\taddu $s0, %s, $s0\n", reg(c.getBase()));
			writer.format("\tlw %s, 0($s0)\n", reg(c.getDst()));
		}
		else if (cmd instanceof IrCommandStoreArray)
		{
			IrCommandStoreArray c = (IrCommandStoreArray) cmd;
			writer.format("\taddiu $s0, %s, 1\n", reg(c.getIndex()));
			writer.println("\tsll $s0, $s0, 2");
			writer.format("\taddu $s0, %s, $s0\n", reg(c.getBase()));
			writer.format("\tsw %s, 0($s0)\n", reg(c.getSrc()));
		}
		else if (cmd instanceof IrCommandLoadParam)
		{
			IrCommandLoadParam c = (IrCommandLoadParam) cmd;
			int offset = 8 + c.getParamIndex() * 4;
			writer.format("\tlw %s, %d($fp)\n", reg(c.getDst()), offset);
		}
		else if (cmd instanceof IrCommandCall)
		{
			IrCommandCall c = (IrCommandCall) cmd;
			List<Temp> args = c.getArgs();
			for (int i = args.size() - 1; i >= 0; i--)
			{
				writer.println("\tsubu $sp, $sp, 4");
				writer.format("\tsw %s, 0($sp)\n", reg(args.get(i)));
			}
			writer.format("\tjal %s\n", c.getFuncLabel());
			if (!args.isEmpty())
				writer.format("\taddu $sp, $sp, %d\n", args.size() * 4);
			if (c.getDst() != null)
				writer.format("\tmove %s, $v0\n", reg(c.getDst()));
		}
		else if (cmd instanceof IrCommandVirtualCall)
		{
			IrCommandVirtualCall c = (IrCommandVirtualCall) cmd;
			List<Temp> args = c.getArgs();
			writer.format("\tlw $s0, 0(%s)\n", reg(c.getBaseObj()));
			writer.format("\tlw $s1, %d($s0)\n", c.getMethodIndex() * 4);
			for (int i = args.size() - 1; i >= 0; i--)
			{
				writer.println("\tsubu $sp, $sp, 4");
				writer.format("\tsw %s, 0($sp)\n", reg(args.get(i)));
			}
			writer.println("\tjalr $s1");
			if (!args.isEmpty())
				writer.format("\taddu $sp, $sp, %d\n", args.size() * 4);
			if (c.getDst() != null)
				writer.format("\tmove %s, $v0\n", reg(c.getDst()));
		}
		else if (cmd instanceof IrCommandPrintString)
		{
			IrCommandPrintString c = (IrCommandPrintString) cmd;
			writer.format("\tmove $a0, %s\n", reg(c.getStrAddr()));
			writer.println("\tli $v0, 4");
			writer.println("\tsyscall");
		}
		else if (cmd instanceof IrCommandStringConcat)
		{
			IrCommandStringConcat c = (IrCommandStringConcat) cmd;
			writer.format("\tmove $a0, %s\n", reg(c.getStr1()));
			writer.format("\tmove $a1, %s\n", reg(c.getStr2()));
			writer.println("\tjal __string_concat");
			writer.format("\tmove %s, $v0\n", reg(c.getDst()));
		}
		else if (cmd instanceof IrCommandStringEq)
		{
			IrCommandStringEq c = (IrCommandStringEq) cmd;
			writer.format("\tmove $a0, %s\n", reg(c.getStr1()));
			writer.format("\tmove $a1, %s\n", reg(c.getStr2()));
			writer.println("\tjal __string_eq");
			writer.format("\tmove %s, $v0\n", reg(c.getDst()));
		}
	}

	private void emitClamp(String regName)
	{
		String noMax = freshLabel("no_max");
		String done = freshLabel("clamp_done");
		writer.println("\tli $s0, 32767");
		writer.format("\tble %s, $s0, %s\n", regName, noMax);
		writer.format("\tli %s, 32767\n", regName);
		writer.format("\tj %s\n", done);
		writer.format("%s:\n", noMax);
		writer.println("\tli $s0, -32768");
		writer.format("\tbge %s, $s0, %s\n", regName, done);
		writer.format("\tli %s, -32768\n", regName);
		writer.format("%s:\n", done);
	}
}
