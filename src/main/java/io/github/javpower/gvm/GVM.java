package io.github.javpower.gvm;

import io.github.javpower.gvm.bytecode.InstructionSet;
import io.github.javpower.gvm.classloader.ApplicationClassLoader;
import io.github.javpower.gvm.classloader.CustomClassLoader;
import io.github.javpower.gvm.classloader.GVMClass;
import io.github.javpower.gvm.classloader.GVMClassLoader;
import io.github.javpower.gvm.execution.ExecutionEngine;
import io.github.javpower.gvm.execution.MethodInvoker;
import io.github.javpower.gvm.memory.Heap;
import io.github.javpower.gvm.runtime.stack.JVMStack;
import io.github.javpower.gvm.utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * GVM - 简化的Java虚拟机实现
 * 用于学习和理解JVM的工作原理
 */
public class GVM {
    
    // JVM版本信息
    public static final String VERSION = "1.0.0";
    public static final String NAME = "GVM (Generic Virtual Machine)";
    
    // JVM组件
    private GVMClassLoader systemClassLoader;
    private ExecutionEngine executionEngine;
    private JVMStack mainThreadStack;
    
    // JVM状态
    private boolean initialized = false;
    private boolean running = false;
    
    // JVM配置
    private GVMConfig config;
    
    /**
     * GVM配置类
     */
    public static class GVMConfig {
        private List<String> classPaths = new ArrayList<>();
        private boolean debugMode = false;
        private ExecutionEngine.ExecutionMode executionMode = ExecutionEngine.ExecutionMode.INTERPRET;
        private int stackSize = 1000;
        
        public GVMConfig() {
            // 默认配置
        }
        
        public GVMConfig addClassPath(String path) {
            classPaths.add(path);
            return this;
        }
        
        public GVMConfig setDebugMode(boolean debug) {
            this.debugMode = debug;
            return this;
        }
        
        public GVMConfig setExecutionMode(ExecutionEngine.ExecutionMode mode) {
            this.executionMode = mode;
            return this;
        }
        
        public GVMConfig setStackSize(int size) {
            this.stackSize = size;
            return this;
        }
        
        // Getters
        public List<String> getClassPaths() { return new ArrayList<>(classPaths); }
        public boolean isDebugMode() { return debugMode; }
        public ExecutionEngine.ExecutionMode getExecutionMode() { return executionMode; }
        public int getStackSize() { return stackSize; }
    }
    
    /**
     * 构造GVM
     */
    public GVM() {
        this(new GVMConfig());
    }
    
    /**
     * 构造GVM
     * 
     * @param config GVM配置
     */
    public GVM(GVMConfig config) {
        this.config = config;
    }
    
    /**
     * 初始化JVM
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        
        // 设置日志级别
        if (config.isDebugMode()) {
            Logger.setLevel(Logger.Level.TRACE);
        } else {
            Logger.setLevel(Logger.Level.INFO);
        }
        
        Logger.separator("GVM 初始化开始");
        Logger.info("正在初始化 %s v%s", NAME, VERSION);
        Logger.debug("调试模式: %s", config.isDebugMode() ? "开启" : "关闭");
        Logger.debug("执行模式: %s", config.getExecutionMode());
        Logger.debug("栈大小: %d", config.getStackSize());
        
        try {
            // 1. 初始化类加载器体系
            Logger.subtitle("步骤1: 初始化类加载器体系");
            initializeClassLoaders();
            
            // 2. 初始化执行引擎
            Logger.subtitle("步骤2: 初始化执行引擎");
            initializeExecutionEngine();
            
            // 3. 初始化运行时数据区
            Logger.subtitle("步骤3: 初始化运行时数据区");
            initializeRuntimeDataAreas();
            
            // 4. 初始化指令集
            Logger.subtitle("步骤4: 初始化指令集");
            initializeInstructionSet();
            
            initialized = true;
            Logger.separator("GVM 初始化完成");
            Logger.info("GVM 初始化成功完成！");
            
        } catch (Exception e) {
            Logger.error("GVM 初始化失败: %s", e.getMessage());
            throw new RuntimeException("Failed to initialize GVM", e);
        }
    }
    
    /**
     * 初始化类加载器体系
     */
    private void initializeClassLoaders() {
        Logger.classload("正在初始化类加载器层次结构...");
        
        // 创建系统类加载器（应用程序类加载器）
        systemClassLoader = new ApplicationClassLoader(null, config.getClassPaths());
        
        Logger.classload("创建系统类加载器: %s", systemClassLoader.getClass().getSimpleName());
        Logger.debug("类路径: %s", config.getClassPaths());
        Logger.classload("类加载器体系初始化完成");
    }
    
