package io.github.javpower.gvm.bytecode;

import io.github.javpower.gvm.runtime.stack.StackFrame;

/**
 * 字节码指令抽象基类
 */
public abstract class Instruction {
    
    // 指令操作码
    protected final int opcode;
    
    // 指令名称
    protected final String name;
    
    // 指令长度（包括操作码和操作数）
    protected final int length;
    
    /**
     * 构造指令
     * 
     * @param opcode 操作码
     * @param name 指令名称
     * @param length 指令长度
     */
    protected Instruction(int opcode, String name, int length) {
        this.opcode = opcode;
        this.name = name;
        this.length = length;
    }
    
    /**
     * 执行指令
     * 
     * @param frame 当前栈帧
     * @param bytecode 字节码数组
     * @param pc 程序计数器位置
     * @return 执行后的程序计数器位置
     */
    public abstract int execute(StackFrame frame, byte[] bytecode, int pc);
    
    /**
     * 获取操作码
     */
    public int getOpcode() {
        return opcode;
    }
    
    /**
     * 获取指令名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取指令长度
     */
    public int getLength() {
        return length;
    }
    
    /**
     * 从字节码中读取无符号字节
     */
    protected int readUnsignedByte(byte[] bytecode, int index) {
        return bytecode[index] & 0xFF;
    }
    
    /**
     * 从字节码中读取有符号字节
     */
    protected byte readByte(byte[] bytecode, int index) {
        return bytecode[index];
    }
    
    /**
     * 从字节码中读取无符号短整型
     */
    protected int readUnsignedShort(byte[] bytecode, int index) {
        return ((bytecode[index] & 0xFF) << 8) | (bytecode[index + 1] & 0xFF);
    }
    
    /**
     * 从字节码中读取有符号短整型
     */
    protected short readShort(byte[] bytecode, int index) {
        return (short) readUnsignedShort(bytecode, index);
    }
    
    /**
     * 从字节码中读取整型
     */
    protected int readInt(byte[] bytecode, int index) {
        return (readUnsignedShort(bytecode, index) << 16) | readUnsignedShort(bytecode, index + 2);
    }
    
    /**
     * 检查数组边界
     */
    protected void checkArrayBounds(byte[] bytecode, int index, int requiredLength) {
        if (index + requiredLength > bytecode.length) {
            throw new IndexOutOfBoundsException(
                String.format("Bytecode index %d + %d exceeds array length %d", 
                    index, requiredLength, bytecode.length));
        }
    }
    
    /**
     * 获取指令的字符串表示
     */
    public String toString(byte[] bytecode, int pc) {
        return String.format("%s (0x%02X)", name, opcode);
    }
    
    @Override
    public String toString() {
        return String.format("%s (0x%02X, length=%d)", name, opcode, length);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Instruction that = (Instruction) obj;
        return opcode == that.opcode;
    }
    
    @Override
    public int hashCode() {
        return opcode;
    }
} 