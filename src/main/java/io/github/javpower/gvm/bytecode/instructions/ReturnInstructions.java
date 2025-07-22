package io.github.javpower.gvm.bytecode.instructions;

import io.github.javpower.gvm.bytecode.Instruction;
import io.github.javpower.gvm.runtime.stack.OperandStack;
import io.github.javpower.gvm.runtime.stack.StackFrame;

/**
 * 返回指令集
 */
public class ReturnInstructions {
    
    /**
     * 返回void指令 - RETURN (0xB1)
     */
    public static class Return extends Instruction {
        public Return() {
            super(0xB1, "return", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            // return指令不需要特殊处理，执行引擎会检测到这是返回指令
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "return";
        }
    }
    
    /**
     * 返回整数指令 - IRETURN (0xAC)
     */
    public static class IReturn extends Instruction {
        public IReturn() {
            super(0xAC, "ireturn", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            // ireturn指令不需要特殊处理，执行引擎会检测到这是返回指令并获取返回值
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "ireturn";
        }
    }
    
    /**
     * 返回长整数指令 - LRETURN (0xAD)
     */
    public static class LReturn extends Instruction {
        public LReturn() {
            super(0xAD, "lreturn", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "lreturn";
        }
    }
    
    /**
     * 返回浮点数指令 - FRETURN (0xAE)
     */
    public static class FReturn extends Instruction {
        public FReturn() {
            super(0xAE, "freturn", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "freturn";
        }
    }
    
    /**
     * 返回双精度浮点数指令 - DRETURN (0xAF)
     */
    public static class DReturn extends Instruction {
        public DReturn() {
            super(0xAF, "dreturn", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "dreturn";
        }
    }
    
    /**
     * 返回引用指令 - ARETURN (0xB0)
     */
    public static class AReturn extends Instruction {
        public AReturn() {
            super(0xB0, "areturn", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "areturn";
        }
    }
} 