    /**
     * 初始化执行引擎
     */
    private void initializeExecutionEngine() {
        Logger.info("正在初始化执行引擎...");
        
        executionEngine = new ExecutionEngine(config.getExecutionMode(), config.isDebugMode());
        Logger.debug("执行引擎创建完成: %s", executionEngine.getClass().getSimpleName());
        
        // 创建方法调用器
        MethodInvoker methodInvoker = new MethodInvoker(executionEngine, executionEngine.getHeap(), systemClassLoader);
        executionEngine.setMethodInvoker(methodInvoker);
        Logger.method("方法调用器创建完成");
        
        // 显示内存统计信息
        Heap.HeapStatistics heapStats = executionEngine.getHeap().getStatistics();
        Logger.memory("堆内存初始化完成 - 最大大小: %d MB, 当前使用: %d bytes", 
                     heapStats.getMaxSize() / (1024 * 1024), heapStats.getCurrentSize());
        
        Logger.info("执行引擎初始化完成");
    }
    
    /**
     * 初始化运行时数据区
     */
    private void initializeRuntimeDataAreas() {
        Logger.info("正在初始化运行时数据区...");
        
        // 创建主线程栈
        mainThreadStack = new JVMStack("main", config.getStackSize());
        Logger.stack("创建主线程栈: 线程名=%s, 最大深度=%d", 
                    mainThreadStack.getThreadName(), config.getStackSize());
        
        Logger.info("运行时数据区初始化完成");
    }
    
    /**
     * 初始化指令集
     */
    private void initializeInstructionSet() {
        Logger.info("正在初始化字节码指令集...");
        
        int instructionCount = InstructionSet.getInstructionCount();
        Logger.bytecode("加载了 %d 个字节码指令", instructionCount);
        
        if (config.isDebugMode()) {
            Logger.debug("字节码指令统计信息:");
            System.out.println(InstructionSet.getStatistics());
        }
        
        Logger.info("字节码指令集初始化完成");
    }
    
    /**
     * 运行主类
     * 
     * @param mainClassName 主类名
     * @param args 命令行参数
     */
    public void run(String mainClassName, String[] args) {
        if (!initialized) {
            initialize();
        }
        
        if (running) {
            throw new RuntimeException("GVM is already running");
        }
        
        running = true;
        
        try {
            Logger.separator("GVM 开始执行");
            Logger.info("主类: %s", mainClassName);
            Logger.info("参数: %s", Arrays.toString(args));
            
            // 1. 加载主类
            Logger.subtitle("步骤1: 加载主类");
            Logger.classload("正在加载主类: %s", mainClassName);
            GVMClass mainClass = loadMainClass(mainClassName);
            Logger.classload("主类加载成功: %s", mainClass.getClassName());
            
            // 2. 查找main方法
            Logger.subtitle("步骤2: 查找main方法");
            Logger.method("正在查找main方法...");
            GVMClass.MethodInfo mainMethod = findMainMethod(mainClass);
            Logger.method("找到main方法: %s", mainMethod);
            Logger.debug("方法描述符: %s", mainMethod.getDescriptor());
            Logger.debug("方法访问标志: 0x%04X", mainMethod.getAccessFlags());
            Logger.debug("最大栈深度: %d, 最大局部变量: %d", mainMethod.getMaxStack(), mainMethod.getMaxLocals());
            
            // 3. 初始化主类
            Logger.subtitle("步骤3: 初始化主类");
            Logger.classload("正在初始化主类...");
            systemClassLoader.initializeClass(mainClass);
            Logger.classload("主类初始化完成");
            
            // 4. 执行main方法
            Logger.subtitle("步骤4: 执行main方法");
            Logger.method("开始执行main方法...");
            executeMainMethod(mainClass, mainMethod, args);
            
            Logger.separator("GVM 执行完成");
            Logger.info("执行统计信息: %s", executionEngine.getStatistics());
            
            // 显示最终的内存统计
            Heap.HeapStatistics finalStats = executionEngine.getHeap().getStatistics();
            Logger.memory("最终内存统计: %s", finalStats);
            
            // 在调试模式下显示详细的内存区域信息
            if (config.isDebugMode()) {
                Logger.separator("内存区域详细信息");
                Logger.debug("堆内存详情:\n%s", executionEngine.getHeap().getDetailedInfo());
                Logger.debug("JVM栈详情:\n%s", getStackDetailedInfo());
            }
            
        } catch (Exception e) {
            Logger.separator("GVM 执行失败");
            Logger.error("执行错误: %s", e.getMessage());
            if (config.isDebugMode()) {
                e.printStackTrace();
            }
            throw new RuntimeException("GVM execution failed", e);
        } finally {
            running = false;
        }
    }
    
