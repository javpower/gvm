package io.github.javpower.gvm;

import io.github.javpower.gvm.bytecode.InstructionSet;
import io.github.javpower.gvm.classloader.ApplicationClassLoader;
import io.github.javpower.gvm.classloader.CustomClassLoader;
import io.github.javpower.gvm.execution.ExecutionEngine;
import io.github.javpower.gvm.execution.ExceptionHandler;
import io.github.javpower.gvm.execution.MethodInvoker;
import io.github.javpower.gvm.memory.GVMObject;
import io.github.javpower.gvm.memory.Heap;
import io.github.javpower.gvm.runtime.stack.JVMStack;
import io.github.javpower.gvm.runtime.stack.LocalVariableTable;
import io.github.javpower.gvm.runtime.stack.OperandStack;
import io.github.javpower.gvm.runtime.stack.StackFrame;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * GVM测试类
 */
public class GVMTest {
    
    private GVM gvm;
    private GVM.GVMConfig config;
    
    @Before
    public void setUp() {
        config = new GVM.GVMConfig()
            .setDebugMode(true)
            .addClassPath(".")
            .setStackSize(100);
        gvm = new GVM(config);
    }
    
    @Test
    public void testGVMInitialization() {
        assertFalse("GVM should not be initialized initially", gvm.isInitialized());
        assertFalse("GVM should not be running initially", gvm.isRunning());
        
        gvm.initialize();
        
        assertTrue("GVM should be initialized after initialize()", gvm.isInitialized());
        assertFalse("GVM should not be running after initialize()", gvm.isRunning());
        
        assertNotNull("System class loader should not be null", gvm.getSystemClassLoader());
        assertNotNull("Execution engine should not be null", gvm.getExecutionEngine());
        assertNotNull("Main thread stack should not be null", gvm.getMainThreadStack());
    }
    
    @Test
    public void testLocalVariableTable() {
        LocalVariableTable locals = new LocalVariableTable(5);
        
        // 测试整数操作
        locals.setInt(0, 42);
        assertEquals("Integer value should match", 42, locals.getInt(0));
        
        // 测试长整数操作
        locals.setLong(1, 123456789L);
        assertEquals("Long value should match", 123456789L, locals.getLong(1));
        
        // 测试浮点数操作
        locals.setFloat(3, 3.14f);
        assertEquals("Float value should match", 3.14f, locals.getFloat(3), 0.001f);
        
        // 测试引用操作
        String testString = "Hello, GVM!";
        locals.setReference(4, testString);
        assertEquals("Reference value should match", testString, locals.getReference(4));
    }
    
    @Test
    public void testOperandStack() {
        OperandStack stack = new OperandStack(10);
        
        assertTrue("Stack should be empty initially", stack.isEmpty());
        assertEquals("Stack size should be 0", 0, stack.size());
        
        // 测试整数栈操作
        stack.pushInt(100);
        stack.pushInt(200);
        assertEquals("Stack size should be 2", 2, stack.size());
        
        assertEquals("Second integer should be 200", 200, stack.popInt());
        assertEquals("First integer should be 100", 100, stack.popInt());
        assertTrue("Stack should be empty after popping all elements", stack.isEmpty());
        
        // 测试浮点数栈操作
        stack.pushFloat(1.5f);
        stack.pushFloat(2.5f);
        assertEquals("Second float should be 2.5", 2.5f, stack.popFloat(), 0.001f);
        assertEquals("First float should be 1.5", 1.5f, stack.popFloat(), 0.001f);
        
        // 测试引用栈操作
        String ref1 = "Reference1";
        String ref2 = "Reference2";
        stack.pushReference(ref1);
        stack.pushReference(ref2);
        assertEquals("Second reference should match", ref2, stack.popReference());
        assertEquals("First reference should match", ref1, stack.popReference());
    }
    
    @Test
    public void testStackFrame() {
        StackFrame frame = new StackFrame(3, 5, "TestClass", "testMethod", "()V");
        
        assertEquals("Class name should match", "TestClass", frame.getClassName());
        assertEquals("Method name should match", "testMethod", frame.getMethodName());
        assertEquals("Method descriptor should match", "()V", frame.getMethodDescriptor());
        assertEquals("PC should be 0 initially", 0, frame.getPC());
        assertTrue("Frame should be active", frame.isActive());
        
        // 测试PC操作
        frame.setPC(10);
        assertEquals("PC should be updated", 10, frame.getPC());
        
        frame.incrementPC();
        assertEquals("PC should be incremented", 11, frame.getPC());
        
        frame.incrementPC(5);
        assertEquals("PC should be incremented by 5", 16, frame.getPC());
        
        // 测试局部变量表和操作数栈
        assertNotNull("Local variable table should not be null", frame.getLocalVariableTable());
        assertNotNull("Operand stack should not be null", frame.getOperandStack());
        
        assertEquals("Max locals should match", 3, frame.getLocalVariableTable().getMaxLocals());
        assertEquals("Max stack should match", 5, frame.getOperandStack().getMaxStack());
    }
    
