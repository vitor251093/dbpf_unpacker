package sporemodder.file.arth;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import sporemodder.file.filestructures.Stream.StringEncoding;
import sporemodder.file.filestructures.StreamReader;
import sporemodder.file.filestructures.StreamWriter;

public class ArithmeticaFile {
	public static final int OP_ADD = 0;  // stack(-2) + stack(-1), pops 2 values
	public static final int OP_SUBTRACT = 1;  // stack(-2) - stack(-1), pops 2 values
	public static final int OP_MULTIPLY = 2;  // stack(-2) * stack(-1), pops 2 values
	public static final int OP_DIVIDE = 3;  // stack(-2) / stack(-1), pops 2 values
	public static final int OP_POW = 4;  // stack(-2) ^ stack(-1), pops 2 values  //TODO I'm not sure of the order
	public static final int OP_PUSH = 5;  // pushes value from this definition
	public static final int OP_PUSH_VARIABLE = 6;  // pushes from code set variables
	public static final int OP_POP_INTO_VARIABLE = 7;  // assigns last pushed value into variable, pops
	public static final int OP_POP_INTO_VARIABLE2 = 8;
	public static final int OP_NOP = 9;
	public static final int OP_RANDOM = 10;  // pushes a random number between 0.0 and 1.0
	public static final int OP_GREATER = 11;  // pushes 1 or 0, stack(-2) > stack(-1)
	public static final int OP_LESS = 12;  // pushes 1 or 0, stack(-2) < stack(-1)
	public static final int OP_GREATER_EQUAL = 13;  // pushes 1 or 0, stack(-2) >= stack(-1)
	public static final int OP_LESS_EQUAL = 14;  // pushes 1 or 0, stack(-2) <= stack(-1)
	public static final int OP_EQUAL = 15;  // pushes 1 or 0, stack(-2) == stack(-1) (with epsilon check)
	public static final int OP_NOT_EQUAL = 16;  // pushes 1 or 0, stack(-2) != stack(-1) (with epsilon check)
	public static final int OP_BEGIN_IF = 17;  // if stack(-1) < 1.0, skips until it finds a OP_END_IF (accounting for nesting)
	public static final int OP_ELSE = 18;  // skips until it goes outside the conditional
	public static final int OP_END_IF = 19;
	public static final int OP_AND = 20;  // pushes 1 if stack(-2) and stack(-1) are greater than 0.0, pops 2 values
	public static final int OP_OR = 21;  // pushes 1 if stack(-2) or stack(-1) are greater than 0.0, pops 2 values
	public static final int OP_GOTO = 22;  // jumps to a certain operation in indexValue
	public static final int OP_CALL = 23;  // saves this operation index and jumps to a certain operation in indexValue
	public static final int OP_RETURN = 24;  // exits the current function
	public static final int OP_FLOOR = 25;  // floor(stack(-1)), pops
	public static final int OP_CEIL = 26;  // ceil(stack(-1)), pops
	public static final int OP_SIN = 27;  // sin(stack(-1)), pops
	public static final int OP_COS = 28;  // cos(stack(-1)), pops
	public static final int OP_TAN = 29;  // tan(stack(-1)), pops
	public static final int OP_ASIN = 30;  // asin(stack(-1)), pops
	public static final int OP_ACOS = 31;  // acos(stack(-1)), pops
	public static final int OP_ATAN = 32;  // atan(stack(-1)), pops
	
	public static class Operation
	{
		public int opCode = OP_NOP;
		public float value = 0f;
		public int indexValue = -1;  // index to string or to another instruction
		public int line;
	}
	public static class Function
	{
		public int stringIndex;
		public int firstOpIndex;
	}
	public final List<Operation> operations = new ArrayList<>();
	public final List<Function> functions = new ArrayList<>();
	public final List<String> strings = new ArrayList<>();
	
	private String getValueOrVariable(Operation op) {
		if (op.indexValue != -1) return strings.get(op.indexValue);
		else return Float.toString(op.value);
	}
	