    /**
     * 加载主类
     */
    private GVMClass loadMainClass(String className) throws ClassNotFoundException {
        System.out.println("Loading main class: " + className);
        
        GVMClass mainClass = systemClassLoader.loadClass(className);
        
        if (config.isDebugMode()) {
            System.out.println("Main class loaded successfully:");
            System.out.println(mainClass.getDetailedInfo());
        }
        
        return mainClass;
    }
    
    /**
     * 查找main方法
     */
    private GVMClass.MethodInfo findMainMethod(GVMClass mainClass) {
        System.out.println("Looking for main method...");
        
        // 查找 public static void main(String[] args) 方法
        GVMClass.MethodInfo mainMethod = mainClass.getMethod("main", "([Ljava/lang/String;)V");
        
        if (mainMethod == null) {
            throw new RuntimeException("Main method not found in class: " + mainClass.getClassName());
        }
        
        // 验证main方法的访问标志
        if (!mainMethod.isPublic() || !mainMethod.isStatic()) {
            throw new RuntimeException("Main method must be public static");
        }
        
        System.out.println("Main method found: " + mainMethod);
        return mainMethod;
    }
    
    /**
     * 执行main方法
     */
    private void executeMainMethod(GVMClass mainClass, GVMClass.MethodInfo mainMethod, String[] args) {
        System.out.println("Executing main method...");
        
        // TODO: 将String[]参数传递给main方法
        // 这里需要创建String数组对象并将其放入局部变量表
        
        Object result = executionEngine.executeMethod(mainClass, mainMethod, mainThreadStack);
        
        if (config.isDebugMode()) {
            System.out.println("Main method execution result: " + result);
        }
    }
    
    /**
     * 关闭JVM
     */
    public void shutdown() {
        if (!initialized) {
            return;
        }
        
        System.out.println("=== Shutting down GVM ===");
        
        try {
            // 清理资源
            if (mainThreadStack != null) {
                mainThreadStack.deactivate();
            }
            
            if (systemClassLoader != null) {
                systemClassLoader.clearCache();
            }
            
            System.out.println("GVM shutdown completed");
            
        } catch (Exception e) {
            System.err.println("Error during GVM shutdown: " + e.getMessage());
        } finally {
            initialized = false;
            running = false;
        }
    }
    
    /**
     * 获取系统类加载器
     */
    public GVMClassLoader getSystemClassLoader() {
        return systemClassLoader;
    }
    
    /**
     * 获取执行引擎
     */
    public ExecutionEngine getExecutionEngine() {
        return executionEngine;
    }
    
    /**
     * 获取主线程栈
     */
    public JVMStack getMainThreadStack() {
        return mainThreadStack;
    }
    
    /**
     * 是否已初始化
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * 是否正在运行
     */
    public boolean isRunning() {
        return running;
    }
    
    /**
     * 获取配置
     */
    public GVMConfig getConfig() {
        return config;
    }
    