    @Test
    public void testJVMStack() {
        JVMStack jvmStack = new JVMStack("test-thread", 10);
        
        assertEquals("Thread name should match", "test-thread", jvmStack.getThreadName());
        assertEquals("Max depth should match", 10, jvmStack.getMaxDepth());
        assertTrue("Stack should be empty initially", jvmStack.isEmpty());
        assertEquals("Depth should be 0", 0, jvmStack.getDepth());
        assertTrue("Stack should be active", jvmStack.isActive());
        
        // 测试栈帧操作
        StackFrame frame1 = new StackFrame(2, 3, "Class1", "method1", "()V");
        StackFrame frame2 = new StackFrame(1, 2, "Class2", "method2", "(I)V");
        
        jvmStack.pushFrame(frame1);
        assertEquals("Depth should be 1", 1, jvmStack.getDepth());
        assertEquals("Current frame should be frame1", frame1, jvmStack.getCurrentFrame());
        
        jvmStack.pushFrame(frame2);
        assertEquals("Depth should be 2", 2, jvmStack.getDepth());
        assertEquals("Current frame should be frame2", frame2, jvmStack.getCurrentFrame());
        assertEquals("Caller frame should be frame1", frame1, jvmStack.getCallerFrame());
        
        StackFrame poppedFrame = jvmStack.popFrame();
        assertEquals("Popped frame should be frame2", frame2, poppedFrame);
        assertEquals("Depth should be 1", 1, jvmStack.getDepth());
        assertEquals("Current frame should be frame1", frame1, jvmStack.getCurrentFrame());
        
        jvmStack.popFrame();
        assertTrue("Stack should be empty", jvmStack.isEmpty());
    }
    
    @Test
    public void testInstructionSet() {
        assertTrue("Instruction set should have instructions", InstructionSet.getInstructionCount() > 0);
        
        // 测试常见指令
        assertTrue("Should have iconst_0 instruction", InstructionSet.hasInstruction("iconst_0"));
        assertTrue("Should have iconst_1 instruction", InstructionSet.hasInstruction("iconst_1"));
        assertTrue("Should have iadd instruction", InstructionSet.hasInstruction("iadd"));
        assertTrue("Should have isub instruction", InstructionSet.hasInstruction("isub"));
        
        // 测试指令解码
        byte[] bytecode = {0x03, 0x04, 0x60}; // iconst_0, iconst_1, iadd
        
        assertNotNull("Should decode iconst_0", InstructionSet.decode(bytecode, 0));
        assertNotNull("Should decode iconst_1", InstructionSet.decode(bytecode, 1));
        assertNotNull("Should decode iadd", InstructionSet.decode(bytecode, 2));
        
        assertEquals("iconst_0 name should match", "iconst_0", InstructionSet.decode(bytecode, 0).getName());
        assertEquals("iconst_1 name should match", "iconst_1", InstructionSet.decode(bytecode, 1).getName());
        assertEquals("iadd name should match", "iadd", InstructionSet.decode(bytecode, 2).getName());
    }
    
    @Test
    public void testBytecodeValidation() {
        // 有效的字节码
        byte[] validBytecode = {0x03, 0x04, 0x60}; // iconst_0, iconst_1, iadd
        assertTrue("Valid bytecode should pass validation", InstructionSet.validate(validBytecode));
        
        // 无效的字节码（未知指令）
        byte[] invalidBytecode = {(byte) 0xFF}; // 未知指令
        assertFalse("Invalid bytecode should fail validation", InstructionSet.validate(invalidBytecode));
    }
    
    @Test
    public void testBytecodeDisassembly() {
        byte[] bytecode = {0x03, 0x04, 0x60}; // iconst_0, iconst_1, iadd
        String disassembly = InstructionSet.disassemble(bytecode);
        
        assertNotNull("Disassembly should not be null", disassembly);
        assertTrue("Should contain iconst_0", disassembly.contains("iconst_0"));
        assertTrue("Should contain iconst_1", disassembly.contains("iconst_1"));
        assertTrue("Should contain iadd", disassembly.contains("iadd"));
    }
    
