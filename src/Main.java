import java.io.*;
import java_cup.runtime.Symbol;
import ast.*;
import ir.*;
import dfa.LivenessAnalyzer;
import regalloc.*;
import mips.MipsGenerator;
import java.util.*;

public class Main
{
	static public void main(String argv[])
	{
		Lexer l;
		Parser p;
		Symbol s;
		AstDecList ast;
		FileReader fileReader;
		PrintWriter fileWriter;
		String inputFileName = argv[0];
		String outputFileName = argv[1];

		try
		{
			fileReader = new FileReader(inputFileName);
			fileWriter = new PrintWriter(outputFileName);

			l = new Lexer(fileReader);
			p = new Parser(l, fileWriter);

			ast = (AstDecList) p.parse().value;
			ast.semantMe();
			ast.irMe();

			List<IrCommand> allIR = Ir.getInstance().getAllCommands();

			List<IrCommand> initCommands = new ArrayList<>();
			List<List<IrCommand>> functions = new ArrayList<>();

			splitIR(allIR, initCommands, functions);

			Map<Integer, String> initRegMap = new HashMap<>();
			if (!initCommands.isEmpty())
			{
				initRegMap = allocateRegisters(initCommands);
				if (initRegMap == null)
				{
					fileWriter.print("Register Allocation Failed");
					fileWriter.close();
					return;
				}
			}

			List<Map<Integer, String>> funcRegMaps = new ArrayList<>();
			for (List<IrCommand> func : functions)
			{
				Map<Integer, String> rm = allocateRegisters(func);
				if (rm == null)
				{
					fileWriter.print("Register Allocation Failed");
					fileWriter.close();
					return;
				}
				funcRegMaps.add(rm);
			}

			MipsGenerator mips = new MipsGenerator(fileWriter);
			mips.generate(
				initCommands,
				functions,
				initRegMap,
				funcRegMaps,
				GlobalVarRegistry.getInstance().getAll()
			);

			fileWriter.close();
			AstGraphviz.getInstance().finalizeFile();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static void splitIR(List<IrCommand> allIR,
		List<IrCommand> initCommands,
		List<List<IrCommand>> functions)
	{
		List<IrCommand> current = null;
		boolean seenFirstFunc = false;

		for (IrCommand cmd : allIR)
		{
			if (cmd instanceof IrCommandLabel)
			{
				String label = ((IrCommandLabel) cmd).getLabelName();
				if (!label.startsWith("Label_"))
				{
					if (current != null)
						functions.add(current);
					current = new ArrayList<>();
					current.add(cmd);
					seenFirstFunc = true;
					continue;
				}
			}

			if (!seenFirstFunc)
				initCommands.add(cmd);
			else if (current != null)
				current.add(cmd);
		}

		if (current != null)
			functions.add(current);
	}

	private static Map<Integer, String> allocateRegisters(List<IrCommand> commands)
	{
		if (commands.isEmpty()) return new HashMap<>();

		LivenessAnalyzer liveness = new LivenessAnalyzer(commands);
		InterferenceGraph ig = InterferenceGraph.build(liveness, commands);
		return RegisterAllocator.allocate(ig);
	}
}