    /**
     * 主入口方法
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        
        try {
            // 解析命令行参数
            CommandLineArgs cmdArgs = parseCommandLine(args);
            
            // 创建GVM配置
            GVMConfig config = new GVMConfig()
                .setDebugMode(cmdArgs.debug)
                .setExecutionMode(cmdArgs.executionMode)
                .setStackSize(cmdArgs.stackSize);
            
            // 添加类路径
            for (String classPath : cmdArgs.classPaths) {
                config.addClassPath(classPath);
            }
            
            // 创建并运行GVM
            GVM gvm = new GVM(config);
            gvm.run(cmdArgs.mainClass, cmdArgs.programArgs);
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    /**
     * 获取栈的详细信息
     */
    private String getStackDetailedInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== JVM栈详情 ===\n");
        sb.append(String.format("线程名: %s\n", mainThreadStack.getThreadName()));
        sb.append(String.format("最大深度: %d\n", mainThreadStack.getMaxDepth()));
        sb.append(String.format("当前深度: %d\n", mainThreadStack.getDepth()));
        
        if (mainThreadStack.isEmpty()) {
            sb.append("栈为空\n");
        } else {
            sb.append("\n--- 当前栈帧 ---\n");
            sb.append(mainThreadStack.getCurrentFrame().toString());
        }
        sb.append("===============\n");
        return sb.toString();
    }
    
    /**
     * 命令行参数类
     */
    private static class CommandLineArgs {
        String mainClass;
        String[] programArgs = new String[0];
        List<String> classPaths = new ArrayList<>();
        boolean debug = false;
        ExecutionEngine.ExecutionMode executionMode = ExecutionEngine.ExecutionMode.INTERPRET;
        int stackSize = 1000;
    }
    
    /**
     * 解析命令行参数
     */
    private static CommandLineArgs parseCommandLine(String[] args) {
        CommandLineArgs cmdArgs = new CommandLineArgs();
        
        int i = 0;
        while (i < args.length) {
            String arg = args[i];
            
            if ("-cp".equals(arg) || "-classpath".equals(arg)) {
                if (i + 1 >= args.length) {
                    throw new RuntimeException("Missing classpath argument");
                }
                String[] paths = args[++i].split(System.getProperty("path.separator"));
                cmdArgs.classPaths.addAll(Arrays.asList(paths));
            } else if ("-debug".equals(arg)) {
                cmdArgs.debug = true;
            } else if ("-mode".equals(arg)) {
                if (i + 1 >= args.length) {
                    throw new RuntimeException("Missing execution mode argument");
                }
                String mode = args[++i].toUpperCase();
                cmdArgs.executionMode = ExecutionEngine.ExecutionMode.valueOf(mode);
            } else if ("-stack".equals(arg)) {
                if (i + 1 >= args.length) {
                    throw new RuntimeException("Missing stack size argument");
                }
                cmdArgs.stackSize = Integer.parseInt(args[++i]);
            } else if (!arg.startsWith("-")) {
                // 主类名
                cmdArgs.mainClass = arg;
                // 剩余参数作为程序参数
                cmdArgs.programArgs = Arrays.copyOfRange(args, i + 1, args.length);
                break;
            } else {
                throw new RuntimeException("Unknown option: " + arg);
            }
            i++;
        }
        
        if (cmdArgs.mainClass == null) {
            throw new RuntimeException("Main class not specified");
        }
        
        return cmdArgs;
    }
    
    /**
     * 打印使用说明
     */
    private static void printUsage() {
        System.out.println(NAME + " v" + VERSION);
        System.out.println("Usage: java -jar gvm.jar [options] <main-class> [args...]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -cp, -classpath <paths>  Class search paths");
        System.out.println("  -debug                   Enable debug mode");
        System.out.println("  -mode <mode>             Execution mode (INTERPRET, JIT, MIXED)");
        System.out.println("  -stack <size>            Stack size (default: 1000)");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar gvm.jar HelloWorld");
        System.out.println("  java -jar gvm.jar -debug -cp ./classes HelloWorld arg1 arg2");
        System.out.println("  java -jar gvm.jar -mode INTERPRET com.example.Main");
    }
} 