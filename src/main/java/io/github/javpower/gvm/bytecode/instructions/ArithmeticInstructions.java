package io.github.javpower.gvm.bytecode.instructions;

import io.github.javpower.gvm.bytecode.Instruction;
import io.github.javpower.gvm.runtime.stack.OperandStack;
import io.github.javpower.gvm.runtime.stack.StackFrame;

/**
 * 算术运算指令集
 */
public class ArithmeticInstructions {
    
    /**
     * 整数加法指令 - IADD (0x60)
     */
    public static class IAdd extends Instruction {
        public IAdd() {
            super(0x60, "iadd", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            int value2 = stack.popInt();
            int value1 = stack.popInt();
            int result = value1 + value2;
            stack.pushInt(result);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "iadd";
        }
    }
    
    /**
     * 整数减法指令 - ISUB (0x64)
     */
    public static class ISub extends Instruction {
        public ISub() {
            super(0x64, "isub", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            int value2 = stack.popInt();
            int value1 = stack.popInt();
            int result = value1 - value2;
            stack.pushInt(result);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "isub";
        }
    }
    
    /**
     * 整数乘法指令 - IMUL (0x68)
     */
    public static class IMul extends Instruction {
        public IMul() {
            super(0x68, "imul", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            int value2 = stack.popInt();
            int value1 = stack.popInt();
            int result = value1 * value2;
            stack.pushInt(result);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "imul";
        }
    }
    
    /**
     * 整数除法指令 - IDIV (0x6C)
     */
    public static class IDiv extends Instruction {
        public IDiv() {
            super(0x6C, "idiv", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            int value2 = stack.popInt();
            int value1 = stack.popInt();
            
            if (value2 == 0) {
                throw new ArithmeticException("Division by zero");
            }
            
            int result = value1 / value2;
            stack.pushInt(result);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "idiv";
        }
    }
    
    /**
     * 整数取模指令 - IREM (0x70)
     */
    public static class IRem extends Instruction {
        public IRem() {
            super(0x70, "irem", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            int value2 = stack.popInt();
            int value1 = stack.popInt();
            
            if (value2 == 0) {
                throw new ArithmeticException("Division by zero");
            }
            
            int result = value1 % value2;
            stack.pushInt(result);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "irem";
        }
    }
    
    /**
     * 整数取负指令 - INEG (0x74)
     */
    public static class INeg extends Instruction {
        public INeg() {
            super(0x74, "ineg", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            int value = stack.popInt();
            int result = -value;
            stack.pushInt(result);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "ineg";
        }
    }
    
    /**
     * 长整数加法指令 - LADD (0x61)
     */
    public static class LAdd extends Instruction {
        public LAdd() {
            super(0x61, "ladd", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            long value2 = stack.popLong();
            long value1 = stack.popLong();
            long result = value1 + value2;
            stack.pushLong(result);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "ladd";
        }
    }
    
    /**
     * 长整数减法指令 - LSUB (0x65)
     */
    public static class LSub extends Instruction {
        public LSub() {
            super(0x65, "lsub", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            long value2 = stack.popLong();
            long value1 = stack.popLong();
            long result = value1 - value2;
            stack.pushLong(result);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "lsub";
        }
    }
    
    /**
     * 浮点数加法指令 - FADD (0x62)
     */
    public static class FAdd extends Instruction {
        public FAdd() {
            super(0x62, "fadd", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            float value2 = stack.popFloat();
            float value1 = stack.popFloat();
            float result = value1 + value2;
            stack.pushFloat(result);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "fadd";
        }
    }
    
    /**
     * 浮点数减法指令 - FSUB (0x66)
     */
    public static class FSub extends Instruction {
        public FSub() {
            super(0x66, "fsub", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            float value2 = stack.popFloat();
            float value1 = stack.popFloat();
            float result = value1 - value2;
            stack.pushFloat(result);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "fsub";
        }
    }
    
    /**
     * 双精度浮点数加法指令 - DADD (0x63)
     */
    public static class DAdd extends Instruction {
        public DAdd() {
            super(0x63, "dadd", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            double value2 = stack.popDouble();
            double value1 = stack.popDouble();
            double result = value1 + value2;
            stack.pushDouble(result);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "dadd";
        }
    }
    
    /**
     * 双精度浮点数减法指令 - DSUB (0x67)
     */
    public static class DSub extends Instruction {
        public DSub() {
            super(0x67, "dsub", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            OperandStack stack = frame.getOperandStack();
            double value2 = stack.popDouble();
            double value1 = stack.popDouble();
            double result = value1 - value2;
            stack.pushDouble(result);
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "dsub";
        }
    }
} 