    @Test
    public void testApplicationClassLoader() {
        ApplicationClassLoader classLoader = new ApplicationClassLoader(null, Arrays.asList("."));
        
        assertEquals("Name should match", "Application ClassLoader", classLoader.getName());
        assertNull("Parent should be null", classLoader.getParent());
        assertEquals("Should have one classpath", 1, classLoader.getClassPathCount());
        assertTrue("Should have current directory in classpath", classLoader.hasClassPath("."));
        
        // 测试添加类路径
        classLoader.addClassPath("/test/path");
        assertEquals("Should have two classpaths", 2, classLoader.getClassPathCount());
        assertTrue("Should have test path", classLoader.hasClassPath("/test/path"));
    }
    
    @Test
    public void testCustomClassLoader() {
        CustomClassLoader classLoader = new CustomClassLoader("Test ClassLoader", null);
        
        assertEquals("Name should match", "Test ClassLoader", classLoader.getName());
        assertNull("Parent should be null", classLoader.getParent());
        assertFalse("Should not break parent delegation by default", classLoader.isBreakParentDelegation());
        
        // 测试内存提供者
        CustomClassLoader.MemoryProvider memoryProvider = new CustomClassLoader.MemoryProvider();
        byte[] testClassData = {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE}; // 简化的class文件头
        
        memoryProvider.addClass("TestClass", testClassData);
        assertTrue("Should be able to provide TestClass", memoryProvider.canProvide("TestClass"));
        try {
            assertArrayEquals("Class data should match", testClassData, memoryProvider.getClassData("TestClass"));
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
        
        classLoader.addClassDataProvider("memory", memoryProvider);
        assertTrue("Should have memory provider", classLoader.hasClassDataProvider("memory"));
        assertTrue("Should be able to load TestClass", classLoader.canLoadClass("TestClass"));
    }
    
    @Test
    public void testExecutionEngine() {
        ExecutionEngine engine = new ExecutionEngine();
        
        assertEquals("Default mode should be INTERPRET", ExecutionEngine.ExecutionMode.INTERPRET, engine.getExecutionMode());
        assertFalse("Debug mode should be false by default", engine.isDebugMode());
        assertNotNull("Statistics should not be null", engine.getStatistics());
        
        // 测试配置更新
        engine.setExecutionMode(ExecutionEngine.ExecutionMode.JIT);
        assertEquals("Mode should be updated", ExecutionEngine.ExecutionMode.JIT, engine.getExecutionMode());
        
        engine.setDebugMode(true);
        assertTrue("Debug mode should be enabled", engine.isDebugMode());
        
        // 测试统计信息
        ExecutionEngine.ExecutionStatistics stats = engine.getStatistics();
        assertEquals("Method calls should be 0", 0, stats.getMethodCalls());
        assertEquals("Instructions executed should be 0", 0, stats.getInstructionsExecuted());
        assertEquals("Exceptions should be 0", 0, stats.getExceptions());
    }
    
    @Test
    public void testGVMConfig() {
        GVM.GVMConfig config = new GVM.GVMConfig();
        
        assertTrue("Class paths should be empty initially", config.getClassPaths().isEmpty());
        assertFalse("Debug mode should be false by default", config.isDebugMode());
        assertEquals("Default execution mode should be INTERPRET", 
            ExecutionEngine.ExecutionMode.INTERPRET, config.getExecutionMode());
        assertEquals("Default stack size should be 1000", 1000, config.getStackSize());
        
        // 测试配置更新
        config.addClassPath("/test/path")
              .setDebugMode(true)
              .setExecutionMode(ExecutionEngine.ExecutionMode.JIT)
              .setStackSize(2000);
        
        assertEquals("Should have one classpath", 1, config.getClassPaths().size());
        assertTrue("Should contain test path", config.getClassPaths().contains("/test/path"));
        assertTrue("Debug mode should be enabled", config.isDebugMode());
        assertEquals("Execution mode should be JIT", ExecutionEngine.ExecutionMode.JIT, config.getExecutionMode());
        assertEquals("Stack size should be 2000", 2000, config.getStackSize());
    }
    
    @Test
    public void testGVMShutdown() {
        gvm.initialize();
        assertTrue("GVM should be initialized", gvm.isInitialized());
        
        gvm.shutdown();
        assertFalse("GVM should not be initialized after shutdown", gvm.isInitialized());
        assertFalse("GVM should not be running after shutdown", gvm.isRunning());
    }
    
    @Test(expected = RuntimeException.class)
    public void testRunWithoutMainMethod() {
        // 这个测试会失败，因为我们没有实际的类文件
        gvm.run("NonExistentClass", new String[0]);
    }
    
    @Test
    public void testHeapManagement() {
        Heap heap = new Heap();
        
        // 测试基本配置
        assertNotNull("Heap should not be null", heap);
        assertEquals("Initial allocated objects should be 0", 0, heap.getAllocatedObjects());
        
        // 测试堆统计信息
        Heap.HeapStatistics stats = heap.getStatistics();
        assertNotNull("Heap statistics should not be null", stats);
        assertEquals("Initial live objects should be 0", 0, stats.getLiveObjects());
        assertTrue("Usage ratio should be 0", stats.getUsageRatio() == 0.0);
    }
    
    @Test
    public void testGVMObjectCreation() {
        Heap heap = new Heap();
        
        // 创建简单的类定义（用于测试）
        java.util.Map<String, io.github.javpower.gvm.classloader.GVMClass.FieldInfo> fields = new java.util.HashMap<>();
        java.util.Map<String, io.github.javpower.gvm.classloader.GVMClass.MethodInfo> methods = new java.util.HashMap<>();
        
        io.github.javpower.gvm.classloader.GVMClass testClass = 
            new io.github.javpower.gvm.classloader.GVMClass("TestClass", "java/lang/Object", 
                new String[0], 0x0021, new Object[0], null);
        
        // 测试对象分配
        GVMObject obj = heap.allocateObject(testClass);
        assertNotNull("Allocated object should not be null", obj);
        assertEquals("Object class should match", testClass, obj.getObjectClass());
        assertEquals("Object should be alive", GVMObject.ObjectState.ALIVE, obj.getState());
        assertFalse("Object should not be array", obj.isArray());
        
        // 测试数组对象分配
        GVMObject arrayObj = heap.allocateArray(testClass, 10);
        assertNotNull("Allocated array should not be null", arrayObj);
        assertTrue("Object should be array", arrayObj.isArray());
        assertEquals("Array length should be 10", 10, arrayObj.getArrayLength());
        
        // 测试引用计数
        obj.incrementReferenceCount();
        assertEquals("Reference count should be 1", 1, obj.getReferenceCount());
        obj.decrementReferenceCount();
        assertEquals("Reference count should be 0", 0, obj.getReferenceCount());
    }
    
    @Test
    public void testExceptionHandler() {
        Heap heap = new Heap();
        ExceptionHandler exceptionHandler = new ExceptionHandler(heap);
        
        assertNotNull("Exception handler should not be null", exceptionHandler);
        
        // 测试异常处理统计信息
        ExceptionHandler.ExceptionStatistics stats = exceptionHandler.getStatistics();
        assertNotNull("Exception statistics should not be null", stats);
        assertTrue("Should have registered exception classes", stats.getRegisteredExceptionClasses() > 0);
    }
    
    @Test
    public void testMethodInvoker() {
        ExecutionEngine engine = new ExecutionEngine();
        Heap heap = engine.getHeap();
        ApplicationClassLoader classLoader = new ApplicationClassLoader(null);
        
        MethodInvoker methodInvoker = new MethodInvoker(engine, heap, classLoader);
        assertNotNull("Method invoker should not be null", methodInvoker);
        
        // 测试方法调用统计信息
        MethodInvoker.MethodInvocationStatistics stats = methodInvoker.getStatistics();
        assertNotNull("Method invocation statistics should not be null", stats);
        assertTrue("Should have registered native methods", stats.getRegisteredNativeMethods() > 0);
    }
    
    @Test
    public void testExecutionEngineWithMemoryManagement() {
        ExecutionEngine engine = new ExecutionEngine();
        
        // 测试组件集成
        assertNotNull("Heap should not be null", engine.getHeap());
        assertNotNull("Exception handler should not be null", engine.getExceptionHandler());
        
        // 测试堆统计信息
        Heap.HeapStatistics heapStats = engine.getHeap().getStatistics();
        assertNotNull("Heap statistics should not be null", heapStats);
        
        // 测试异常处理统计信息
        ExceptionHandler.ExceptionStatistics exceptionStats = engine.getExceptionHandler().getStatistics();
        assertNotNull("Exception statistics should not be null", exceptionStats);
    }
} 