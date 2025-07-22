package io.github.javpower.gvm.execution;

import io.github.javpower.gvm.bytecode.Instruction;
import io.github.javpower.gvm.bytecode.InstructionSet;
import io.github.javpower.gvm.classloader.GVMClass;
import io.github.javpower.gvm.memory.Heap;
import io.github.javpower.gvm.runtime.stack.JVMStack;
import io.github.javpower.gvm.runtime.stack.StackFrame;
import io.github.javpower.gvm.utils.Logger;

/**
 * 执行引擎 - 负责字节码的解释执行
 */
public class ExecutionEngine {
    
    // 执行模式
    public enum ExecutionMode {
        INTERPRET,    // 解释执行
        JIT,          // 即时编译
        MIXED         // 混合模式
    }
    
    // 当前执行模式
    private ExecutionMode mode;
    
    // 是否启用调试模式
    private boolean debugMode;
    
    // 执行统计信息
    private ExecutionStatistics statistics;
    
    // 内存管理器
    private Heap heap;
    
    // 方法调用器
    private MethodInvoker methodInvoker;
    
    // 异常处理器
    private ExceptionHandler exceptionHandler;
    
    /**
     * 构造执行引擎
     */
    public ExecutionEngine() {
        this(ExecutionMode.INTERPRET, false);
    }
    
    /**
     * 构造执行引擎
     * 
     * @param mode 执行模式
     * @param debugMode 是否启用调试模式
     */
    public ExecutionEngine(ExecutionMode mode, boolean debugMode) {
        this.mode = mode;
        this.debugMode = debugMode;
        this.statistics = new ExecutionStatistics();
        this.heap = new Heap();
        this.exceptionHandler = new ExceptionHandler(heap);
        // methodInvoker will be set later when classloader is available
    }
    
    /**
     * 设置方法调用器
     */
    public void setMethodInvoker(MethodInvoker methodInvoker) {
        this.methodInvoker = methodInvoker;
    }
    
