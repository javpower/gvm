package io.github.javpower.gvm.bytecode.instructions;

import io.github.javpower.gvm.bytecode.Instruction;
import io.github.javpower.gvm.runtime.stack.LocalVariableTable;
import io.github.javpower.gvm.runtime.stack.OperandStack;
import io.github.javpower.gvm.runtime.stack.StackFrame;

/**
 * 常量和加载指令集
 */
public class ConstantInstructions {
    
    /**
     * 空引用常量指令 - ACONST_NULL (0x01)
     */
    public static class AConstNull extends Instruction {
        public AConstNull() {
            super(0x01, "aconst_null", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushReference(null);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "aconst_null";
        }
    }
    
    /**
     * 整数常量-1指令 - ICONST_M1 (0x02)
     */
    public static class IConstM1 extends Instruction {
        public IConstM1() {
            super(0x02, "iconst_m1", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushInt(-1);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "iconst_m1";
        }
    }
    
    /**
     * 整数常量0指令 - ICONST_0 (0x03)
     */
    public static class IConst0 extends Instruction {
        public IConst0() {
            super(0x03, "iconst_0", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushInt(0);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "iconst_0";
        }
    }
    
    /**
     * 整数常量1指令 - ICONST_1 (0x04)
     */
    public static class IConst1 extends Instruction {
        public IConst1() {
            super(0x04, "iconst_1", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushInt(1);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "iconst_1";
        }
    }
    
    /**
     * 整数常量2指令 - ICONST_2 (0x05)
     */
    public static class IConst2 extends Instruction {
        public IConst2() {
            super(0x05, "iconst_2", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushInt(2);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "iconst_2";
        }
    }
    
    /**
     * 整数常量3指令 - ICONST_3 (0x06)
     */
    public static class IConst3 extends Instruction {
        public IConst3() {
            super(0x06, "iconst_3", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushInt(3);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "iconst_3";
        }
    }
    
    /**
     * 整数常量4指令 - ICONST_4 (0x07)
     */
    public static class IConst4 extends Instruction {
        public IConst4() {
            super(0x07, "iconst_4", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushInt(4);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "iconst_4";
        }
    }
    
    /**
     * 整数常量5指令 - ICONST_5 (0x08)
     */
    public static class IConst5 extends Instruction {
        public IConst5() {
            super(0x08, "iconst_5", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushInt(5);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "iconst_5";
        }
    }
    
    /**
     * 长整数常量0指令 - LCONST_0 (0x09)
     */
    public static class LConst0 extends Instruction {
        public LConst0() {
            super(0x09, "lconst_0", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushLong(0L);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "lconst_0";
        }
    }
    
    /**
     * 长整数常量1指令 - LCONST_1 (0x0A)
     */
    public static class LConst1 extends Instruction {
        public LConst1() {
            super(0x0A, "lconst_1", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushLong(1L);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "lconst_1";
        }
    }
    
    /**
     * 浮点数常量0指令 - FCONST_0 (0x0B)
     */
    public static class FConst0 extends Instruction {
        public FConst0() {
            super(0x0B, "fconst_0", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushFloat(0.0f);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "fconst_0";
        }
    }
    
    /**
     * 浮点数常量1指令 - FCONST_1 (0x0C)
     */
    public static class FConst1 extends Instruction {
        public FConst1() {
            super(0x0C, "fconst_1", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushFloat(1.0f);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "fconst_1";
        }
    }
    
    /**
     * 双精度浮点数常量0指令 - DCONST_0 (0x0E)
     */
    public static class DConst0 extends Instruction {
        public DConst0() {
            super(0x0E, "dconst_0", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushDouble(0.0);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "dconst_0";
        }
    }
    
    /**
     * 双精度浮点数常量1指令 - DCONST_1 (0x0F)
     */
    public static class DConst1 extends Instruction {
        public DConst1() {
            super(0x0F, "dconst_1", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            frame.getOperandStack().pushDouble(1.0);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "dconst_1";
        }
    }
    
    /**
     * 推送字节常量指令 - BIPUSH (0x10)
     */
    public static class BiPush extends Instruction {
        public BiPush() {
            super(0x10, "bipush", 2);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 1);
            byte value = readByte(bytecode, pc + 1);
            frame.getOperandStack().pushInt(value);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 1);
            byte value = readByte(bytecode, pc + 1);
            return String.format("bipush %d", value);
        }
    }
    
    /**
     * 推送短整数常量指令 - SIPUSH (0x11)
     */
    public static class SiPush extends Instruction {
        public SiPush() {
            super(0x11, "sipush", 3);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 2);
            short value = readShort(bytecode, pc + 1);
            frame.getOperandStack().pushInt(value);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 2);
            short value = readShort(bytecode, pc + 1);
            return String.format("sipush %d", value);
        }
    }
    
    /**
     * 从局部变量表加载整数指令 - ILOAD (0x15)
     */
    public static class ILoad extends Instruction {
        public ILoad() {
            super(0x15, "iload", 2);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 1);
            int index = readUnsignedByte(bytecode, pc + 1);
            LocalVariableTable locals = frame.getLocalVariableTable();
            int value = locals.getInt(index);
            frame.getOperandStack().pushInt(value);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 1);
            int index = readUnsignedByte(bytecode, pc + 1);
            return String.format("iload %d", index);
        }
    }
    
    /**
     * 从局部变量表加载整数指令0 - ILOAD_0 (0x1A)
     */
    public static class ILoad0 extends Instruction {
        public ILoad0() {
            super(0x1A, "iload_0", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            LocalVariableTable locals = frame.getLocalVariableTable();
            int value = locals.getInt(0);
            frame.getOperandStack().pushInt(value);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "iload_0";
        }
    }
    
    /**
     * 从局部变量表加载整数指令1 - ILOAD_1 (0x1B)
     */
    public static class ILoad1 extends Instruction {
        public ILoad1() {
            super(0x1B, "iload_1", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            LocalVariableTable locals = frame.getLocalVariableTable();
            int value = locals.getInt(1);
            frame.getOperandStack().pushInt(value);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "iload_1";
        }
    }
    
    /**
     * 从局部变量表加载整数指令2 - ILOAD_2 (0x1C)
     */
    public static class ILoad2 extends Instruction {
        public ILoad2() {
            super(0x1C, "iload_2", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            LocalVariableTable locals = frame.getLocalVariableTable();
            int value = locals.getInt(2);
            frame.getOperandStack().pushInt(value);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "iload_2";
        }
    }
    
    /**
     * 从局部变量表加载整数指令3 - ILOAD_3 (0x1D)
     */
    public static class ILoad3 extends Instruction {
        public ILoad3() {
            super(0x1D, "iload_3", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            LocalVariableTable locals = frame.getLocalVariableTable();
            int value = locals.getInt(3);
            frame.getOperandStack().pushInt(value);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "iload_3";
        }
    }
} 