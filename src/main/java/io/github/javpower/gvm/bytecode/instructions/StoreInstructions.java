package io.github.javpower.gvm.bytecode.instructions;

import io.github.javpower.gvm.bytecode.Instruction;
import io.github.javpower.gvm.runtime.stack.LocalVariableTable;
import io.github.javpower.gvm.runtime.stack.OperandStack;
import io.github.javpower.gvm.runtime.stack.StackFrame;

/**
 * 存储指令集
 */
public class StoreInstructions {
    
    /**
     * 存储整数到局部变量指令 - ISTORE (0x36)
     */
    public static class IStore extends Instruction {
        public IStore() {
            super(0x36, "istore", 2);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 1);
            int index = readUnsignedByte(bytecode, pc + 1);
            OperandStack stack = frame.getOperandStack();
            LocalVariableTable locals = frame.getLocalVariableTable();
            
            int value = stack.popInt();
            locals.setInt(index, value);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 1);
            int index = readUnsignedByte(bytecode, pc + 1);
            return String.format("istore %d", index);
        }
    }
    
    /**
     * 存储整数到局部变量0指令 - ISTORE_0 (0x3B)
     */
    public static class IStore0 extends Instruction {
        public IStore0() {
            super(0x3B, "istore_0", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            LocalVariableTable locals = frame.getLocalVariableTable();
            
            int value = stack.popInt();
            locals.setInt(0, value);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "istore_0";
        }
    }
    
    /**
     * 存储整数到局部变量1指令 - ISTORE_1 (0x3C)
     */
    public static class IStore1 extends Instruction {
        public IStore1() {
            super(0x3C, "istore_1", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            LocalVariableTable locals = frame.getLocalVariableTable();
            
            int value = stack.popInt();
            locals.setInt(1, value);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "istore_1";
        }
    }
    
    /**
     * 存储整数到局部变量2指令 - ISTORE_2 (0x3D)
     */
    public static class IStore2 extends Instruction {
        public IStore2() {
            super(0x3D, "istore_2", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            LocalVariableTable locals = frame.getLocalVariableTable();
            
            int value = stack.popInt();
            locals.setInt(2, value);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "istore_2";
        }
    }
    
    /**
     * 存储整数到局部变量3指令 - ISTORE_3 (0x3E)
     */
    public static class IStore3 extends Instruction {
        public IStore3() {
            super(0x3E, "istore_3", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            LocalVariableTable locals = frame.getLocalVariableTable();
            
            int value = stack.popInt();
            locals.setInt(3, value);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "istore_3";
        }
    }
    
    /**
     * 存储整数到局部变量4指令 - ISTORE 4 (0x36 0x04)
     */
    public static class IStore4 extends Instruction {
        public IStore4() {
            super(0x36, "istore_4", 2); // 这个实际上是通过istore指令实现的
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            LocalVariableTable locals = frame.getLocalVariableTable();
            
            int value = stack.popInt();
            locals.setInt(4, value);
            
            return pc + 2; // istore指令长度为2
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "istore 4";
        }
    }
} 