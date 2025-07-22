package io.github.javpower.gvm.utils;

/**
 * GVM日志系统
 * 提供分级日志输出，帮助理解JVM工作原理
 */
public class Logger {
    
    public enum Level {
        ERROR(0, "ERROR", "\u001B[31m"),   // 红色
        WARN(1, "WARN", "\u001B[33m"),     // 黄色
        INFO(2, "INFO", "\u001B[32m"),     // 绿色
        DEBUG(3, "DEBUG", "\u001B[36m"),   // 青色
        TRACE(4, "TRACE", "\u001B[37m");   // 白色
        
        private final int level;
        private final String name;
        private final String color;
        
        Level(int level, String name, String color) {
            this.level = level;
            this.name = name;
            this.color = color;
        }
        
        public int getLevel() { return level; }
        public String getName() { return name; }
        public String getColor() { return color; }
    }
    
    private static final String RESET = "\u001B[0m";
    private static Level currentLevel = Level.INFO;
    private static boolean colorEnabled = true;
    private static String prefix = "[GVM] ";
    
    /**
     * 设置日志级别
     */
    public static void setLevel(Level level) {
        currentLevel = level;
    }
    
    /**
     * 设置是否启用颜色
     */
    public static void setColorEnabled(boolean enabled) {
        colorEnabled = enabled;
    }
    
    /**
     * 设置日志前缀
     */
    public static void setPrefix(String prefix) {
        Logger.prefix = prefix;
    }
    
    /**
     * 错误日志
     */
    public static void error(String message) {
        log(Level.ERROR, message);
    }
    
    public static void error(String format, Object... args) {
        log(Level.ERROR, String.format(format, args));
    }
    
    /**
     * 警告日志
     */
    public static void warn(String message) {
        log(Level.WARN, message);
    }
    
    public static void warn(String format, Object... args) {
        log(Level.WARN, String.format(format, args));
    }
    
    /**
     * 信息日志
     */
    public static void info(String message) {
        log(Level.INFO, message);
    }
    
    public static void info(String format, Object... args) {
        log(Level.INFO, String.format(format, args));
    }
    
    /**
     * 调试日志
     */
    public static void debug(String message) {
        log(Level.DEBUG, message);
    }
    
    public static void debug(String format, Object... args) {
        log(Level.DEBUG, String.format(format, args));
    }
    
    /**
     * 跟踪日志
     */
    public static void trace(String message) {
        log(Level.TRACE, message);
    }
    
    public static void trace(String format, Object... args) {
        log(Level.TRACE, String.format(format, args));
    }
    
    /**
     * 内存相关日志
     */
    public static void memory(String message) {
        if (currentLevel.getLevel() >= Level.DEBUG.getLevel()) {
            String coloredMessage = colorEnabled ? 
                "\u001B[35m[MEMORY] " + message + RESET :  // 紫色
                "[MEMORY] " + message;
            System.out.println(prefix + coloredMessage);
        }
    }
    
    public static void memory(String format, Object... args) {
        memory(String.format(format, args));
    }
    
    /**
     * 类加载相关日志
     */
    public static void classload(String message) {
        if (currentLevel.getLevel() >= Level.DEBUG.getLevel()) {
            String coloredMessage = colorEnabled ? 
                "\u001B[34m[CLASS] " + message + RESET :   // 蓝色
                "[CLASS] " + message;
            System.out.println(prefix + coloredMessage);
        }
    }
    
    public static void classload(String format, Object... args) {
        classload(String.format(format, args));
    }
    
    /**
     * 字节码执行相关日志
     */
    public static void bytecode(String message) {
        if (currentLevel.getLevel() >= Level.TRACE.getLevel()) {
            String coloredMessage = colorEnabled ? 
                "\u001B[93m[BYTECODE] " + message + RESET : // 亮黄色
                "[BYTECODE] " + message;
            System.out.println(prefix + coloredMessage);
        }
    }
    
    public static void bytecode(String format, Object... args) {
        bytecode(String.format(format, args));
    }
    
    /**
     * 方法调用相关日志
     */
    public static void method(String message) {
        if (currentLevel.getLevel() >= Level.DEBUG.getLevel()) {
            String coloredMessage = colorEnabled ? 
                "\u001B[96m[METHOD] " + message + RESET :  // 亮青色
                "[METHOD] " + message;
            System.out.println(prefix + coloredMessage);
        }
    }
    
    public static void method(String format, Object... args) {
        method(String.format(format, args));
    }
    
    /**
     * 异常处理相关日志
     */
    public static void exception(String message) {
        if (currentLevel.getLevel() >= Level.DEBUG.getLevel()) {
            String coloredMessage = colorEnabled ? 
                "\u001B[91m[EXCEPTION] " + message + RESET : // 亮红色
                "[EXCEPTION] " + message;
            System.out.println(prefix + coloredMessage);
        }
    }
    
    public static void exception(String format, Object... args) {
        exception(String.format(format, args));
    }
    
    /**
     * 栈操作相关日志
     */
    public static void stack(String message) {
        if (currentLevel.getLevel() >= Level.TRACE.getLevel()) {
            String coloredMessage = colorEnabled ? 
                "\u001B[94m[STACK] " + message + RESET :   // 亮蓝色
                "[STACK] " + message;
            System.out.println(prefix + coloredMessage);
        }
    }
    
    public static void stack(String format, Object... args) {
        stack(String.format(format, args));
    }
    
    /**
     * 垃圾回收相关日志
     */
    public static void gc(String message) {
        if (currentLevel.getLevel() >= Level.INFO.getLevel()) {
            String coloredMessage = colorEnabled ? 
                "\u001B[95m[GC] " + message + RESET :      // 亮紫色
                "[GC] " + message;
            System.out.println(prefix + coloredMessage);
        }
    }
    
    public static void gc(String format, Object... args) {
        gc(String.format(format, args));
    }
    
    /**
     * 通用日志方法
     */
    private static void log(Level level, String message) {
        if (currentLevel.getLevel() >= level.getLevel()) {
            String timestamp = String.format("%tT", System.currentTimeMillis());
            String coloredMessage = colorEnabled ? 
                level.getColor() + "[" + level.getName() + "] " + message + RESET :
                "[" + level.getName() + "] " + message;
            System.out.println(prefix + timestamp + " " + coloredMessage);
        }
    }
    
    /**
     * 打印分隔线
     */
    public static void separator() {
        System.out.println(prefix + repeatChar('=', 60));
    }
    
    public static void separator(String title) {
        int totalLength = 60;
        int titleLength = title.length();
        int padding = (totalLength - titleLength - 2) / 2;
        
        String line = repeatChar('=', padding) + " " + title + " " + repeatChar('=', totalLength - padding - titleLength - 2);
        System.out.println(prefix + line);
    }
    
    /**
     * 重复字符（兼容Java 1.8）
     */
    private static String repeatChar(char c, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
    
    /**
     * 打印子标题
     */
    public static void subtitle(String title) {
        System.out.println(prefix + "--- " + title + " ---");
    }
} 