    /**
     * 执行方法
     * 
     * @param gvmClass 类对象
     * @param method 方法信息
     * @param jvmStack JVM栈
     * @return 方法执行结果
     */
    public Object executeMethod(GVMClass gvmClass, GVMClass.MethodInfo method, JVMStack jvmStack) {
        if (method.getBytecode() == null || method.getBytecode().length == 0) {
            if (method.isNative()) {
                return executeNativeMethod(gvmClass, method, jvmStack);
            } else if (method.isAbstract()) {
                throw new RuntimeException("Cannot execute abstract method: " + method.getName());
            } else {
                throw new RuntimeException("Method has no bytecode: " + method.getName());
            }
        }
        
        // 创建方法栈帧
        StackFrame frame = new StackFrame(
            method.getMaxLocals(),
            method.getMaxStack(),
            gvmClass.getClassName(),
            method.getName(),
            method.getDescriptor()
        );
        
        // 将栈帧压入JVM栈
        jvmStack.pushFrame(frame);
        
        try {
            statistics.incrementMethodCalls();
            
            Logger.method("=== 开始执行方法: %s ===", method.getName());
            Logger.method("类: %s", gvmClass.getClassName());
            Logger.method("方法描述符: %s", method.getDescriptor());
            Logger.method("字节码长度: %d", method.getBytecode().length);
            Logger.method("最大栈深度: %d, 最大局部变量: %d", method.getMaxStack(), method.getMaxLocals());
            Logger.stack("栈帧已压入，当前栈深度: %d", jvmStack.getDepth());
            
            // 显示栈帧详细信息
            if (debugMode) {
                Logger.debug("栈帧详细信息:\n%s", frame.toString());
            }
            
            // 执行字节码
            Object result = interpretBytecode(method.getBytecode(), frame, jvmStack);
            
            Logger.method("=== 方法执行完成 ===");
            Logger.method("返回值: %s", result);
            Logger.stack("准备弹出栈帧，当前栈深度: %d", jvmStack.getDepth());
            
            return result;
            
        } catch (Exception e) {
            statistics.incrementExceptions();
            if (debugMode) {
                System.err.println("=== Method Execution Failed ===");
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
            throw new RuntimeException("Method execution failed: " + method.getName(), e);
        } finally {
            // 弹出栈帧
            jvmStack.popFrame();
        }
    }
    
    /**
     * 解释执行字节码
     */
    private Object interpretBytecode(byte[] bytecode, StackFrame frame, JVMStack jvmStack) {
        int pc = 0;
        frame.setPC(pc);
        
        while (pc < bytecode.length) {
            try {
                // 更新栈帧的PC
                frame.setPC(pc);
                
                // 解码指令
                Instruction instruction = InstructionSet.decode(bytecode, pc);
                if (instruction == null) {
                    int opcode = bytecode[pc] & 0xFF;
                    throw new RuntimeException("Unknown instruction: 0x" + Integer.toHexString(opcode));
                }
                
                Logger.bytecode("PC=%3d: %s", pc, instruction.toString(bytecode, pc));
                Logger.stack("操作数栈: %d/%d 个元素", frame.getOperandStack().size(), frame.getOperandStack().getMaxStack());
                
                // 在TRACE级别显示详细的栈和局部变量状态
                if (debugMode) {
                    // 显示操作数栈内容
                    if (frame.getOperandStack().size() > 0) {
                        StringBuilder stackContent = new StringBuilder("栈内容: [");
                        for (int i = 0; i < frame.getOperandStack().size(); i++) {
                            if (i > 0) stackContent.append(", ");
                            // 注意：这里需要实现一个能够查看栈中特定位置元素的方法
                            stackContent.append("element_").append(i);
                        }
                        stackContent.append("]");
                        Logger.trace(stackContent.toString());
                    }
                    
                    // 显示局部变量表内容
                    StringBuilder localsContent = new StringBuilder("局部变量: [");
                    for (int i = 0; i < frame.getLocalVariableTable().getMaxLocals(); i++) {
                        if (i > 0) localsContent.append(", ");
                        Object value = frame.getLocalVariableTable().getSlot(i);
                        localsContent.append(String.format("slot%d=%s", i, value));
                    }
                    localsContent.append("]");
                    Logger.trace(localsContent.toString());
                }
                
                // 执行指令
                statistics.incrementInstructions();
                int nextPC;
                
                try {
                    nextPC = instruction.execute(frame, bytecode, pc);
                } catch (ArithmeticException e) {
                    // 处理算术异常（如除零）
                    exceptionHandler.throwArithmeticException(e.getMessage(), jvmStack);
                    return null; // 异常处理后不会到达这里
                } catch (NullPointerException e) {
                    // 处理空指针异常
                    exceptionHandler.throwNullPointerException(e.getMessage(), jvmStack);
                    return null;
                } catch (ArrayIndexOutOfBoundsException e) {
                    // 处理数组越界异常
                    exceptionHandler.throwArrayIndexOutOfBoundsException(e.getMessage(), jvmStack);
                    return null;
                }
                
                // 检查PC是否有效
                if (nextPC < 0 || nextPC > bytecode.length) {
                    throw new RuntimeException("Invalid PC after instruction execution: " + nextPC);
                }
                
                // 检查是否是方法返回指令
                if (isReturnInstruction(instruction)) {
                    Object returnValue = getReturnValue(instruction, frame);
                    if (debugMode) {
                        System.out.println("  Method returning: " + returnValue);
                    }
                    return returnValue;
                }
                
                pc = nextPC;
                Logger.trace("下一个PC: %d", pc);
                
            } catch (Exception e) {
                statistics.incrementInstructionErrors();
                throw new RuntimeException("Error executing instruction at PC " + pc + ": " + e.getMessage(), e);
            }
        }
        
        // 如果没有显式的返回指令，返回null（相当于return指令）
        return null;
    }
    
    /**
     * 执行本地方法
     */
    private Object executeNativeMethod(GVMClass gvmClass, GVMClass.MethodInfo method, JVMStack jvmStack) {
        if (debugMode) {
            System.out.println("=== Executing Native Method: " + method.getName() + " ===");
        }
        
        // 简化的本地方法处理
        // 在真实的JVM中，这里会通过JNI调用本地代码
        String methodName = method.getName();
        
        if ("println".equals(methodName)) {
            // 模拟System.out.println
            StackFrame frame = jvmStack.getCurrentFrame();
            if (frame != null && !frame.getOperandStack().isEmpty()) {
                Object value = frame.getOperandStack().pop().getValue();
                System.out.println(value);
            }
            return null;
        }
        
        throw new RuntimeException("Native method not implemented: " + methodName);
    }
    
    /**
     * 检查是否是返回指令
     */
    private boolean isReturnInstruction(Instruction instruction) {
        String name = instruction.getName();
        return name.equals("return") || name.equals("ireturn") || name.equals("lreturn") ||
               name.equals("freturn") || name.equals("dreturn") || name.equals("areturn");
    }
    
    /**
     * 获取返回值
     */
    private Object getReturnValue(Instruction instruction, StackFrame frame) {
        String name = instruction.getName();
        
        switch (name) {
            case "return":
                return null;
            case "ireturn":
                return frame.getOperandStack().popInt();
            case "lreturn":
                return frame.getOperandStack().popLong();
            case "freturn":
                return frame.getOperandStack().popFloat();
            case "dreturn":
                return frame.getOperandStack().popDouble();
            case "areturn":
                return frame.getOperandStack().popReference();
            default:
                return null;
        }
    }
    
    /**
     * 设置执行模式
     */
    public void setExecutionMode(ExecutionMode mode) {
        this.mode = mode;
    }
    
    /**
     * 获取执行模式
     */
    public ExecutionMode getExecutionMode() {
        return mode;
    }
    
    /**
     * 设置调试模式
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
    
    /**
     * 是否启用调试模式
     */
    public boolean isDebugMode() {
        return debugMode;
    }
    
    /**
     * 获取执行统计信息
     */
    public ExecutionStatistics getStatistics() {
        return statistics;
    }
    
    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        this.statistics = new ExecutionStatistics();
    }
    