	public void read(StreamReader stream) throws IOException
	{
		int numDwords = stream.readInt();
		if (stream.length() - stream.getFilePointer() != numDwords * 4)
			throw new IOException("Error: Inconsistent number of dwords in HTRA file");
		
		int magicWord = stream.readInt();
		if (magicWord != 0x48545241)
			throw new IOException("Error: Wrong magic word in HTRA file");
		
		int version = stream.readLEInt();
		if (version != 1)
			throw new IOException("Error: Wrong version in HTRA file");
		
		if (stream.readLEInt() != 2) 
			throw new IOException("Error: Wrong field_8 in HTRA file");
		if (stream.readLEInt() != numDwords) 
			throw new IOException("Error: Wrong field_C in HTRA file");
		
		int count = stream.readLEInt();
		for (int i = 0; i < count; i++) {
			Operation operation = new Operation();
			operation.line = stream.readLEInt();
			operation.value = stream.readLEFloat();
			operation.indexValue = stream.readLEInt();
			operation.opCode = stream.readLEInt();
			operations.add(operation);
		}

		int functionsCount = stream.readLEInt();
		for (int i = 0; i < functionsCount; i++) {
			Function function = new Function();
			function.stringIndex = stream.readLEInt();
			function.firstOpIndex = stream.readLEInt();
			functions.add(function);
		}
		
		int stringsCount = stream.readLEInt();
		if (stream.readLEInt() != stringsCount)
			throw new IOException("Error: Inconsistent strings count");
		
		for (int i = 0; i < stringsCount; i++) {
			strings.add(stream.readCString(StringEncoding.ASCII));
		}
	}
	
	public void write(StreamWriter stream) throws IOException {
		stream.writeInt(0);
		stream.writeInt(0);
		stream.writeInt(0);
		stream.writeInt(0);
		stream.writeInt(0);
		
		stream.writeLEInt(operations.size());
		for (Operation op : operations) {
			stream.writeLEInt(op.line);
			stream.writeLEFloat(op.value);
			stream.writeLEInt(op.indexValue);
			stream.writeLEInt(op.opCode);
		}
		
		stream.writeLEInt(functions.size());
		for (Function function : functions) {
			stream.writeLEInt(function.stringIndex);
			stream.writeLEInt(function.firstOpIndex);
		}
		
		stream.writeLEInt(strings.size());
		stream.writeLEInt(strings.size());
		for (String str : strings) {
			stream.writeCString(str, StringEncoding.ASCII);
		}
		// Make the size multiple of 4
		long offset = stream.getFilePointer();
		stream.writePadding((int) (((offset + 4-1) & ~(4-1)) - offset));
		
		long numDwords = (stream.length() - 4) / 4;
		stream.seek(0);
		stream.writeUInt(numDwords);
		stream.writeInt(0x48545241);
		stream.writeLEInt(1);
		stream.writeLEInt(2);
		stream.writeLEUInt(numDwords);
	}
	
	public void debugPrint() {
		for (Function function : functions) {
			System.out.println("function " + strings.get(function.stringIndex) + " -> " + function.firstOpIndex);
		}
		System.out.println();
		
		int lastLine = 0;
		int i = 0;
		for (Operation operation : operations) {
			if (operation.line != lastLine) {
				System.out.println();
				lastLine = operation.line;
			}
			if (operation.opCode >= 20 && operation.opCode <= 24) {
				System.err.println("Unknown OP CODE " + operation.opCode);
			}
			String str = "[" + lastLine + "] " + i + " OP=" + operation.opCode + "  " + operation.value;
			if (operation.indexValue != -1) {
				str += " (" + strings.get(operation.indexValue) + ") " + operation.indexValue;
			}
			System.out.println(str);
			i++;
		}
	}
	
	public boolean parse(File file) throws IOException {
		ArithmeticaFileGrammarLexer lexer = new ArithmeticaFileGrammarLexer(CharStreams.fromPath(file.toPath()));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ArithmeticaFileGrammarParser parser = new ArithmeticaFileGrammarParser(tokens);
		ParseTree tree = parser.program();
		ParseTreeWalker walker = new ParseTreeWalker();
		ArithmeticaFileListener listener = new ArithmeticaFileListener(this, parser);
		walker.walk(listener, tree);
		
		return parser.getNumberOfSyntaxErrors() == 0;
	}
	
	private static void checkStack(Stack<String> stack, int numValues, Operation op) throws UnsupportedOperationException {
		if (stack.size() < numValues) 
			throw new UnsupportedOperationException("Invalid ARTH: not enough stack for op " + op.opCode);
	}
	
