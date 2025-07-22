package io.github.javpower.gvm.execution;

import io.github.javpower.gvm.classloader.GVMClass;
import io.github.javpower.gvm.classloader.GVMClassLoader;
import io.github.javpower.gvm.memory.GVMObject;
import io.github.javpower.gvm.memory.Heap;
import io.github.javpower.gvm.runtime.stack.JVMStack;
import io.github.javpower.gvm.runtime.stack.StackFrame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 方法调用器
 * 负责处理各种类型的方法调用：静态方法、实例方法、构造方法、本地方法等
 */
public class MethodInvoker {
    
    // 方法调用类型
    public enum InvokeType {
        STATIC,         // 静态方法调用
        VIRTUAL,        // 虚方法调用
        SPECIAL,        // 特殊方法调用（构造方法、私有方法、父类方法）
        INTERFACE,      // 接口方法调用
        NATIVE          // 本地方法调用
    }
    
    private final ExecutionEngine executionEngine;
    private final Heap heap;
    private final GVMClassLoader classLoader;
    
    // 本地方法注册表
    private final Map<String, NativeMethod> nativeMethods;
    
    public MethodInvoker(ExecutionEngine executionEngine, Heap heap, GVMClassLoader classLoader) {
        this.executionEngine = executionEngine;
        this.heap = heap;
        this.classLoader = classLoader;
        this.nativeMethods = new ConcurrentHashMap<>();
        registerBuiltinNativeMethods();
    }
    
    /**
     * 调用方法
     */
    public Object invokeMethod(InvokeType invokeType, String className, String methodName, 
                             String methodDescriptor, Object[] args, JVMStack jvmStack) {
        try {
            // 加载目标类
            GVMClass targetClass = classLoader.loadClass(className, true);
            
            // 查找方法
            GVMClass.MethodInfo method = findMethod(targetClass, methodName, methodDescriptor, invokeType);
            if (method == null) {
                throw new NoSuchMethodError(className + "." + methodName + methodDescriptor);
            }
            
            // 根据调用类型执行不同的调用逻辑
            switch (invokeType) {
                case STATIC:
                    return invokeStaticMethod(targetClass, method, args, jvmStack);
                case VIRTUAL:
                    return invokeVirtualMethod(targetClass, method, args, jvmStack);
                case SPECIAL:
                    return invokeSpecialMethod(targetClass, method, args, jvmStack);
                case INTERFACE:
                    return invokeInterfaceMethod(targetClass, method, args, jvmStack);
                case NATIVE:
                    return invokeNativeMethod(targetClass, method, args, jvmStack);
                default:
                    throw new UnsupportedOperationException("Unsupported invoke type: " + invokeType);
            }
        } catch (Exception e) {
            throw new RuntimeException("Method invocation failed: " + className + "." + methodName, e);
        }
    }
    
    /**
     * 查找方法
     */
    private GVMClass.MethodInfo findMethod(GVMClass targetClass, String methodName, 
                                         String methodDescriptor, InvokeType invokeType) {
        // 在当前类中查找
        GVMClass.MethodInfo method = targetClass.getMethod(methodName, methodDescriptor);
        if (method != null) {
            return method;
        }
        
        // 如果是虚方法调用或接口方法调用，需要在继承层次中查找
        if (invokeType == InvokeType.VIRTUAL || invokeType == InvokeType.INTERFACE) {
            return findMethodInHierarchy(targetClass, methodName, methodDescriptor);
        }
        
        return null;
    }
    
    /**
     * 在类继承层次中查找方法
     */
    private GVMClass.MethodInfo findMethodInHierarchy(GVMClass startClass, String methodName, String methodDescriptor) {
        GVMClass currentClass = startClass;
        
        // 向上查找父类
        while (currentClass != null) {
            GVMClass.MethodInfo method = currentClass.getMethod(methodName, methodDescriptor);
            if (method != null) {
                return method;
            }
            
            // 查找父类
            String superClassName = currentClass.getSuperClassName();
            if (superClassName != null && !superClassName.equals("java/lang/Object")) {
                try {
                    currentClass = classLoader.loadClass(superClassName, false);
                } catch (ClassNotFoundException e) {
                    break;
                }
            } else {
                break;
            }
        }
        
        return null;
    }
    
