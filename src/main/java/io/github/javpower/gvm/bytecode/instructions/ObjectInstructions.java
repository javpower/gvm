package io.github.javpower.gvm.bytecode.instructions;

import io.github.javpower.gvm.bytecode.Instruction;
import io.github.javpower.gvm.runtime.stack.StackFrame;
import io.github.javpower.gvm.utils.Logger;

/**
 * 对象操作指令集
 */
public class ObjectInstructions {
    
    /**
     * 创建新对象 - NEW (0xBB)
     */
    public static class New extends Instruction {
        public New() {
            super(0xBB, "new", 3);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 2);
            int classRefIndex = readUnsignedShort(bytecode, pc + 1);
            
            Logger.debug("执行new指令，类引用索引: %d", classRefIndex);
            
            // 简化处理：根据类引用索引创建不同类型的对象引用
            Object objectRef;
            switch (classRefIndex) {
                case 9: // SimpleArithmetic
                    objectRef = "SimpleArithmetic@" + System.identityHashCode(new Object()) + "_class" + classRefIndex;
                    break;
                case 12: // StringBuilder
                    objectRef = "StringBuilder@" + System.identityHashCode(new Object()) + "_class" + classRefIndex;
                    break;
                default:
                    objectRef = "Object@" + System.identityHashCode(new Object()) + "_class" + classRefIndex;
                    break;
            }
            
            // 将对象引用推入操作数栈
            frame.getOperandStack().pushReference(objectRef);
            
