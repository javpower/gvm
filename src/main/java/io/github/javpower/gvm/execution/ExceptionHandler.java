package io.github.javpower.gvm.execution;

import io.github.javpower.gvm.classloader.GVMClass;
import io.github.javpower.gvm.memory.GVMObject;
import io.github.javpower.gvm.memory.Heap;
import io.github.javpower.gvm.runtime.stack.JVMStack;
import io.github.javpower.gvm.runtime.stack.StackFrame;

import java.util.*;

/**
 * 异常处理器
 * 负责处理运行时异常、错误和异常表的查找
 */
public class ExceptionHandler {
    
    // 异常处理类型
    public enum ExceptionType {
        RUNTIME_EXCEPTION,      // 运行时异常
        ERROR,                  // 错误
        CHECKED_EXCEPTION,      // 受检异常
        THROWABLE              // 所有可抛出的异常
    }
    
    // 异常表项
    public static class ExceptionTableEntry {
        private final int startPC;         // 异常处理范围起始PC
        private final int endPC;           // 异常处理范围结束PC
        private final int handlerPC;       // 异常处理器PC
        private final String exceptionType; // 异常类型（null表示catch all）
        
        public ExceptionTableEntry(int startPC, int endPC, int handlerPC, String exceptionType) {
            this.startPC = startPC;
            this.endPC = endPC;
            this.handlerPC = handlerPC;
            this.exceptionType = exceptionType;
        }
        
        public int getStartPC() { return startPC; }
        public int getEndPC() { return endPC; }
        public int getHandlerPC() { return handlerPC; }
        public String getExceptionType() { return exceptionType; }
        
        public boolean isInRange(int pc) {
            return pc >= startPC && pc < endPC;
        }
        
        @Override
        public String toString() {
            return String.format("ExceptionTableEntry{%d-%d -> %d, type=%s}", 
                    startPC, endPC, handlerPC, exceptionType);
        }
    }
    
    private final Heap heap;
    private final Map<String, GVMClass> exceptionClasses;
    
    public ExceptionHandler(Heap heap) {
        this.heap = heap;
        this.exceptionClasses = new HashMap<>();
        initializeBuiltinExceptions();
    }
    
