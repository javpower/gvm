package io.github.javpower.gvm.runtime.stack;

import java.util.Stack;

/**
 * 操作数栈 - 存储字节码指令执行过程中的临时数据
 * 后进先出(LIFO)的数据结构
 */
public class OperandStack {
    
    /**
     * 操作数栈元素
     */
    public static class StackElement {
        private Object value;
        private String type;
        
        public StackElement(Object value, String type) {
            this.value = value;
            this.type = type;
        }
        
        public Object getValue() {
            return value;
        }
        
        public String getType() {
            return type;
        }
        
        public int getInt() {
            if (!"int".equals(type)) {
                throw new RuntimeException("Type mismatch: expected int, got " + type);
            }
            return (Integer) value;
        }
        
        public float getFloat() {
            if (!"float".equals(type)) {
                throw new RuntimeException("Type mismatch: expected float, got " + type);
            }
            return (Float) value;
        }
        
        public long getLong() {
            if (!"long".equals(type)) {
                throw new RuntimeException("Type mismatch: expected long, got " + type);
            }
            return (Long) value;
        }
        
        public double getDouble() {
            if (!"double".equals(type)) {
                throw new RuntimeException("Type mismatch: expected double, got " + type);
            }
            return (Double) value;
        }
        
        public Object getReference() {
            if (!"reference".equals(type)) {
                throw new RuntimeException("Type mismatch: expected reference, got " + type);
            }
            return value;
        }
        
        @Override
        public String toString() {
            return String.format("Element[type=%s, value=%s]", type, value);
        }
    }
    
    private final Stack<StackElement> stack;
    private final int maxStack;
    
    public OperandStack(int maxStack) {
        this.maxStack = maxStack;
        this.stack = new Stack<>();
    }
    
    /**
     * 压入int值
     */
    public void pushInt(int value) {
        checkStackOverflow();
        stack.push(new StackElement(value, "int"));
    }
    
    /**
     * 弹出int值
     */
    public int popInt() {
        checkStackUnderflow();
        return stack.pop().getInt();
    }
    
    /**
     * 压入float值
     */
    public void pushFloat(float value) {
        checkStackOverflow();
        stack.push(new StackElement(value, "float"));
    }
    
    /**
     * 弹出float值
     */
    public float popFloat() {
        checkStackUnderflow();
        return stack.pop().getFloat();
    }
    
    /**
     * 压入long值 (占用两个栈位)
     */
    public void pushLong(long value) {
        checkStackOverflow();
        checkStackOverflow(); // long占用两个位置
        stack.push(new StackElement(value, "long"));
        stack.push(new StackElement(value, "long")); // 推入两次表示占用两个栈位
    }
    
    /**
     * 弹出long值
     */
    public long popLong() {
        checkStackUnderflow();
        checkStackUnderflow();
        stack.pop(); // 弹出高位
        return stack.pop().getLong(); // 弹出低位
    }
    
    /**
     * 压入double值 (占用两个栈位)
     */
    public void pushDouble(double value) {
        checkStackOverflow();
        checkStackOverflow();
        stack.push(new StackElement(value, "double"));
        stack.push(new StackElement(value, "double"));
    }
    
    /**
     * 弹出double值
     */
    public double popDouble() {
        checkStackUnderflow();
        checkStackUnderflow();
        stack.pop(); // 弹出高位
        return stack.pop().getDouble(); // 弹出低位
    }
    
    /**
     * 压入引用值
     */
    public void pushReference(Object value) {
        checkStackOverflow();
        stack.push(new StackElement(value, "reference"));
    }
    
    /**
     * 弹出引用值
     */
    public Object popReference() {
        checkStackUnderflow();
        return stack.pop().getReference();
    }
    
    /**
     * 压入任意类型的栈元素
     */
    public void push(StackElement element) {
        checkStackOverflow();
        stack.push(element);
    }
    
    /**
     * 弹出栈顶元素
     */
    public StackElement pop() {
        checkStackUnderflow();
        return stack.pop();
    }
    
    /**
     * 查看栈顶元素但不弹出
     */
    public StackElement peek() {
        checkStackUnderflow();
        return stack.peek();
    }
    
    /**
     * 获取栈的当前大小
     */
    public int size() {
        return stack.size();
    }
    
    /**
     * 检查栈是否为空
     */
    public boolean isEmpty() {
        return stack.isEmpty();
    }
    
    /**
     * 清空操作数栈
     */
    public void clear() {
        stack.clear();
    }
    
    /**
     * 复制栈顶元素
     */
    public void dup() {
        checkStackUnderflow();
        checkStackOverflow();
        StackElement top = stack.peek();
        stack.push(new StackElement(top.getValue(), top.getType()));
    }
    
    /**
     * 复制栈顶两个元素
     */
    public void dup2() {
        checkStackUnderflow();
        checkStackUnderflow();
        checkStackOverflow();
        checkStackOverflow();
        
        StackElement second = stack.get(stack.size() - 2);
        StackElement first = stack.get(stack.size() - 1);
        
        stack.push(new StackElement(second.getValue(), second.getType()));
        stack.push(new StackElement(first.getValue(), first.getType()));
    }
    
    /**
     * 交换栈顶两个元素
     */
    public void swap() {
        checkStackUnderflow();
        checkStackUnderflow();
        
        StackElement first = stack.pop();
        StackElement second = stack.pop();
        
        stack.push(first);
        stack.push(second);
    }
    
    /**
     * 检查栈溢出
     */
    private void checkStackOverflow() {
        if (stack.size() >= maxStack) {
            throw new RuntimeException("Operand stack overflow: max=" + maxStack + ", current=" + stack.size());
        }
    }
    
    /**
     * 检查栈下溢
     */
    private void checkStackUnderflow() {
        if (stack.isEmpty()) {
            throw new RuntimeException("Operand stack underflow");
        }
    }
    
    /**
     * 获取最大栈深度
     */
    public int getMaxStack() {
        return maxStack;
    }
    
    /**
     * 调试输出
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("OperandStack[maxStack=").append(maxStack).append(", size=").append(stack.size()).append("]:\n");
        
        if (stack.isEmpty()) {
            sb.append("  (empty)\n");
        } else {
            // 从栈顶到栈底显示
            for (int i = stack.size() - 1; i >= 0; i--) {
                String prefix = (i == stack.size() - 1) ? "  -> " : "     ";
                sb.append(prefix).append(stack.get(i)).append("\n");
            }
        }
        
        return sb.toString();
    }
} 