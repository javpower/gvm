package io.github.javpower.gvm.runtime.stack;

/**
 * 栈帧 - 每个方法调用都会创建一个栈帧
 * 包含局部变量表、操作数栈、动态链接、返回地址等信息
 */
public class StackFrame {
    
    // 局部变量表
    private final LocalVariableTable localVariableTable;
    
    // 操作数栈
    private final OperandStack operandStack;
    
    // 动态链接 - 指向运行时常量池中该栈帧所属方法的引用
    private final String methodReference;
    
    // 返回地址 - 方法正常返回或异常返回的地址
    private int returnAddress;
    
    // 方法信息
    private final String className;
    private final String methodName;
    private final String methodDescriptor;
    
    // 栈帧状态
    private boolean isActive;
    
    // 程序计数器 - 当前执行的字节码指令位置
    private int pc;
    
    /**
     * 构造栈帧
     * 
     * @param maxLocals 最大局部变量数
     * @param maxStack 最大操作数栈深度
     * @param className 类名
     * @param methodName 方法名
     * @param methodDescriptor 方法描述符
     */
    public StackFrame(int maxLocals, int maxStack, String className, String methodName, String methodDescriptor) {
        this.localVariableTable = new LocalVariableTable(maxLocals);
        this.operandStack = new OperandStack(maxStack);
        this.className = className;
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
        this.methodReference = className + "." + methodName + methodDescriptor;
        this.isActive = true;
        this.pc = 0;
        this.returnAddress = -1;
    }
    
    /**
     * 获取局部变量表
     */
    public LocalVariableTable getLocalVariableTable() {
        return localVariableTable;
    }
    
    /**
     * 获取操作数栈
     */
    public OperandStack getOperandStack() {
        return operandStack;
    }
    
    /**
     * 获取方法引用
     */
    public String getMethodReference() {
        return methodReference;
    }
    
    /**
     * 获取类名
     */
    public String getClassName() {
        return className;
    }
    
    /**
     * 获取方法名
     */
    public String getMethodName() {
        return methodName;
    }
    
    /**
     * 获取方法描述符
     */
    public String getMethodDescriptor() {
        return methodDescriptor;
    }
    
    /**
     * 获取程序计数器
     */
    public int getPC() {
        return pc;
    }
    
    /**
     * 设置程序计数器
     */
    public void setPC(int pc) {
        this.pc = pc;
    }
    
    /**
     * 程序计数器递增
     */
    public void incrementPC() {
        this.pc++;
    }
    
    /**
     * 程序计数器递增指定步长
     */
    public void incrementPC(int step) {
        this.pc += step;
    }
    
    /**
     * 获取返回地址
     */
    public int getReturnAddress() {
        return returnAddress;
    }
    
    /**
     * 设置返回地址
     */
    public void setReturnAddress(int returnAddress) {
        this.returnAddress = returnAddress;
    }
    
    /**
     * 检查栈帧是否活跃
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * 激活栈帧
     */
    public void activate() {
        this.isActive = true;
    }
    
    /**
     * 停用栈帧
     */
    public void deactivate() {
        this.isActive = false;
    }
    
    /**
     * 清理栈帧资源
     */
    public void cleanup() {
        operandStack.clear();
        localVariableTable.clear();
        isActive = false;
    }
    
    /**
     * 复制栈帧 (用于调试)
     */
    public StackFrame copy() {
        StackFrame copy = new StackFrame(
            localVariableTable.getMaxLocals(),
            operandStack.getMaxStack(),
            className,
            methodName,
            methodDescriptor
        );
        copy.pc = this.pc;
        copy.returnAddress = this.returnAddress;
        copy.isActive = this.isActive;
        return copy;
    }
    
    /**
     * 获取栈帧信息摘要
     */
    public String getSummary() {
        return String.format("%s.%s%s [PC=%d, Active=%s]", 
            className, methodName, methodDescriptor, pc, isActive);
    }
    
    /**
     * 调试输出
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Stack Frame ===\n");
        sb.append("Method: ").append(methodReference).append("\n");
        sb.append("PC: ").append(pc).append("\n");
        sb.append("Return Address: ").append(returnAddress).append("\n");
        sb.append("Active: ").append(isActive).append("\n");
        sb.append("\n--- Local Variable Table ---\n");
        sb.append(localVariableTable.toString());
        sb.append("\n--- Operand Stack ---\n");
        sb.append(operandStack.toString());
        sb.append("===================\n");
        return sb.toString();
    }
    
    /**
     * 简化的字符串表示
     */
    public String toSimpleString() {
        return String.format("Frame[%s, PC=%d, Stack=%d/%d, Locals=%d]",
            methodName, pc, 
            operandStack.size(), operandStack.getMaxStack(),
            localVariableTable.getMaxLocals());
    }
} 