    /**
     * 抛出异常
     */
    public void throwException(String exceptionClassName, String message, JVMStack jvmStack) {
        try {
            // 创建异常对象
            GVMObject exceptionObject = createExceptionObject(exceptionClassName, message);
            
            // 查找异常处理器
            ExceptionTableEntry handler = findExceptionHandler(exceptionObject, jvmStack);
            
            if (handler != null) {
                // 找到异常处理器，跳转到处理代码
                handleException(exceptionObject, handler, jvmStack);
            } else {
                // 没有找到异常处理器，异常传播到上层
                propagateException(exceptionObject, jvmStack);
            }
        } catch (Exception e) {
            // 异常处理过程中出现异常，直接终止程序
            System.err.println("Fatal error in exception handling: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * 创建异常对象
     */
    private GVMObject createExceptionObject(String exceptionClassName, String message) {
        GVMClass exceptionClass = exceptionClasses.get(exceptionClassName);
        if (exceptionClass == null) {
            // 如果找不到指定的异常类，使用RuntimeException
            exceptionClass = exceptionClasses.get("java.lang.RuntimeException");
        }
        
        GVMObject exceptionObject = heap.allocateObject(exceptionClass);
        
        // 设置异常消息
        if (message != null) {
            exceptionObject.setInstanceField("detailMessage", message);
        }
        
        // 设置堆栈跟踪信息（简化版本）
        exceptionObject.setInstanceField("stackTrace", createStackTrace());
        
        return exceptionObject;
    }
    
    /**
     * 创建堆栈跟踪信息
     */
    private String createStackTrace() {
        StringBuilder stackTrace = new StringBuilder();
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        
        for (int i = 2; i < elements.length && i < 12; i++) { // 跳过前两个，最多显示10层
            StackTraceElement element = elements[i];
            stackTrace.append("\tat ").append(element.toString()).append("\n");
        }
        
        return stackTrace.toString();
    }
    
    /**
     * 查找异常处理器
     */
    private ExceptionTableEntry findExceptionHandler(GVMObject exceptionObject, JVMStack jvmStack) {
        if (jvmStack.isEmpty()) {
            return null;
        }
        
        StackFrame currentFrame = jvmStack.getCurrentFrame();
        int currentPC = currentFrame.getPC();
        
        // 获取当前方法的异常表（简化版本，实际应该从方法的Code属性中获取）
        List<ExceptionTableEntry> exceptionTable = getExceptionTable(currentFrame);
        
        // 查找匹配的异常处理器
        for (ExceptionTableEntry entry : exceptionTable) {
            if (entry.isInRange(currentPC)) {
                // 检查异常类型是否匹配
                if (entry.getExceptionType() == null || // catch all
                    isInstanceOf(exceptionObject, entry.getExceptionType())) {
                    return entry;
                }
            }
        }
        
        return null;
    }
    
    /**
     * 获取方法的异常表（简化版本）
     */
    private List<ExceptionTableEntry> getExceptionTable(StackFrame frame) {
        // 简化版本：返回一些预定义的异常处理器
        List<ExceptionTableEntry> exceptionTable = new ArrayList<>();
        
        // 为常见的异常添加默认处理器
        exceptionTable.add(new ExceptionTableEntry(0, 100, 50, "java.lang.ArithmeticException"));
        exceptionTable.add(new ExceptionTableEntry(0, 100, 60, "java.lang.NullPointerException"));
        exceptionTable.add(new ExceptionTableEntry(0, 100, 70, "java.lang.ArrayIndexOutOfBoundsException"));
        
        return exceptionTable;
    }
    
    /**
     * 检查对象是否为指定类型的实例
     */
    private boolean isInstanceOf(GVMObject object, String className) {
        if (object == null || object.getObjectClass() == null) {
            return false;
        }
        
        // 简化版本：直接比较类名
        String objectClassName = object.getObjectClass().getClassName();
        return className.equals(objectClassName) || isSubclassOf(objectClassName, className);
    }
    
    /**
     * 检查是否为子类关系
     */
    private boolean isSubclassOf(String childClass, String parentClass) {
        // 简化版本：硬编码一些继承关系
        if ("java.lang.RuntimeException".equals(parentClass)) {
            return childClass.equals("java.lang.ArithmeticException") ||
                   childClass.equals("java.lang.NullPointerException") ||
                   childClass.equals("java.lang.ArrayIndexOutOfBoundsException");
        }
        
        if ("java.lang.Exception".equals(parentClass)) {
            return childClass.equals("java.lang.RuntimeException") || 
                   isSubclassOf(childClass, "java.lang.RuntimeException");
        }
        
        if ("java.lang.Throwable".equals(parentClass)) {
            return childClass.equals("java.lang.Exception") ||
                   childClass.equals("java.lang.Error") ||
                   isSubclassOf(childClass, "java.lang.Exception");
        }
        
        return false;
    }
    
    /**
     * 处理异常
     */
    private void handleException(GVMObject exceptionObject, ExceptionTableEntry handler, JVMStack jvmStack) {
        StackFrame currentFrame = jvmStack.getCurrentFrame();
        
        // 清空操作数栈
        while (!currentFrame.getOperandStack().isEmpty()) {
            currentFrame.getOperandStack().pop();
        }
        
        // 将异常对象压入操作数栈
        currentFrame.getOperandStack().pushReference(exceptionObject);
        
        // 跳转到异常处理器
        currentFrame.setPC(handler.getHandlerPC());
        
        System.out.println("Exception handled: " + exceptionObject.getObjectClass().getClassName() + 
                          " at PC " + handler.getHandlerPC());
    }
    
    /**
     * 传播异常到上层
     */
    private void propagateException(GVMObject exceptionObject, JVMStack jvmStack) {
        // 弹出当前栈帧
        if (!jvmStack.isEmpty()) {
            jvmStack.popFrame();
        }
        
        // 如果还有上层栈帧，继续查找异常处理器
        if (!jvmStack.isEmpty()) {
            ExceptionTableEntry handler = findExceptionHandler(exceptionObject, jvmStack);
            if (handler != null) {
                handleException(exceptionObject, handler, jvmStack);
                return;
            } else {
                // 继续向上传播
                propagateException(exceptionObject, jvmStack);
                return;
            }
        }
        
        // 没有更多栈帧，异常未被处理，程序终止
        terminateWithUnhandledException(exceptionObject);
    }
    
    /**
     * 因未处理异常终止程序
     */
    private void terminateWithUnhandledException(GVMObject exceptionObject) {
        System.err.println("Exception in thread \"main\" " + 
                          exceptionObject.getObjectClass().getClassName() + ": " +
                          exceptionObject.getInstanceField("detailMessage"));
        
        String stackTrace = (String) exceptionObject.getInstanceField("stackTrace");
        if (stackTrace != null) {
            System.err.print(stackTrace);
        }
        
        System.exit(1);
    }
    
    /**
     * 抛出算术异常
     */
    public void throwArithmeticException(String message, JVMStack jvmStack) {
        throwException("java.lang.ArithmeticException", message, jvmStack);
    }
    
    /**
     * 抛出空指针异常
     */
    public void throwNullPointerException(String message, JVMStack jvmStack) {
        throwException("java.lang.NullPointerException", message, jvmStack);
    }
    
    /**
     * 抛出数组越界异常
     */
    public void throwArrayIndexOutOfBoundsException(String message, JVMStack jvmStack) {
        throwException("java.lang.ArrayIndexOutOfBoundsException", message, jvmStack);
    }
    
    /**
     * 抛出栈溢出错误
     */
    public void throwStackOverflowError(JVMStack jvmStack) {
        throwException("java.lang.StackOverflowError", "Stack overflow", jvmStack);
    }
    
    /**
     * 抛出内存不足错误
     */
    public void throwOutOfMemoryError(String message, JVMStack jvmStack) {
        throwException("java.lang.OutOfMemoryError", message, jvmStack);
    }
    
    /**
     * 初始化内置异常类
     */
    private void initializeBuiltinExceptions() {
        // 创建简化的异常类定义
        createExceptionClass("java.lang.Throwable");
        createExceptionClass("java.lang.Exception");
        createExceptionClass("java.lang.RuntimeException");
        createExceptionClass("java.lang.ArithmeticException");
        createExceptionClass("java.lang.NullPointerException");
        createExceptionClass("java.lang.ArrayIndexOutOfBoundsException");
        createExceptionClass("java.lang.Error");
        createExceptionClass("java.lang.StackOverflowError");
        createExceptionClass("java.lang.OutOfMemoryError");
    }
    
    /**
     * 创建异常类
     */
    private void createExceptionClass(String className) {
        // 简化版本：创建基本的异常类结构
        GVMClass exceptionClass = new GVMClass(className, "java/lang/Object", new String[0], 
                                              0x0021, new Object[0], null);
        
        // 添加字段
        exceptionClass.addField(new GVMClass.FieldInfo("detailMessage", "Ljava/lang/String;", 0, null));
        exceptionClass.addField(new GVMClass.FieldInfo("stackTrace", "Ljava/lang/String;", 0, null));
        
        // 添加基本的构造方法
        exceptionClass.addMethod(new GVMClass.MethodInfo("<init>", "()V", 1, new byte[0], 1, 1));
        exceptionClass.addMethod(new GVMClass.MethodInfo("<init>", "(Ljava/lang/String;)V", 1, new byte[0], 2, 1));
        
        exceptionClasses.put(className, exceptionClass);
    }
    
    /**
     * 获取异常处理统计信息
     */
    public ExceptionStatistics getStatistics() {
        return new ExceptionStatistics(exceptionClasses.size());
    }
    
    /**
     * 异常处理统计信息
     */
    public static class ExceptionStatistics {
        private final int registeredExceptionClasses;
        
        public ExceptionStatistics(int registeredExceptionClasses) {
            this.registeredExceptionClasses = registeredExceptionClasses;
        }
        
        public int getRegisteredExceptionClasses() {
            return registeredExceptionClasses;
        }
        
        @Override
        public String toString() {
            return String.format("ExceptionStatistics{exceptionClasses=%d}", registeredExceptionClasses);
        }
    }
} 