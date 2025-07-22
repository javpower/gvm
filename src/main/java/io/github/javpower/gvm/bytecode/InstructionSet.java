package io.github.javpower.gvm.bytecode;

import io.github.javpower.gvm.bytecode.instructions.ArithmeticInstructions;
import io.github.javpower.gvm.bytecode.instructions.ConstantInstructions;
import io.github.javpower.gvm.bytecode.instructions.StoreInstructions;
import io.github.javpower.gvm.bytecode.instructions.ReturnInstructions;
import io.github.javpower.gvm.bytecode.instructions.InvokeInstructions;
import io.github.javpower.gvm.bytecode.instructions.FieldInstructions;
import io.github.javpower.gvm.bytecode.instructions.ObjectInstructions;
import io.github.javpower.gvm.bytecode.instructions.LoadStoreInstructions;

import java.util.HashMap;
import java.util.Map;

/**
 * 指令集管理器 - 负责指令的注册、解码和管理
 */
public class InstructionSet {
    
    // 指令映射表：操作码 -> 指令实例
    private static final Map<Integer, Instruction> INSTRUCTIONS = new HashMap<>();
    
    // 指令名称映射表：名称 -> 指令实例
    private static final Map<String, Instruction> INSTRUCTION_NAMES = new HashMap<>();
    
    // 静态初始化所有支持的指令
    static {
        registerInstructions();
    }
    
    /**
     * 注册所有指令
     */
    private static void registerInstructions() {
        // 常量指令
        register(new ConstantInstructions.AConstNull());
        register(new ConstantInstructions.IConstM1());
        register(new ConstantInstructions.IConst0());
        register(new ConstantInstructions.IConst1());
        register(new ConstantInstructions.IConst2());
        register(new ConstantInstructions.IConst3());
        register(new ConstantInstructions.IConst4());
        register(new ConstantInstructions.IConst5());
        register(new ConstantInstructions.LConst0());
        register(new ConstantInstructions.LConst1());
        register(new ConstantInstructions.FConst0());
        register(new ConstantInstructions.FConst1());
        register(new ConstantInstructions.DConst0());
        register(new ConstantInstructions.DConst1());
        register(new ConstantInstructions.BiPush());
        register(new ConstantInstructions.SiPush());
        register(new ConstantInstructions.ILoad());
        register(new ConstantInstructions.ILoad0());
        register(new ConstantInstructions.ILoad1());
        register(new ConstantInstructions.ILoad2());
        register(new ConstantInstructions.ILoad3());
        
        // 存储指令
        register(new StoreInstructions.IStore());
        register(new StoreInstructions.IStore0());
        register(new StoreInstructions.IStore1());
        register(new StoreInstructions.IStore2());
        register(new StoreInstructions.IStore3());
        
        // 引用加载和存储指令
        register(new LoadStoreInstructions.ALoad());
        register(new LoadStoreInstructions.ALoad0());
        register(new LoadStoreInstructions.ALoad1());
        register(new LoadStoreInstructions.AStore());
        register(new LoadStoreInstructions.AStore0());
        register(new LoadStoreInstructions.AStore1());
        register(new LoadStoreInstructions.AStore2());
        register(new LoadStoreInstructions.AStore3());
        
        // 返回指令
        register(new ReturnInstructions.Return());
        register(new ReturnInstructions.IReturn());
        register(new ReturnInstructions.LReturn());
        register(new ReturnInstructions.FReturn());
        register(new ReturnInstructions.DReturn());
        register(new ReturnInstructions.AReturn());
        
        // 方法调用指令
        register(new InvokeInstructions.InvokeStatic());
        
        // 字段访问指令
        register(new FieldInstructions.GetStatic());
        register(new FieldInstructions.PutStatic());
        register(new FieldInstructions.GetField());
        register(new FieldInstructions.PutField());
        
        // 对象操作指令
        register(new ObjectInstructions.New());
        register(new ObjectInstructions.Dup());
        register(new ObjectInstructions.InvokeSpecial());
        register(new ObjectInstructions.InvokeVirtual());
        register(new ObjectInstructions.Ldc());
        
        // 算术指令
        register(new ArithmeticInstructions.IAdd());
        register(new ArithmeticInstructions.ISub());
        register(new ArithmeticInstructions.IMul());
        register(new ArithmeticInstructions.IDiv());
        register(new ArithmeticInstructions.IRem());
        register(new ArithmeticInstructions.INeg());
        register(new ArithmeticInstructions.LAdd());
        register(new ArithmeticInstructions.LSub());
        register(new ArithmeticInstructions.FAdd());
        register(new ArithmeticInstructions.FSub());
        register(new ArithmeticInstructions.DAdd());
        register(new ArithmeticInstructions.DSub());
        
        // TODO: 添加更多指令类型
        // - 存储指令 (istore, lstore, fstore, dstore, astore)
        // - 栈操作指令 (pop, dup, swap)
        // - 类型转换指令 (i2l, i2f, i2d, l2i, f2i, d2i)
        // - 比较指令 (lcmp, fcmpl, fcmpg, dcmpl, dcmpg)
        // - 条件跳转指令 (ifeq, ifne, iflt, ifge, ifgt, ifle)
        // - 比较跳转指令 (if_icmpeq, if_icmpne, if_icmplt, if_icmpge, if_icmpgt, if_icmple)
        // - 无条件跳转指令 (goto, jsr, ret)
        // - 表跳转指令 (tableswitch, lookupswitch)
        // - 方法调用指令 (invokevirtual, invokespecial, invokestatic, invokeinterface, invokedynamic)
        // - 方法返回指令 (ireturn, lreturn, freturn, dreturn, areturn, return)
        // - 对象操作指令 (new, newarray, anewarray, arraylength)
        // - 字段访问指令 (getfield, putfield, getstatic, putstatic)
        // - 数组访问指令 (iaload, laload, faload, daload, aaload, baload, caload, saload)
        // - 数组存储指令 (iastore, lastore, fastore, dastore, aastore, bastore, castore, sastore)
        // - 异常处理指令 (athrow)
        // - 类型检查指令 (instanceof, checkcast)
        // - 同步指令 (monitorenter, monitorexit)
    }
    