    /**
     * 调用静态方法
     */
    private Object invokeStaticMethod(GVMClass targetClass, GVMClass.MethodInfo method, 
                                    Object[] args, JVMStack jvmStack) {
        // 确保类已初始化
        classLoader.initializeClass(targetClass);
        
        // 检查方法是否为静态方法
        if (!method.isStatic()) {
            throw new IncompatibleClassChangeError("Expected static method");
        }
        
        // 检查是否为本地方法
        if (method.isNative()) {
            return invokeNativeMethod(targetClass, method, args, jvmStack);
        }
        
        // 创建新的栈帧
        StackFrame frame = createStackFrame(targetClass, method, args, null);
        
        // 执行方法
        return executionEngine.executeMethod(targetClass, method, jvmStack);
    }
    
    /**
     * 调用虚方法
     */
    private Object invokeVirtualMethod(GVMClass targetClass, GVMClass.MethodInfo method, 
                                     Object[] args, JVMStack jvmStack) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Virtual method requires object instance");
        }
        
        // 第一个参数应该是对象实例
        Object instance = args[0];
        if (!(instance instanceof GVMObject)) {
            throw new IllegalArgumentException("First argument must be object instance");
        }
        
        GVMObject objectInstance = (GVMObject) instance;
        
        // 动态方法查找：根据对象的实际类型查找方法
        GVMClass actualClass = objectInstance.getObjectClass();
        GVMClass.MethodInfo actualMethod = findMethodInHierarchy(actualClass, method.getName(), method.getDescriptor());
        
        if (actualMethod == null) {
            actualMethod = method; // 使用原始方法
        }
        
        // 检查是否为本地方法
        if (actualMethod.isNative()) {
            return invokeNativeMethod(actualClass, actualMethod, args, jvmStack);
        }
        
        // 创建新的栈帧
        StackFrame frame = createStackFrame(actualClass, actualMethod, args, objectInstance);
        
        // 执行方法
        return executionEngine.executeMethod(actualClass, actualMethod, jvmStack);
    }
    
    /**
     * 调用特殊方法（构造方法、私有方法、父类方法）
     */
    private Object invokeSpecialMethod(GVMClass targetClass, GVMClass.MethodInfo method, 
                                     Object[] args, JVMStack jvmStack) {
        // 检查是否为本地方法
        if (method.isNative()) {
            return invokeNativeMethod(targetClass, method, args, jvmStack);
        }
        
        // 对于构造方法，需要创建新对象
        GVMObject instance = null;
        if ("<init>".equals(method.getName())) {
            instance = heap.allocateObject(targetClass);
            
            // 将新创建的对象作为第一个参数
            Object[] newArgs = new Object[args.length + 1];
            newArgs[0] = instance;
            System.arraycopy(args, 0, newArgs, 1, args.length);
            args = newArgs;
        } else if (args != null && args.length > 0 && args[0] instanceof GVMObject) {
            instance = (GVMObject) args[0];
        }
        
        // 创建新的栈帧
        StackFrame frame = createStackFrame(targetClass, method, args, instance);
        
        // 执行方法
        Object result = executionEngine.executeMethod(targetClass, method, jvmStack);
        
        // 如果是构造方法，返回新创建的对象
        if ("<init>".equals(method.getName())) {
            return instance;
        }
        
        return result;
    }
    
    /**
     * 调用接口方法
     */
    private Object invokeInterfaceMethod(GVMClass targetClass, GVMClass.MethodInfo method, 
                                       Object[] args, JVMStack jvmStack) {
        // 接口方法调用与虚方法调用类似
        return invokeVirtualMethod(targetClass, method, args, jvmStack);
    }
    
    /**
     * 调用本地方法
     */
    private Object invokeNativeMethod(GVMClass targetClass, GVMClass.MethodInfo method, 
                                    Object[] args, JVMStack jvmStack) {
        String methodKey = targetClass.getClassName() + "." + method.getName() + method.getDescriptor();
        NativeMethod nativeMethod = nativeMethods.get(methodKey);
        
        if (nativeMethod != null) {
            return nativeMethod.invoke(args);
        }
        
        // 如果没有找到本地方法实现，抛出异常
        throw new UnsatisfiedLinkError("Native method not found: " + methodKey);
    }
    
    /**
     * 创建栈帧
     */
    private StackFrame createStackFrame(GVMClass targetClass, GVMClass.MethodInfo method, 
                                      Object[] args, GVMObject instance) {
        int maxLocals = method.getMaxLocals();
        int maxStack = method.getMaxStack();
        
        StackFrame frame = new StackFrame(maxLocals, maxStack, 
                targetClass.getClassName(), method.getName(), method.getDescriptor());
        
        // 设置参数到局部变量表
        int localIndex = 0;
        
        // 如果是实例方法，第一个局部变量是this引用
        if (!method.isStatic() && instance != null) {
            frame.getLocalVariableTable().setReference(localIndex++, instance);
        }
        
        // 设置方法参数
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Integer) {
                    frame.getLocalVariableTable().setInt(localIndex++, (Integer) arg);
                } else if (arg instanceof Long) {
                    frame.getLocalVariableTable().setLong(localIndex, (Long) arg);
                    localIndex += 2; // long占用两个slot
                } else if (arg instanceof Float) {
                    frame.getLocalVariableTable().setFloat(localIndex++, (Float) arg);
                } else if (arg instanceof Double) {
                    frame.getLocalVariableTable().setDouble(localIndex, (Double) arg);
                    localIndex += 2; // double占用两个slot
                } else if (arg instanceof Boolean) {
                    frame.getLocalVariableTable().setInt(localIndex++, (Boolean) arg ? 1 : 0);
                } else {
                    frame.getLocalVariableTable().setReference(localIndex++, arg);
                }
            }
        }
        
        return frame;
    }
    
    /**
     * 注册本地方法
     */
    public void registerNativeMethod(String className, String methodName, String methodDescriptor, 
                                   NativeMethod implementation) {
        String methodKey = className + "." + methodName + methodDescriptor;
        nativeMethods.put(methodKey, implementation);
    }
    
    /**
     * 注册内置的本地方法
     */
    private void registerBuiltinNativeMethods() {
        // System.out.println
        registerNativeMethod("java/lang/System", "println", "(Ljava/lang/String;)V", 
            args -> {
                if (args != null && args.length > 0) {
                    System.out.println(args[0]);
                }
                return null;
            });
        
        // System.currentTimeMillis
        registerNativeMethod("java/lang/System", "currentTimeMillis", "()J", 
            args -> System.currentTimeMillis());
        
        // Object.hashCode
        registerNativeMethod("java/lang/Object", "hashCode", "()I", 
            args -> {
                if (args != null && args.length > 0 && args[0] instanceof GVMObject) {
                    return (int) ((GVMObject) args[0]).getObjectId();
                }
                return 0;
            });
        
        // Object.equals
        registerNativeMethod("java/lang/Object", "equals", "(Ljava/lang/Object;)Z", 
            args -> {
                if (args != null && args.length >= 2) {
                    return args[0] == args[1];
                }
                return false;
            });
    }
    
    /**
     * 本地方法接口
     */
    @FunctionalInterface
    public interface NativeMethod {
        Object invoke(Object[] args);
    }
    
    /**
     * 获取方法调用统计信息
     */
    public MethodInvocationStatistics getStatistics() {
        return new MethodInvocationStatistics(nativeMethods.size());
    }
    
    /**
     * 方法调用统计信息
     */
    public static class MethodInvocationStatistics {
        private final int registeredNativeMethods;
        
        public MethodInvocationStatistics(int registeredNativeMethods) {
            this.registeredNativeMethods = registeredNativeMethods;
        }
        
        public int getRegisteredNativeMethods() {
            return registeredNativeMethods;
        }
        
        @Override
        public String toString() {
            return String.format("MethodInvocationStatistics{nativeMethods=%d}", registeredNativeMethods);
        }
    }
} 