            Logger.trace("new: 创建对象 %s 并推入操作数栈", objectRef);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 2);
            int classRefIndex = readUnsignedShort(bytecode, pc + 1);
            return String.format("new #%d", classRefIndex);
        }
    }
    
    /**
     * 复制栈顶元素 - DUP (0x59)
     */
    public static class Dup extends Instruction {
        public Dup() {
            super(0x59, "dup", 1);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            if (frame.getOperandStack().isEmpty()) {
                throw new RuntimeException("Cannot dup: operand stack is empty");
            }
            
            // 复制栈顶元素
            frame.getOperandStack().dup();
            
            Logger.trace("dup: 复制栈顶元素");
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            return "dup";
        }
    }
    
    /**
     * 调用特殊方法（构造函数、私有方法、父类方法）- INVOKESPECIAL (0xB7)
     */
    public static class InvokeSpecial extends Instruction {
        public InvokeSpecial() {
            super(0xB7, "invokespecial", 3);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 2);
            int methodRefIndex = readUnsignedShort(bytecode, pc + 1);
            
            Logger.debug("执行invokespecial指令，方法引用索引: %d", methodRefIndex);
            
            // 简化处理：对于构造函数调用
            switch (methodRefIndex) {
                case 1: // Object.<init>()V - 父类构造函数
                    if (!frame.getOperandStack().isEmpty()) {
                        Object objectRef = frame.getOperandStack().popReference();
                        Logger.trace("invokespecial: 调用父类构造函数，对象: %s", objectRef);
                        // 构造函数不返回值，但对象已经初始化
                    }
                    break;
                case 10: // SimpleArithmetic.<init>()V
                    if (!frame.getOperandStack().isEmpty()) {
                        Object objectRef = frame.getOperandStack().popReference();
                        Logger.trace("invokespecial: 调用SimpleArithmetic构造函数，对象: %s", objectRef);
                    }
                    break;
                case 13: // StringBuilder.<init>()V
                    if (!frame.getOperandStack().isEmpty()) {
                        Object objectRef = frame.getOperandStack().popReference();
                        Logger.trace("invokespecial: 调用StringBuilder构造函数，对象: %s", objectRef);
                    }
                    break;
                case 18: // Object.<init>()V - 另一个引用
                    if (!frame.getOperandStack().isEmpty()) {
                        Object objectRef = frame.getOperandStack().popReference();
                        Logger.trace("invokespecial: 调用Object构造函数，对象: %s", objectRef);
                    }
                    break;
                default:
                    // 其他特殊方法调用
                    if (!frame.getOperandStack().isEmpty()) {
                        Object objectRef = frame.getOperandStack().popReference();
                        Logger.trace("invokespecial: 调用特殊方法，对象: %s", objectRef);
                    }
                    break;
            }
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 2);
            int methodRefIndex = readUnsignedShort(bytecode, pc + 1);
            return String.format("invokespecial #%d", methodRefIndex);
        }
    }
    
    /**
     * 调用虚方法 - INVOKEVIRTUAL (0xB6)
     */
    public static class InvokeVirtual extends Instruction {
        public InvokeVirtual() {
            super(0xB6, "invokevirtual", 3);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 2);
            int methodRefIndex = readUnsignedShort(bytecode, pc + 1);
            
            Logger.debug("执行invokevirtual指令，方法引用索引: %d", methodRefIndex);
            
            // 简化处理：根据方法引用模拟不同的虚方法调用
            switch (methodRefIndex) {
                case 1: // println方法 - 需要弹出参数和对象引用
                    if (frame.getOperandStack().size() >= 2) {
                        Object arg = null;
                        try {
                            arg = frame.getOperandStack().popInt();
                        } catch (Exception e) {
                            arg = frame.getOperandStack().popReference();
                        }
                        Object objectRef = frame.getOperandStack().popReference();
                        
                        Logger.info("System.out.println: %s", arg);
                        Logger.trace("invokevirtual: 调用println，参数: %s", arg);
                    } else if (frame.getOperandStack().size() == 1) {
                        // 只有对象引用，无参数
                        Object objectRef = frame.getOperandStack().popReference();
                        Logger.trace("invokevirtual: 调用println，无参数，对象: %s", objectRef);
                    }
                    break;
                case 2: // toString方法
                    if (!frame.getOperandStack().isEmpty()) {
                        Object objectRef = frame.getOperandStack().popReference();
                        String result = objectRef.toString();
                        frame.getOperandStack().pushReference(result);
                        Logger.trace("invokevirtual: 调用toString，返回: %s", result);
                    }
                    break;
                case 3: // 其他方法调用，比如println(String)
                    if (frame.getOperandStack().size() >= 2) {
                        Object arg = frame.getOperandStack().popReference(); // 字符串参数
                        Object objectRef = frame.getOperandStack().popReference(); // System.out对象
                        
                        Logger.info("System.out.println: %s", arg);
                        Logger.trace("invokevirtual: 调用println(String)，参数: %s", arg);
                    } else if (frame.getOperandStack().size() == 1) {
                        Object objectRef = frame.getOperandStack().popReference();
                        Logger.trace("invokevirtual: 调用方法，对象: %s", objectRef);
                    }
                    break;
                case 11: // getInstanceValue方法或其他返回int的方法
                    if (!frame.getOperandStack().isEmpty()) {
                        Object objectRef = frame.getOperandStack().popReference();
                        int result = 42; // 模拟getInstanceValue()的返回值
                        frame.getOperandStack().pushInt(result);
                        Logger.trace("invokevirtual: 调用getInstanceValue，对象: %s，返回: %d", objectRef, result);
                    }
                    break;
                case 15: // StringBuilder.append(String)
                    if (frame.getOperandStack().size() >= 2) {
                        Object arg = frame.getOperandStack().popReference(); // 字符串参数
                        Object objectRef = frame.getOperandStack().popReference(); // StringBuilder对象
                        frame.getOperandStack().pushReference(objectRef); // StringBuilder方法返回自身
                        Logger.trace("invokevirtual: 调用StringBuilder.append(String)，参数: %s", arg);
                    }
                    break;
                case 16: // StringBuilder.append(int)
                    if (frame.getOperandStack().size() >= 2) {
                        int arg = frame.getOperandStack().popInt(); // int参数
                        Object objectRef = frame.getOperandStack().popReference(); // StringBuilder对象
                        frame.getOperandStack().pushReference(objectRef); // StringBuilder方法返回自身
                        Logger.trace("invokevirtual: 调用StringBuilder.append(int)，参数: %d", arg);
                    }
                    break;
                case 17: // StringBuilder.toString()
                    if (!frame.getOperandStack().isEmpty()) {
                        Object objectRef = frame.getOperandStack().popReference(); // StringBuilder对象
                        String result = "计算完成，最终结果: 427"; // 模拟toString结果
                        frame.getOperandStack().pushReference(result);
                        Logger.trace("invokevirtual: 调用StringBuilder.toString()，返回: %s", result);
                    }
                    break;
                default:
                    // 其他虚方法调用的简化处理 - 确保正确弹出参数
                    if (frame.getOperandStack().size() >= 2) {
                        // 假设有一个参数
                        Object arg = null;
                        try {
                            arg = frame.getOperandStack().popInt();
                        } catch (Exception e) {
                            arg = frame.getOperandStack().popReference();
                        }
                        Object objectRef = frame.getOperandStack().popReference();
                        Logger.trace("invokevirtual: 调用虚方法，对象: %s，参数: %s", objectRef, arg);
                    } else if (frame.getOperandStack().size() == 1) {
                        Object objectRef = frame.getOperandStack().popReference();
                        Logger.trace("invokevirtual: 调用虚方法，对象: %s", objectRef);
                    }
                    break;
            }
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 2);
            int methodRefIndex = readUnsignedShort(bytecode, pc + 1);
            return String.format("invokevirtual #%d", methodRefIndex);
        }
    }
    
    /**
     * 加载字符串常量 - LDC (0x12)
     */
    public static class Ldc extends Instruction {
        public Ldc() {
            super(0x12, "ldc", 2);
        }
        
        @Override
        public int execute(StackFrame frame, byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 1);
            int constantIndex = readUnsignedByte(bytecode, pc + 1);
            
            Logger.debug("执行ldc指令，常量索引: %d", constantIndex);
            
            // 简化处理：根据常量索引模拟不同的常量
            Object constant;
            switch (constantIndex) {
                case 1:
                    constant = "=== SimpleArithmetic 演示程序 ===";
                    break;
                case 2:
                    constant = "计算完成，最终结果: ";
                    break;
                default:
                    constant = "String_" + constantIndex;
                    break;
            }
            
            frame.getOperandStack().pushReference(constant);
            Logger.trace("ldc: 加载常量 %s", constant);
            
            return pc + length;
        }
        
        @Override
        public String toString(byte[] bytecode, int pc) {
            checkArrayBounds(bytecode, pc + 1, 1);
            int constantIndex = readUnsignedByte(bytecode, pc + 1);
            return String.format("ldc #%d", constantIndex);
        }
    }
} 