    /**
     * 注册单个指令
     */
    private static void register(Instruction instruction) {
        INSTRUCTIONS.put(instruction.getOpcode(), instruction);
        INSTRUCTION_NAMES.put(instruction.getName(), instruction);
    }
    
    /**
     * 根据操作码获取指令
     */
    public static Instruction getInstruction(int opcode) {
        return INSTRUCTIONS.get(opcode);
    }
    
    /**
     * 根据名称获取指令
     */
    public static Instruction getInstruction(String name) {
        return INSTRUCTION_NAMES.get(name);
    }
    
    /**
     * 解码字节码指令
     */
    public static Instruction decode(byte[] bytecode, int pc) {
        if (pc >= bytecode.length) {
            throw new IndexOutOfBoundsException("PC out of bounds: " + pc);
        }
        
        int opcode = bytecode[pc] & 0xFF;
        return getInstruction(opcode);
    }
    
    /**
     * 检查指令是否存在
     */
    public static boolean hasInstruction(int opcode) {
        return INSTRUCTIONS.containsKey(opcode);
    }
    
    /**
     * 检查指令是否存在
     */
    public static boolean hasInstruction(String name) {
        return INSTRUCTION_NAMES.containsKey(name);
    }
    
    /**
     * 获取所有支持的操作码
     */
    public static int[] getSupportedOpcodes() {
        return INSTRUCTIONS.keySet().stream().mapToInt(Integer::intValue).sorted().toArray();
    }
    
    /**
     * 获取所有支持的指令名称
     */
    public static String[] getSupportedInstructionNames() {
        return INSTRUCTION_NAMES.keySet().toArray(new String[0]);
    }
    
    /**
     * 获取指令数量
     */
    public static int getInstructionCount() {
        return INSTRUCTIONS.size();
    }
    
    /**
     * 反汇编字节码 - 将字节码转换为可读的指令序列
     */
    public static String disassemble(byte[] bytecode) {
        StringBuilder sb = new StringBuilder();
        int pc = 0;
        
        while (pc < bytecode.length) {
            try {
                Instruction instruction = decode(bytecode, pc);
                if (instruction != null) {
                    sb.append(String.format("%4d: %s\n", pc, instruction.toString(bytecode, pc)));
                    pc += instruction.getLength();
                } else {
                    int opcode = bytecode[pc] & 0xFF;
                    sb.append(String.format("%4d: unknown (0x%02X)\n", pc, opcode));
                    pc++;
                }
            } catch (Exception e) {
                int opcode = pc < bytecode.length ? (bytecode[pc] & 0xFF) : -1;
                sb.append(String.format("%4d: error (0x%02X) - %s\n", pc, opcode, e.getMessage()));
                pc++;
            }
        }
        
        return sb.toString();
    }
    
    /**
     * 验证字节码 - 检查字节码是否有效
     */
    public static boolean validate(byte[] bytecode) {
        try {
            int pc = 0;
            while (pc < bytecode.length) {
                Instruction instruction = decode(bytecode, pc);
                if (instruction == null) {
                    return false;
                }
                
                // 检查指令长度是否超出字节码边界
                if (pc + instruction.getLength() > bytecode.length) {
                    return false;
                }
                
                pc += instruction.getLength();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取指令集统计信息
     */
    public static String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Instruction Set Statistics ===\n");
        sb.append("Total Instructions: ").append(getInstructionCount()).append("\n");
        
        // 按类型分组统计
        Map<String, Integer> typeCount = new HashMap<>();
        for (Instruction instruction : INSTRUCTIONS.values()) {
            String type = getInstructionType(instruction);
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }
        
        sb.append("\nInstructions by Type:\n");
        for (Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        sb.append("\nSupported Opcodes: ");
        int[] opcodes = getSupportedOpcodes();
        for (int i = 0; i < Math.min(opcodes.length, 10); i++) {
            if (i > 0) sb.append(", ");
            sb.append(String.format("0x%02X", opcodes[i]));
        }
        if (opcodes.length > 10) {
            sb.append(", ... (").append(opcodes.length - 10).append(" more)");
        }
        sb.append("\n");
        
        sb.append("===============================\n");
        return sb.toString();
    }
    
    /**
     * 获取指令类型（用于统计）
     */
    private static String getInstructionType(Instruction instruction) {
        String className = instruction.getClass().getSimpleName();
        
        if (className.startsWith("IConst") || className.startsWith("LConst") || 
            className.startsWith("FConst") || className.startsWith("DConst") ||
            className.equals("AConstNull") || className.equals("BiPush") || className.equals("SiPush")) {
            return "Constant";
        } else if (className.startsWith("ILoad") || className.startsWith("LLoad") ||
                  className.startsWith("FLoad") || className.startsWith("DLoad") ||
                  className.startsWith("ALoad")) {
            return "Load";
        } else if (className.endsWith("Add") || className.endsWith("Sub") ||
                  className.endsWith("Mul") || className.endsWith("Div") ||
                  className.endsWith("Rem") || className.endsWith("Neg")) {
            return "Arithmetic";
        } else {
            return "Other";
        }
    }
} 