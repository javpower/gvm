package io.github.javpower.gvm.bytecode.instructions;

import io.github.javpower.gvm.bytecode.Instruction;
import io.github.javpower.gvm.runtime.stack.StackFrame;
import io.github.javpower.gvm.utils.Logger;

/**
 * 加载和存储指令集
 */
public class LoadStoreInstructions {
    
    /**
     * 加载对象引用 - ALOAD (0x19)
     */
    public static class ALoad extends Instruction {
        public ALoad() {
            super(0x19, "aload", 2);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 1);
            int index = readUnsignedByte(bytecode, pc + 1);
            
            Object value = frame.getLocalVariableTable().getSlot(index);
            frame.getOperandStack().pushReference(value);
            
            Logger.trace("aload: 从局部变量表[%d]加载引用 %s", index, value);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 1);
            int index = readUnsignedByte(bytecode, pc + 1);
            return String.format("aload %d", index);
        }
    }
    
    /**
     * 加载对象引用0 - ALOAD_0 (0x2A)
     */
    public static class ALoad0 extends Instruction {
        public ALoad0() {
            super(0x2A, "aload_0", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            Object value = frame.getLocalVariableTable().getSlot(0);
            frame.getOperandStack().pushReference(value);
            
            Logger.trace("aload_0: 从局部变量表[0]加载引用 %s", value);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "aload_0";
        }
    }
    
    /**
     * 加载对象引用1 - ALOAD_1 (0x2B)
     */
    public static class ALoad1 extends Instruction {
        public ALoad1() {
            super(0x2B, "aload_1", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            Object value = frame.getLocalVariableTable().getSlot(1);
            frame.getOperandStack().pushReference(value);
            
            Logger.trace("aload_1: 从局部变量表[1]加载引用 %s", value);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "aload_1";
        }
    }
    
    /**
     * 存储对象引用 - ASTORE (0x3A)
     */
    public static class AStore extends Instruction {
        public AStore() {
            super(0x3A, "astore", 2);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 1);
            int index = readUnsignedByte(bytecode, pc + 1);
            
            Object value = frame.getOperandStack().popReference();
            frame.getLocalVariableTable().setReference(index, value);
            
            Logger.trace("astore: 将引用 %s 存储到局部变量表[%d]", value, index);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 1);
            int index = readUnsignedByte(bytecode, pc + 1);
            return String.format("astore %d", index);
        }
    }
    
    /**
     * 存储对象引用0 - ASTORE_0 (0x4B)
     */
    public static class AStore0 extends Instruction {
        public AStore0() {
            super(0x4B, "astore_0", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            Object value = frame.getOperandStack().popReference();
            frame.getLocalVariableTable().setReference(0, value);
            
            Logger.trace("astore_0: 将引用 %s 存储到局部变量表[0]", value);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "astore_0";
        }
    }
    
    /**
     * 存储对象引用1 - ASTORE_1 (0x4C)
     */
    public static class AStore1 extends Instruction {
        public AStore1() {
            super(0x4C, "astore_1", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            Object value = frame.getOperandStack().popReference();
            frame.getLocalVariableTable().setReference(1, value);
            
            Logger.trace("astore_1: 将引用 %s 存储到局部变量表[1]", value);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "astore_1";
        }
    }
    
    /**
     * 存储对象引用2 - ASTORE_2 (0x4D)
     */
    public static class AStore2 extends Instruction {
        public AStore2() {
            super(0x4D, "astore_2", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            Object value = frame.getOperandStack().popReference();
            frame.getLocalVariableTable().setReference(2, value);
            
            Logger.trace("astore_2: 将引用 %s 存储到局部变量表[2]", value);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "astore_2";
        }
    }
    
    /**
     * 存储对象引用3 - ASTORE_3 (0x4E)
     */
    public static class AStore3 extends Instruction {
        public AStore3() {
            super(0x4E, "astore_3", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            Object value = frame.getOperandStack().popReference();
            frame.getLocalVariableTable().setReference(3, value);
            
            Logger.trace("astore_3: 将引用 %s 存储到局部变量表[3]", value);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "astore_3";
        }
    }
} 