	private static String addParenthesis(String text) {
		if (text.contains(" ") || text.contains("^")) return "(" + text + ")";
		return text;
	}
	
	private void generateText(StringBuilder sb, String indentation, int startIndex) {
		Stack<String> stack = new Stack<>();
		int ifLevel = 0;
		boolean justAddedBlankLine = false;
		boolean justAddedElse = false;
		// Used to keep track of which 'if' blocks are an elseif, as we don't need to remove indentation
		Stack<Boolean> elseifStack = new Stack<>(); 
		boolean lastFinishedIfWasAnElseIf = false;
		
		for (int i = startIndex; i < operations.size(); i++) {
			Operation op = operations.get(i);
			String value1, value2;
			switch (op.opCode) {
			case OP_ADD:
				checkStack(stack, 2, op);
				value1 = stack.pop();
				value2 = stack.pop();
				stack.push(addParenthesis(value2) + " + " + addParenthesis(value1));
				break;
			case OP_SUBTRACT:
				checkStack(stack, 2, op);
				value1 = stack.pop();
				value2 = stack.pop();
				stack.push(addParenthesis(value2) + " - " + addParenthesis(value1));
				break;
			case OP_MULTIPLY:
				checkStack(stack, 2, op);
				value1 = stack.pop();
				value2 = stack.pop();
				// Special case
				if (value1.equals("-1.0")) {
					stack.push("-" + addParenthesis(value2));
				}
				else if (value2.equals("-1.0")) {
					stack.push("-" + addParenthesis(value1));
				}
				else {
					stack.push(addParenthesis(value2) + " * " + addParenthesis(value1));
				}
				break;
			case OP_DIVIDE:
				checkStack(stack, 2, op);
				value1 = stack.pop();
				value2 = stack.pop();
				stack.push(addParenthesis(value2) + " / " + addParenthesis(value1));
				break;
			case OP_POW:
				checkStack(stack, 2, op);
				value1 = stack.pop();
				value2 = stack.pop();
				stack.push(addParenthesis(value2) + "^" + addParenthesis(value1));
				break;
			case OP_PUSH:
			case OP_PUSH_VARIABLE:
				stack.push(getValueOrVariable(op));
				break;
			case OP_POP_INTO_VARIABLE:
			case OP_POP_INTO_VARIABLE2:
				checkStack(stack, 1, op);
				sb.append(indentation);
				sb.append(getValueOrVariable(op));
				sb.append(" = ");
				sb.append(stack.pop());
				sb.append('\n');
				justAddedBlankLine = false;
				justAddedElse = false;
				break;
				
			case OP_RANDOM:
				stack.push("RAND");
				break;
			case OP_GREATER:
				checkStack(stack, 2, op);
				value1 = stack.pop();
				value2 = stack.pop();
				stack.push(addParenthesis(value2) + " > " + addParenthesis(value1));
				break;
			case OP_LESS:
				checkStack(stack, 2, op);
				value1 = stack.pop();
				value2 = stack.pop();
				stack.push(addParenthesis(value2) + " < " + addParenthesis(value1));
				break;
			case OP_GREATER_EQUAL:
				checkStack(stack, 2, op);
				value1 = stack.pop();
				value2 = stack.pop();
				stack.push(addParenthesis(value2) + " >= " + addParenthesis(value1));
				break;
			case OP_LESS_EQUAL:
				checkStack(stack, 2, op);
				value1 = stack.pop();
				value2 = stack.pop();
				stack.push(addParenthesis(value2) + " <= " + addParenthesis(value1));
				break;
			case OP_EQUAL:
				checkStack(stack, 2, op);
				value1 = stack.pop();
				value2 = stack.pop();
				stack.push(addParenthesis(value2) + " ~= " + addParenthesis(value1));
				break;
			case OP_NOT_EQUAL:
				checkStack(stack, 2, op);
				value1 = stack.pop();
				value2 = stack.pop();
				stack.push(addParenthesis(value2) + " != " + addParenthesis(value1));
				break;
			case OP_BEGIN_IF:
				checkStack(stack, 1, op);
				value1 = stack.pop();
				if (justAddedElse) {
					// The last character we added was a \n, remove it
					sb.deleteCharAt(sb.length() - 1);
					sb.append(" if (");
					sb.append(value1);
					sb.append(")\n");
					elseifStack.add(true);
				}
				else {
					if (ifLevel == 0 && !justAddedBlankLine) {
						sb.append('\n');
					}
					sb.append(indentation);
					sb.append("if (");
					sb.append(value1);
					sb.append(")\n");
					indentation += '\t';
					elseifStack.add(false);
				}
				ifLevel += 1;
				justAddedBlankLine = false;
				justAddedElse = false;
				break;
			case OP_ELSE:
				if (ifLevel == 0) 
					throw new UnsupportedOperationException("Invalid ARTH: op OP_ELSE outside a conditional");
				sb.append(indentation.substring(0, indentation.length() - 1));
				sb.append("else\n");
				justAddedElse = true;
				break;
			case OP_END_IF:
				if (ifLevel == 0) 
					throw new UnsupportedOperationException("Invalid ARTH: op OP_END_IF outside a conditional");
				ifLevel--;
				if (!lastFinishedIfWasAnElseIf) {
					indentation = indentation.substring(0, indentation.length() - 1);
				}
				sb.append(indentation);
				sb.append("endif\n");
				if (ifLevel == 0) {
					justAddedBlankLine = true;
					sb.append('\n');
				}
				lastFinishedIfWasAnElseIf = elseifStack.pop();
				justAddedElse = false;
				break;
			case OP_AND:
				checkStack(stack, 2, op);
				value1 = stack.pop();
				value2 = stack.pop();
				stack.push(addParenthesis(value2) + " and " + addParenthesis(value1));
				break;
			case OP_OR:
				checkStack(stack, 2, op);
				value1 = stack.pop();
				value2 = stack.pop();
				stack.push(addParenthesis(value2) + " or " + addParenthesis(value1));
				break;
			case OP_CALL:
				Function calledFunction = null;
				for (Function function : functions) {
					if (function.firstOpIndex == op.indexValue) {
						calledFunction = function;
						break;
					}
				}
				if (calledFunction == null) {
					throw new UnsupportedOperationException("Invalid ARTH: no function with start index " + op.indexValue + " for OP_CALL");
				}
				stack.push(strings.get(calledFunction.stringIndex) + "()");
				break;
			case OP_RETURN:
				//TODO can it return not at the end of the function?
				return;
				
			case OP_FLOOR:
				checkStack(stack, 1, op);
				value1 = stack.pop();
				stack.push("floor(" + value1 + ")");
				break;
			case OP_CEIL:
				checkStack(stack, 1, op);
				value1 = stack.pop();
				stack.push("ceil(" + value1 + ")");
				break;
			case OP_SIN:
				checkStack(stack, 1, op);
				value1 = stack.pop();
				stack.push("sin(" + value1 + ")");
				break;
			case OP_COS:
				checkStack(stack, 1, op);
				value1 = stack.pop();
				stack.push("cos(" + value1 + ")");
				break;
			case OP_TAN:
				checkStack(stack, 1, op);
				value1 = stack.pop();
				stack.push("tan(" + value1 + ")");
				break;
			case OP_ASIN:
				checkStack(stack, 1, op);
				value1 = stack.pop();
				stack.push("asin(" + value1 + ")");
				break;
			case OP_ACOS:
				checkStack(stack, 1, op);
				value1 = stack.pop();
				stack.push("acos(" + value1 + ")");
				break;
			case OP_ATAN:
				checkStack(stack, 1, op);
				value1 = stack.pop();
				stack.push("atan(" + value1 + ")");
				break;
				
			default:
				throw new UnsupportedOperationException("Invalid ARTH: unhandled op " + op.opCode);
			}
		}
	}
	
	public String generateText() {
		StringBuilder sb = new StringBuilder();
		
		if (functions.isEmpty()) {
			generateText(sb, "", 0);
		}
		else {
			for (Function function : functions) {
				sb.append("def ");
				sb.append(strings.get(function.stringIndex));
				sb.append("()\n");
				generateText(sb, "\t", function.firstOpIndex);
				sb.append("enddef\n\n");
			}
		}
		
		return sb.toString();
	}
}