    /**
     * 获取堆内存管理器
     */
    public Heap getHeap() {
        return heap;
    }
    
    /**
     * 获取异常处理器
     */
    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }
    
    /**
     * 获取方法调用器
     */
    public MethodInvoker getMethodInvoker() {
        return methodInvoker;
    }
    
    /**
     * 执行统计信息类
     */
    public static class ExecutionStatistics {
        private long methodCalls = 0;
        private long instructionsExecuted = 0;
        private long instructionErrors = 0;
        private long exceptions = 0;
        private long startTime = System.currentTimeMillis();
        
        public void incrementMethodCalls() { methodCalls++; }
        public void incrementInstructions() { instructionsExecuted++; }
        public void incrementInstructionErrors() { instructionErrors++; }
        public void incrementExceptions() { exceptions++; }
        
        public long getMethodCalls() { return methodCalls; }
        public long getInstructionsExecuted() { return instructionsExecuted; }
        public long getInstructionErrors() { return instructionErrors; }
        public long getExceptions() { return exceptions; }
        public long getExecutionTime() { return System.currentTimeMillis() - startTime; }
        
        public double getInstructionsPerSecond() {
            long time = getExecutionTime();
            return time > 0 ? (instructionsExecuted * 1000.0 / time) : 0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "ExecutionStatistics[methods=%d, instructions=%d, errors=%d, exceptions=%d, time=%dms, ips=%.2f]",
                methodCalls, instructionsExecuted, instructionErrors, exceptions, 
                getExecutionTime(), getInstructionsPerSecond()
            );
        }
    }
    
    @Override
    public String toString() {
        return String.format("ExecutionEngine[mode=%s, debug=%s, %s]", 
            mode, debugMode, statistics);
    }
} 