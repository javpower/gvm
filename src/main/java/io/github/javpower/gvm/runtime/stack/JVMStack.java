package io.github.javpower.gvm.runtime.stack;

import java.util.Stack;

/**
 * JVM栈 - 管理线程的方法调用栈
 * 每个线程都有自己独立的JVM栈
 */
public class JVMStack {
    
    // 栈帧调用栈
    private final Stack<StackFrame> frames;
    
    // 最大栈深度
    private final int maxDepth;
    
    // 所属线程名称
    private final String threadName;
    
    // 栈状态
    private boolean isActive;
    
    /**
     * 默认构造函数，使用默认栈深度
     */
    public JVMStack() {
        this("main", 1000);
    }
    
    /**
     * 构造JVM栈
     * 
     * @param threadName 线程名称
     * @param maxDepth 最大栈深度
     */
    public JVMStack(String threadName, int maxDepth) {
        this.threadName = threadName;
        this.maxDepth = maxDepth;
        this.frames = new Stack<>();
        this.isActive = true;
    }
    
    /**
     * 压入栈帧 (方法调用)
     */
    public void pushFrame(StackFrame frame) {
        if (!isActive) {
            throw new RuntimeException("JVM Stack is not active for thread: " + threadName);
        }
        
        if (frames.size() >= maxDepth) {
            throw new StackOverflowError("Stack overflow: max depth " + maxDepth + " exceeded");
        }
        
        // 设置返回地址
        if (!frames.isEmpty()) {
            StackFrame callerFrame = frames.peek();
            frame.setReturnAddress(callerFrame.getPC());
        }
        
        frames.push(frame);
    }
    
    /**
     * 弹出栈帧 (方法返回)
     */
    public StackFrame popFrame() {
        if (frames.isEmpty()) {
            throw new RuntimeException("Cannot pop from empty stack");
        }
        
        StackFrame frame = frames.pop();
        frame.deactivate();
        frame.cleanup();
        
        return frame;
    }
    
    /**
     * 获取当前栈帧 (栈顶)
     */
    public StackFrame getCurrentFrame() {
        if (frames.isEmpty()) {
            return null;
        }
        return frames.peek();
    }
    
    /**
     * 获取调用者栈帧
     */
    public StackFrame getCallerFrame() {
        if (frames.size() < 2) {
            return null;
        }
        return frames.get(frames.size() - 2);
    }
    
    /**
     * 获取栈深度
     */
    public int getDepth() {
        return frames.size();
    }
    
    /**
     * 检查栈是否为空
     */
    public boolean isEmpty() {
        return frames.isEmpty();
    }
    
    /**
     * 获取最大栈深度
     */
    public int getMaxDepth() {
        return maxDepth;
    }
    
    /**
     * 获取线程名称
     */
    public String getThreadName() {
        return threadName;
    }
    
    /**
     * 检查栈是否活跃
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * 激活栈
     */
    public void activate() {
        this.isActive = true;
    }
    
    /**
     * 停用栈
     */
    public void deactivate() {
        this.isActive = false;
        // 清理所有栈帧
        while (!frames.isEmpty()) {
            popFrame();
        }
    }
    
    /**
     * 获取方法调用链
     */
    public String[] getCallStack() {
        String[] callStack = new String[frames.size()];
        for (int i = 0; i < frames.size(); i++) {
            StackFrame frame = frames.get(frames.size() - 1 - i); // 从栈顶开始
            callStack[i] = frame.getSummary();
        }
        return callStack;
    }
    
    /**
     * 打印调用栈
     */
    public void printStackTrace() {
        System.out.println("=== Call Stack for Thread: " + threadName + " ===");
        if (frames.isEmpty()) {
            System.out.println("  (empty stack)");
        } else {
            String[] callStack = getCallStack();
            for (int i = 0; i < callStack.length; i++) {
                System.out.printf("  [%d] %s\n", i, callStack[i]);
            }
        }
        System.out.println("=======================================");
    }
    
    /**
     * 获取栈的详细信息
     */
    public String getDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== JVM Stack Details ===\n");
        sb.append("Thread: ").append(threadName).append("\n");
        sb.append("Depth: ").append(frames.size()).append("/").append(maxDepth).append("\n");
        sb.append("Active: ").append(isActive).append("\n");
        
        if (frames.isEmpty()) {
            sb.append("(No frames)\n");
        } else {
            sb.append("\n--- Frame Stack (top to bottom) ---\n");
            for (int i = frames.size() - 1; i >= 0; i--) {
                StackFrame frame = frames.get(i);
                sb.append(String.format("[%d] %s\n", frames.size() - 1 - i, frame.toSimpleString()));
            }
        }
        
        sb.append("========================\n");
        return sb.toString();
    }
    
    /**
     * 检查栈完整性
     */
    public boolean checkIntegrity() {
        try {
            // 检查栈帧链的完整性
            for (int i = 0; i < frames.size(); i++) {
                StackFrame frame = frames.get(i);
                if (frame == null) {
                    System.err.println("Null frame at index " + i);
                    return false;
                }
                if (!frame.isActive()) {
                    System.err.println("Inactive frame at index " + i + ": " + frame.getSummary());
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Stack integrity check failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 重置栈到初始状态
     */
    public void reset() {
        while (!frames.isEmpty()) {
            popFrame();
        }
        isActive = true;
    }
    
    @Override
    public String toString() {
        return String.format("JVMStack[thread=%s, depth=%d/%d, active=%s]", 
            threadName, frames.size(), maxDepth, isActive);
    }
} 